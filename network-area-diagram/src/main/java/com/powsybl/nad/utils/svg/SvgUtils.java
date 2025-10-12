/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.utils.svg;

import java.text.DecimalFormat;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
public final class SvgUtils {

    private SvgUtils() {
    }

    public static String getArrowPathDIn(double arrowHeight) {
        String hf = formatHeight(arrowHeight);
        return String.format("M-%s -%s H%s L0 %sz", hf,  hf, hf, hf);
    }

    public static String getArrowPathDOut(double arrowHeight) {
        String hf = formatHeight(arrowHeight);
        return String.format("M-%s %s H%s L0 -%sz", hf,  hf, hf, hf);
    }

    private static String formatHeight(double arrowHeight) {
        DecimalFormat df = new DecimalFormat("0.##");
        return df.format(arrowHeight);
    }
}
