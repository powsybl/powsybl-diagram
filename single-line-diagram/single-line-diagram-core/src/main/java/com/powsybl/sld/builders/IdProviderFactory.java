/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.builders;

import com.powsybl.sld.svg.IdProvider;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
@FunctionalInterface
public interface IdProviderFactory {

    /**
     * Creates an identifier provider for the given SVG identifier prefix.
     *
     * @param prefix prefix to apply to generated SVG identifiers
     * @return a new identifier provider
     */
    IdProvider create(String prefix);
}
