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
package com.powsybl.sld.force.layout.gephi.graph.api;

import java.awt.Color;

/**
 * Text visual properties.
 */
public interface TextProperties {

    /**
     * Returns the red color component between zero and one.
     *
     * @return the red color component
     */
    public float getR();

    /**
     * Returns the green color component between zero and one.
     *
     * @return the green color component
     */
    public float getG();

    /**
     * Returns the blue color component between zero and one.
     *
     * @return the blue color component
     */
    public float getB();

    /**
     * Returns the RGBA color.
     *
     * @return the color
     */
    public int getRGBa();

    /**
     * Returns the color.
     *
     * @return the color
     */
    public Color getColor();

    /**
     * Returns the alpha (transparency) component between zero and one.
     *
     * @return the alpha
     */
    public float getAlpha();

    /**
     * Returns the size.
     *
     * @return the size
     */
    public float getSize();

    /**
     * Returns true if visible.
     *
     * @return true if visible, false otherwise
     */
    public boolean isVisible();

    /**
     * Returns the text.
     *
     * @return the text
     */
    public String getText();

    /**
     * Returns the text's width.
     *
     * @return the width
     */
    public float getWidth();

    /**
     * Returns the text's height.
     *
     * @return the height
     */
    public float getHeight();

    /**
     * Sets the red color component.
     *
     * @param r the color component, between zero and one
     */
    public void setR(float r);

    /**
     * Sets the green color component.
     *
     * @param g the color component, between zero and one
     */
    public void setG(float g);

    /**
     * Sets the blue color component.
     *
     * @param b the color component, between zero and one
     */
    public void setB(float b);

    /**
     * Sets the alpha (transparency) component.
     *
     * @param a the alpha component, between zero and one
     */
    public void setAlpha(float a);

    /**
     * Sets the color.
     *
     * @param color the color
     */
    public void setColor(Color color);

    /**
     * Sets the size.
     *
     * @param size the size
     */
    public void setSize(float size);

    /**
     * Sets the visibility.
     *
     * @param visible true if visible, false otherwise
     */
    public void setVisible(boolean visible);

    /**
     * Sets the text.
     *
     * @param text the text
     */
    public void setText(String text);

    /**
     * Sets the text's dimensions.
     *
     * @param width width
     * @param height height
     */
    public void setDimensions(float width, float height);
}
