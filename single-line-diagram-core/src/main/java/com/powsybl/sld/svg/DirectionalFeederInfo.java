package com.powsybl.sld.svg;

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
        this(componentType, value, null);
    }

    public DirectionalFeederInfo(String componentType, double value, String userDefinedId) {
        this(componentType, value > 0 ? DiagramLabelProvider.LabelDirection.OUT : DiagramLabelProvider.LabelDirection.IN, null, String.valueOf(Math.round(value)), userDefinedId);
    }

    public DiagramLabelProvider.LabelDirection getDirection() {
        return arrowDirection;
    }
}
