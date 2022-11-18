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
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class ValueFormatter {
    /** em dash unicode for undefined value */
    public static final String DASH_CHAR = "\u2014";

    /** degree sign unicode for degree symbol */
    private static final String DEGREE_CHAR = "\u00b0";

    private final int powerValuePrecision;
    private final int voltageValuePrecision;
    private final int angleValuePrecision;
    private final DecimalFormat format;

    public ValueFormatter(int powerValuePrecision, int voltageValuePrecision, int angleValuePrecision, Locale locale) {
        this.powerValuePrecision = powerValuePrecision;
        this.voltageValuePrecision = voltageValuePrecision;
        this.angleValuePrecision = angleValuePrecision;
        this.format = new DecimalFormat();
        format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(locale));
    }

    public String formatVoltage(double voltage) {
        return formatVoltage(voltage, "");
    }

    public String formatVoltage(double voltage, String unit) {
        setFractionDigits(voltageValuePrecision);
        String valueFormatted = Double.isNaN(voltage) ? DASH_CHAR : format.format(voltage);
        return valueFormatted + " " + unit;
    }

    public String formatPower(double power) {
        return formatPower(power, "");
    }

    public String formatPower(double power, String unit) {
        setFractionDigits(powerValuePrecision);
        String valueFormatted = Double.isNaN(power) ? DASH_CHAR : format.format(power);
        return unit.isEmpty() ? valueFormatted : (valueFormatted + " " + unit);
    }

    public String formatAngleInDegrees(double angleInDegrees) {
        setFractionDigits(angleValuePrecision);
        String valueFormatted = Double.isNaN(angleInDegrees) ? DASH_CHAR : format.format(angleInDegrees);
        return valueFormatted + DEGREE_CHAR;
    }

    private void setFractionDigits(int precision) {
        format.setMaximumFractionDigits(precision);
        format.setMinimumFractionDigits(precision);
    }
}