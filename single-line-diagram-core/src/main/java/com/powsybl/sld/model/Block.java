/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.layout.LayoutParameters;

import java.io.IOException;
import java.util.Set;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public interface Block {
    enum Type {
        LEGPRIMARY, BODYPRIMARY, LEGPARALLEL, BODYPARALLEL, SERIAL, UNDEFINED;

        public boolean isPrimary(Type type) {
            return type == LEGPRIMARY || type == BODYPRIMARY;
        }
    }

    enum Extremity {
        START, END, NONE;

        public Extremity flip() {
            if (this.equals(START)) {
                return END;
            }
            if (this.equals(END)) {
                return START;
            }
            return NONE;
        }
    }

    Graph getGraph();

    Node getExtremityNode(Extremity extremity);

    Extremity getExtremity(Node node);

    Node getStartingNode();

    Node getEndingNode();

    void reverseBlock();

    boolean isEmbedingNodeType(Node.NodeType type);

    void setParentBlock(Block parentBlock);

    Position getPosition();

    Coord getCoord();

    void setXSpan(double xSpan);

    void setYSpan(double ySpan);

    void setX(double x);

    void setY(double y);


    /**
     * Calculate maximal pxWidth that layout.block can use in a cell without modifying
     * root pxWidth
     */
    void sizing();

    /**
     * Calculates all the blocks dimensions and find the order of the layout.block inside
     * the cell
     */
    void calculateCoord(LayoutParameters layoutParam);

    void calculateRootCoord(LayoutParameters layoutParam);

    double calculateHeight(Set<Node> encounteredNodes, LayoutParameters layoutParam);

    double calculateRootHeight(LayoutParameters layoutParam);

    default int getOrder() {
        return 0;
    }

    void coordVerticalCase(LayoutParameters layoutParam);

    void coordHorizontalCase(LayoutParameters layoutParam);

    void setCardinality(Extremity extremity, int i);

    int getCardinality(Extremity extremity);

    int getCardinality(Node commonNode);

    void setCell(Cell cell);

    Cell getCell();

    void setOrientation(Orientation orientation);

    Type getType();

    void writeJson(JsonGenerator generator) throws IOException;

}
