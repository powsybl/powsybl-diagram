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

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class VoltageLevelFilter implements Predicate<VoltageLevel> {

    public static final Predicate<VoltageLevel> NO_FILTER = voltageLevel -> true;

    private final Map<VoltageLevel, Boolean> voltageLevelsWithVisibility;

    public VoltageLevelFilter(Map<VoltageLevel, Boolean> voltageLevelsWithVisibility) {
        this.voltageLevelsWithVisibility = voltageLevelsWithVisibility;
    }

    @Override
    public boolean test(VoltageLevel voltageLevel) {
        return voltageLevelsWithVisibility.containsKey(voltageLevel);
    }

    public Map<VoltageLevel, Boolean> getVoltageLevelsWithVisibility() {
        return voltageLevelsWithVisibility;
    }

    public static VoltageLevelFilter createVoltageLevelDepthFilter(Network network, String voltageLevelId, int depth) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(voltageLevelId);

        VoltageLevel vl = network.getVoltageLevel(voltageLevelId);
        if (vl == null) {
            throw new PowsyblException("Unknown voltage level id '" + voltageLevelId + "'");
        }
        Map<VoltageLevel, Boolean> startingHashMap = new HashMap<>();
        startingHashMap.put(vl, true);

        Map<VoltageLevel, Boolean> voltageLevelsWithVisibility = new HashMap<>();

        traverseVoltageLevels(startingHashMap, depth, voltageLevelsWithVisibility);
        return new VoltageLevelFilter(voltageLevelsWithVisibility);
    }

    public static VoltageLevelFilter createVoltageLevelsDepthFilter(Network network, List<String> voltageLevelIds, int depth) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(voltageLevelIds);
        Map<VoltageLevel, Boolean> startingHashMap = new HashMap<>();
        for (String voltageLevelId : voltageLevelIds) {
            VoltageLevel vl = network.getVoltageLevel(voltageLevelId);
            if (vl == null) {
                throw new PowsyblException("Unknown voltage level id '" + voltageLevelId + "'");
            }
            startingHashMap.put(vl, true);
        }

        Map<VoltageLevel, Boolean> voltageLevelsWithVisibility = new HashMap<>();
        traverseVoltageLevels(startingHashMap, depth, voltageLevelsWithVisibility);
        return new VoltageLevelFilter(voltageLevelsWithVisibility);
    }

    public static VoltageLevelFilter createVoltageLevelsFilter(Network network, List<String> voltageLevelIds) {
        return createVoltageLevelsDepthFilter(network, voltageLevelIds, 0);
    }

    private static void traverseVoltageLevels(Map<VoltageLevel, Boolean> voltageLevelsDepth, int depth, Map<VoltageLevel, Boolean> visitedVoltageLevelsWithVisibility) {
        if (depth < -1) {
            return;
        }
        Map<VoltageLevel, Boolean> nextDepthVoltageLevelsWithVisibility = new HashMap<>();

        for (Map.Entry voltageLevelWithVisibilityDepth : voltageLevelsDepth.entrySet()) {
            if (!visitedVoltageLevelsWithVisibility.containsKey(voltageLevelWithVisibilityDepth.getKey())) {
                visitedVoltageLevelsWithVisibility.put((VoltageLevel) voltageLevelWithVisibilityDepth.getKey(), (Boolean) voltageLevelWithVisibilityDepth.getValue());
                ((VoltageLevel) voltageLevelWithVisibilityDepth.getKey()).visitEquipments(new VlVisitor(nextDepthVoltageLevelsWithVisibility, visitedVoltageLevelsWithVisibility, depth));
            }
        }
        traverseVoltageLevels(nextDepthVoltageLevelsWithVisibility, depth - 1, visitedVoltageLevelsWithVisibility);
    }

    private static class VlVisitor extends DefaultTopologyVisitor {
        private final Map<VoltageLevel, Boolean> nextDepthVoltageLevels;
        private final Map<VoltageLevel, Boolean> visitedVoltageLevels;
        private final int depth;

        public VlVisitor(Map<VoltageLevel, Boolean> nextDepthVoltageLevels, Map<VoltageLevel, Boolean> visitedVoltageLevels, int depth) {
            this.nextDepthVoltageLevels = nextDepthVoltageLevels;
            this.visitedVoltageLevels = visitedVoltageLevels;
            this.depth = depth;
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
            if (!visitedVoltageLevels.containsKey(voltageLevel)) {
                nextDepthVoltageLevels.put(voltageLevel, isVisibleVoltageLevel(depth));
            }
        }

        private Boolean isVisibleVoltageLevel(int depth) {
            if (depth >= 0) {
                return true;
            }
            return false;
        }
    }

}
