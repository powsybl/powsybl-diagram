/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.layout.positionfromextension.PositionFromExtension;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;

import java.util.Objects;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PositionVoltageLevelLayoutFactory implements VoltageLevelLayoutFactory {

    private final PositionFinder positionFinder;

    private PositionVoltageLevelLayoutFactoryParameters positionVoltageLevelLayoutFactoryParameters = new PositionVoltageLevelLayoutFactoryParameters();

    public PositionVoltageLevelLayoutFactory() {
        this(new PositionFromExtension());
    }

    public PositionVoltageLevelLayoutFactory(PositionVoltageLevelLayoutFactoryParameters positionVoltageLevelLayoutFactoryParameters) {
        this(new PositionFromExtension());
        this.positionVoltageLevelLayoutFactoryParameters = positionVoltageLevelLayoutFactoryParameters;
    }

    public PositionVoltageLevelLayoutFactory(PositionFinder positionFinder) {
        this.positionFinder = Objects.requireNonNull(positionFinder);
    }

    public PositionVoltageLevelLayoutFactory(PositionFinder positionFinder, PositionVoltageLevelLayoutFactoryParameters positionVoltageLevelLayoutFactoryParameters) {
        this.positionFinder = Objects.requireNonNull(positionFinder);
        this.positionVoltageLevelLayoutFactoryParameters = positionVoltageLevelLayoutFactoryParameters;
    }

    @Override
    public Layout create(VoltageLevelGraph graph) {
        // For adapting the graph to the diagram layout
        GraphRefiner graphRefiner = new GraphRefiner(positionVoltageLevelLayoutFactoryParameters.isRemoveUnnecessaryFictitiousNodes(), positionVoltageLevelLayoutFactoryParameters.isSubstituteSingularFictitiousByFeederNode());

        // For cell detection
        ImplicitCellDetector cellDetector = new ImplicitCellDetector(positionVoltageLevelLayoutFactoryParameters.isExceptionIfPatternNotHandled());

        // For building blocks from cells
        BlockOrganizer blockOrganizer = new BlockOrganizer(positionFinder, positionVoltageLevelLayoutFactoryParameters.isFeederStacked(), positionVoltageLevelLayoutFactoryParameters.isExceptionIfPatternNotHandled(), positionVoltageLevelLayoutFactoryParameters.isHandleShunts(), positionVoltageLevelLayoutFactoryParameters.getBusInfoMap());

        return new PositionVoltageLevelLayout(graph, graphRefiner, cellDetector, blockOrganizer);
    }
}
