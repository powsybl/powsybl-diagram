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
        return getStreamMaxFiltered(Stream.of(TwoSides.ONE, TwoSides.TWO)
            .flatMap(side -> branch.getAllSelectedCurrentLimits(side).stream()
                .map(l -> getPermanentLimitPercentageMax(branch.getTerminal(side), l))
            ));
    }

    public static double getPermanentLimitPercentageMax(ThreeWindingsTransformer twt) {
        return getStreamMaxFiltered(Stream.of(ThreeSides.ONE, ThreeSides.TWO, ThreeSides.THREE)
            .flatMap(side -> twt.getLeg(side).getAllSelectedCurrentLimits().stream()
                .map(l -> getPermanentLimitPercentageMax(twt.getTerminal(side), l))
            ));
    }

    private static double getStreamMaxFiltered(Stream<Double> values) {
        return values.mapToDouble(Double::doubleValue).filter(v -> !Double.isNaN(v)).max().orElse(Double.NaN);
    }

    private static double getPermanentLimitPercentageMax(Terminal terminal, CurrentLimits currentLimits) {
        return currentLimits != null && currentLimits.getDetectionKind() == DetectionKind.HIGH ? Math.abs(terminal.getI() * 100) / currentLimits.getPermanentLimit() : Double.NaN;
    }
}
