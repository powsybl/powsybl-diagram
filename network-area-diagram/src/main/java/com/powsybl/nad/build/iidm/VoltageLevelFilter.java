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

    private static void traverseVoltageLevels(Set<VoltageLevel> voltageLevelsDepth, int depth, Set<VoltageLevel> visitedVoltageLevels, Predicate<VoltageLevel> predicate) {
        if (depth < 0) {
            return;
        }
        Set<VoltageLevel> nextDepthVoltageLevels = new HashSet<>();
        for (VoltageLevel vl : voltageLevelsDepth) {
            if (!visitedVoltageLevels.contains(vl) && predicate.test(vl)) {
                visitedVoltageLevels.add(vl);
                vl.visitEquipments(new VlVisitor(nextDepthVoltageLevels, visitedVoltageLevels));
            }
        }
        traverseVoltageLevels(nextDepthVoltageLevels, depth - 1, visitedVoltageLevels, predicate);
    }

    private static void traverseVoltageLevels(Set<VoltageLevel> voltageLevelsDepth, int depth, Set<VoltageLevel> visitedVoltageLevels) {
        traverseVoltageLevels(voltageLevelsDepth, depth, visitedVoltageLevels, NO_FILTER);
    }

    private static void traverseVoltageLevelsWithVoltageFilter(Set<VoltageLevel> voltageLevelsDepth, int depth, Set<VoltageLevel> visitedVoltageLevels,
                                                               double nominalVoltageLowerBound, double nominalVoltageUpperBound) {

        Predicate<VoltageLevel> voltageFilterPredicate = voltageLevel -> voltageLevel.getNominalV() >= nominalVoltageLowerBound && voltageLevel.getNominalV() <= nominalVoltageUpperBound;
        traverseVoltageLevels(voltageLevelsDepth, depth, visitedVoltageLevels, voltageFilterPredicate);
    }

    public static VoltageLevelFilter createNominalVoltageFilter(Network network, List<String> voltageLevelIds,
                                                                double nominalVoltageLowerBound, double nominalVoltageUpperBound,
                                                                int depth) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(voltageLevelIds);
        checkVoltageBoundValues(nominalVoltageLowerBound, nominalVoltageUpperBound);
        Set<VoltageLevel> startingSet = new HashSet<>();

        for (String voltageLevelId : voltageLevelIds) {
            VoltageLevel vl = network.getVoltageLevel(voltageLevelId);
            if (vl == null) {
                throw new PowsyblException(UNKNOWN_VOLTAGE_LEVEL + voltageLevelId + "'");
            }
            if (vl.getNominalV() < nominalVoltageLowerBound || vl.getNominalV() > nominalVoltageUpperBound) {
                LOGGER.warn("vl '{}' has his nominal voltage out of the indicated thresholds", voltageLevelId);
            } else {
                startingSet.add(vl);
            }
        }

        Set<VoltageLevel> voltageLevels = new HashSet<>();
        VoltageLevelFilter.traverseVoltageLevelsWithVoltageFilter(startingSet, depth, voltageLevels, nominalVoltageLowerBound, nominalVoltageUpperBound);
        return new VoltageLevelFilter(voltageLevels);
    }

    private static void checkVoltageBoundValues(double nominalVoltageLowerBound, double nominalVoltageUpperBound) {
        if (nominalVoltageLowerBound < 0 || nominalVoltageUpperBound < 0) {
            throw new PowsyblException("Voltage bounds must be positive");
        }
        if (nominalVoltageLowerBound > nominalVoltageUpperBound) {
            throw new PowsyblException("Low bound must be less than or equal to high bound");
        }
    }

    public static VoltageLevelFilter createNominalVoltageLowerBoundFilter(Network network, List<String> voltageLevelIds, double nominalVoltageLowerBound, int depth) {
        return createNominalVoltageFilter(network, voltageLevelIds, nominalVoltageLowerBound, Double.MAX_VALUE, depth);
    }

    public static VoltageLevelFilter createNominalVoltageUpperBoundFilter(Network network, List<String> voltageLevelIds, double nominalVoltageUpperBound, int depth) {
        return createNominalVoltageFilter(network, voltageLevelIds, 0, nominalVoltageUpperBound, depth);
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
