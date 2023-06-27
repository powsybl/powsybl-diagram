/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.nad.svg.iidm;

import com.powsybl.nad.build.iidm.IdProvider;

/**
 *
 * @author Sophie Frasnedo <sophie.frasnedo at rte-france.com>
 */
@FunctionalInterface
public interface IdProviderFactory {
    IdProvider create();

}
