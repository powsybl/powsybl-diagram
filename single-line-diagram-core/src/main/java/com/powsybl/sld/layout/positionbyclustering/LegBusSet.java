/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionbyclustering;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.InternCell;
import com.powsybl.sld.model.Side;

import java.util.*;

/**
 * A LegBusSet contains the set of BusNodes that shall be vertically presented, and the cells that have a pattern of
 * connection included in the busNodeSet. It is embedded into a LBSCluster. It contains links to all the other
 * LegBusSet of the Graph. It extends AbstractClusterConnector as it can be used as a clusterConnector for the cluster
 * merging strategy based on LegBusSet Link.
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
class LegBusSet implements ClusterConnector<LegBusSet> {

    private final Set<BusNode> busNodeSet;
    private final Set<BusCell> embeddedCells;
    private final Map<InternCell, Side> candidateFlatCells;
    private final Map<InternCell, Side> crossoverInternCells;
    private LBSCluster lbsCluster;
    private final List<Link<LegBusSet>> myLinks;
    private final Map<BusNode, Integer> nodeToNb;

    LegBusSet(Map<BusNode, Integer> nodeToNb, List<BusNode> busNodes) {
        this.nodeToNb = nodeToNb;
        busNodeSet = new TreeSet<>(Comparator.comparingInt(nodeToNb::get));
        busNodeSet.addAll(busNodes);
        embeddedCells = new LinkedHashSet<>();
        candidateFlatCells = new LinkedHashMap<>();
        crossoverInternCells = new LinkedHashMap<>();
        myLinks = new ArrayList<>();
    }

    LegBusSet(Map<BusNode, Integer> nodeToNb, BusCell cell) {
        this(nodeToNb, cell.getBusNodes());
        embeddedCells.add(cell);
    }

    LegBusSet(Map<BusNode, Integer> nodeToNb, InternCell internCell, Side side) {
        this(nodeToNb, internCell.getSideBusNodes(side));
        if (internCell.getBusNodes().size() == 1) {
            embeddedCells.add(internCell);
        } else if (internCell.getBusNodes().size() == 2) {
            candidateFlatCells.put(internCell, side);
        } else {
            crossoverInternCells.put(internCell, side);
        }
    }

    LegBusSet(Map<BusNode, Integer> nodeToNb, BusNode busNode) {
        this(nodeToNb, Collections.singletonList(busNode));
    }

    boolean contains(Collection<BusNode> busNodeCollection) {
        return busNodeSet.containsAll(busNodeCollection);
    }

    boolean contains(LegBusSet lbs) {
        return contains(lbs.getBusNodeSet());
    }

    void addEmbededCell(BusCell busCell) {
        embeddedCells.add(busCell);
    }

    void absorbs(LegBusSet lbsToAbsorb) {
        busNodeSet.addAll(lbsToAbsorb.getBusNodeSet());
        embeddedCells.addAll(lbsToAbsorb.getEmbeddedCells());
        absorbMap(candidateFlatCells, lbsToAbsorb.getCandidateFlatCells());
        absorbMap(crossoverInternCells, lbsToAbsorb.getCrossoverInternCell());
    }

    private void absorbMap(Map<InternCell, Side> myMap, Map<InternCell, Side> map) {
        Set<InternCell> commonCells = new LinkedHashSet<>(myMap.keySet());
        Set<InternCell> cellToAbsorb = map.keySet();
        commonCells.retainAll(cellToAbsorb);
        for (InternCell commonCell : commonCells) {
            if (myMap.get(commonCell) == Side.RIGHT && map.get(commonCell) == Side.LEFT
                    || myMap.get(commonCell) == Side.LEFT && map.get(commonCell) == Side.RIGHT) {
                embeddedCells.add(commonCell);
                myMap.remove(commonCell);
            } else {
                throw new PowsyblException("Absorption of InternCell in a LegBusSet should concern both side of the InternCell");
            }
        }
        cellToAbsorb.removeAll(commonCells);
        cellToAbsorb.forEach(internCell -> myMap.put(internCell, map.get(internCell)));
    }

    void checkInternCells() {
        genericCheckInternCells(candidateFlatCells);
        genericCheckInternCells(crossoverInternCells);
    }

    private void genericCheckInternCells(Map<InternCell, Side> cells) {
        List<InternCell> cellActuallyEmbeded = new ArrayList<>();
        cells.forEach((internCell, side) -> {
            List<BusNode> otherLegBusNodes = internCell
                    .getSideBusNodes(side.getFlip());
            if (busNodeSet.containsAll(otherLegBusNodes)) {
                cellActuallyEmbeded.add(internCell);
            }
        });
        cellActuallyEmbeded.forEach(cells::remove);
        embeddedCells.addAll(cellActuallyEmbeded);
    }

    void setLbsCluster(LBSCluster lbsCluster) {
        this.lbsCluster = lbsCluster;
    }

    public LBSCluster getCluster() {
        return lbsCluster;
    }

    @Override
    public Side getMySideInCluster() {
        return lbsCluster.getLbsSide(this);
    }

    Map<InternCell, Side> getCandidateFlatCells() {
        return candidateFlatCells;
    }

    @Override
    public List<InternCell> getCandidateFlatCellList() {
        return new ArrayList<>(candidateFlatCells.keySet());
    }

    Map<InternCell, Side> getCrossoverInternCell() {
        return crossoverInternCells;
    }

    @Override
    public List<InternCell> getCrossOverCellList() {
        return new ArrayList<>(crossoverInternCells.keySet());
    }

    @Override
    public Set<BusNode> getBusNodeSet() {
        return busNodeSet;
    }

    @Override
    public int getDistanceToEdge(InternCell internCell) {
        return 0;
    }

    @Override
    public boolean hasSameRoot(Object other) {
        if (other.getClass() != LegBusSet.class) {
            return false;
        }
        return this == other;
    }

    @Override
    public void addLink(Link<LegBusSet> link) {
        myLinks.add(link);
    }

    @Override
    public void removeLink(Link<LegBusSet> link) {
        myLinks.remove(link);
    }

    @Override
    public List<Link<LegBusSet>> getLinks() {
        return myLinks;
    }

    Set<BusCell> getEmbeddedCells() {
        return embeddedCells;
    }

    public LegBusSet getOtherSameRoot(List<LegBusSet> clusterConnectors) {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LegBusSet) {
            return busNodeSet.equals(((LegBusSet) o).busNodeSet);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int i = 2039;
        int hash = 0;
        for (BusNode busNode : busNodeSet) {
            hash += i * nodeToNb.get(busNode);
            i *= 2029;
        }
        return hash;
    }

}
