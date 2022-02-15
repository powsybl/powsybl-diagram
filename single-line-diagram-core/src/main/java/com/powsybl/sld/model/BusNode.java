/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.coordinate.Position;

import java.io.IOException;

import static com.powsybl.sld.library.ComponentTypeName.BUSBAR_SECTION;
import static com.powsybl.sld.model.coordinate.Position.Dimension.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class BusNode extends Node {

    private double pxWidth = 1;

    private int busbarIndex;
    private int sectionIndex;

    private Position position = new Position(-1, -1);

    BusNode(String id, String name, boolean fictitious, VoltageLevelGraph graph) {
        super(NodeType.BUS, id, name, id, BUSBAR_SECTION, fictitious, graph);
    }

    public void calculateCoord(LayoutParameters layoutParameters) {
        double elementaryWidth = layoutParameters.getCellWidth() / 2;
        double busPadding = layoutParameters.getBusPadding();
        setCoordinates(position.get(H) * elementaryWidth + busPadding,
            getVoltageLevelGraph().getFirstBusY(layoutParameters) + position.get(V) * layoutParameters.getVerticalSpaceBus());
        setPxWidth(position.getSpan(H) * elementaryWidth - 2 * busPadding);
    }

    @Override
    public void setCell(Cell cell) {
        if (!(cell instanceof BusCell)) {
            throw new PowsyblException("The Cell of a BusNode shall be a BusCell");
        }
        super.setCell(cell);
    }

    public double getPxWidth() {
        return pxWidth;
    }

    public void setPxWidth(double widthBus) {
        this.pxWidth = widthBus;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setBusBarIndexSectionIndex(int busbarIndex, int sectionIndex) {
        this.busbarIndex = busbarIndex;
        this.sectionIndex = sectionIndex;
    }

    public int getBusbarIndex() {
        return busbarIndex;
    }

    public void setBusbarIndex(int busbarIndex) {
        this.busbarIndex = busbarIndex;
    }

    public int getSectionIndex() {
        return sectionIndex;
    }

    public void setSectionIndex(int sectionIndex) {
        this.sectionIndex = sectionIndex;
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator, boolean includeCoordinates) throws IOException {
        super.writeJsonContent(generator, includeCoordinates);
        generator.writeNumberField("pxWidth", pxWidth);
        generator.writeNumberField("busbarIndex", busbarIndex);
        generator.writeNumberField("sectionIndex", sectionIndex);
        if (position != null) {
            generator.writeFieldName("position");
            position.writeJsonContent(generator, true);
        }
    }
}
