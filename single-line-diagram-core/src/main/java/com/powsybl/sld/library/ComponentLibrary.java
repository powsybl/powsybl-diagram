/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import org.apache.batik.anim.dom.SVGOMDocument;

import java.util.List;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ComponentLibrary {

    List<AnchorPoint> getAnchorPoints(String type);

    SVGOMDocument getSvgDocument(String type);

    ComponentSize getSize(String type);

    boolean isAllowRotation(String type);

    String getStyleSheet();
}
