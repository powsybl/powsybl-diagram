/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionprocessor;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

public interface HorizontalBusListManager {

    void mergeHbl(BSCluster leftCluster, BSCluster rightCluster);

    default void mergeHblWithNoLink(BSCluster leftCluster, BSCluster rightCluster) {
        rightCluster.getHorizontalBusLists().forEach(hbl -> {
            hbl.shift(leftCluster.getLength());
            hbl.setBsCluster(leftCluster);
        });
        leftCluster.getHorizontalBusLists().addAll(rightCluster.getHorizontalBusLists());
        rightCluster.getHorizontalBusLists().removeAll(rightCluster.getHorizontalBusLists());
    }
}
