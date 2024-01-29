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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class VoltageLevelFilter implements Predicate<VoltageLevel> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(VoltageLevelFilter.class);

    public static final Predicate<VoltageLevel> NO_FILTER = voltageLevel -> true;

    private static final String UNKNOWN_VOLTAGE_LEVEL = "Unknown voltage level id '";

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

    @Override
    public boolean test(VoltageLevel voltageLevel) {
        return voltageLevels.contains(voltageLevel);
    }

    public static VoltageLevelFilter createVoltageLevelsFilter(Network network, List<String> voltageLevelIds) {
        return createVoltageLevelsDepthFilter(network, voltageLevelIds, 0);
    }

    public static VoltageLevelFilter createVoltageLevelDepthFilter(Network network, String voltageLevelId, int depth) {
        return createVoltageLevelsDepthFilter(network, List.of(voltageLevelId), depth);
    }

    public static VoltageLevelFilter createVoltageLevelsDepthFilter(Network network, List<String> voltageLevelIds, int depth) {
        return createVoltageLevelFilterWithPredicate(network, voltageLevelIds, depth, NO_FILTER);
    }

    public static VoltageLevelFilter createNominalVoltageLowerBoundFilter(Network network, List<String> voltageLevelIds, double nominalVoltageLowerBound, int depth) {
        return createNominalVoltageFilter(network, voltageLevelIds, nominalVoltageLowerBound, Double.MAX_VALUE, depth);
    }

    public static VoltageLevelFilter createNominalVoltageLowerBoundFilter(Network network, double nominalVoltageLowerBound) {
        return createNominalVoltageFilter(network, nominalVoltageLowerBound, Double.MAX_VALUE);
    }

    public static VoltageLevelFilter createNominalVoltageUpperBoundFilter(Network network, List<String> voltageLevelIds, double nominalVoltageUpperBound, int depth) {
        return createNominalVoltageFilter(network, voltageLevelIds, 0, nominalVoltageUpperBound, depth);
    }

    public static VoltageLevelFilter createNominalVoltageUpperBoundFilter(Network network, double nominalVoltageUpperBound) {
        return createNominalVoltageFilter(network, 0, nominalVoltageUpperBound);
    }

    public static VoltageLevelFilter createNominalVoltageFilter(Network network, List<String> voltageLevelIds,
                                                                double nominalVoltageLowerBound, double nominalVoltageUpperBound,
                                                                int depth) {
        return createVoltageLevelFilterWithPredicate(network, voltageLevelIds, depth, getPredicateFromNominalVoltageBounds(nominalVoltageLowerBound, nominalVoltageUpperBound));
    }

    public static VoltageLevelFilter createNominalVoltageFilter(Network network, double nominalVoltageLowerBound, double nominalVoltageUpperBound) {
        return createNominalVoltageFilterWithPredicate(network, getPredicateFromNominalVoltageBounds(nominalVoltageLowerBound, nominalVoltageUpperBound));
    }

    private static Predicate<VoltageLevel> getPredicateFromNominalVoltageBounds(double nominalVoltageLowerBound, double nominalVoltageUpperBound) {
        checkVoltageBoundValues(nominalVoltageLowerBound, nominalVoltageUpperBound);
        return voltageLevel -> voltageLevel.getNominalV() >= nominalVoltageLowerBound && voltageLevel.getNominalV() <= nominalVoltageUpperBound;
    }

    public static VoltageLevelFilter createVoltageLevelFilterWithPredicate(Network network, List<String> voltageLevelIds, int depth, Predicate<VoltageLevel> voltageLevelPredicate) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(voltageLevelIds);
        Set<VoltageLevel> startingSet = new HashSet<>();

        for (String voltageLevelId : voltageLevelIds) {
            VoltageLevel vl = network.getVoltageLevel(voltageLevelId);
            if (vl == null) {
                throw new PowsyblException(UNKNOWN_VOLTAGE_LEVEL + voltageLevelId + "'");
            }
            if (!voltageLevelPredicate.test(vl)) {
                LOGGER.warn("vl '{}' does not comply with the predicate", voltageLevelId);
            }
            startingSet.add(vl);
        }
        Set<VoltageLevel> voltageLevels = new HashSet<>();
        VoltageLevelFilter.traverseVoltageLevels(startingSet, depth, voltageLevels, voltageLevelPredicate);
        return new VoltageLevelFilter(voltageLevels);
    }

    public static VoltageLevelFilter createNominalVoltageFilterWithPredicate(Network network, Predicate<VoltageLevel> voltageLevelPredicate) {
        return new VoltageLevelFilter(network.getVoltageLevelStream().filter(voltageLevelPredicate).collect(Collectors.toSet()));
    }

    public static Collection<VoltageLevel> getNextDepthVoltageLevels(Network network, List<VoltageLevel> voltageLevels) {
        List<String> voltageLevelIds = voltageLevels.stream().map(VoltageLevel::getId).collect(Collectors.toList());
        VoltageLevelFilter voltageLevelFilter = createVoltageLevelsDepthFilter(network, voltageLevelIds, 1);
        Set<VoltageLevel> voltageLevelSet = new HashSet<>(voltageLevelFilter.getVoltageLevels());
        voltageLevels.forEach(voltageLevelSet::remove);
        return voltageLevelSet;
    }

    private static void traverseVoltageLevels(Set<VoltageLevel> voltageLevelsDepth, int depth, Set<VoltageLevel> visitedVoltageLevels, Predicate<VoltageLevel> predicate) {
        if (depth < 0) {
            return;
        }
        Set<VoltageLevel> nextDepthVoltageLevels = new HashSet<>();
        for (VoltageLevel vl : voltageLevelsDepth) {
            if (!visitedVoltageLevels.contains(vl)) {
                visitedVoltageLevels.add(vl);
                vl.visitEquipments(new VlVisitor(nextDepthVoltageLevels, visitedVoltageLevels, predicate));
            }
        }
        traverseVoltageLevels(nextDepthVoltageLevels, depth - 1, visitedVoltageLevels, predicate);
    }

    private static void checkVoltageBoundValues(double nominalVoltageLowerBound, double nominalVoltageUpperBound) {
        if (nominalVoltageLowerBound < 0 || nominalVoltageUpperBound < 0) {
            throw new PowsyblException("Voltage bounds must be positive");
        }
        if (nominalVoltageLowerBound > nominalVoltageUpperBound) {
            throw new PowsyblException("Low bound must be less than or equal to high bound");
        }
    }

    private static class VlVisitor extends DefaultTopologyVisitor {
        private final Set<VoltageLevel> nextDepthVoltageLevels;
        private final Set<VoltageLevel> visitedVoltageLevels;
        private final Predicate<VoltageLevel> voltageLevelPredicate;

        public VlVisitor(Set<VoltageLevel> nextDepthVoltageLevels, Set<VoltageLevel> visitedVoltageLevels, Predicate<VoltageLevel> voltageLevelPredicate) {
            this.nextDepthVoltageLevels = nextDepthVoltageLevels;
            this.visitedVoltageLevels = visitedVoltageLevels;
            this.voltageLevelPredicate = voltageLevelPredicate;
        }

        @Override
        public void visitLine(Line line, TwoSides side) {
            visitBranch(line, side);
        }

        @Override
        public void visitTwoWindingsTransformer(TwoWindingsTransformer twt, TwoSides side) {
            visitBranch(twt, side);
        }

        @Override
        public void visitThreeWindingsTransformer(ThreeWindingsTransformer twt, ThreeSides side) {
            if (side == ThreeSides.ONE) {
                visitTerminal(twt.getTerminal(ThreeSides.TWO));
                visitTerminal(twt.getTerminal(ThreeSides.THREE));
            } else if (side == ThreeSides.TWO) {
                visitTerminal(twt.getTerminal(ThreeSides.ONE));
                visitTerminal(twt.getTerminal(ThreeSides.THREE));
            } else {
                visitTerminal(twt.getTerminal(ThreeSides.ONE));
                visitTerminal(twt.getTerminal(ThreeSides.TWO));
            }
        }

        @Override
        public void visitHvdcConverterStation(HvdcConverterStation<?> converterStation) {
            converterStation.getOtherConverterStation().ifPresent(c -> visitTerminal(c.getTerminal()));
        }

        private void visitBranch(Branch<?> branch, TwoSides side) {
            visitTerminal(branch.getTerminal(IidmUtils.getOpposite(side)));
        }

        private void visitTerminal(Terminal terminal) {
            VoltageLevel voltageLevel = terminal.getVoltageLevel();
            if (!visitedVoltageLevels.contains(voltageLevel) && voltageLevelPredicate.test(voltageLevel)) {
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
