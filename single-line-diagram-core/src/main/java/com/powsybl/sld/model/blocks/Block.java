/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.blocks;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.layout.LayoutContext;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.coordinate.Coord;
import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.coordinate.Position;
import com.powsybl.sld.model.nodes.Node;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public interface Block {
    enum Type {
        LEGPRIMARY, BODYPRIMARY, FEEDERPRIMARY, LEGPARALLEL, BODYPARALLEL, SERIAL, UNDEFINED;

        public boolean isPrimary() {
            return this == LEGPRIMARY || this == BODYPRIMARY || this == FEEDERPRIMARY;
        }

        public boolean isParallel() {
            return this == LEGPARALLEL || this == BODYPARALLEL;
        }

        public boolean isLeg() {
            return this == LEGPRIMARY || this == LEGPARALLEL;
        }
    }

    enum Extremity {
        START, END;
    }

    Node getExtremityNode(Extremity extremity);

    Optional<Extremity> getExtremity(Node node);

    Node getStartingNode();

    Node getEndingNode();

    void reverseBlock();

    boolean isEmbeddingNodeType(Node.NodeType type);

    List<Block> findBlockEmbeddingNode(Node node);

    void setParentBlock(Block parentBlock);

    Position getPosition();

    Coord getCoord();

    /**
     * Calculate maximal pxWidth that layout.block can use in a cell without modifying
     * root pxWidth
     */
    void sizing();

    void accept(BlockVisitor blockVisitor);

    /**
     * Calculates all the blocks dimensions and find the order of the layout.block inside
     * the cell
     */
    void calculateCoord(LayoutParameters layoutParam, LayoutContext layoutContext);

    void calculateRootCoord(LayoutParameters layoutParam, LayoutContext layoutContext);

    void coordVerticalCase(LayoutParameters layoutParam, LayoutContext layoutContext);

    void coordHorizontalCase(LayoutParameters layoutParam, LayoutContext layoutContext);

    double calculateHeight(Set<Node> encounteredNodes, LayoutParameters layoutParam);

    double calculateRootHeight(LayoutParameters layoutParam);

    default int getOrder() {
        return 0;
    }

    void setCardinality(Extremity extremity, int i);

    int getCardinality(Extremity extremity);

    int getCardinality(Node commonNode);

    void setOrientation(Orientation orientation);

    void setOrientation(Orientation orientation, boolean recursively);

    Orientation getOrientation();

    Type getType();

    void writeJson(JsonGenerator generator, boolean includeCoordinates) throws IOException;

}
