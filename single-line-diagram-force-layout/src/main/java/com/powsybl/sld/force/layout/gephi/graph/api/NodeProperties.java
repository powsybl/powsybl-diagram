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

import com.powsybl.sld.force.layout.gephi.graph.spi.LayoutData;

/**
 * Node properties.
 */
public interface NodeProperties extends ElementProperties {

    /**
     * Returns the x position.
     *
     * @return the x position
     */
    public float x();

    /**
     * Returns the y position.
     *
     * @return the y position
     */
    public float y();

    /**
     * Returns the z position.
     *
     * @return the z position
     */
    public float z();

    /**
     * Returns the size.
     *
     * @return the size
     */
    public float size();

    /**
     * Returns true of this node is fixed (can't be moved).
     *
     * @return true if fixed, false otherwise
     */
    public boolean isFixed();

    /**
     * Returns the layout-specific data, if any.
     *
     * @param <T> the class that implements the <em>LayoutData</em>.
     * @return the layout data
     */
    public <T extends LayoutData> T getLayoutData();

    /**
     * Sets the x position.
     *
     * @param x the x position
     */
    public void setX(float x);

    /**
     * Sets the y position.
     *
     * @param y the y position
     */
    public void setY(float y);

    /**
     * Sets the z position.
     *
     * @param z the z position
     */
    public void setZ(float z);

    /**
     * Sets the size.
     *
     * @param size the size
     */
    public void setSize(float size);

    /**
     * Sets the x and y position.
     *
     * @param x the x position
     * @param y the y position
     */
    public void setPosition(float x, float y);

    /**
     * Sets the x, y and z position.
     *
     * @param x the x position
     * @param y the y position
     * @param z the z position
     */
    public void setPosition(float x, float y, float z);

    /**
     * Sets whether to fix this node (can't move its position)
     *
     * @param fixed true to fix the node, false to unfix
     */
    public void setFixed(boolean fixed);

    /**
     * Sets the layout data.
     *
     * @param layoutData the layout data
     */
    public void setLayoutData(LayoutData layoutData);
}
