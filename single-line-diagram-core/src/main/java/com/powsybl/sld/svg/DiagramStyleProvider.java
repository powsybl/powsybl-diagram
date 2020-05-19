/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.library.ComponentSize;
import com.powsybl.sld.model.Edge;
import com.powsybl.sld.model.Node;

import java.util.Map;
import java.util.Optional;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public interface DiagramStyleProvider {

    Optional<String> getCssNodeStyle(Node node, boolean isShowInternalNodes);

    Map<String, String> getCssWireStyleAttributes(Edge edge);

    Map<String, String> getSvgNodeStyleAttributes(Node node, ComponentSize size, String subComponentName, boolean isShowInternalNodes);

    Map<String, String> getSvgArrowStyleAttributes(int num);

    void reset();
}
