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
@FunctionalInterface
public interface IdProvider {

    /**
     * Creates a new unique id for a svg tag related to the given network element id,
     * knowing that:
     * <ul>
     *     <li>calling twice on the same object should result in two different ids,</li>
     *     <li>using the object fields to create an id should be limited to debug mode.</li>
     * </ul>
     * @return a unique id
     */
    String createSvgId(String idNetworkElement);
}
