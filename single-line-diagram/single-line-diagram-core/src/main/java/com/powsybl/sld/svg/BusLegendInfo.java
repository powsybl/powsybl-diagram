/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.sld.svg;

import java.util.List;

/**
 * @author Kevin Le Saulnier {@literal <kevin.lesaulnier at rte-france.com>}
 */
public record BusLegendInfo(String busId, List<Caption> captions) {
    public record Caption(String label, String type) { }
}
