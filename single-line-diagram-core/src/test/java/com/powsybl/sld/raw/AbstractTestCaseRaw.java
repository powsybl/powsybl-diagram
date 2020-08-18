/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.AbstractTestCase;
import com.powsybl.sld.RawGraphBuilder;
import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.FeederNode;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.svg.DiagramInitialValueProvider;
import com.powsybl.sld.svg.InitialValue;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public abstract class AbstractTestCaseRaw extends AbstractTestCase {

    DiagramInitialValueProvider initialValueProvider = new DiagramInitialValueProvider() {
        @Override
        public InitialValue getInitialValue(Node node) {
            InitialValue initialValue;
            if (node.getType() == Node.NodeType.BUS) {
                initialValue = new InitialValue(null, null, node.getLabel(), null, null, null);
            } else {
                initialValue = new InitialValue(DiagramInitialValueProvider.Direction.UP, DiagramInitialValueProvider.Direction.DOWN, "10", "20", null, null);
            }
            return initialValue;
        }

        @Override
        public List<String> getNodeLabelValue(Node node) {
            List<String> res = new ArrayList<>();
            if (node instanceof FeederNode || node instanceof BusNode) {
                res.add(node.getLabel());
            }
            return res;
        }
    };

    protected RawGraphBuilder rawGraphBuilder = new RawGraphBuilder();


}
