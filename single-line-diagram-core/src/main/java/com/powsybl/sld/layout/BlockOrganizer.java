/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.blocks.LegPrimaryBlock;
import com.powsybl.sld.model.cells.Cell;
import com.powsybl.sld.model.cells.ExternCell;
import com.powsybl.sld.model.cells.InternCell;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.powsybl.sld.model.blocks.Block.Extremity.END;
import static com.powsybl.sld.model.blocks.Block.Extremity.START;
import static com.powsybl.sld.model.cells.Cell.CellType.INTERN;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BlockOrganizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockOrganizer.class);

    private final PositionFinder positionFinder;

    private final boolean stack;

    private final boolean handleShunt;

    private final boolean exceptionIfPatternNotHandled;

    private final Map<String, Side> busInfoMap;

    public BlockOrganizer(PositionFinder positionFinder, boolean stack, boolean exceptionIfPatternNotHandled, boolean handleShunt, Map<String, Side> busInfoMap) {
        this.positionFinder = Objects.requireNonNull(positionFinder);
        this.stack = stack;
        this.exceptionIfPatternNotHandled = exceptionIfPatternNotHandled;
        this.handleShunt = handleShunt;
        this.busInfoMap = busInfoMap;
    }

    /**
     * Organize cells into blocks and call the layout resolvers
     */
    public void organize(VoltageLevelGraph graph) {
        LOGGER.info("Organizing graph cells into blocks");
        graph.getBusCellStream().forEach(cell -> {
            CellBlockDecomposer.determineComplexCell(graph, cell, exceptionIfPatternNotHandled);
            if (cell.getType() == INTERN) {
                ((InternCell) cell).organizeBlocks();
            }
        });
        graph.getShuntCellStream().forEach(CellBlockDecomposer::determineShuntCellBlocks);

        if (stack) {
            determineStackableBlocks(graph);
        }

        List<Subsection> subsections = positionFinder.buildLayout(graph, handleShunt);
        //TODO introduce a stackable Blocks check after positionFinder (case of externCell jumping over subSections)

        graph.getExternCellStream().forEach(ExternCell::organizeBlockDirections);

        graph.getCellStream().forEach(Cell::blockSizing);

        new BlockPositionner().determineBlockPositions(graph, subsections, busInfoMap);
    }

    /**
     * Determines blocks connected to busbar that are stackable
     */
    private void determineStackableBlocks(VoltageLevelGraph graph) {
        LOGGER.info("Determining stackable Blocks");
        graph.getBusCells().forEach(cell -> {
            List<LegPrimaryBlock> blocks = cell.getLegPrimaryBlocks();
            for (int i = 0; i < blocks.size(); i++) {
                LegPrimaryBlock block1 = blocks.get(i);
                if (block1.getNodes().size() == 3) {
                    for (int j = i + 1; j < blocks.size(); j++) {
                        LegPrimaryBlock block2 = blocks.get(j);
                        if (block2.getNodes().size() == 3
                                && block1.getExtremityNode(END).equals(block2.getExtremityNode(END))
                                && !block1.getExtremityNode(START).equals(block2.getExtremityNode(START))) {
                            block1.addStackableBlock(block2);
                            block2.addStackableBlock(block1);
                        }
                    }
                }
            }
        });
    }
}
