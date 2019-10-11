/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ComponentSizeAdapter extends XmlAdapter<AdaptedComponentSize, ComponentSize> {

    @Override
    public ComponentSize unmarshal(AdaptedComponentSize adapted) {
        return new ComponentSize(adapted.getWidth(), adapted.getHeight());
    }

    @Override
    public AdaptedComponentSize marshal(ComponentSize size) {
        AdaptedComponentSize adapted = new AdaptedComponentSize();
        adapted.setWidth(size.getWidth());
        adapted.setHeight(size.getHeight());
        return adapted;
    }
}
