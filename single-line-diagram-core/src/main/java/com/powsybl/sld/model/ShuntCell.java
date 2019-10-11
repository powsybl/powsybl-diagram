/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.layout.LayoutParameters;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ShuntCell extends AbstractCell {
    public ShuntCell(Graph graph) {
        super(graph, CellType.SHUNT);
    }

    public void calculateCoord(LayoutParameters layoutParam) {
        if (getRootBlock() instanceof BodyPrimaryBlock) {
            ((BodyPrimaryBlock) getRootBlock()).coordShuntCase();
        } else {
            throw new PowsyblException("ShuntCell can only be composed of a single BodyPrimaryBlock");
        }
    }

    @Override
    public String toString() {
        return "ShuntCell(" + nodes + " )";
    }
}
