/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class AdaptedAnchorPoint {

    @XmlAttribute(name = "x")
    private double x = 0;

    @XmlAttribute(name = "y")
    private double y = 0;

    @XmlAttribute(name = "orientation")
    private AnchorOrientation orientation;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public AnchorOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(AnchorOrientation orientation) {
        this.orientation = orientation;
    }

    @Override
    public String toString() {
        return "AnchorPoint(x=" + x + ", y=" + y + ", orientation=" + orientation + ")";
    }
}
