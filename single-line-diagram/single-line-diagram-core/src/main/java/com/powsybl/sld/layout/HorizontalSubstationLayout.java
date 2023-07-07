/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.*;
import com.powsybl.sld.model.nodes.Node;
import org.jgrapht.alg.util.Pair;

import java.util.List;
import java.util.Optional;

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
    protected List<Point> calculatePolylineSnakeLine(LayoutParameters layoutParam, Pair<Node, Node> nodes,
                                                     boolean increment) {
        double yMin = getGraph().getVoltageLevels().stream().mapToDouble(VoltageLevelGraph::getY).min().orElse(0.0);
        double yMax = getGraph().getVoltageLevels().stream().mapToDouble(g -> g.getY() + g.getInnerHeight(layoutParam.getVerticalSpaceBus())).max().orElse(0.0);
        return calculatePolylineSnakeLineForHorizontalLayout(getGraph(), layoutParam, nodes, increment, infosNbSnakeLines, yMin, yMax);
    }

    /**
     * Calculate relative coordinate of voltageLevels in the substation
     */
    @Override
    protected void calculateCoordVoltageLevels(LayoutParameters layoutParameters) {

        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();
        LayoutParameters.Padding voltageLevelPadding = layoutParameters.getVoltageLevelPadding();

        double topPadding = diagramPadding.getTop() + voltageLevelPadding.getTop();
        double x = diagramPadding.getLeft();
        double substationHeight = 0;

        for (VoltageLevelGraph vlGraph : getGraph().getVoltageLevels()) {

            // Calculate the objects coordinates inside the voltageLevel graph
            Layout vLayout = vLayoutFactory.create(vlGraph);
            vLayout.run(layoutParameters);

            x += voltageLevelPadding.getLeft();
            vlGraph.setCoord(x, computeCoordY(getGraph(), layoutParameters, topPadding, vlGraph));

            x += vlGraph.getWidth() + voltageLevelPadding.getRight();

            double deltaY = vlGraph.getY() - topPadding;
            substationHeight = Math.max(substationHeight, deltaY + vlGraph.getHeight());
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
        adaptPaddingToSnakeLinesForHorizontal(getGraph(), layoutParameters);

        getGraph().getVoltageLevels().forEach(g -> manageSnakeLines(g, layoutParameters));
        manageSnakeLines(getGraph(), layoutParameters);
    }

    @Override
    public Optional<InfosNbSnakeLinesHorizontal> getInfosNbSnakeLinesHorizontal() {
        return Optional.of(infosNbSnakeLines);
    }
}
