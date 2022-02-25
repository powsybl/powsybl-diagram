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
 * <li>a string on its right</li>
 * <li>a string on its left</li>
 * </ul>
 * Each of these two element part is optional
 *
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class BusInfo {

    private final String componentType;

    private final String userDefinedId;

    private final String leftLabel;

    private final String rightLabel;

    private final Side anchor;

    public BusInfo(String componentType, String leftLabel, String rightLabel) {
        this(componentType, leftLabel, rightLabel, Side.LEFT, null);
    }

    public BusInfo(String componentType, String leftLabel, String rightLabel, Side anchor, String userDefinedId) {
        this.componentType = componentType;
        this.leftLabel = leftLabel;
        this.rightLabel = rightLabel;
        this.anchor = anchor;
        this.userDefinedId = userDefinedId;
    }

    public String getComponentType() {
        return componentType;
    }

    public String getUserDefinedId() {
        return userDefinedId;
    }

    public Optional<String> getLeftLabel() {
        return Optional.ofNullable(leftLabel);
    }

    public Optional<String> getRightLabel() {
        return Optional.ofNullable(rightLabel);
    }

    public Side getAnchor() {
        return anchor;
    }
}
