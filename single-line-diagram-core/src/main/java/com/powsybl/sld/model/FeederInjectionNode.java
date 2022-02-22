/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.sld.library.ComponentTypeName;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class FeederInjectionNode extends FeederNode {

    protected FeederInjectionNode(String id, String name, String componentType) {
        super(id, name, id, componentType, FeederType.INJECTION);
    }

    public static FeederInjectionNode createGenerator(String id, String name) {
        return new FeederInjectionNode(id, name, ComponentTypeName.GENERATOR);
    }

    public static FeederInjectionNode createLoad(String id, String name) {
        return new FeederInjectionNode(id, name, ComponentTypeName.LOAD);
    }

    public static FeederInjectionNode createVscConverterStation(String id, String name) {
        return new FeederInjectionNode(id, name, ComponentTypeName.VSC_CONVERTER_STATION);
    }

    public static FeederInjectionNode createStaticVarCompensator(String id, String name) {
        return new FeederInjectionNode(id, name, ComponentTypeName.STATIC_VAR_COMPENSATOR);
    }

    public static FeederInjectionNode createInductor(String id, String name) {
        return new FeederInjectionNode(id, name, ComponentTypeName.INDUCTOR);
    }

    public static FeederInjectionNode createCapacitor(String id, String name) {
        return new FeederInjectionNode(id, name, ComponentTypeName.CAPACITOR);
    }

    public static FeederInjectionNode createDanglingLine(String id, String name) {
        return new FeederInjectionNode(id, name, ComponentTypeName.DANGLING_LINE);
    }
}
