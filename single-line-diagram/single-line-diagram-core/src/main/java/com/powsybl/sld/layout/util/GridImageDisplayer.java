/**
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout.util;

import com.powsybl.sld.layout.pathfinding.Grid;
import com.powsybl.sld.model.coordinate.Point;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.function.Function;

/**
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
public final class GridImageDisplayer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GridImageDisplayer.class);

    private static final Color NOT_AVAILABLE_BLACK = new Color(0, 0, 0);
    private static final Color AVAILABLE_GREEN = new Color(0, 255, 0);

    private GridImageDisplayer() { }

    /**
     * Builds an image representing the state of a grid. Black for not accessible, green for accessible,
     * red for wire, blue around the wire, pink for unknown state
     * @param grid the grid with which to build the image
     * @param outputPath the path where the image will be saved, the actual filename will append the timedate in the ISO-8601 calendar system, such as 2007-12-03T10:15:30
     *                   the complete filename will therefore be {@code outputPath-timedate.png}
     */
    public static void makeImage(Grid grid, String outputPath) {
        makeImage(grid, outputPath, GridImageDisplayer::defaultGridColoring);
    }

    public static void makeImage(Grid grid, String outputPath, Function<Boolean, Color> gridColoring) {
        BufferedImage image = new BufferedImage(grid.getWidth(), grid.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

        for (int y = 0; y < grid.getHeight(); ++y) {
            for (int x = 0; x < grid.getWidth(); ++x) {
                Color color = gridColoring.apply(grid.isAvailable(new Point(x, y)));
                image.setRGB(x, y, color.getRGB());
            }
        }

        try {
            String filename = String.format("%s-%s.png", FilenameUtils.removeExtension(outputPath), LocalDateTime.now());
            File output = new File(filename);
            ImageIO.write(image, "png", output);
            LOGGER.info(" grid display image successfully saved to {}", outputPath);
        } catch (IOException e) {
            LOGGER.error("Could not save grid image to {} for the following reason:", outputPath, e);
        }
    }

    private static Color defaultGridColoring(boolean state) {
        if (state) {
            return AVAILABLE_GREEN;
        } else {
            return NOT_AVAILABLE_BLACK;
        }
    }
}
