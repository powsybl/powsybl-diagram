/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TextNode extends AbstractNode {

    public TextNode(String diagramId) {
        super(diagramId, null, null);
    }
}
