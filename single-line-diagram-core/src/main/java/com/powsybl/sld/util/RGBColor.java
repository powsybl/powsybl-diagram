/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class RGBColor {

    private int red;
    private int green;
    private int blue;

    public RGBColor(int r, int g, int b) {
        red = r;
        green = g;
        blue = b;
    }

    public static RGBColor parse(String color) {
        Objects.requireNonNull(color);

        if (color.length() < 7) {
            throw new IllegalArgumentException("Invalid length");
        }

        int r = Integer.parseInt(color.substring(1, 3), 16);
        int g = Integer.parseInt(color.substring(3, 5), 16);
        int b = Integer.parseInt(color.substring(5, 7), 16);
        return new RGBColor(r, g, b);
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public RGBColor getBrighter(double factor) {
        int x = (int) (1.0 / (1.0 - factor));
        if (red == 0 && green == 0 && blue == 0) {
            return new RGBColor(x, x, x);
        }
        int r = red;
        int g = green;
        int b = blue;
        if (red > 0 && red < x) {
            r = x;
        }
        if (green > 0 && green < x) {
            g = x;
        }
        if (blue > 0 && b < x) {
            b = x;
        }
        return new RGBColor(Math.min((int) (r / factor), 255), Math.min((int) (g / factor), 255), Math.min((int) (b / factor), 255));
    }

    public RGBColor getDarker(double factor) {
        return new RGBColor(Math.max((int) (red * factor), 0), Math.max((int) (green * factor), 0), Math.max((int) (blue * factor), 0));
    }

    public List<RGBColor> getColorGradient(int steps) {
        ArrayList<RGBColor> gradient = new ArrayList();

        double factor = Math.min(Math.max(1d / steps, 0.2), 0.75);

        RGBColor c1 = getBrighter(factor);
        RGBColor c2 = getDarker(factor);
        if (steps <= 2) {
            gradient.add(c1);
            gradient.add(getDarker(0.75));
        } else {
            for (int i = 0; i < steps; i++) {
                float ratio = (float) Math.pow(i / (float) steps, 2);
                int r = (int) (c2.getRed() * ratio + c1.getRed() * (1 - ratio));
                int g = (int) (c2.getGreen() * ratio + c1.getGreen() * (1 - ratio));
                int b = (int) (c2.getBlue() * ratio + c1.getBlue() * (1 - ratio));
                RGBColor c = new RGBColor(r, g, b);
                gradient.add(c);
            }
        }
        return gradient;
    }

    public String toString() {
        return String.format("#%02X%02X%02X", red, green, blue);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + blue;
        result = prime * result + green;
        result = prime * result + red;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RGBColor other = (RGBColor) obj;
        return blue == other.blue && green == other.green && red == other.red;
    }

}
