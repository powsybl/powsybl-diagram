/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.model.coordinate.Side;

import java.util.Optional;

/**
 * Class used to describe an information element which is displayed at busbar section, which contains one or more of the following:
 * <ul>
 * <li>a string on its top</li>
 * <li>a string on its bottom</li>
 * <li>a side to anchor indicator</li>
 * <li>an state power</li>
 * </ul>
 * Each of these two labels part is optional
 *
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class BusInfo {

    private final String componentType;

    private final String userDefinedId;

    private final String topLabel;

    private final String bottomLabel;

    private final Side anchor;

    public BusInfo(String componentType) {
        this(componentType, null, null);
    }

    public BusInfo(String componentType, String topLabel, String bottomLabel) {
        this(componentType, topLabel, bottomLabel, Side.LEFT, null);
    }

    public BusInfo(String componentType, String topLabel, String bottomLabel, Side anchor, String userDefinedId) {
        this.componentType = componentType;
        this.topLabel = topLabel;
        this.bottomLabel = bottomLabel;
        this.anchor = anchor;
        this.userDefinedId = userDefinedId;
    }

    public String getComponentType() {
        return componentType;
    }

    public String getUserDefinedId() {
        return userDefinedId;
    }

    public Optional<String> getTopLabel() {
        return Optional.ofNullable(topLabel);
    }

    public Optional<String> getBottomLabel() {
        return Optional.ofNullable(bottomLabel);
    }

    public Side getAnchor() {
        return anchor;
    }
}
