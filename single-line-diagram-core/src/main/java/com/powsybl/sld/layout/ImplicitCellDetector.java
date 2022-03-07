/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.*;
import com.powsybl.sld.model.coordinate.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImplicitCellDetector implements CellDetector {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImplicitCellDetector.class);
    private boolean removeUnnecessaryFictitiousNodes;
    private boolean substituteSingularFictitiousByFeederNode;
    private boolean exceptionIfPatternNotHandled;

    public ImplicitCellDetector(boolean removeUnnecessaryFictitiousNodes, boolean substituteSingularFictitiousByFeederNode, boolean exceptionIfPatternNotHandled) {
        this.removeUnnecessaryFictitiousNodes = removeUnnecessaryFictitiousNodes;
        this.substituteSingularFictitiousByFeederNode = substituteSingularFictitiousByFeederNode;
        this.exceptionIfPatternNotHandled = exceptionIfPatternNotHandled;
    }

    public ImplicitCellDetector() {
        this(true, true, false);
    }


    /**
     * internCell detection : an internal cell is composed of nodes connecting BUSes without connecting Feeder.
     * detectCell is used to detect cells exploring the graph and scanning exclusionTypes and stopTypes
     * <p>
     * *************INTERN CELL*******************
     * exclusion types = {FEEDER} : if a FEEDER type is reached it is not an INTERN CELL
     * stop the visit if reach a Bus : stopTypes = {BUS}
     * ***************EXTERN AND SHUNT CELLS******
     * detection : nodes connecting buses and departures without being in an internCell (previously allocated nodes)
     * exclusion types = {}
     * stop the visit if reach a Bus : stopTypes = {BUS,FEEDER} * @param graph g
     */
    @Override
    public void detectCells(VoltageLevelGraph graph) {
        cleaning(graph);

        LOGGER.info("Detecting cells...");

        List<Node> allocatedNodes = new ArrayList<>();
        // **************INTERN CELL*******************
        List<Node.NodeType> exclusionTypes = new ArrayList<>();
        exclusionTypes.add(Node.NodeType.FEEDER);
        List<Node.NodeType> stopTypes = new ArrayList<>();
        stopTypes.add(Node.NodeType.BUS);
        List<Set<Node>> internCellsNodes = detectCell(graph, stopTypes, exclusionTypes, allocatedNodes);
        for (Set<Node> nodes : internCellsNodes) {
            graph.addCell(new InternCell(graph.getNextCellNumber(), nodes, exceptionIfPatternNotHandled));
        }

        // ****************EXTERN AND SHUNT CELLS******
        stopTypes.add(Node.NodeType.FEEDER);
        List<Set<Node>> externCellsNodes = detectCell(graph, stopTypes, new ArrayList<>(), allocatedNodes);

        for (Set<Node> nodes : externCellsNodes) {
            if (isPureExternCell(graph, nodes)) {
                //*****************EXTERN CELL
                graph.addCell(new ExternCell(graph.getNextCellNumber(), nodes));
            } else {
                //*****************SHUNT CELL
                // if a shunt cell is detected two or more EXTERN cells and one or more SHUNT cells are created
                detectAndTypeShunt(graph, nodes);
            }
        }

        graph.getCells().forEach(Cell::getFullId);

        graph.logCellDetectionStatus();
    }

    private void cleaning(VoltageLevelGraph graph) {
        graph.substituteFictitiousNodesMirroringBusNodes();
        if (removeUnnecessaryFictitiousNodes) {
            graph.removeUnnecessaryFictitiousNodes();
        }
        if (substituteSingularFictitiousByFeederNode) {
            graph.substituteSingularFictitiousByFeederNode();
        }
        graph.insertFictitiousNodesAtFeeders();
        graph.conditionalExtensionOfNodeConnectedToBus(node ->
                node.getType() == Node.NodeType.SWITCH && ((SwitchNode) node).getKind() != SwitchNode.SwitchKind.DISCONNECTOR
                        || node instanceof Middle2WTNode || node instanceof Middle3WTNode
        );
        graph.extendFirstOutsideNode();
        graph.extendBusConnectedToBus();
    }

    /**
     * @param graph          is the voltage level graph
     * @param typeStops      is the types of node that stops the exploration
     * @param exclusionTypes is the types when reached considers the exploration unsuccessful
     * @param allocatedNodes is the list of nodes already allocated to a cell.
     * @return the list of nodes for each detected cell
     */
    private List<Set<Node>> detectCell(VoltageLevelGraph graph,
            List<Node.NodeType> typeStops,
            List<Node.NodeType> exclusionTypes,
            List<Node> allocatedNodes) {
        List<Set<Node>> cellsNodes = new ArrayList<>();
        graph.getNodeBuses().forEach(bus -> bus.getAdjacentNodes().forEach(adj -> {
            Set<Node> cellNodes = new LinkedHashSet<>();
            cellNodes.add(bus);
            Set<Node> outsideNodes = new HashSet<>(allocatedNodes);
            outsideNodes.add(bus);
            if (GraphTraversal.run(
                    adj, node -> typeStops.contains(node.getType()), node -> exclusionTypes.contains(node.getType()),
                    cellNodes, outsideNodes)) {
                cellsNodes.add(cellNodes);
                cellNodes.stream()
                        .filter(node -> node.getType() != Node.NodeType.BUS)
                        .forEach(allocatedNodes::add);
            }
        }));
        return cellsNodes;
    }

    /**
     * Check if the given nodes could constitute a pure extern and return true in that case, else false (suspected shunt)
     */
    private boolean isPureExternCell(VoltageLevelGraph graph, Set<Node> cellNodes) {
        /*Explore the graph of the candidate cell. Remove successively one node, assess if it splits the graph into n>1 branches
        if so, then check if each component is exclusively reaching FEEDER or exclusively reaching BUS
        And verify you have at least one of them
        Return true in that case else false meaning there is one shunt
        */
        for (Node n : cellNodes) {
            List<Node> nodes = new ArrayList<>(cellNodes);
            nodes.remove(n);
            List<List<Node>> connexComponents = graph.getConnexComponents(nodes);
            if (checkExternComponents(connexComponents)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param connexComponents components partition to analyse
     * @return true if this partition reflects an extern cell
     */
    private boolean checkExternComponents(List<List<Node>> connexComponents) {
        if (connexComponents.size() > 1) {
            boolean hasDepartBranch = false;
            boolean hasBusBranch = false;
            boolean hasMixBranch = false;
            for (List<Node> nodesConnex : connexComponents) {
                List<Node.NodeType> types = nodesConnex.stream()
                        .map(Node::getType)
                        .distinct().filter(t -> t == Node.NodeType.FEEDER || t == Node.NodeType.BUS)
                        .collect(Collectors.toList());
                if (types.size() == 2) {
                    hasMixBranch = true;
                } else if (types.isEmpty()) {
                    return false;
                } else if (types.get(0).equals(Node.NodeType.FEEDER)) {
                    hasDepartBranch = true;
                } else {
                    hasBusBranch = true;
                }
            }
            return hasBusBranch && hasDepartBranch && !hasMixBranch;
        }
        return false;
    }

    /**
     * @param nodes the set of nodes among which there is a suspected shunt
     **/
    private void detectAndTypeShunt(VoltageLevelGraph graph, Set<Node> nodes) {
        detectAndTypeShunt(graph, nodes, new ArrayList<>());
    }

    /**
     * @param nodes the nodes among which there is a suspected shunt
     * @param shuntCells the shunt cells which are linked to the given nodes
     **/
    private void detectAndTypeShunt(VoltageLevelGraph graph, Set<Node> nodes, List<ShuntCell> shuntCells) {

        List<Node> externalNodes = graph.getNodes()
                .stream()
                .filter(node -> !nodes.contains(node))
                .collect(Collectors.toList());

        Optional<List<Node>> cellNodesExtern = nodes.stream().filter(n -> n.getAdjacentNodes().size() > 2) // optimisation : a Shunt node has necessarily 3 ore more adjacent nodes
                .map(n -> checkCandidateShuntNode(n, externalNodes))
                .filter(Objects::nonNull).findFirst();

        if (cellNodesExtern.isPresent()) {
            Set<Node> remainingNodes = new LinkedHashSet<>(nodes);
            List<ShuntCell> shuntCellsCreated = new ArrayList<>(shuntCells);
            Node shuntNode = cellNodesExtern.get().get(0);
            splitNodes(graph, nodes, shuntNode, cellNodesExtern.get(), remainingNodes, externalNodes, shuntCellsCreated);

            remainingNodes.removeIf(rn -> isIsolatedBusOrShunt(remainingNodes, rn));

            // when created, a shunt cell created is attached to the left to a pure extern cell, hence only the right side is checked
            List<ShuntCell> linkedShuntCells = shuntCellsCreated.stream()
                    .filter(sc -> remainingNodes.contains(sc.getSideShuntNode(Side.RIGHT)))
                    .collect(Collectors.toList());

            if (isPureExternCell(graph, remainingNodes)) {
                // create the last external cell with the remaining nodes.
                graph.addCell(new ExternCell(graph.getNextCellNumber(), remainingNodes, linkedShuntCells));
            } else {
                // another shunt should be in the remaining nodes: detects it and create the corresponding cells
                detectAndTypeShunt(graph, remainingNodes, linkedShuntCells);
            }
        } else {
            // if no shunt node is found (checkCandidateShuntNode always returns null), create a cell anyway with all nodes
            graph.addCell(new ExternCell(graph.getNextCellNumber(), nodes, shuntCells));
        }
    }

    /**
     * Split the given nodes in one or several shunt cells, in one or more extern cells and in remaining nodes
     * @param graph the voltage level graph
     * @param nodes the original nodes of the non-pure external detected cell
     * @param shuntNode the detected shunt node
     * @param cellNodesExtern the extern cell nodes detected while detecting the shunt node
     * @param remainingNodes the nodes which remain after creating the shunt cells and pure extern cells
     * @param externalNodes the nodes in the graph which are not in the <code>nodes</code>
     * @param shuntCellsCreated the shunt cells created
     */
    private void splitNodes(VoltageLevelGraph graph, Set<Node> nodes, Node shuntNode, List<Node> cellNodesExtern,
                            Set<Node> remainingNodes, List<Node> externalNodes, List<ShuntCell> shuntCellsCreated) {

        // create the new pure external cell
        List<ShuntCell> linkedShuntCells = shuntCellsCreated.stream()
                .filter(shuntCell -> cellNodesExtern.contains(shuntCell.getSideShuntNode(Side.RIGHT)))
                .collect(Collectors.toList());
        ExternCell newExternCell = new ExternCell(graph.getNextCellNumber(), cellNodesExtern, linkedShuntCells);
        graph.addCell(newExternCell);

        // remove used nodes from remaining nodes
        cellNodesExtern.stream()
                .filter(m -> m.getType() != Node.NodeType.BUS && m.getType() != Node.NodeType.SHUNT)
                .forEach(remainingNodes::remove);

        //create the shunt cells
        List<List<Node>> shuntsNodes = createShuntCellNodes(shuntNode, newExternCell, nodes);
        shuntsNodes.stream().flatMap(Collection::stream)
                .filter(m -> !m.getType().equals(Node.NodeType.SHUNT))
                .forEach(remainingNodes::remove);
        shuntsNodes.stream()
                .map(shuntNodes -> createShuntCell(graph, shuntNodes))
                .forEach(shuntCell -> {
                    newExternCell.addShuntCell(shuntCell);
                    shuntCellsCreated.add(shuntCell);
                });

        //look for consecutive shunt nodes
        for (List<Node> shuntNodes : shuntsNodes) {
            Node consecutiveShuntNode = shuntNodes.get(shuntNodes.size() - 1);
            List<Node> cellNodesExtern2 = checkCandidateShuntNode(consecutiveShuntNode, externalNodes);
            if (cellNodesExtern2 != null) {
                splitNodes(graph, nodes, consecutiveShuntNode, cellNodesExtern2, remainingNodes, externalNodes, shuntCellsCreated);
            }
        }
    }

    private boolean isIsolatedBusOrShunt(Set<Node> remainingNodes, Node rn) {
        return (rn.getType() == Node.NodeType.BUS || rn.getType() == Node.NodeType.SHUNT)
                && rn.getAdjacentNodes().stream().noneMatch(remainingNodes::contains);
    }

    private ShuntCell createShuntCell(VoltageLevelGraph vlGraph, List<Node> shuntNodes) {
        int cellNumber = vlGraph.getNextCellNumber();
        InternalNode iNode1 = vlGraph.insertInternalNode(shuntNodes.get(0), shuntNodes.get(1),
                "Shunt " + cellNumber + ".1");
        InternalNode iNode2 = vlGraph.insertInternalNode(shuntNodes.get(shuntNodes.size() - 1),
                shuntNodes.get(shuntNodes.size() - 2), "Shunt " + cellNumber + ".2");
        shuntNodes.add(1, iNode1);
        shuntNodes.add(shuntNodes.size() - 1, iNode2);
        ShuntCell shuntCell = ShuntCell.create(cellNumber, shuntNodes);
        vlGraph.addCell(shuntCell);
        return shuntCell;
    }

    private List<Node> checkCandidateShuntNode(Node n, List<Node> externalNodes) {
        List<Node.NodeType> kindToFilter = Arrays.asList(Node.NodeType.BUS,
                Node.NodeType.FEEDER,
                Node.NodeType.SHUNT);
        /*
        the node n is candidate to be a SHUNT node if there is
        (i) at least one branch exclusively reaching BUSes
        (ii) at least one branch exclusively reaching FEEDERs
        (iii) at least one branch reaching BUSes and FEEDERs (this branch would be a Shunt)
        In that case, the BUSes branches and FEEDERs Branches constitute an EXTERN Cell,
        and returned in the cellNodesExtern
         */

        Set<Node> visitedNodes = new HashSet<>(externalNodes);
        visitedNodes.add(n); //removal of the node to explore branches from it

        List<Node> cellNodesExtern = new ArrayList<>();
        cellNodesExtern.add(n);

        boolean hasFeederBranch = false;
        boolean hasBusBranch = false;
        boolean hasMixBranch = false;

        List<Node> adjList = new ArrayList<>(n.getAdjacentNodes());
        adjList.removeAll(visitedNodes);
        for (Node adj : adjList) {
            if (!visitedNodes.contains(adj)) {
                Set<Node> resultNodes = GraphTraversal.run(adj, node -> kindToFilter.contains(node.getType()), visitedNodes);

                Set<Node.NodeType> types = resultNodes.stream() // what are the types of terminal node of the branch
                        .map(Node::getType)
                        .filter(kindToFilter::contains)
                        .collect(Collectors.toSet());

                if (types.size() > 1) {
                    hasMixBranch = true;
                } else if (types.size() == 1) {
                    hasBusBranch |= types.contains(Node.NodeType.BUS);
                    hasFeederBranch |= types.contains(Node.NodeType.FEEDER);

                    if (types.contains(Node.NodeType.BUS) || types.contains(Node.NodeType.FEEDER)) {
                        cellNodesExtern.addAll(resultNodes);
                    }
                }
                resultNodes.stream()
                        .filter(m -> m.getType() != Node.NodeType.BUS)
                        .forEach(visitedNodes::add);
            }
        }
        if (hasBusBranch && hasFeederBranch && hasMixBranch) {
            n.setType(Node.NodeType.SHUNT);
            return cellNodesExtern;
        } else {
            return null;
        }
    }

    private List<List<Node>> createShuntCellNodes(Node n, ExternCell cellExtern1, Set<Node> cellNodes) {
        List<List<Node>> shuntCellsNodes = new ArrayList<>();
        n.getAdjacentNodes().stream()
                .filter(node -> !cellExtern1.getNodes().contains(node))
                .filter(node -> node.getType() != Node.NodeType.SHUNT)
                .filter(node -> node.getCell() == null || node.getCell().getType() != Cell.CellType.SHUNT)
                .forEach(node -> {
                    List<Node> shuntCellNodes = new ArrayList<>();
                    shuntCellsNodes.add(shuntCellNodes);
                    shuntCellNodes.add(n);
                    Node currentNode = node;
                    while (currentNode.getAdjacentNodes().size() == 2 && cellNodes.contains(currentNode)) {
                        shuntCellNodes.add(currentNode);
                        currentNode = shuntCellNodes.contains(currentNode.getAdjacentNodes().get(0))
                                ? currentNode.getAdjacentNodes().get(1) : currentNode.getAdjacentNodes().get(0);
                    }
                    shuntCellNodes.add(currentNode);
                    currentNode.setType(Node.NodeType.SHUNT);
                });
        return shuntCellsNodes;
    }
}
