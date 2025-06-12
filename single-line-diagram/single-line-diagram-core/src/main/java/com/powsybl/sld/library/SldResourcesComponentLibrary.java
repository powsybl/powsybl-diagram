/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import com.powsybl.diagram.components.ResourcesComponentLibrary;
import com.powsybl.sld.model.coordinate.Orientation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Library of resources components, that is, the SVG image files representing the components, together with the styles
 * associated to each component
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public class SldResourcesComponentLibrary extends ResourcesComponentLibrary<SldComponent> implements SldComponentLibrary {

    private static final String[] HIDDEN_COMPONENTS = new String[]{
        SldComponentTypeName.PHASE_SHIFT_TRANSFORMER_LEG,
        SldComponentTypeName.TWO_WINDINGS_TRANSFORMER_LEG,
        SldComponentTypeName.THREE_WINDINGS_TRANSFORMER_LEG,
        SldComponentTypeName.LINE,
        SldComponentTypeName.TIE_LINE,
        SldComponentTypeName.DANGLING_LINE,
        SldComponentTypeName.BUSBAR_SECTION
    };

    public SldResourcesComponentLibrary(String name, String directory, String... additionalDirectories) {
        super(name, SldComponent.class, directory, additionalDirectories);
        for (String hiddenComponent : HIDDEN_COMPONENTS) {
            addNoComponentType(hiddenComponent);
        }
    }

    @Override
    public List<AnchorPoint> getAnchorPoints(String type) {
        Objects.requireNonNull(type);
        SldComponent component = getComponent(type);
        return component != null ? component.getAnchorPoints()
                : Collections.singletonList(new AnchorPoint(0, 0, AnchorOrientation.NONE));
    }

    @Override
    public Map<Orientation, SldComponent.Transformation> getTransformations(String type) {
        Objects.requireNonNull(type);
        SldComponent component = getComponent(type);
        return component != null ? component.getTransformations() : Collections.emptyMap();
    }
}
