/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.*;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.nodes.Node;

import static com.powsybl.sld.model.coordinate.Direction.*;

import java.util.List;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class HorizontalSubstationLayout extends AbstractSubstationLayout {

    private final InfosNbSnakeLinesHorizontal infosNbSnakeLines;

    public HorizontalSubstationLayout(SubstationGraph graph, VoltageLevelLayoutFactory vLayoutFactory) {
        super(graph, vLayoutFactory);
        this.infosNbSnakeLines = InfosNbSnakeLinesHorizontal.create(graph);
    }

    @Override
    protected List<Point> calculatePolylineSnakeLine(LayoutParameters layoutParam, Node node1, Node node2,
                                                     boolean increment) {
        return calculatePolylineSnakeLineForHorizontalLayout(layoutParam, node1, node2, increment, infosNbSnakeLines);
    }

    /**
     * Calculate relative coordinate of voltageLevels in the substation
     */
    @Override
    protected void calculateCoordVoltageLevels(LayoutParameters layoutParameters) {

        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();
        LayoutParameters.Padding voltageLevelPadding = layoutParameters.getVoltageLevelPadding();

        double yVoltageLevels = diagramPadding.getTop() + voltageLevelPadding.getTop();
        double x = diagramPadding.getLeft();
        double substationHeight = 0;

        for (VoltageLevelGraph vlGraph : getGraph().getVoltageLevels()) {

            // Calculate the objects coordinates inside the voltageLevel graph
            Layout vLayout = vLayoutFactory.create(vlGraph);
            vLayout.run(layoutParameters);

            x += voltageLevelPadding.getLeft();
            vlGraph.setCoord(x, yVoltageLevels);

            x += vlGraph.getWidth() + voltageLevelPadding.getRight();
            substationHeight = Math.max(substationHeight, vlGraph.getHeight());
        }

        double substationWidth = x - diagramPadding.getLeft();
        getGraph().setSize(substationWidth, substationHeight);
    }

    @Override
    public void manageSnakeLines(LayoutParameters layoutParameters) {

        getGraph().getVoltageLevels().forEach(g -> manageSnakeLines(g, layoutParameters));
        manageSnakeLines(getGraph(), layoutParameters);

        adaptPaddingToSnakeLines(layoutParameters);
    }

    private void adaptPaddingToSnakeLines(LayoutParameters layoutParameters) {
        double heightSnakeLinesTop = getHeightSnakeLines(layoutParameters, TOP, infosNbSnakeLines);

        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();
        LayoutParameters.Padding voltageLevelPadding = layoutParameters.getVoltageLevelPadding();

        double yVoltageLevels = heightSnakeLinesTop + diagramPadding.getTop() + voltageLevelPadding.getTop();
        double x = diagramPadding.getLeft();

        for (VoltageLevelGraph vlGraph : getGraph().getVoltageLevels()) {
            x += getWidthVerticalSnakeLines(vlGraph.getId(), layoutParameters, infosNbSnakeLines);
            vlGraph.setCoord(x + voltageLevelPadding.getLeft(), yVoltageLevels);
            x += vlGraph.getWidth();
        }

        double substationWidth = x - diagramPadding.getLeft();
        double heightSnakeLinesBottom = getHeightSnakeLines(layoutParameters, BOTTOM,  infosNbSnakeLines);
        double substationHeight = getGraph().getHeight() + heightSnakeLinesTop + heightSnakeLinesBottom;

        getGraph().setSize(substationWidth, substationHeight);

        infosNbSnakeLines.reset();
        getGraph().getVoltageLevels().forEach(g -> manageSnakeLines(g, layoutParameters));
        manageSnakeLines(getGraph(), layoutParameters);
    }

}
