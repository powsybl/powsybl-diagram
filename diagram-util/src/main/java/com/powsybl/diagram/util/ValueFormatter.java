/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.diagram.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ValueFormatter {

    /** degree sign unicode for degree symbol */
    private static final String DEGREE_CHAR = "\u00b0";
    private static final String PERCENT_CHAR = "\u0025";

    private final int powerValuePrecision;
    private final int voltageValuePrecision;
    private final int currentValuePrecision;
    private final int angleValuePrecision;
    private final int percentageValuePrecision;
    private final DecimalFormat format;
    private final String undefinedValueSymbol;

    public ValueFormatter(int powerValuePrecision, int voltageValuePrecision, int currentValuePrecision, int angleValuePrecision, int percentageValuePrecision, Locale locale, String undefinedValueSymbol) {
        this.powerValuePrecision = powerValuePrecision;
        this.voltageValuePrecision = voltageValuePrecision;
        this.currentValuePrecision = currentValuePrecision;
        this.angleValuePrecision = angleValuePrecision;
        this.percentageValuePrecision = percentageValuePrecision;
        this.format = new DecimalFormat();
        format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(locale));
        this.undefinedValueSymbol = undefinedValueSymbol;
    }

    public String formatVoltage(double voltage) {
        return formatVoltage(voltage, "");
    }

    public String formatVoltage(double voltage, String unit) {
        setFractionDigits(voltageValuePrecision);
        String valueFormatted = Double.isNaN(voltage) ? undefinedValueSymbol : format.format(voltage);
        return valueFormatted + " " + unit;
    }

    public String formatPower(double power) {
        return formatPower(power, "");
    }

    public String formatPower(double power, String unit) {
        setFractionDigits(powerValuePrecision);
        String valueFormatted = Double.isNaN(power) ? undefinedValueSymbol : format.format(power);
        return unit.isEmpty() ? valueFormatted : (valueFormatted + " " + unit);
    }

    public String formatAngleInDegrees(double angleInDegrees) {
        setFractionDigits(angleValuePrecision);
        String valueFormatted = Double.isNaN(angleInDegrees) ? undefinedValueSymbol : format.format(angleInDegrees);
        return valueFormatted + DEGREE_CHAR;
    }

    private void setFractionDigits(int precision) {
        format.setMaximumFractionDigits(precision);
        format.setMinimumFractionDigits(precision);
    }

    public String formatCurrent(double current, String unit) {
        setFractionDigits(currentValuePrecision);
        String valueFormatted = Double.isNaN(current) ? undefinedValueSymbol : format.format(current);
        return unit.isEmpty() ? valueFormatted : (valueFormatted + " " + unit);
    }

    public String formatCurrent(double current) {
        return formatCurrent(current, "");
    }

    public String formatPercentage(double percentage) {
        setFractionDigits(percentageValuePrecision);
        String valueFormatted = Double.isNaN(percentage) ? undefinedValueSymbol : format.format(percentage);
        return valueFormatted + " " + PERCENT_CHAR;
    }
}
