package com.powsybl.sld.svg;

import java.util.Objects;
import java.util.function.DoubleFunction;

/**
 * Class used to specify a directional element
 * (see FeederInfo & AbstractFeederInfo)
 *
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class DirectionalFeederInfo extends AbstractFeederInfo {

    private final LabelProvider.LabelDirection arrowDirection;

    private final double value;

    public DirectionalFeederInfo(String componentType, LabelProvider.LabelDirection arrowDirection, String leftLabel, String rightLabel) {
        this(componentType, arrowDirection, leftLabel, rightLabel, null);
    }

    public DirectionalFeederInfo(String componentType, LabelProvider.LabelDirection arrowDirection, String leftLabel, String rightLabel, String userDefinedId) {
        super(componentType, leftLabel, rightLabel, userDefinedId);
        this.arrowDirection = Objects.requireNonNull(arrowDirection);
        this.value = Double.NaN;
    }

    public DirectionalFeederInfo(String componentType, double value, DoubleFunction<String> formatter) {
        this(componentType, value, formatter, null);
    }

    public DirectionalFeederInfo(String componentType, double value, DoubleFunction<String> formatter, String userDefinedId) {
        super(componentType, null, formatter.apply(value), userDefinedId);
        this.arrowDirection = Objects.requireNonNull(getArrowDirection(value));
        this.value = value;
    }

    private static LabelProvider.LabelDirection getArrowDirection(double value) {
        return value > 0 ? LabelProvider.LabelDirection.OUT : LabelProvider.LabelDirection.IN;
    }

    public LabelProvider.LabelDirection getDirection() {
        return arrowDirection;
    }

    public double getValue() {
        return value;
    }
}
