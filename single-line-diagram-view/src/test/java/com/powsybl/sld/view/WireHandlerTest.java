/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.view;

import com.google.common.collect.ImmutableList;
import com.powsybl.sld.view.WireHandler.OrientedPosition;
import javafx.geometry.Point2D;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class WireHandlerTest {

    @Test
    public void computePointAtDistance() {

        List<Point2D> points = ImmutableList.of(new Point2D(1, 0),
                new Point2D(1, 1),
                new Point2D(2, 1),
                new Point2D(2, 2),
                new Point2D(1, 2));

        OrientedPosition middle1 = WireHandler.positionAtDistance(points, 0.5);
        assertEquals(new Point2D(1, 0.5), middle1.getPoint());
        assertEquals(0, middle1.getOrientation(), 0);

        OrientedPosition middle2 = WireHandler.positionAtDistance(points, 1.5);
        assertEquals(new Point2D(1.5, 1), middle2.getPoint());
        assertEquals(-90, middle2.getOrientation(), 0);

        OrientedPosition middle3 = WireHandler.positionAtDistance(points, 2.5);
        assertEquals(new Point2D(2, 1.5), middle3.getPoint());
        assertEquals(0, middle3.getOrientation(), 0);

        OrientedPosition middle4 = WireHandler.positionAtDistance(points, 3.5);
        assertEquals(new Point2D(1.5, 2), middle4.getPoint());
        assertEquals(90, middle4.getOrientation(), 0);
    }

    @Test
    public void computeAngle() {
        List<Point2D> points1 = ImmutableList.of(new Point2D(0, 0), new Point2D(1, 1));
        OrientedPosition position1 = WireHandler.positionAtDistance(points1, 0.5);
        assertEquals(-45, position1.getOrientation(), 1e-5);

        List<Point2D> points2 = ImmutableList.of(new Point2D(0, 0), new Point2D(-1, 1));
        OrientedPosition position2 = WireHandler.positionAtDistance(points2, 0.5);
        assertEquals(45, position2.getOrientation(), 1e-5);

        List<Point2D> points3 = ImmutableList.of(new Point2D(0, 0), new Point2D(-1, -1));
        OrientedPosition position3 = WireHandler.positionAtDistance(points3, 0.5);
        assertEquals(135, position3.getOrientation(), 1e-5);

        List<Point2D> points4 = ImmutableList.of(new Point2D(0, 0), new Point2D(1, -1));
        OrientedPosition position4 = WireHandler.positionAtDistance(points4, 0.5);
        assertEquals(-135, position4.getOrientation(), 1e-5);
    }
}
