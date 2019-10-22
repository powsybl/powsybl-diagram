/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public interface ZoneLayout {

    /**
     * Calculate real coordinates of nodes in the zone
     */
    void run(LayoutParameters layoutParam);

}
