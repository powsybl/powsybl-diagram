/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Connectable;

import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ConnectablePosition<C extends Connectable<C>> extends Extension<C> {

    String NAME = "position";

    enum Direction {
        TOP,
        BOTTOM,
        UNDEFINED
    }

    @Override
    default String getName() {
        return NAME;
    }

    interface Info {
        String getName();

        Info setName(String name);

        Optional<Integer> getOrder();

        Info setOrder(int order);

        Info removeOrder();

        Direction getDirection();

        Info setDirection(Direction direction);
    }

    Info getInfo();

    Info getInfo1();

    Info getInfo2();

    Info getInfo3();

    static void check(Info info, Info info1, Info info2, Info info3) {
        if (info == null && info1 == null && info2 == null && info3 == null) {
            throw new IllegalArgumentException("invalid info");
        }
        if (info != null && (info1 != null || info2 != null || info3 != null)) {
            throw new IllegalArgumentException("info and info 1|2|3 are exclusives");
        }
        boolean error = false;
        if (info1 != null) {
            if (info2 == null && info3 != null) {
                error = true;
            }
        } else {
            if (info2 != null || info3 != null) {
                error = true;
            }
        }
        if (error) {
            throw new IllegalArgumentException("info 1|2|3 have to be set in the right order");
        }
    }

}
