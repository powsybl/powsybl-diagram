package com.powsybl.sld.svg;

import org.apache.commons.math3.util.Precision;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
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
                format(value, precision),
                userDefinedId);
    }

    private static String format(double value, int precision) {
        // build pattern
        String pattern = precision == 0 ? "#;#" : "#.0" + "#".repeat(precision - 1);
        String formattedValue = new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(Locale.ENGLISH)).format(value);
        return formattedValue.replaceAll("^-(?=0(\\.0*)?$)", ""); // Avoid negative zero
    }

    public DiagramLabelProvider.LabelDirection getDirection() {
        return arrowDirection;
    }
}
