/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout.pathfinding.geometry;

import com.powsybl.sld.model.coordinate.PointInteger;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public class PointHeading {
    private PointInteger point;
    private PointInteger heading;

    private int hashCode = 0;

    public PointHeading(PointInteger point, PointInteger heading) {
        this.point = point;
        this.heading = heading;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PointHeading that)) {
            return false;
        }
        return point.equals(that.point) && heading.equals(that.heading);
    }

    public PointInteger getPoint() {
        return point;
    }

    public void setPoint(PointInteger point) {
        this.point = point;
    }

    public PointInteger getHeading() {
        return heading;
    }

    public void setHeading(PointInteger heading) {
        this.heading = heading;
    }

    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = 17;
            // https://stackoverflow.com/a/2816747
            result = 92821 * result + point.getX();
            result = 92821 * result + point.getY();
            result = 92821 * result + heading.getX();
            result = 92821 * result + heading.getY();
            hashCode = result;
        }
        return result;
    }
}

