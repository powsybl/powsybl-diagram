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

    private Point connection;

    public TextNode(String diagramId) {
        super(diagramId, null, null);
        connection = new Point();
    }

    public Point getConnection() {
        return connection;
    }

    public void setConnection(Point connection) {
        this.connection = connection;
    }
}
