/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.sld.layout.LayoutParameters;

import java.util.List;
import java.util.Set;

import static com.powsybl.sld.model.Block.Extremity.*;
import static com.powsybl.sld.model.Block.Type.BODYPRIMARY;
import static com.powsybl.sld.model.Cell.CellType.INTERN;
import static com.powsybl.sld.model.InternCell.Shape.FLAT;
import static com.powsybl.sld.model.Node.NodeType.*;
import static com.powsybl.sld.model.coordinate.Coord.Dimension.*;
import static com.powsybl.sld.model.coordinate.Orientation.*;
import static com.powsybl.sld.model.coordinate.Position.Dimension.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BodyPrimaryBlock extends AbstractPrimaryBlock {

    public BodyPrimaryBlock(List<Node> nodes, Cell cell) {
        super(BODYPRIMARY, nodes, cell);
    }

    public BodyPrimaryBlock(BodyPrimaryBlock bodyPrimaryBlock) {
        this(bodyPrimaryBlock.getNodes(), bodyPrimaryBlock.getCell());
    }

    @Override
    public void sizing() {
        if (getPosition().getOrientation().isVertical()) {
            getPosition().setSpan(H, 2);
            // in the case of vertical Blocks the x Spanning is a ratio of the nb of edges of the blocks/overall edges
            getPosition().setSpan(V, 2 * (nodes.size() - 1));
        } else {
            // in the case of horizontal Blocks having 1 switch/1 position => 1 hPos / 2 edges rounded to the superior int
            getPosition().setSpan(H, 2 * Math.max(1, nodes.size() - 2));
            getPosition().setSpan(V, 2);
        }
    }

    @Override
    public double calculateHeight(Set<Node> encounteredNodes, LayoutParameters layoutParameters) {
        // we do not consider the exact height of components as the maximum height will later be split up equally between nodes
        double componentHeight = layoutParameters.getMaxComponentHeight() + layoutParameters.getMinSpaceBetweenComponents();

        // we increment the height only if the node is not a bus node and has not been already encountered
        long nbNodes = nodes.stream().filter(n -> !encounteredNodes.contains(n) && n.getType() != BUS).count();

        return (nbNodes - 1) * componentHeight;
    }

    @Override
    public void coordVerticalCase(LayoutParameters layoutParam) {
        int sign = getOrientation() == UP ? 1 : -1;
        double y0 = getCoord().get(Y) + sign * getCoord().getSpan(Y) / 2;
        double yPxStep = calcYPxStep(sign);
        int v = 0;
        for (Node node : nodes) {
            node.setCoordinates(getCoord().get(X), y0 - yPxStep * v);
            node.setOrientation(getOrientation());
            v++;
        }
    }

    @Override
    public void coordHorizontalCase(LayoutParameters layoutParam) {
        double x0 = getCoord().get(X) - getCoord().getSpan(X) / 2;
        if (getCell().getType() == INTERN
                && ((InternCell) getCell()).getShape().checkIsNotShape(FLAT)) {
            x0 += layoutParam.getCellWidth() / 2;
        }
        double xPxStep = getCoord().getSpan(X) / (nodes.size() - 1);
        int h = 0;
        for (Node node : nodes) {
            node.setCoordinates(x0 + xPxStep * h, getCoord().get(Y));
            node.setOrientation(LEFT);
            h++;
        }
    }

    void coordShuntCase(LayoutParameters layoutParameters, int hLeft, int hRight) {
        double x0 = hToX(layoutParameters, hLeft);
        double y0 = getExtremityNode(START).getY();
        double x1 = hToX(layoutParameters, hRight);
        double y1 = getExtremityNode(END).getY();
        double y = (y0 + y1) / 2;

        nodes.get(1).setCoordinates(x0, y);
        nodes.get(nodes.size() - 2).setCoordinates(x1, y);

        double dx = (x1 - x0) / (nodes.size() - 3);
        for (int i = 2; i < nodes.size() - 2; i++) {
            Node node = nodes.get(i);
            node.setCoordinates(x0 + (i - 1) * dx, y);
            node.setOrientation(LEFT);
        }
    }

    private double calcYPxStep(int sign) {
        if (getPosition().getSpan(V) == 0) {
            return 0;
        }
        return sign * getCoord().getSpan(Y) / (nodes.size() - 1);
    }
}
