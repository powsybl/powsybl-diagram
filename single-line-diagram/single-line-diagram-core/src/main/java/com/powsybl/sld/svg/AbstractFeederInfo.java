/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import java.util.Objects;
import java.util.Optional;

/**
 * Class used to describe an information element which is displayed below feeders, which contains one or more of the following:
 * <ul>
 * <li>a string on its right</li>
 * <li>a string on its left</li>
 * </ul>
 * Each of these two element parts is optional
 *
 * @author Giovanni Ferrari {@literal <giovanni.ferrari at techrain.eu>}
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public abstract class AbstractFeederInfo implements FeederInfo {

    private final String componentType;
    private final String leftLabel;
    private final String rightLabel;
    private final String userDefinedId;

    AbstractFeederInfo(String componentType, String leftLabel, String rightLabel, String userDefinedId) {
        this.componentType = Objects.requireNonNull(componentType);
        this.leftLabel = leftLabel;
        this.rightLabel = rightLabel;
        this.userDefinedId = userDefinedId;
    }

    public String getUserDefinedId() {
        return userDefinedId;
    }

    public String getComponentType() {
        return componentType;
    }

    public Optional<String> getLeftLabel() {
        return Optional.ofNullable(leftLabel);
    }

    public Optional<String> getRightLabel() {
        return Optional.ofNullable(rightLabel);
    }
}
