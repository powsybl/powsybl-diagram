package com.powsybl.diagram.util;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class ValueFormatterTest {

    @Test
    void test() {
        ValueFormatter valueFormatter = new ValueFormatter(1, 2, 3, 4, 5, Locale.US, "N/A");

        assertEquals("0.1", valueFormatter.formatPower(0.123456789));
        assertEquals("0.1 W", valueFormatter.formatPower(0.123456789, "W"));
        assertEquals("0.12", valueFormatter.formatVoltage(0.123456789));
        assertEquals("0.12 V", valueFormatter.formatVoltage(0.123456789, "V"));
        assertEquals("0.123", valueFormatter.formatCurrent(0.123456789));
        assertEquals("0.123 A", valueFormatter.formatCurrent(0.123456789, "A"));
        assertEquals("0.1235°", valueFormatter.formatAngleInDegrees(0.123456789));
        assertEquals("0.12346 %", valueFormatter.formatPercentage(0.123456789));
    }
}
