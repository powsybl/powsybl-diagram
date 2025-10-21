/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.build.iidm;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class IntIdProvider implements IdProvider {
    private int count;

    public IntIdProvider() {
        count = 0;
    }

    @Override
    public String createSvgId(String idNetworkElement) {
        return nextId();
    }

    private String nextId() {
        return String.valueOf(count++);
    }
}
