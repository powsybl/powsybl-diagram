/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import com.powsybl.diagram.components.ComponentTypeName;

import java.util.Set;

/**
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public final class SldComponentTypeName extends ComponentTypeName {
    public static final String ARROW_ACTIVE = "ARROW_ACTIVE";
    public static final String ARROW_REACTIVE = "ARROW_REACTIVE";
    public static final String ARROW_CURRENT = "ARROW_CURRENT";
    public static final String BUSBAR_SECTION = "BUSBAR_SECTION";
    public static final String BREAKER = "BREAKER";
    public static final String DISCONNECTOR = "DISCONNECTOR";
    public static final String GROUND = "GROUND";
    public static final String GROUND_DISCONNECTION = "GROUND_DISCONNECTION";
    public static final String BUS_CONNECTION = "BUS_CONNECTION";
    public static final String GENERATOR = "GENERATOR";
    public static final String LINE = "LINE";
    public static final String LOAD_BREAK_SWITCH = "LOAD_BREAK_SWITCH";
    public static final String NODE = "NODE";
    public static final String TWO_WINDINGS_TRANSFORMER = "TWO_WINDINGS_TRANSFORMER";
    public static final String TWO_WINDINGS_TRANSFORMER_LEG = "TWO_WINDINGS_TRANSFORMER_LEG";
    public static final String THREE_WINDINGS_TRANSFORMER = "THREE_WINDINGS_TRANSFORMER";
    public static final String THREE_WINDINGS_TRANSFORMER_LEG = "THREE_WINDINGS_TRANSFORMER_LEG";
    public static final String THREE_WINDINGS_TRANSFORMER_PST_1_2_3 = "THREE_WINDINGS_TRANSFORMER_PST_1_2_3";
    public static final String THREE_WINDINGS_TRANSFORMER_PST_1_2 = "THREE_WINDINGS_TRANSFORMER_PST_1_2";
    public static final String THREE_WINDINGS_TRANSFORMER_PST_1_3 = "THREE_WINDINGS_TRANSFORMER_PST_1_3";
    public static final String THREE_WINDINGS_TRANSFORMER_PST_2_3 = "THREE_WINDINGS_TRANSFORMER_PST_2_3";
    public static final String THREE_WINDINGS_TRANSFORMER_PST_1 = "THREE_WINDINGS_TRANSFORMER_PST_1";
    public static final String THREE_WINDINGS_TRANSFORMER_PST_2 = "THREE_WINDINGS_TRANSFORMER_PST_2";
    public static final String THREE_WINDINGS_TRANSFORMER_PST_3 = "THREE_WINDINGS_TRANSFORMER_PST_3";
    public static final Set<String> THREE_WINDINGS_TRANSFORMER_COMPONENTS = Set.of(THREE_WINDINGS_TRANSFORMER,
            THREE_WINDINGS_TRANSFORMER_PST_1_2_3, THREE_WINDINGS_TRANSFORMER_PST_1_2,
            THREE_WINDINGS_TRANSFORMER_PST_1_3, THREE_WINDINGS_TRANSFORMER_PST_2_3, THREE_WINDINGS_TRANSFORMER_PST_1,
            THREE_WINDINGS_TRANSFORMER_PST_2, THREE_WINDINGS_TRANSFORMER_PST_3);
    public static final String VSC_CONVERTER_STATION = "VSC_CONVERTER_STATION";
    public static final String LCC_CONVERTER_STATION = "LCC_CONVERTER_STATION";
    public static final String DANGLING_LINE = "DANGLING_LINE";
    public static final String TIE_LINE = "TIE_LINE";
    public static final String PHASE_SHIFT_TRANSFORMER = "PHASE_SHIFT_TRANSFORMER";
    public static final String PHASE_SHIFT_TRANSFORMER_LEG = "PHASE_SHIFT_TRANSFORMER_LEG";
}
