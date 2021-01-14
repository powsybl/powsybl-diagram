/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.Coord;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.SubstationGraph;

import java.util.Objects;

import static com.powsybl.sld.model.Coord.Dimension.X;
import static com.powsybl.sld.model.Coord.Dimension.Y;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public abstract class AbstractSubstationLayout extends AbstractLayout implements SubstationLayout {

    protected VoltageLevelLayoutFactory vLayoutFactory;

    public AbstractSubstationLayout(SubstationGraph graph, VoltageLevelLayoutFactory vLayoutFactory) {
        super(graph);
        this.vLayoutFactory = Objects.requireNonNull(vLayoutFactory);
    }

    public SubstationGraph getGrah() {
        return (SubstationGraph) graph;
    }

    @Override
    public void run(LayoutParameters layoutParameters) {
        // Calculate all the coordinates for the voltageLevel graphs in the substation graph
        double graphX = layoutParameters.getHorizontalSubstationPadding();
        double graphY = layoutParameters.getVerticalSubstationPadding();

        InfosNbSnakeLines infosNbSnakeLines = InfosNbSnakeLines.create(getGrah());

        for (Graph vlGraph : ((SubstationGraph) graph).getNodes()) {
            vlGraph.setX(graphX);
            vlGraph.setY(graphY);

            // Calculate the objects coordinates inside the voltageLevel graph
            VoltageLevelLayout vLayout = vLayoutFactory.create(vlGraph);
            vLayout.run(layoutParameters);

            // Calculate the global coordinate of the voltageLevel graph
            Coord posVLGraph = calculateCoordVoltageLevel(layoutParameters, vlGraph);

            graphX += posVLGraph.get(X) + getHorizontalSubstationPadding(layoutParameters);
            graphY += posVLGraph.get(Y) + getVerticalSubstationPadding(layoutParameters);
        }

        // Calculate all the coordinates for the middle nodes and the snake lines between the voltageLevel graphs
        manageSnakeLines(layoutParameters);
    }

    protected abstract Coord calculateCoordVoltageLevel(LayoutParameters layoutParameters, Graph vlGraph);

    protected abstract double getHorizontalSubstationPadding(LayoutParameters layoutParameters);

    protected abstract double getVerticalSubstationPadding(LayoutParameters layoutParameters);

    @Override
    protected void manageSnakeLines(LayoutParameters layoutParameters) {
        InfosNbSnakeLines infosNbSnakeLines = InfosNbSnakeLines.create(getGrah());

        getGrah().getNodes().forEach(g -> manageSnakeLines(g, layoutParameters, infosNbSnakeLines));

        manageSnakeLines(graph, layoutParameters, infosNbSnakeLines);
    }

}
