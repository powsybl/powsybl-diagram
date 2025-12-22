/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
abstract class AbstractIdentifiable implements Identifiable {

    private final String svgId;
    private final String equipmentId;
    private final String name;

    protected AbstractIdentifiable(String svgId, String equipmentId, String nameOrId) {
        this.svgId = Objects.requireNonNull(svgId);
        this.equipmentId = equipmentId;
        this.name = nameOrId;
    }

    @Override
    public String getSvgId() {
        return svgId;
    }

    @Override
    public String getEquipmentId() {
        return equipmentId;
    }

    @Override
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }
}
