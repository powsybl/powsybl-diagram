/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg.iidm;

import com.powsybl.commons.config.BaseVoltagesConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class NominalVoltageStyleProvider extends AbstractVoltageStyleProvider {

    public NominalVoltageStyleProvider(Network network) {
        super(network);
    }

    public NominalVoltageStyleProvider(Network network, BaseVoltagesConfig baseVoltageStyle) {
        super(network, baseVoltageStyle);
    }

    @Override
    public List<String> getCssFilenames() {
        return Collections.singletonList("nominalStyle.css");
    }

    @Override
    protected Optional<String> getBaseVoltageStyle(Terminal terminal) {
        if (terminal == null) {
            return Optional.empty();
        }
        return getBaseVoltageStyle(terminal.getVoltageLevel().getNominalV());
    }
}
