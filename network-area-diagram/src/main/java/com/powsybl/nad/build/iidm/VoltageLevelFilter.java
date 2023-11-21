/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.build.iidm;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.nad.utils.iidm.IidmUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class VoltageLevelFilter implements Predicate<VoltageLevel> {

    public static final Predicate<VoltageLevel> NO_FILTER = voltageLevel -> true;

    private final Set<VoltageLevel> voltageLevels;

    public VoltageLevelFilter(Set<VoltageLevel> voltageLevels) {
        this.voltageLevels = voltageLevels;
    }

    public int getNbVoltageLevels() {
        return voltageLevels.size();
    }

    private Set<VoltageLevel> getVoltageLevels() {
        return voltageLevels;
    }

    private static final String UNKNOWN_VOLTAGE_LEVEL = "Unknown voltage level id '";

    @Override
    public boolean test(VoltageLevel voltageLevel) {
        return voltageLevels.contains(voltageLevel);
    }

    public static VoltageLevelFilter createVoltageLevelDepthFilter(Network network, String voltageLevelId, int depth) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(voltageLevelId);

        Set<VoltageLevel> voltageLevels = new HashSet<>();
        VoltageLevel vl = network.getVoltageLevel(voltageLevelId);
        if (vl == null) {
            throw new PowsyblException(UNKNOWN_VOLTAGE_LEVEL + voltageLevelId + "'");
        }

        Set<VoltageLevel> startingSet = new HashSet<>();
        startingSet.add(vl);
        traverseVoltageLevels(startingSet, depth, voltageLevels);
        return new VoltageLevelFilter(voltageLevels);
    }

    public static VoltageLevelFilter createVoltageLevelsDepthFilter(Network network, List<String> voltageLevelIds, int depth) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(voltageLevelIds);
        Set<VoltageLevel> startingSet = new HashSet<>();
        for (String voltageLevelId : voltageLevelIds) {
            VoltageLevel vl = network.getVoltageLevel(voltageLevelId);
            if (vl == null) {
                throw new PowsyblException(UNKNOWN_VOLTAGE_LEVEL + voltageLevelId + "'");
            }
            startingSet.add(vl);
        }

        Set<VoltageLevel> voltageLevels = new HashSet<>();
        traverseVoltageLevels(startingSet, depth, voltageLevels);
        return new VoltageLevelFilter(voltageLevels);
    }

    public static VoltageLevelFilter createVoltageLevelsFilter(Network network, List<String> voltageLevelIds) {
        return createVoltageLevelsDepthFilter(network, voltageLevelIds, 0);
    }

    public static Collection<VoltageLevel> getNextDepthVoltageLevels(Network network, List<VoltageLevel> voltageLevels) {
        List<String> voltageLevelIds = voltageLevels.stream().map(VoltageLevel::getId).collect(Collectors.toList());
        VoltageLevelFilter voltageLevelFilter = createVoltageLevelsDepthFilter(network, voltageLevelIds, 1);
        Set<VoltageLevel> voltageLevelSet = new HashSet<>(voltageLevelFilter.getVoltageLevels());
        voltageLevels.forEach(voltageLevelSet::remove);
        return voltageLevelSet;
    }

    private static void traverseVoltageLevels(Set<VoltageLevel> voltageLevelsDepth, int depth, Set<VoltageLevel> visitedVoltageLevels) {
        if (depth < 0) {
            return;
        }
        Set<VoltageLevel> nextDepthVoltageLevels = new HashSet<>();
        for (VoltageLevel vl : voltageLevelsDepth) {
            if (!visitedVoltageLevels.contains(vl)) {
                visitedVoltageLevels.add(vl);
                vl.visitEquipments(new VlVisitor(nextDepthVoltageLevels, visitedVoltageLevels));
            }
        }
        traverseVoltageLevels(nextDepthVoltageLevels, depth - 1, visitedVoltageLevels);
    }

    private static void traverseVoltageLevelsWithVoltageFilter(Set<VoltageLevel> voltageLevelsDepth, int depth, Set<VoltageLevel> visitedVoltageLevels,
                                              double highNominalVoltageBound, double lowNominalVoltageBound) {
        if (depth >= 0) {
            Set<VoltageLevel> nextDepthVoltageLevels = new HashSet<>();
            for (VoltageLevel vl : voltageLevelsDepth) {
                if (!visitedVoltageLevels.contains(vl)) {
                    if (highNominalVoltageBound > 0 && lowNominalVoltageBound > 0) {
                        if (vl.getNominalV() >= lowNominalVoltageBound
                                && vl.getNominalV() <= highNominalVoltageBound) {
                            traverseVoltageLevel(visitedVoltageLevels, nextDepthVoltageLevels, vl);
                        }
                    } else if (highNominalVoltageBound > 0) {
                        if (vl.getNominalV() <= highNominalVoltageBound) {
                            traverseVoltageLevel(visitedVoltageLevels, nextDepthVoltageLevels, vl);
                        }
                    } else if (lowNominalVoltageBound > 0) {
                        if (vl.getNominalV() >= lowNominalVoltageBound) {
                            traverseVoltageLevel(visitedVoltageLevels, nextDepthVoltageLevels, vl);
                        }
                    } else {
                        traverseVoltageLevel(visitedVoltageLevels, nextDepthVoltageLevels, vl);
                    }
                }
            }
            traverseVoltageLevelsWithVoltageFilter(nextDepthVoltageLevels, depth - 1, visitedVoltageLevels, highNominalVoltageBound, lowNominalVoltageBound);
        }
    }

    private static void traverseVoltageLevel(Set<VoltageLevel> visitedVoltageLevels, Set<VoltageLevel> nextDepthVoltageLevels, VoltageLevel vl) {
        visitedVoltageLevels.add(vl);
        vl.visitEquipments(new VlVisitor(nextDepthVoltageLevels, visitedVoltageLevels));
    }

    public static VoltageLevelFilter createNominalVoltageFilter(Network network, List<String> voltageLevelIds,
                                                             double highNominalVoltageBound,
                                                             double lowNominalVoltageBound, int depth) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(voltageLevelIds);
        Set<VoltageLevel> startingSet = new HashSet<>();

        for (String voltageLevelId : voltageLevelIds) {
            VoltageLevel vl = network.getVoltageLevel(voltageLevelId);
            if (vl == null) {
                throw new PowsyblException(UNKNOWN_VOLTAGE_LEVEL + voltageLevelId + "'");
            }
            if (lowNominalVoltageBound > 0 && vl.getNominalV() < lowNominalVoltageBound ||
                    highNominalVoltageBound > 0 && vl.getNominalV() > highNominalVoltageBound) {
                throw new PowsyblException("vl '" + voltageLevelId +
                        "' has his nominal voltage out of the indicated thresholds");
            }
            startingSet.add(vl);
        }

        Set<VoltageLevel> voltageLevels = new HashSet<>();
        VoltageLevelFilter.traverseVoltageLevelsWithVoltageFilter(startingSet, depth, voltageLevels, highNominalVoltageBound, lowNominalVoltageBound);
        return new VoltageLevelFilter(voltageLevels);
    }

    private static class VlVisitor extends DefaultTopologyVisitor {
        private final Set<VoltageLevel> nextDepthVoltageLevels;
        private final Set<VoltageLevel> visitedVoltageLevels;

        public VlVisitor(Set<VoltageLevel> nextDepthVoltageLevels, Set<VoltageLevel> visitedVoltageLevels) {
            this.nextDepthVoltageLevels = nextDepthVoltageLevels;
            this.visitedVoltageLevels = visitedVoltageLevels;
        }

        @Override
        public void visitLine(Line line, Branch.Side side) {
            visitBranch(line, side);
        }

        @Override
        public void visitTwoWindingsTransformer(TwoWindingsTransformer twt, Branch.Side side) {
            visitBranch(twt, side);
        }

        @Override
        public void visitThreeWindingsTransformer(ThreeWindingsTransformer twt, ThreeWindingsTransformer.Side side) {
            if (side == ThreeWindingsTransformer.Side.ONE) {
                visitTerminal(twt.getTerminal(ThreeWindingsTransformer.Side.TWO));
                visitTerminal(twt.getTerminal(ThreeWindingsTransformer.Side.THREE));
            } else if (side == ThreeWindingsTransformer.Side.TWO) {
                visitTerminal(twt.getTerminal(ThreeWindingsTransformer.Side.ONE));
                visitTerminal(twt.getTerminal(ThreeWindingsTransformer.Side.THREE));
            } else {
                visitTerminal(twt.getTerminal(ThreeWindingsTransformer.Side.ONE));
                visitTerminal(twt.getTerminal(ThreeWindingsTransformer.Side.TWO));
            }
        }

        @Override
        public void visitHvdcConverterStation(HvdcConverterStation<?> converterStation) {
            converterStation.getOtherConverterStation().ifPresent(c -> visitTerminal(c.getTerminal()));
        }

        private void visitBranch(Branch<?> branch, Branch.Side side) {
            visitTerminal(branch.getTerminal(IidmUtils.getOpposite(side)));
        }

        private void visitTerminal(Terminal terminal) {
            VoltageLevel voltageLevel = terminal.getVoltageLevel();
            if (!visitedVoltageLevels.contains(voltageLevel)) {
                nextDepthVoltageLevels.add(voltageLevel);
            }
        }

        @Override
        public void visitDanglingLine(DanglingLine danglingLine) {
            if (danglingLine.isPaired()) {
                danglingLine.getTieLine().ifPresent(tieline -> visitBranch(tieline, tieline.getSide(danglingLine.getTerminal())));
            }
        }
    }

}
