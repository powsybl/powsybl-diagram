/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

// WE ASSUME THAT IT IS POSSIBLE TO STACK ALL CELLS AND BE ABLE TO ORGANISE THE VOLTAGELEVAL CONSITENTLY

public class PositionFree implements PositionFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PositionFree.class);

    private static final BusCell.Direction DEFAULTDIRECTION = BusCell.Direction.TOP;

    private class Context {
        private final Graph graph;
        private final Map<BusNode, Integer> nodeToNb = new HashMap<>();
        private final Map<VerticalBusConnectionPattern, List<ExternCell>> vbcpToCells = new HashMap<>();
        private List<HorizontalChain> hChains;
        private final Map<BusNode, NodeBelonging> busToBelonging = new HashMap<>();
        private final List<ConnectedCluster> connectedClusters = new ArrayList<>();

        public Context(Graph graph) {
            this.graph = Objects.requireNonNull(graph);
        }
    }

    @Override
    public void buildLayout(Graph graph) {
        LOGGER.info("start BuildLayout");

        Context context = new Context(graph);

        indexBusPosition(context);
        initVbpcToCell(context);
        organizeWithInternCells(context);
//
//        newStructuralPosition(context);
//        initiateFeederPosition(context);

        graph.setMaxBusPosition();
        forceSameOrientationForShuntedCell(graph);
    }

    private void indexBusPosition(Context context) {
        int i = 1;
        for (BusNode n : new ArrayList<>(context.graph.getNodeBuses())) {
            context.nodeToNb.put(n, i);
            i++;
        }
    }

    private void initiateFeederPosition(Context context) {
        int i = 0;
        for (FeederNode feederNode : context.graph.getNodes().stream()
                .filter(node -> node.getType() == Node.NodeType.FEEDER)
                .map(FeederNode.class::cast)
                .sorted(Comparator.comparing(Node::getId))
                .collect(Collectors.toList())) {
            if (feederNode.getCell() != null) {
                ((ExternCell) feederNode.getCell()).setDirection(DEFAULTDIRECTION);
                feederNode.setOrder(12 * i);
                i++;
            }
        }
        context.graph.getCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.EXTERN)
                .map(ExternCell.class::cast)
                .forEach(ExternCell::orderFromFeederOrders);
    }

    private void initVbpcToCell(Context context) {
        context.graph.getCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.EXTERN)
                .map(ExternCell.class::cast)
                .forEach(cell -> addBusNodeSet(cell.getBusNodes(), cell, context));
    }

    private void addBusNodeSet(List<BusNode> busNodes, ExternCell externCell, Context context) {
        VerticalBusConnectionPattern vbcp = new VerticalBusConnectionPattern(context, busNodes);
        VerticalBusConnectionPattern targetBcp = null;
        for (VerticalBusConnectionPattern vbcp1 : new ArrayList<>(context.vbcpToCells.keySet())) {
            if (vbcp.isIncludedIn(vbcp1)) {
                targetBcp = vbcp1;
            } else if (vbcp1.isIncludedIn(vbcp)) {
                context.vbcpToCells.putIfAbsent(vbcp, new ArrayList<>());
                context.vbcpToCells.get(vbcp).addAll(context.vbcpToCells.get(vbcp1));
                context.vbcpToCells.remove(vbcp1);
                targetBcp = vbcp;
            }
        }
        if (targetBcp == null) {
            context.vbcpToCells.put(vbcp, new ArrayList<>());
            targetBcp = vbcp;
        }

        if (externCell != null) {
            context.vbcpToCells.get(targetBcp).add(externCell);
        }
    }

    private void addBusNodeSet(List<BusNode> busNodes, Context context) {
        addBusNodeSet(busNodes, null, context);
    }

    private void organizeWithInternCells(Context context) {
        List<InternCell> structuringInternCells = identifyStructuringCells(context);
        List<InternCell> candidateFlatCell = structuringInternCells.stream()
                .filter(internCell -> internCell.getBusNodes().size() == 2)
                .collect(Collectors.toList());
        context.hChains = chainNodeBusesWithFlatCells(context, candidateFlatCell);
        buildBusToBelongings(context);
        buildConnexClusters(context);
        organizeClusters(context);
    }

    private List<InternCell> identifyStructuringCells(Context context) {
        List<InternCell> structuringInternCells
                = context.graph.getCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.INTERN)
                .map(InternCell.class::cast)
                .collect(Collectors.toList());

        structuringInternCells.forEach(c -> {
            if (c.isUniLeg()) {
                addBusNodeSet(c.getSideBusNodes(Side.UNDEFINED), context);
            } else {
                addBusNodeSet(c.getSideBusNodes(Side.LEFT), context);
                addBusNodeSet(c.getSideBusNodes(Side.RIGHT), context);
            }
        });

        List<InternCell> verticalCells = structuringInternCells.stream()
                .filter(internCell ->
                        new VerticalBusConnectionPattern(context, internCell.getBusNodes()).isIncludedIn(context.vbcpToCells.keySet()) != null)
                .collect(Collectors.toList());
        structuringInternCells.removeAll(verticalCells);
        return structuringInternCells;
    }

    private List<HorizontalChain> chainNodeBusesWithFlatCells(Context context, List<InternCell> flatCells) {
        Map<BusNode, List<InternCell>> bus2flatCells = new HashMap<>();
        flatCells.forEach(cell ->
                cell.getBusNodes().forEach(busNode -> {
                    bus2flatCells.putIfAbsent(busNode, new ArrayList<>());
                    bus2flatCells.get(busNode).add(cell);
                }));

        Map<BusNode, Set<BusNode>> bus2vbcpBus = new HashMap<>();
        context.vbcpToCells.keySet()
                .forEach(vbcp -> {
                    vbcp.getBusNodeSet().forEach(bus -> {
                        bus2vbcpBus.putIfAbsent(bus, new HashSet<>());
                        bus2vbcpBus.get(bus).addAll(vbcp.getBusNodeSet());
                    });
                });

        List<HorizontalChain> chains = new ArrayList<>();

        List<BusNode> busConnectedToFlatCell = bus2flatCells.keySet().stream()
                .sorted(Comparator.comparingInt(context.nodeToNb::get))
                .sorted(Comparator.comparingInt(bus -> bus2flatCells.get(bus).size()))
                .collect(Collectors.toList());
        //this sorting is to ensure that in most cases (non circular chain) the first bus of a chain is connected to
        // a single flat cell and constitutes one extremity of the chain.

        Set<BusNode> remainingBus = new HashSet<>(context.graph.getNodeBuses());
        remainingBus.removeAll(busConnectedToFlatCell);

        while (!busConnectedToFlatCell.isEmpty()) {
            BusNode bus = busConnectedToFlatCell.get(0);
            HorizontalChain hChain = new HorizontalChain();
            rBuildHChain(hChain, bus, busConnectedToFlatCell, new ArrayList<>(busConnectedToFlatCell), bus2vbcpBus, bus2flatCells);
            chains.add(hChain);
        }
        for (BusNode bus : remainingBus) {
            HorizontalChain chain = new HorizontalChain(bus);
            chains.add(chain);
        }

        return chains.stream()
                .sorted(Comparator.comparingInt(hchain -> -hchain.busNodes.size()))
                .collect(Collectors.toList());
    }

    private void rBuildHChain(HorizontalChain hChain,
                              BusNode bus,
                              List<BusNode> busConnectedToFlatCell,
                              List<BusNode> busOnRight,
                              Map<BusNode, Set<BusNode>> bus2vbcpBus,
                              Map<BusNode, List<InternCell>> bus2flatCells) {
        hChain.busNodes.add(bus);
        busConnectedToFlatCell.remove(bus);
        busOnRight.removeAll(bus2vbcpBus.get(bus));
        for (InternCell cell : bus2flatCells.get(bus)) {
            BusNode otherBus = cell.getBusNodes()
                    .stream()
                    .filter(busNode -> busOnRight.contains(busNode)).findAny().orElse(null);
            if (otherBus != null && busConnectedToFlatCell.contains(otherBus)) {
                rBuildHChain(hChain, otherBus, busConnectedToFlatCell, busOnRight, bus2vbcpBus, bus2flatCells);
            }
        }

    }

    private void buildBusToBelongings(Context context) {
        context.vbcpToCells.keySet().forEach(vbcp ->
                vbcp.busNodeSet.forEach(busNode -> {
                    context.busToBelonging.putIfAbsent(busNode, new NodeBelonging(busNode));
                    context.busToBelonging.get(busNode).vbcps.add(vbcp);
                }));
        context.hChains.forEach(hChain -> hChain.busNodes.forEach(busNode -> {
            context.busToBelonging.putIfAbsent(busNode, new NodeBelonging(busNode));
            context.busToBelonging.get(busNode).hChain = hChain;
        }));
    }

    private void buildConnexClusters(Context context) {
        List<BusNode> remainingBuses = context.graph.getNodeBuses();
        while (!remainingBuses.isEmpty()) {
            context.connectedClusters.add(new ConnectedCluster(context, remainingBuses));
        }
    }

    private void organizeClusters(Context context) {
        int firstStructuralPosition = 0;
        int firstFeederOrder = 1;
        context.graph.getNodeBuses().forEach(busNode -> busNode.setStructuralPosition(null));
        for (ConnectedCluster cc : context.connectedClusters) {
            firstStructuralPosition = cc.setStructuralPositions(firstStructuralPosition + 1);
            firstFeederOrder = cc.setCellOrders(firstFeederOrder);
        }
    }

    private void newStructuralPosition(Context context) {
        int i = 1;
        for (VerticalBusConnectionPattern vbcp : context.vbcpToCells.keySet()) {
            int j = 1;
            for (BusNode busNode : vbcp.getBusNodeSet()) {
                if (busNode.getStructuralPosition() == null) {
                    busNode.setStructuralPosition(new Position(i, j));
                }
                j++;
            }
            i++;
        }
        for (BusNode bus : context.graph.getNodeBuses()) {
            if (bus.getStructuralPosition() == null) {
                bus.setStructuralPosition(new Position(i, 1));
                i++;
            }
        }
    }

    private class VerticalBusConnectionPattern {
        private Set<BusNode> busNodeSet;

        VerticalBusConnectionPattern(Context context, List<BusNode> busNodees) {
            busNodeSet = new TreeSet<>(Comparator.comparingInt(context.nodeToNb::get));
            busNodeSet.addAll(busNodees);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof VerticalBusConnectionPattern) {
                return busNodeSet.equals(((VerticalBusConnectionPattern) o).busNodeSet);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return busNodeSet.hashCode();
        }

        boolean isIncludedIn(VerticalBusConnectionPattern vbcp2) {
            Iterator<BusNode> it1 = busNodeSet.iterator();
            Iterator<BusNode> it2 = vbcp2.getBusNodeSet().iterator();
            boolean match = true;
            while (it1.hasNext() && match) {
                BusNode n1 = it1.next();
                match = false;
                while (it2.hasNext() && !match) {
                    BusNode n2 = it2.next();
                    match = n2 == n1;
                }
            }
            return match;
        }

        VerticalBusConnectionPattern isIncludedIn(Set<VerticalBusConnectionPattern> busConnectionPatterns) {
            for (VerticalBusConnectionPattern candidateBCPIncluser : busConnectionPatterns) {
                if (isIncludedIn(candidateBCPIncluser)) {
                    return candidateBCPIncluser;
                }
            }
            return null;
        }

        Set<BusNode> getBusNodeSet() {
            return busNodeSet;
        }
    }

    private class HorizontalChain {
        List<BusNode> busNodes;
        int vbcpOrder;
        int v;

        HorizontalChain() {
            busNodes = new ArrayList<>();
            v = 0;
        }

        HorizontalChain(BusNode busNode) {
            this();
            busNodes.add(busNode);
        }

        int getPosition(BusNode bus) {
            return busNodes.indexOf(bus);
        }

        int getDeltaPosition(BusNode bus1, BusNode bus2) {
            return getPosition(bus1) - getPosition(bus2);
        }

        void alignTo(HorizontalChain other) {
            List<BusNode> intersection = new ArrayList<>(other.busNodes);
            intersection.retainAll(busNodes);
            for (int i = 0; i < intersection.size(); i++) {
                for (int j = i + 1; j < intersection.size(); j++) {
                    BusNode bus1 = intersection.get(i);
                    BusNode bus2 = intersection.get(j);
                    if (getDeltaPosition(bus1, bus2) * other.getDeltaPosition(bus1, bus2) < 0) {
                        Collections.reverse(busNodes);
                        return;
                    }
                }
            }
        }
    }

    private class NodeBelonging {
        BusNode busNode;
        List<VerticalBusConnectionPattern> vbcps;
        HorizontalChain hChain;

        NodeBelonging(BusNode bus) {
            busNode = bus;
            vbcps = new ArrayList<>();
        }
    }

    /**
     * A connectedCluster bundle busNodes that are connected through a path of HChains and VerticalBusConnectionPatterns
     */
    private class ConnectedCluster {
        Set<NodeBelonging> buses;
        List<VerticalBusConnectionPattern> vbcps;
        List<HorizontalChain> hChains;
        Context context;

        ConnectedCluster(Context context, List<BusNode> remainingBuses) {
            this.context = context;
            buses = new HashSet<>();
            rBuild(remainingBuses.get(0), remainingBuses);
            vbcps = buses.stream().flatMap(bus -> bus.vbcps.stream()).distinct().collect(Collectors.toList());
            hChains = buses.stream().map(bus -> bus.hChain).distinct().collect(Collectors.toList());
            alignChains();
            sortVbcp();
            organizeHChainsVertically();
        }

        private void rBuild(BusNode startingNode, List<BusNode> remainingBuses) {
            if (remainingBuses.contains(startingNode)) {
                buses.add(context.busToBelonging.get(startingNode));
                remainingBuses.remove(startingNode);
                NodeBelonging nodeBelonging = context.busToBelonging.get(startingNode);
                List<BusNode> busToHandle = nodeBelonging.vbcps.stream()
                        .flatMap(vbcp -> vbcp.busNodeSet.stream()).collect(Collectors.toList());
                busToHandle.addAll(nodeBelonging.hChain.busNodes);
                busToHandle.forEach(busNode -> rBuild(busNode, remainingBuses));
            }
        }

        private void alignChains() {
            for (int i = 0; i < hChains.size(); i++) {
                for (int j = i + 1; j < hChains.size(); j++) {
                    hChains.get(j).alignTo(hChains.get(i));
                }
            }
        }

        private List<HorizontalChain> gethChainsFromVbcp(VerticalBusConnectionPattern vbcp) {
            return vbcp.busNodeSet.stream()
                    .map(context.busToBelonging::get)
                    .map(nodeBelonging -> nodeBelonging.hChain).collect(Collectors.toList());
        }

        private BusNode intersectionNode(Collection<BusNode> busNodes1, Collection<BusNode> busNodes2) {
            List<BusNode> intersectionList = new ArrayList<>(busNodes1);
            intersectionList.retainAll(busNodes2);
            if (intersectionList.isEmpty()) {
                return null;
            } else {
                return intersectionList.get(0);
            }
        }

        private void sortVbcp() {
            if (vbcps.isEmpty()) {
                return;
            }
            List<VerticalBusConnectionPattern> remainingVbcp = new ArrayList<>(vbcps);
            List<VerticalBusConnectionPattern> sortedVbcp = new ArrayList<>();
            sortedVbcp.add(remainingVbcp.get(0));
            remainingVbcp.remove(0);
            int previousSize;
            while (!remainingVbcp.isEmpty()) {
                previousSize = remainingVbcp.size();
                sortWhenObviousComparisonsExist(remainingVbcp, sortedVbcp);
                if (previousSize == remainingVbcp.size()) { //plan B !
                    unblockSortingWithSimilarityCriteria(remainingVbcp, sortedVbcp);
                }
            }
            vbcps = sortedVbcp;
        }

        private void sortWhenObviousComparisonsExist(List<VerticalBusConnectionPattern> remainingVbcp,
                                                     List<VerticalBusConnectionPattern> sortedVbcp) {
            for (VerticalBusConnectionPattern vbcp : remainingVbcp) {
                if (tryToInsertVbcp(vbcp, sortedVbcp)) {
                    remainingVbcp.remove(vbcp);
                    break;
                }
            }
        }

        private boolean tryToInsertVbcp(VerticalBusConnectionPattern vbcp,
                                        List<VerticalBusConnectionPattern> sortedList) {
            int compare = 0;
            for (VerticalBusConnectionPattern iterVbcp : sortedList) {
                compare = compareHVbcp(vbcp, iterVbcp);
                if (compare < 0) {
                    int position = sortedList.indexOf(iterVbcp);
                    sortedList.add(position, vbcp);
                    return true;
                }
            }
            if (compare > 0) {
                sortedList.add(vbcp);
                return true;
            }
            return false;
        }

        // don't use it as a comparator : if 2 vbcp are not comparable, the result is 0,
        // but the to vbcp could be far from one another -> necessary to have a dedicated sorting function -> sortVbcp()
        private int compareHVbcp(VerticalBusConnectionPattern vbcp1, VerticalBusConnectionPattern vbcp2) {
            List<HorizontalChain> commonChains = intersectChains(vbcp1, vbcp2);
            if (commonChains.isEmpty()) {
                return 0;
            }
            for (HorizontalChain chain : commonChains) {
                int index1 = chain.getPosition(intersectionNode(chain.busNodes, vbcp1.busNodeSet));
                int index2 = chain.getPosition(intersectionNode(chain.busNodes, vbcp2.busNodeSet));
                if (index1 != -1 && index2 != -1 && index1 != index2) {
                    return index1 - index2;
                }
            }
            return 0;
        }

        private List<HorizontalChain> intersectChains(VerticalBusConnectionPattern vbcp1, VerticalBusConnectionPattern vbcp2) {
            List<HorizontalChain> commonChains = gethChainsFromVbcp(vbcp1);
            commonChains.retainAll(gethChainsFromVbcp(vbcp2));
            return commonChains;
        }

        private void unblockSortingWithSimilarityCriteria(List<VerticalBusConnectionPattern> remainingVbcps,
                                                          List<VerticalBusConnectionPattern> sortedVbcps) {
            VerticalBusConnectionPattern matchingRemainingVbcp = remainingVbcps.get(0);
            VerticalBusConnectionPattern matchingSortedVbcp = sortedVbcps.get(0);
            int maxIntersection = 0;
            for (VerticalBusConnectionPattern sortedVbcp : sortedVbcps) {
                for (VerticalBusConnectionPattern remainingVbcp : remainingVbcps) {
                    int intersectionSize = intersectChains(sortedVbcp, remainingVbcp).size();
                    if (intersectionSize > maxIntersection) {
                        maxIntersection = intersectionSize;
                        matchingRemainingVbcp = remainingVbcp;
                        matchingSortedVbcp = sortedVbcp;
                    }
                }
            }
            sortedVbcps.add(sortedVbcps.indexOf(matchingSortedVbcp) + 1, matchingRemainingVbcp);
            remainingVbcps.remove(matchingRemainingVbcp);
        }

        private void organizeHChainsVertically() {
            int vbcpOrder = 0;
            for (VerticalBusConnectionPattern vbcp : vbcps) {
                Set<Integer> vBooked = new TreeSet<>(Comparator.comparingInt(Integer::intValue));
                vbcp.busNodeSet.forEach(bus -> vBooked.add(context.busToBelonging.get(bus).hChain.v));
                for (BusNode bus : vbcp.busNodeSet) {
                    HorizontalChain chain = context.busToBelonging.get(bus).hChain;
                    if (chain.v == 0) {
                        int v = firstAvailableIndex(vBooked);
                        chain.v = v;
                        chain.vbcpOrder = vbcpOrder;
                        vBooked.add(v);
                    }
                }
                vbcpOrder++;
            }
        }

        private int firstAvailableIndex(Set<Integer> integerSet) {
            if (integerSet.isEmpty()
                    || integerSet.size() == 1 && integerSet.iterator().next() == 0) {
                return 1;
            }
            int h = 1;
            if (integerSet.iterator().next() == 0) {
                h = 0;
            }
            for (int i : integerSet) {
                if (i == h) {
                    h++;
                } else {
                    return h;
                }
            }
            return h;
        }

        int setStructuralPositions(int firstStructuralHPosition) {
            int maxH = firstStructuralHPosition;
            for (HorizontalChain chain : hChains) {
                int newH = chain.vbcpOrder + firstStructuralHPosition;
                for (BusNode bus : chain.busNodes) {
                    if (bus.getStructuralPosition() == null) {
                        bus.setStructuralPosition(new Position(newH++, chain.v));
                    }
                }
                maxH = Math.max(maxH, newH);
            }
            return maxH;
        }

        int setCellOrders(int firstFeederOrder) {
            int feederPosition = firstFeederOrder;
            int cellPos = 0;
            for (VerticalBusConnectionPattern vbcp : vbcps) {
                for (ExternCell cell : context.vbcpToCells.get(vbcp)) {
                    cell.setDirection(cellPos % 2 == 0 ? BusCell.Direction.TOP : BusCell.Direction.BOTTOM);
                    cell.setOrder(cellPos);
                    cellPos++;
                    for (FeederNode feederNode : cell.getNodes().stream()
                            .filter(n -> n.getType() == Node.NodeType.FEEDER)
                            .map(FeederNode.class::cast).collect(Collectors.toList())) {
                        feederNode.setOrder(feederPosition);
                        feederPosition++;
                    }
                }
            }
            return feederPosition;
        }
    }
}
