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

    private final DiagramLabelProvider.LabelDirection arrowDirection;

    public DirectionalFeederInfo(String componentType, DiagramLabelProvider.LabelDirection arrowDirection, String leftLabel, String rightLabel) {
        this(componentType, arrowDirection, leftLabel, rightLabel, null);
    }

    public DirectionalFeederInfo(String componentType, DiagramLabelProvider.LabelDirection arrowDirection, String leftLabel, String rightLabel, String userDefinedId) {
        super(componentType, leftLabel, rightLabel, userDefinedId);
        this.arrowDirection = Objects.requireNonNull(arrowDirection);
    }

    public DirectionalFeederInfo(String componentType, double value, DoubleFunction<String> formatter) {
        this(componentType, value, formatter, null);
    }

    public DirectionalFeederInfo(String componentType, double value, DoubleFunction<String> formatter, String userDefinedId) {
        this(componentType, getArrowDirection(value), null, formatter.apply(value), userDefinedId);
    }

    private static DiagramLabelProvider.LabelDirection getArrowDirection(double value) {
        return value > 0 ? DiagramLabelProvider.LabelDirection.OUT : DiagramLabelProvider.LabelDirection.IN;
    }

    public DiagramLabelProvider.LabelDirection getDirection() {
        return arrowDirection;
    }
}
