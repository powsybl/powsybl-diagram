package com.powsybl.nad.utils.iidm;

import com.powsybl.iidm.network.*;
import com.powsybl.nad.model.BranchEdge;
import com.powsybl.nad.model.BusNode;
import com.powsybl.nad.model.ThreeWtEdge;

import java.util.Objects;

public final class IidmUtils {

    private IidmUtils() {
    }

    public static boolean isDisconnected(Network network, BranchEdge edge, BranchEdge.Side side) {
        if (edge.getType().equals(BranchEdge.DANGLING_LINE_EDGE) && side.equals(BranchEdge.Side.TWO)) {
            return false;
        }
        Terminal terminal = IidmUtils.getTerminalFromEdge(network, edge, side);
        return terminal == null || !terminal.isConnected();
    }

    public static Terminal getTerminalFromEdge(Network network, BranchEdge edge, BranchEdge.Side side) {
        if (edge.getType().equals(BranchEdge.HVDC_LINE_EDGE)) {
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

    public static ThreeWindingsTransformer.Leg get3wtLeg(ThreeWindingsTransformer twt, ThreeWindingsTransformer.Side side) {
        if (side == ThreeWindingsTransformer.Side.ONE) {
            return twt.getLeg1();
        } else if (side == ThreeWindingsTransformer.Side.TWO) {
            return twt.getLeg2();
        } else {
            return twt.getLeg3();
        }
    }

    public static Branch.Side getOpposite(Branch.Side side) {
        return side == Branch.Side.ONE ? Branch.Side.TWO : Branch.Side.ONE;
    }

    public static Branch.Side getIidmSideFromBranchEdgeSide(BranchEdge.Side side) {
        return Objects.requireNonNull(side) == BranchEdge.Side.ONE ? Branch.Side.ONE : Branch.Side.TWO;
    }

    public static HvdcLine.Side getIidmHvdcSideFromBranchEdgeSide(BranchEdge.Side side) {
        return Objects.requireNonNull(side) == BranchEdge.Side.ONE ? HvdcLine.Side.ONE : HvdcLine.Side.TWO;
    }

    public static ThreeWindingsTransformer.Side getIidmSideFromThreeWtEdgeSide(ThreeWtEdge.Side side) {
        switch (Objects.requireNonNull(side)) {
            case ONE:
                return ThreeWindingsTransformer.Side.ONE;
            case TWO:
                return ThreeWindingsTransformer.Side.TWO;
            case THREE:
                return ThreeWindingsTransformer.Side.THREE;
        }
        return null;
    }

    public static ThreeWtEdge.Side getThreeWtEdgeSideFromIidmSide(ThreeWindingsTransformer.Side side) {
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

    public static boolean isIidmBranch(BranchEdge edge) {
        boolean special = edge.getType().equals(BranchEdge.HVDC_LINE_EDGE) || edge.getType().equals(BranchEdge.DANGLING_LINE_EDGE);
        return !special;
    }

    public static boolean isBoundary(BusNode busNode) {
        return busNode.getDiagramId().contains("boundary") || busNode.getEquipmentId().contains("boundary");
    }
}
