/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.diagram.util.forcelayout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class BoundingBoxTest {

    @Test
    void test() {
        List<Point> points = new ArrayList<>();
        points.add(new Point(1.0, 2.0));
        points.add(new Point(3.0, 4.0));
        points.add(new Point(5.0, 6.0));
        points.add(new Point(1.0, 2.0));
        BoundingBox bbox = BoundingBox.computeBoundingBox(points);

        assertEquals(4.0, bbox.getWidth(), 0.0);
        assertEquals(4.0, bbox.getHeight(), 0.0);
        assertEquals(1.0, bbox.getLeft(), 0.0);
        assertEquals(2.0, bbox.getTop(), 0.0);
    }
}
