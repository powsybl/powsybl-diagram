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
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
class LegBusSet implements Linkable {
    private Set<BusNode> busNodeSet;
    private Set<BusCell> embededCells;
    private Map<InternCell, Side> candidateFlatCells;
    private Map<InternCell, Side> crossoverInternCells;
    private LBSCluster lbsCluster;
    private List<Link<LegBusSet>> myLinks;

    LegBusSet(Map<BusNode, Integer> nodeToNb, List<BusNode> busNodes) {
        busNodeSet = new TreeSet<>(Comparator.comparingInt(nodeToNb::get));
        busNodeSet.addAll(busNodes);
        embededCells = new HashSet<>();
        candidateFlatCells = new HashMap<>();
        crossoverInternCells = new HashMap<>();
        myLinks = new ArrayList<>();
    }

    LegBusSet(Map<BusNode, Integer> nodeToNb, BusCell cell) {
        this(nodeToNb, cell.getBusNodes());
        embededCells.add(cell);
    }

    LegBusSet(Map<BusNode, Integer> nodeToNb, InternCell internCell, Side side) {
        this(nodeToNb, internCell.getSideBusNodes(side));
        if (internCell.getBusNodes().size() == 1) {
            embededCells.add(internCell);
        } else if (internCell.getBusNodes().size() == 2) {
            candidateFlatCells.put(internCell, side);
        } else {
            crossoverInternCells.put(internCell, side);
        }
    }

    LegBusSet(Map<BusNode, Integer> nodeToNb, BusNode busNode) {
        this(nodeToNb, Collections.singletonList(busNode));
    }

    boolean contains(LegBusSet lbs) {
        return busNodeSet.containsAll(lbs.getBusNodeSet());
    }

    void absorbs(LegBusSet lbsToAbsorb) {
        busNodeSet.addAll(lbsToAbsorb.getBusNodeSet());
        embededCells.addAll(lbsToAbsorb.getEmbededCells());
        absorbMap(candidateFlatCells, lbsToAbsorb.getCandidateFlatCells());
        absorbMap(crossoverInternCells, lbsToAbsorb.getCrossoverInternCell());
    }

    private void absorbMap(Map<InternCell, Side> myMap, Map<InternCell, Side> map) {
        Set<InternCell> commonCells = new HashSet<>(myMap.keySet());
        Set<InternCell> cellToAbsorb = map.keySet();
        commonCells.retainAll(cellToAbsorb);
        for (InternCell commonCell : commonCells) {
            if (myMap.get(commonCell) == Side.RIGHT && map.get(commonCell) == Side.LEFT
                    || myMap.get(commonCell) == Side.LEFT && map.get(commonCell) == Side.RIGHT) {
                embededCells.add(commonCell);
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
        embededCells.addAll(cellActuallyEmbeded);
    }

    void setLbsCluster(LBSCluster lbsCluster) {
        this.lbsCluster = lbsCluster;
    }

    public LBSCluster getCluster() {
        return lbsCluster;
    }

    public Side getMySidInCluster() {
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

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Linkable> void addLink(Link<T> link) {
        myLinks.add((Link<LegBusSet>) link);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Linkable> void removeLink(Link<T> link) {
        myLinks.remove(link);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Link<LegBusSet>> getLinks() {
        return myLinks;
    }

    Set<BusCell> getEmbededCells() {
        return embededCells;
    }

    public <T extends Linkable> T getOtherSameRoot(List<T> linkables) {
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
        return busNodeSet.hashCode();
    }

}
