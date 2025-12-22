/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.cells.Cell;
import com.powsybl.sld.model.cells.ExternCell;
import com.powsybl.sld.model.cells.InternCell;
import com.powsybl.sld.model.cells.ShuntCell;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.ConnectivityNode;
import com.powsybl.sld.model.nodes.Node;
import com.powsybl.sld.util.GraphTraversal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.powsybl.sld.model.nodes.Node.NodeType.BUS;
import static com.powsybl.sld.model.nodes.Node.NodeType.FEEDER;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class ImplicitCellDetector implements CellDetector {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImplicitCellDetector.class);

    /**
     * internCell detection: an internal cell is composed of nodes connecting BUSes without connecting Feeder.
     * detectCell is used to detect cells exploring the graph and scanning exclusionTypes and stopTypes
     * <p>
     * *************INTERN CELL*******************
     * exclusion types = {FEEDER}: if a FEEDER type is reached it is not an INTERN CELL
     * stop the visit if reach a Bus: stopTypes = {BUS}
     * ***************EXTERN AND SHUNT CELLS******
     * detection: nodes connecting buses and departures without being in an internCell (previously allocated nodes)
     * exclusion types = {}
     * stop the visit if reach a Bus: stopTypes = {BUS, FEEDER} * @param graph g
     */
    @Override
    public void detectCells(VoltageLevelGraph graph) {
        LOGGER.info("Detecting cells...");

        List<Node> allocatedNodes = new ArrayList<>();
        // **************INTERN CELL*******************
        List<Node.NodeType> exclusionTypes = new ArrayList<>();
        exclusionTypes.add(FEEDER);
        List<Node.NodeType> stopTypes = new ArrayList<>();
        stopTypes.add(BUS);
        List<Set<Node>> internCellsNodes = detectCell(graph, stopTypes, exclusionTypes, allocatedNodes);
        for (Set<Node> nodes : internCellsNodes) {
            graph.addCell(new InternCell(graph.getNextCellNumber(), nodes));
        }

        // ****************EXTERN AND SHUNT CELLS******
        stopTypes.add(FEEDER);
        List<Set<Node>> externCellsNodes = detectCell(graph, stopTypes, new ArrayList<>(), allocatedNodes);

        for (Set<Node> nodes : externCellsNodes) {
            createExternAndShuntCells(graph, nodes);
        }

        graph.getCellStream().forEach(Cell::getFullId);

        graph.logCellDetectionStatus();
    }

    private void createExternAndShuntCells(VoltageLevelGraph graph, Set<Node> nodes) {
        createExternAndShuntCells(graph, nodes, Collections.emptyList());
    }

    private void createExternAndShuntCells(VoltageLevelGraph graph, Set<Node> nodes, List<ShuntCell> shuntCells) {
        if (isPureExternCell(graph, nodes)) {
            ExternCell.create(graph, nodes, shuntCells);
        } else {
            // if a shunt cell is detected two or more EXTERN cells and one or more SHUNT cells are created
            detectAndTypeShunt(graph, nodes, shuntCells);
        }
    }

    /**
     * @param graph          The voltage level graph
     * @param typeStops      The types of node that stops the exploration
     * @param exclusionTypes The types of node that, when reached, consider the exploration unsuccessful
     * @param allocatedNodes The list of nodes already allocated to a cell.
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
                        .filter(node -> node.getType() != BUS)
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
                        .distinct().filter(t -> t == FEEDER || t == BUS)
                        .toList();
                if (types.size() == 2) {
                    hasMixBranch = true;
                } else if (types.isEmpty()) {
                    return false;
                } else if (types.getFirst().equals(FEEDER)) {
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
     * @param nodes the nodes among which there is a suspected shunt
     * @param shuntCells the shunt cells which are linked to the given nodes
     **/
    private void detectAndTypeShunt(VoltageLevelGraph graph, Set<Node> nodes, List<ShuntCell> shuntCells) {

        List<Node> externalNodes = graph.getNodes().stream()
                .filter(node -> !nodes.contains(node))
                .collect(Collectors.toList());

        Optional<List<Node>> cellNodesExtern = nodes.stream().filter(n -> n.getAdjacentNodes().size() > 2 && n instanceof ConnectivityNode) // optimisation : a Shunt node has necessarily 3 ore more adjacent nodes and must be InternalNode
                .map(n -> checkCandidateShuntNode((ConnectivityNode) n, externalNodes))
                .filter(nodesExternCell -> !nodesExternCell.isEmpty()).findFirst();

        if (cellNodesExtern.isPresent()) {
            Set<Node> remainingNodes = new LinkedHashSet<>(nodes);
            List<ShuntCell> shuntCellsCreated = new ArrayList<>(shuntCells);
            ConnectivityNode shuntNode = (ConnectivityNode) cellNodesExtern.get().getFirst();  // reminder: the first node returned by checkCandidateShuntNode is the candidateShuntNode and is therefore a checked InternalNode
            splitNodes(graph, nodes, shuntNode, cellNodesExtern.get(), remainingNodes, externalNodes, shuntCellsCreated);

            // buses and shunts are kept as they might be shared, but if isolated, they should be removed now from remaining nodes
            remainingNodes.removeIf(rn -> isIsolatedBusOrShunt(remainingNodes, rn));

            // when created, a shunt cell created is attached to the left to a pure extern cell; hence only the right side is checked
            List<ShuntCell> linkedShuntCells = shuntCellsCreated.stream()
                    .filter(sc -> remainingNodes.contains(sc.getSideShuntNode(Side.RIGHT)))
                    .collect(Collectors.toList());

            // create extern and shunt cells with the remaining nodes
            createExternAndShuntCells(graph, remainingNodes, linkedShuntCells);

        } else {
            // if no shunt node is found (checkCandidateShuntNode always returns an empty list), create a cell anyway with all nodes
            ExternCell.create(graph, nodes, shuntCells);
        }
    }

    private static boolean isShunt(Node node) {
        return node instanceof ConnectivityNode && ((ConnectivityNode) node).isShunt();
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
    private void splitNodes(VoltageLevelGraph graph, Set<Node> nodes, ConnectivityNode shuntNode, List<Node> cellNodesExtern,
                            Set<Node> remainingNodes, List<Node> externalNodes, List<ShuntCell> shuntCellsCreated) {

        // create the new pure external cell
        List<ShuntCell> linkedShuntCells = shuntCellsCreated.stream()
                .filter(shuntCell -> cellNodesExtern.contains(shuntCell.getSideShuntNode(Side.RIGHT)))
                .collect(Collectors.toList());
        ExternCell newExternCell = ExternCell.create(graph, cellNodesExtern, linkedShuntCells);

        // remove used nodes from remaining nodes
        cellNodesExtern.stream()
                .filter(m -> m.getType() != BUS && !isShunt(m))
                .forEach(remainingNodes::remove);

        //create the shunt cells
        List<List<Node>> shuntsNodes = createShuntCellNodes(graph, shuntNode, newExternCell, nodes);
        shuntsNodes.stream().flatMap(Collection::stream)
                .filter(m -> !isShunt(m))
                .forEach(remainingNodes::remove);
        shuntsNodes.stream()
                .map(shuntNodes -> ShuntCell.create(graph, shuntNodes))
                .forEach(shuntCell -> {
                    newExternCell.addShuntCell(shuntCell);
                    shuntCellsCreated.add(shuntCell);
                });

        //look for consecutive shunt nodes
        for (List<Node> shuntNodes : shuntsNodes) {
            ConnectivityNode consecutiveShuntNode = (ConnectivityNode) shuntNodes.getLast(); //createShuntCellNodes ensure last node is InternalNode
            List<Node> cellNodesExtern2 = checkCandidateShuntNode(consecutiveShuntNode, externalNodes);
            if (!cellNodesExtern2.isEmpty()) {
                splitNodes(graph, nodes, consecutiveShuntNode, cellNodesExtern2, remainingNodes, externalNodes, shuntCellsCreated);
            }
        }
    }

    private boolean isIsolatedBusOrShunt(Set<Node> remainingNodes, Node rn) {
        return (rn.getType() == BUS || isShunt(rn))
                && rn.getAdjacentNodes().stream().noneMatch(remainingNodes::contains);
    }

    /**
     *
     * @param candidateShuntNode an InternalNode that could be a shunt
     * @param externalNodes the nodes of the graph that are outside of the cell
     * @return a list of Node. Important: the first node is the candidateShuntNode and is an InternalNode
     */
    private List<Node> checkCandidateShuntNode(ConnectivityNode candidateShuntNode, List<Node> externalNodes) {
        Predicate<Node> filter = node -> node.getType() == BUS || node.getType() == FEEDER || isShunt(node);
        /*
        the node n is candidate to be a SHUNT node if there is
        (i) at least one branch exclusively reaching BUSes
        (ii) at least one branch exclusively reaching FEEDERs
        (iii) at least one branch reaching BUSes and FEEDERs (this branch would be a Shunt)
        In that case, the BUSes branches and FEEDERs Branches constitute an EXTERN Cell,
        and returned in the cellNodesExtern
         */

        Set<Node> visitedNodes = new HashSet<>(externalNodes);
        visitedNodes.add(candidateShuntNode); //removal of the node to explore branches from it

        List<Node> cellNodesExtern = new ArrayList<>();
        cellNodesExtern.add(candidateShuntNode); // reminder : the first node of cellNodesExtern is the candidateShuntNode

        boolean hasFeederBranch = false;
        boolean hasBusBranch = false;
        boolean hasMixBranch = false;

        List<Node> adjList = new ArrayList<>(candidateShuntNode.getAdjacentNodes());
        adjList.removeAll(visitedNodes);
        for (Node adj : adjList) {
            Set<Node> resultNodes = GraphTraversal.run(adj, filter, visitedNodes);

            boolean hasShunt = resultNodes.stream().anyMatch(ImplicitCellDetector::isShunt);
            boolean hasBus = resultNodes.stream().anyMatch(node -> node.getType() == BUS);
            boolean hasFeeder = resultNodes.stream().anyMatch(node -> node.getType() == FEEDER);
            int nbTypes = (hasShunt ? 1 : 0) + (hasBus ? 1 : 0) + (hasFeeder ? 1 : 0);

            if (nbTypes > 1) {
                hasMixBranch = true;
            } else if (nbTypes == 1) {
                hasBusBranch |= hasBus;
                hasFeederBranch |= hasFeeder;

                if (hasBus || hasFeeder) {
                    cellNodesExtern.addAll(resultNodes);
                }
            }
            resultNodes.stream()
                    .filter(m -> m.getType() != BUS)
                    .forEach(visitedNodes::add);
        }
        return selectShuntNode(candidateShuntNode, cellNodesExtern, hasBusBranch, hasFeederBranch, hasMixBranch);
    }

    private static List<Node> selectShuntNode(ConnectivityNode candidateShuntNode, List<Node> cellNodesExtern,
                                              boolean hasBusBranch, boolean hasFeederBranch, boolean hasMixBranc) {
        if (hasBusBranch && hasFeederBranch && hasMixBranc) {
            candidateShuntNode.setShunt(true);
            return cellNodesExtern;  // reminder : the first node of cellNodesExtern is the candidateShuntNode
        } else {
            return Collections.emptyList();
        }
    }

    /**
     *
     * @param graph the VoltageLevelGraph
     * @param shuntNode a node that is candidate to be a shunt
     * @param cellExtern1 the ExternCell on one side of the shunt
     * @param cellNodes the nodes that are to be gatherd into shuntCells
     * @return  a list of list of nodes. In each list, the first and last nodes are shunt and therefore InternalNode
     */
    private List<List<Node>> createShuntCellNodes(VoltageLevelGraph graph, ConnectivityNode shuntNode, ExternCell cellExtern1, Set<Node> cellNodes) {
        List<List<Node>> shuntCellsNodes = new ArrayList<>();
        shuntNode.getAdjacentNodes().stream()
                .filter(node -> !cellExtern1.getNodes().contains(node))
                .filter(node -> !isShunt(node))
                .filter(node -> graph.getCell(node).map(c -> c.getType() != Cell.CellType.SHUNT).orElse(true))
                .forEach(node -> {
                    List<Node> shuntCellNodes = new ArrayList<>();
                    shuntCellsNodes.add(shuntCellNodes);
                    shuntCellNodes.add(shuntNode);
                    Node currentNode = node;
                    while (currentNode.getAdjacentNodes().size() == 2 && cellNodes.contains(currentNode)) {
                        shuntCellNodes.add(currentNode);
                        currentNode = shuntCellNodes.contains(currentNode.getAdjacentNodes().get(0))
                                ? currentNode.getAdjacentNodes().get(1) : currentNode.getAdjacentNodes().get(0);
                    }
                    if (currentNode instanceof ConnectivityNode) {
                        shuntCellNodes.add(currentNode);
                        ((ConnectivityNode) currentNode).setShunt(true);
                    } else {
                        shuntCellsNodes.remove(shuntCellNodes);
                    }
                });
        return shuntCellsNodes;
    }
}
