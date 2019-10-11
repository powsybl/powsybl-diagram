/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.model.Edge;
import com.powsybl.sld.model.Feeder2WTNode;
import com.powsybl.sld.model.Fictitious3WTNode;
import com.powsybl.sld.model.Node;

import java.util.Map;
import java.util.Optional;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public interface DiagramStyleProvider {

    Optional<String> getNodeStyle(Node node, boolean avoidSVGComponentsDuplication);

    String getIdWireStyle(Edge edge);

    Optional<String> getWireStyle(Edge edge);

    Optional<String> getNode3WTStyle(Fictitious3WTNode node, ThreeWindingsTransformer.Side side);

    Optional<String> getNode2WTStyle(Feeder2WTNode node, TwoWindingsTransformer.Side side);

    Optional<String> getColor(VoltageLevel vl);

    Map<String, String> getAttributesArrow(int num);
}
