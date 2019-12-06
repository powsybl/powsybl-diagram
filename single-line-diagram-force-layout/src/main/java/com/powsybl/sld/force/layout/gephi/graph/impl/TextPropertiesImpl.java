/*
 * Copyright 2012-2013 Gephi Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.powsybl.sld.force.layout.gephi.graph.impl;

import com.powsybl.sld.force.layout.gephi.graph.api.TextProperties;

import java.awt.Color;

public class TextPropertiesImpl implements TextProperties {

    protected boolean visible;
    protected int rgba;
    protected float size;
    protected String text;
    protected float width;
    protected float height;

    public TextPropertiesImpl() {
        this.rgba = 255 << 24; // Alpha set to 1
        this.size = 1f;
        this.visible = true;
    }

    @Override
    public float getR() {
        return ((rgba >> 16) & 0xFF) / 255f;
    }

    @Override
    public float getG() {
        return ((rgba >> 8) & 0xFF) / 255f;
    }

    @Override
    public float getB() {
        return (rgba & 0xFF) / 255f;
    }

    @Override
    public float getAlpha() {
        return ((rgba >> 24) & 0xFF) / 255f;
    }

    @Override
    public int getRGBa() {
        return rgba;
    }

    @Override
    public Color getColor() {
        return new Color(rgba, true);
    }

    @Override
    public float getSize() {
        return size;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public void setR(float r) {
        rgba = (rgba & 0xFF00FFFF) | (((int) (r * 255f)) << 16);
    }

    @Override
    public void setG(float g) {
        rgba = (rgba & 0xFFFF00FF) | ((int) (g * 255f)) << 8;
    }

    @Override
    public void setB(float b) {
        rgba = (rgba & 0xFFFFFF00) | ((int) (b * 255f));
    }

    @Override
    public void setAlpha(float a) {
        rgba = (rgba & 0xFFFFFF) | ((int) (a * 255f)) << 24;
    }

    @Override
    public void setColor(Color color) {
        this.rgba = (color.getAlpha() << 24) | color.getRGB();
    }

    @Override
    public void setSize(float size) {
        this.size = size;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void setDimensions(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public int deepHashCode() {
        int hash = 7;
        hash = 97 * hash + (this.visible ? 1 : 0);
        hash = 97 * hash + this.rgba;
        hash = 97 * hash + Float.floatToIntBits(this.size);
        hash = 97 * hash + Float.floatToIntBits(this.width);
        hash = 97 * hash + Float.floatToIntBits(this.height);
        hash = 97 * hash + (this.text != null ? this.text.hashCode() : 0);
        return hash;
    }

    public boolean deepEquals(TextPropertiesImpl obj) {
        if (obj == null) {
            return false;
        }
        if (this.visible != obj.visible) {
            return false;
        }
        if (this.rgba != obj.rgba) {
            return false;
        }
        if (Float.floatToIntBits(this.size) != Float.floatToIntBits(obj.size)) {
            return false;
        }
        if (Float.floatToIntBits(this.width) != Float.floatToIntBits(obj.width)) {
            return false;
        }
        if (Float.floatToIntBits(this.height) != Float.floatToIntBits(obj.height)) {
            return false;
        }
        if ((this.text == null) ? (obj.text != null) : !this.text.equals(obj.text)) {
            return false;
        }
        return true;
    }
}
