/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum FeederType {
    INJECTION,
    BRANCH,
    TWO_WINDINGS_TRANSFORMER_LEG,
    THREE_WINDINGS_TRANSFORMER_LEG,
    FICTITIOUS
}
