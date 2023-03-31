/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionprocessor.positionfromextension;

import com.powsybl.sld.layout.positionprocessor.BSCluster;
import com.powsybl.sld.layout.positionprocessor.HorizontalBusList;
import com.powsybl.sld.layout.positionprocessor.HorizontalBusListManager;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.nodes.BusNode;

import java.util.Optional;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

public class HBLManagerByExtension implements HorizontalBusListManager {

    public void mergeHbl(BSCluster leftCluster, BSCluster rightCluster) {
        //for this implementation, the busBar structuralPosition are already defined,
        // we must ensure that structuralPosition vPos when merging left and right HorizontalPosition,
        // and structuralPosition hPos are ordered
        leftCluster.getHorizontalBusLists().forEach(hbl -> {
            BusNode rightNodeOfLeftHbl = hbl.getSideNode(Side.RIGHT);
            Optional<HorizontalBusList> rightHbl = rightCluster.getHorizontalBusLists().stream()
                    .filter(hbl2 -> hbl2.getSideNode(Side.LEFT).getBusbarIndex() == rightNodeOfLeftHbl.getBusbarIndex())
                    .findFirst();
            if (rightHbl.isPresent()) {
                BusNode leftNodeOfRightHbl = rightHbl.get().getSideNode(Side.LEFT);
                if (leftNodeOfRightHbl == rightNodeOfLeftHbl
                        || rightNodeOfLeftHbl.getSectionIndex() < leftNodeOfRightHbl.getSectionIndex()) {
                    hbl.merge(rightHbl.get());
                    rightCluster.removeHbl(rightHbl.get());
                }
            }
        });
        mergeHblWithNoLink(leftCluster, rightCluster);
    }
}
