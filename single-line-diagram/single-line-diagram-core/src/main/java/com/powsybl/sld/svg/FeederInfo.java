/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import java.util.Optional;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public interface FeederInfo {

    String getUserDefinedId();

    String getComponentType();

    Optional<String> getLeftLabel();

    Optional<String> getRightLabel();
}
