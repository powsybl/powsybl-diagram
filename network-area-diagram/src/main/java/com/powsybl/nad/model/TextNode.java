/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class TextNode extends AbstractNode {

    private Point edgeConnection;

    public TextNode(String svgId) {
        super(svgId, null, null, false);
        edgeConnection = new Point();
    }

    public Point getEdgeConnection() {
        return edgeConnection;
    }

    public void setEdgeConnection(Point edgeConnection) {
        this.edgeConnection = edgeConnection;
    }
}
