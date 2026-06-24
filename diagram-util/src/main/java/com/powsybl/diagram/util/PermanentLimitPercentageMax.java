package com.powsybl.diagram.util;

import com.powsybl.iidm.network.*;

import java.util.stream.Stream;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class PermanentLimitPercentageMax {

    private PermanentLimitPercentageMax() {
        // Empty constructor
    }

    public static double getPermanentLimitPercentageMax(Branch<?> branch) {
        return Stream.of(TwoSides.ONE, TwoSides.TWO)
            .flatMap(side -> branch.getAllSelectedCurrentLimits(side).stream()
                .map(l -> getPermanentLimitPercentageMax(branch.getTerminal(side), l))
            )
            .mapToDouble(Double::doubleValue)
            .max().orElse(Double.NaN);
    }

    public static double getPermanentLimitPercentageMax(ThreeWindingsTransformer twt) {
        return Stream.of(ThreeSides.ONE, ThreeSides.TWO, ThreeSides.THREE)
            .flatMap(side -> twt.getLeg(side).getAllSelectedCurrentLimits().stream()
                .map(l -> getPermanentLimitPercentageMax(twt.getTerminal(side), l))
            )
            .mapToDouble(Double::doubleValue)
            .max().orElse(Double.NaN);
    }

    private static double getPermanentLimitPercentageMax(Terminal terminal, CurrentLimits currentLimits) {
        return currentLimits != null && currentLimits.getDetectionKind() == DetectionKind.HIGH ? Math.abs(terminal.getI() * 100) / currentLimits.getPermanentLimit() : Double.NaN;
    }
}
