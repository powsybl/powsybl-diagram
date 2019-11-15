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
public class HSLColor {
    private static final RGBColor BLACK = new RGBColor(0, 0, 0);
    private static final RGBColor WHITE = new RGBColor(255, 255, 255);

    private float hue;
    private float saturation;
    private float luminance;

    private static final int DARKEST_AMOUNT = 50;
    private static final int LIGHTEST_AMOUNT = 70;
    private static final int DARK_COLORS_MIX_ROTATE = -51;
    private static final int LIGHT_COLORS_MIX_ROTATE = 67;
    private static final float LIGHT_SATURATION = 20;
    private static final float DARK_SATURATION = 14;

    public HSLColor(float h, float s, float l) {
        this.hue = h;
        this.saturation = s;
        this.luminance = l;
    }

    public HSLColor(int red, int green, int blue) {
        float r = red / 255f;
        float g = green / 255f;
        float b = blue / 255f;

        float min = Math.min(r, Math.min(g, b));
        float max = Math.max(r, Math.max(g, b));

        float h = 0;

        if (max == min) {
            h = 0;
        } else if (max == r) {
            h = ((60 * (g - b) / (max - min)) + 360) % 360;
        } else if (max == g) {
            h = (60 * (b - r) / (max - min)) + 120;
        } else if (max == b) {
            h = (60 * (r - g) / (max - min)) + 240;
        }
        float l = (max + min) / 2;

        float s = 0;

        if (max == min) {
            s = 0;
        } else if (l <= .5f) {
            s = (max - min) / (max + min);
        } else {
            s = (max - min) / (2 - max - min);
        }
        this.hue = h;
        this.saturation = s * 100;
        this.luminance = l * 100;
    }

    public static HSLColor parse(String color) {
        Objects.requireNonNull(color);

        if (color.length() < 7) {
            throw new IllegalArgumentException("Invalid length");
        }

        int r = Integer.parseInt(color.substring(1, 3), 16);
        int g = Integer.parseInt(color.substring(3, 5), 16);
        int b = Integer.parseInt(color.substring(5, 7), 16);
        return new HSLColor(r, g, b);
    }

    public RGBColor toRGBColor() {

        float h = this.hue % 360.0f;
        h /= 360f;
        float s = this.saturation / 100f;
        float l = this.luminance / 100f;

        float q = 0;

        if (l < 0.5) {
            q = l * (1 + s);
        } else {
            q = (l + s) - (s * l);
        }

        float p = 2 * l - q;

        float r = Math.max(0, hueToRGB(p, q, h + (1.0f / 3.0f)));
        float g = Math.max(0, hueToRGB(p, q, h));
        float b = Math.max(0, hueToRGB(p, q, h - (1.0f / 3.0f)));

        r = Math.min(r, 1.0f);
        g = Math.min(g, 1.0f);
        b = Math.min(b, 1.0f);

        return new RGBColor((int) (255 * r), (int) (255 *  g), (int) (255 * b));
    }

    public List<RGBColor> getColorGradient(int steps) {
        ArrayList<RGBColor> gradient = new ArrayList();
        gradient.addAll(getDarkColorsList(steps / 2));

        gradient.add(this.toRGBColor());

        if (steps % 2 == 0) {
            gradient.addAll(getLightColorsList(steps / 2 - 1));
        } else {
            gradient.addAll(getLightColorsList(steps / 2));
        }
        return gradient;
    }

    private List<RGBColor> getLightColorsList(int steps) {
        ArrayList<RGBColor> gradient = new ArrayList();
        for (int step = 0; step < steps; step++) {
            gradient.add(rotate((float) (step + 1) / steps * -LIGHT_COLORS_MIX_ROTATE).saturate((float) (step + 1) / steps * ((float) this.LIGHT_SATURATION / 100)).mix(WHITE,  ((float) LIGHTEST_AMOUNT / 100) * (float) (step + 1) / steps));
        }
        return gradient;
    }

    private List<RGBColor> getDarkColorsList(int steps) {
        ArrayList<RGBColor> gradient = new ArrayList();

        for (int step = steps - 1; step >= 0; step--) {
            gradient.add(rotate((float) (step + 1) / steps * -DARK_COLORS_MIX_ROTATE).saturate(((float) (step + 1) / steps) * (this.DARK_SATURATION / 100f)).mix(BLACK, ((float) DARKEST_AMOUNT / 100) * (float) (step + 1) / steps));
        }
        return gradient;
    }

    private HSLColor rotate(float degrees) {
        float h1 = (hue + degrees) % 360;
        h1 = h1 < 0 ? 360 + h1 : h1;
        return new HSLColor(h1, saturation, luminance);
    }

    private HSLColor saturate(float ratio) {
        float s1  = Math.min(saturation + saturation * ratio, 100);
        return new HSLColor(hue, s1, luminance);
    }

    private RGBColor mix(RGBColor mixColor, float weight) {
        RGBColor color2 = this.toRGBColor();

        float w = 2 * weight - 1;
        float w1 = (w + 1) / 2.0f;
        float w2 = 1 - w1;

        return new RGBColor(
                (int) (w1 * mixColor.getRed() + w2 * color2.getRed()),
                (int) (w1 * mixColor.getGreen() + w2 * color2.getGreen()),
                (int) (w1 * mixColor.getBlue() + w2 * color2.getBlue()));
    }

    private static float hueToRGB(float p, float q, float h) {
        float hsx = h;
        if (hsx < 0) {
            hsx += 1;
        }

        if (hsx > 1) {
            hsx -= 1;
        }

        if (6 * hsx < 1) {
            return p + ((q - p) * 6 * hsx);
        }

        if (2 * hsx < 1) {
            return  q;
        }

        if (3 * hsx < 2) {
            return p + ((q - p) * 6 * ((2.0f / 3.0f) - hsx));
        }

        return p;
    }

}
