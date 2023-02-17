/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import com.powsybl.sld.model.coordinate.Orientation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AnchorPointTest {

    @Test
    public void rotationTest() {
        AnchorPoint anchorPoint1 = new AnchorPoint(0, -10, AnchorOrientation.VERTICAL);
        AnchorPoint anchorPoint2 = new AnchorPoint(0, 10, AnchorOrientation.VERTICAL);
        AnchorPoint rotatedAnchorPoint1 = anchorPoint1.transformAnchorPoint(Orientation.LEFT, Component.Transformation.ROTATION);
        assertEquals(AnchorOrientation.HORIZONTAL, rotatedAnchorPoint1.getOrientation());
        assertEquals(-10, rotatedAnchorPoint1.getX(), 0);
        assertEquals(0, rotatedAnchorPoint1.getY(), 0);
        AnchorPoint rotatedAnchorPoint2 = anchorPoint2.transformAnchorPoint(Orientation.LEFT, Component.Transformation.ROTATION);
        assertEquals(AnchorOrientation.HORIZONTAL, rotatedAnchorPoint2.getOrientation());
        assertEquals(10, rotatedAnchorPoint2.getX(), 0);
        assertEquals(0, rotatedAnchorPoint2.getY(), 0);
        AnchorPoint rotatedAnchorPoint3 = anchorPoint2.transformAnchorPoint(Orientation.DOWN, Component.Transformation.ROTATION);
        assertEquals(AnchorOrientation.VERTICAL, rotatedAnchorPoint3.getOrientation());
        assertEquals(0, rotatedAnchorPoint3.getX(), 0);
        assertEquals(-10, rotatedAnchorPoint3.getY(), 0);
        AnchorPoint rotatedAnchorPoint4 = anchorPoint1.transformAnchorPoint(Orientation.UP, Component.Transformation.ROTATION);
        assertEquals(AnchorOrientation.VERTICAL, rotatedAnchorPoint4.getOrientation());
        assertEquals(0, rotatedAnchorPoint4.getX(), 0);
        assertEquals(-10, rotatedAnchorPoint4.getY(), 0);
        AnchorPoint transformAnchorPoint = anchorPoint1.transformAnchorPoint(Orientation.LEFT, Component.Transformation.NONE);
        assertEquals(anchorPoint1, transformAnchorPoint);
        AnchorPoint flipAnchorPoint = anchorPoint1.transformAnchorPoint(Orientation.DOWN, Component.Transformation.FLIP);
        assertEquals(AnchorOrientation.VERTICAL, flipAnchorPoint.getOrientation());
        assertEquals(0, flipAnchorPoint.getX(), 0);
        assertEquals(10, flipAnchorPoint.getY(), 0);
        assertEquals("AnchorPoint(x=0.0, y=10.0, orientation=VERTICAL)", flipAnchorPoint.toString());

    }
}
