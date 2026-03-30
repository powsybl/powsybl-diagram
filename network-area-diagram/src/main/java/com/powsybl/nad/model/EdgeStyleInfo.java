/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.model;

import java.util.Collections;
import java.util.List;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
public record EdgeStyleInfo(List<String> styleClasses, String styleClass, List<String> highlightStyleClasses) {

    public EdgeStyleInfo(List<String> styleClasses, String styleClass) {
        this(styleClasses, styleClass, Collections.emptyList());
    }
}
