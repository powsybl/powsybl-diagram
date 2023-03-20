/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

public interface HorizontalBusSetManager {

    void mergeHbs(BSCluster leftCluster, BSCluster rightCluster);

    default void mergeHbsWithNoLink(BSCluster leftCluster, BSCluster rightCluster) {
        rightCluster.getHorizontalBusSets().forEach(hbs -> {
            hbs.shift(leftCluster.getLength());
            hbs.setBsCluster(leftCluster);
        });
        leftCluster.getHorizontalBusSets().addAll(rightCluster.getHorizontalBusSets());
        rightCluster.getHorizontalBusSets().removeAll(rightCluster.getHorizontalBusSets());
    }
}
