/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.sld.layout;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.layout.position.PositionFinder;

/**
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */

@FunctionalInterface
public interface VoltageLevelLayoutFactoryCreator {
    VoltageLevelLayoutFactory create(Network network);

    static VoltageLevelLayoutFactoryCreator newSmartVoltageLevelLayoutFactoryCreator() {
        return SmartVoltageLevelLayoutFactory::new;
    }

    static VoltageLevelLayoutFactoryCreator newPositionVoltageLevelLayoutFactoryCreator(PositionFinder positionFinder) {
        return i -> new PositionVoltageLevelLayoutFactory(positionFinder);
    }

    static VoltageLevelLayoutFactoryCreator newPositionVoltageLevelLayoutFactoryCreator(PositionFinder positionFinder, PositionVoltageLevelLayoutFactoryParameters positionVoltageLevelLayoutFactoryParameters) {
        return i -> new PositionVoltageLevelLayoutFactory(positionFinder, positionVoltageLevelLayoutFactoryParameters);
    }

    static VoltageLevelLayoutFactoryCreator newPositionVoltageLevelLayoutFactoryCreator() {
        return i -> new PositionVoltageLevelLayoutFactory();
    }

    static VoltageLevelLayoutFactoryCreator newPositionVoltageLevelLayoutFactoryCreator(PositionVoltageLevelLayoutFactoryParameters positionVoltageLevelLayoutFactoryParameters) {
        return i -> new PositionVoltageLevelLayoutFactory(positionVoltageLevelLayoutFactoryParameters);
    }

}
