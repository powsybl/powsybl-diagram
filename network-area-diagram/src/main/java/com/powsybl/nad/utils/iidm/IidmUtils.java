/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.utils.iidm;

import com.powsybl.iidm.network.*;
import com.powsybl.nad.model.BranchEdge;
import com.powsybl.nad.model.Edge;
import com.powsybl.nad.model.ThreeWtEdge;

import java.util.Objects;

/**
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */

public final class IidmUtils {

    private IidmUtils() {
    }

    public static boolean isDisconnected(Network network, BranchEdge edge, BranchEdge.Side side) {
        if (edge.getType().equals(BranchEdge.DANGLING_LINE_EDGE) && side.equals(BranchEdge.Side.TWO)) {
            return isDisconnected(network, edge, BranchEdge.Side.ONE);
        }
        Terminal terminal = IidmUtils.getTerminalFromEdge(network, edge, side);
        return terminal == null || !terminal.isConnected();
    }

    public static Terminal getTerminalFromEdge(Network network, BranchEdge edge, BranchEdge.Side side) {
        if (edge.getType().equals(BranchEdge.HVDC_LINE_LCC_EDGE) || edge.getType().equals(BranchEdge.HVDC_LINE_VSC_EDGE)) {
            HvdcLine line = network.getHvdcLine(edge.getEquipmentId());
            return line.getConverterStation(IidmUtils.getIidmHvdcSideFromBranchEdgeSide(side)).getTerminal();
        } else if (edge.getType().equals(BranchEdge.DANGLING_LINE_EDGE)) {
            if (side.equals(BranchEdge.Side.ONE)) {
                return network.getDanglingLine(edge.getEquipmentId()).getTerminal();
            }
            return null;
        } else {
            Branch<?> branch = network.getBranch(edge.getEquipmentId());
            return branch.getTerminal(IidmUtils.getIidmSideFromBranchEdgeSide(side));
        }
    }

    public static ThreeWindingsTransformer.Leg get3wtLeg(ThreeWindingsTransformer twt, ThreeSides side) {
        if (side == ThreeSides.ONE) {
            return twt.getLeg1();
        } else if (side == ThreeSides.TWO) {
            return twt.getLeg2();
        } else {
            return twt.getLeg3();
        }
    }

    public static TwoSides getOpposite(TwoSides side) {
        return side == TwoSides.ONE ? TwoSides.TWO : TwoSides.ONE;
    }

    public static TwoSides getIidmSideFromBranchEdgeSide(BranchEdge.Side side) {
        return Objects.requireNonNull(side) == BranchEdge.Side.ONE ? TwoSides.ONE : TwoSides.TWO;
    }

    public static TwoSides getIidmHvdcSideFromBranchEdgeSide(BranchEdge.Side side) {
        return Objects.requireNonNull(side) == BranchEdge.Side.ONE ? TwoSides.ONE : TwoSides.TWO;
    }

    public static ThreeSides getIidmSideFromThreeWtEdgeSide(ThreeWtEdge.Side side) {
        switch (Objects.requireNonNull(side)) {
            case ONE:
                return ThreeSides.ONE;
            case TWO:
                return ThreeSides.TWO;
            case THREE:
                return ThreeSides.THREE;
        }
        return null;
    }

    public static ThreeWtEdge.Side getThreeWtEdgeSideFromIidmSide(ThreeSides side) {
        switch (Objects.requireNonNull(side)) {
            case ONE:
                return ThreeWtEdge.Side.ONE;
            case TWO:
                return ThreeWtEdge.Side.TWO;
            case THREE:
                return ThreeWtEdge.Side.THREE;
        }
        return null;
    }

    public static boolean isIidmBranch(Edge edge) {
        if (edge instanceof BranchEdge) {
            String edgeType = edge.getType();
            return !edgeType.equals(BranchEdge.HVDC_LINE_LCC_EDGE) &&
                    !edgeType.equals(BranchEdge.HVDC_LINE_VSC_EDGE) &&
                    !edgeType.equals(BranchEdge.DANGLING_LINE_EDGE);
        }
        return false;
    }
}
