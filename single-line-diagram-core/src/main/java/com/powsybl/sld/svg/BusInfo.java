/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

/**
 * Class used to describe an information element which is displayed at busbar section.
 *
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class BusInfo {

    private final String componentType;

    private final String userDefinedId;

    public BusInfo(String componentType) {
        this(componentType, null);
    }

    public BusInfo(String componentType, String userDefinedId) {
        this.userDefinedId = userDefinedId;
        this.componentType = componentType;
    }

    public String getUserDefinedId() {
        return userDefinedId;
    }

    public String getComponentType() {
        return componentType;
    }
}
