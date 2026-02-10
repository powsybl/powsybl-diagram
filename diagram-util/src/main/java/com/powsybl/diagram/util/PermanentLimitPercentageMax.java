package com.powsybl.diagram.util;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoSides;

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
            .map(side -> getPermanentLimitPercentageMax(branch.getTerminal(side), branch.getCurrentLimits(side).orElse(null)))
            .mapToDouble(Double::doubleValue)
            .max().getAsDouble();
    }

    public static double getPermanentLimitPercentageMax(ThreeWindingsTransformer twt) {
        return Stream.of(ThreeSides.ONE, ThreeSides.TWO, ThreeSides.THREE)
            .map(side -> getPermanentLimitPercentageMax(twt.getTerminal(side), twt.getLeg(side).getCurrentLimits().orElse(null)))
            .mapToDouble(Double::doubleValue)
            .max().getAsDouble();
    }

    private static double getPermanentLimitPercentageMax(Terminal terminal, CurrentLimits currentLimits) {
        return currentLimits == null ? Double.NaN : Math.abs(terminal.getI() * 100) / currentLimits.getPermanentLimit();
    }
}
