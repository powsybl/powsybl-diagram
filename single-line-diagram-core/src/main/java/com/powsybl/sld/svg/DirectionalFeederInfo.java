package com.powsybl.sld.svg;

import org.apache.commons.math3.util.Precision;

import java.util.Objects;

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

    public DirectionalFeederInfo(String componentType, double value) {
        this(componentType, value, 0, null);
    }

    public DirectionalFeederInfo(String componentType, double value, int precision) {
        this(componentType, value, precision, null);
    }

    public DirectionalFeederInfo(String componentType, double value, int precision, String userDefinedId) {
        this(componentType,
                value > 0 ? DiagramLabelProvider.LabelDirection.OUT : DiagramLabelProvider.LabelDirection.IN,
                null,
                precision == 0 || Double.isNaN(value) ? String.valueOf(Math.round(value)) : String.valueOf(Precision.round(value, precision)),
                userDefinedId);
    }

    public DiagramLabelProvider.LabelDirection getDirection() {
        return arrowDirection;
    }
}
