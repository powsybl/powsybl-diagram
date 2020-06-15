/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BlockOrganizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockOrganizer.class);

    private final PositionFinder positionFinder;

    private final boolean stack;

    public BlockOrganizer() {
        this(new PositionFromExtension(), true);
    }

    public BlockOrganizer(boolean stack) {
        this(new PositionFree(), stack);
    }

    public BlockOrganizer(PositionFinder positionFinder) {
        this(positionFinder, true);
    }

    public BlockOrganizer(PositionFinder positionFinder, boolean stack) {
        this.positionFinder = Objects.requireNonNull(positionFinder);
        this.stack = stack;
    }

    /**
     * Organize cells into blocks and call the layout resolvers
     */
    public void organize(Graph graph) {
        LOGGER.info("Organizing graph cells into blocks");
        graph.getCells().stream()
                .filter(cell -> cell.getType().equals(Cell.CellType.EXTERN)
                        || cell.getType().equals(Cell.CellType.INTERN))
                .forEach(cell -> {
                    new CellBlockDecomposer().determineBlocks(cell);
                    if (cell.getType() == Cell.CellType.INTERN) {
                        ((InternCell) cell).organizeBlocks();
                    }
                });
        graph.getCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.SHUNT)
                .forEach(cell -> new CellBlockDecomposer().determineBlocks(cell));

        if (stack) {
            determineStackableBlocks(graph);
        }
        positionFinder.buildLayout(graph);

        graph.getCells().stream()
                .filter(c -> c.getType() == Cell.CellType.INTERN)
                .forEach(c -> ((InternCell) c).postPositioningSettings());

        SubSections subSections = new SubSections(graph);
        LOGGER.debug("Subsections {}", subSections);

        graph.getCells().stream()
                .filter(cell -> cell instanceof BusCell)
                .forEach(cell -> ((BusCell) cell).blockSizing());
        new BlockPositionner().determineBlockPositions(graph, subSections);
    }

    /**
     * Determines blocks connected to busbar that are stackable
     */
    private void determineStackableBlocks(Graph graph) {
        LOGGER.info("Determining stackable Blocks");
        graph.getBusCells().forEach(cell -> {
            List<LegPrimaryBlock> blocks = cell.getPrimaryLegBlocks();
            for (int i = 0; i < blocks.size(); i++) {
                LegPrimaryBlock block1 = blocks.get(i);
                if (block1.getNodes().size() == 3) {
                    for (int j = i + 1; j < blocks.size(); j++) {
                        LegPrimaryBlock block2 = blocks.get(j);
                        if (block2.getNodes().size() == 3
                                && block1.getExtremityNode(Block.Extremity.END).equals(block2.getExtremityNode(Block.Extremity.END))
                                && !block1.getExtremityNode(Block.Extremity.START).equals(block2.getExtremityNode(Block.Extremity.START))) {
                            block1.addStackableBlock(block2);
                            block2.addStackableBlock(block1);
                        }
                    }
                }
            }
        });
    }
}
