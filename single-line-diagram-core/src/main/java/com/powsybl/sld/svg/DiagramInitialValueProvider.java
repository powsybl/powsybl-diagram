/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.model.Node;
import org.apache.batik.anim.dom.SVGOMDocument;

import java.util.List;
import java.util.Map;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public interface DiagramInitialValueProvider {

    class NodeLabel {
        private final String label;
        private final LabelPosition position;

        public NodeLabel(String label, LabelPosition labelPosition){
            this.label = label;
            this.position = labelPosition;
        }

        public String getLabel() {
            return label;
        }

        public LabelPosition getPosition() {
            return position;
        }
    }

    public enum Direction {
        UP, DOWN;
    }

    InitialValue getInitialValue(Node node);

    List<NodeLabel> getNodeLabels(Node node);
}
