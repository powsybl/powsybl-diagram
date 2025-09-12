/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.layout.pathfinding;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.util.function.Function;

/**
 * This is a utility class used to display the state of the AvailabilityGrid using colors for easier visual understanding
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public final class AvailabilityGridImageDisplayer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AvailabilityGridImageDisplayer.class);

    private static final Color NOT_AVAILABLE_BLACK = new Color(0, 0, 0);
    private static final Color WIRE_RED = new Color(255, 0, 0);
    private static final Color AROUND_WIRE_BLUE = new Color(30, 144, 255);
    private static final Color AVAILABLE_GREEN = new Color(0, 255, 0);
    private static final Color UNKNOWN_PINK = new Color(255, 15, 192);

    private AvailabilityGridImageDisplayer() { }

    /**
     * Builds an image representing the state of availability grid. Black for not accessible, green for accessible,
     * red for wire, blue around the wire, pink for unknown state
     * @param availabilityGrid the grid with which to build the image
     * @param outputPath the path where the image will be saved, the actual filename will append the timedate in the ISO-8601 calendar system, such as 2007-12-03T10:15:30
     *                   the complete filename will therefore be {@code outputPath-timedate.png}
     */
    public static void makeAvailabilityImage(AvailabilityGrid availabilityGrid, String outputPath) {
        makeAvailabilityImage(availabilityGrid, outputPath, AvailabilityGridImageDisplayer::defaultGridColoring);
    }

    public static void makeAvailabilityImage(AvailabilityGrid availabilityGrid, String outputPath, Function<Byte, Color> gridColoring) {
        byte[][] grid = availabilityGrid.getGrid();
        int height = grid.length;
        int width = grid[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                Color color = gridColoring.apply(grid[y][x]);
                image.setRGB(x, y, color.getRGB());
            }
        }

        try {
            String filename = String.format("%s-%s.png", FilenameUtils.removeExtension(outputPath), LocalDateTime.now());
            File output = new File(filename);
            ImageIO.write(image, "png", output);
            LOGGER.info("Availability grid display image successfully saved to {}", outputPath);
        } catch (Exception e) {
            LOGGER.error("Could not save availability grid image to {} for the following reason:", outputPath, e);
        }
    }

    private static Color defaultGridColoring(byte state) {
        return switch (state) {
            case AvailabilityGrid.NOT_AVAILABLE -> NOT_AVAILABLE_BLACK;
            case AvailabilityGrid.WIRE -> WIRE_RED;
            case AvailabilityGrid.AROUND_WIRE -> AROUND_WIRE_BLUE;
            case AvailabilityGrid.AVAILABLE -> AVAILABLE_GREEN;
            default -> UNKNOWN_PINK;
        };
    }
}

