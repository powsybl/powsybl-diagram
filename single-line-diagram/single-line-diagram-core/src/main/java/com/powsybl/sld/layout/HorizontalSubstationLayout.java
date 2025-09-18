/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.Node;
import org.jgrapht.alg.util.Pair;

import java.util.List;

import static com.powsybl.sld.model.coordinate.Direction.BOTTOM;
import static com.powsybl.sld.model.coordinate.Direction.TOP;

/**
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 * @author Slimane Amar {@literal <slimane.amar at rte-france.com>}
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
        return calculatePolylineSnakeLineForHorizontalLayout(layoutParam, nodes, increment, infosNbSnakeLines, yMin, yMax);
    }

    /**
     * Calculate relative coordinate of voltageLevels in the substation
     */
    @Override
    protected void calculateCoordVoltageLevels(LayoutParameters layoutParameters) {

        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();
        LayoutParameters.Padding voltageLevelPadding = layoutParameters.getVoltageLevelPadding();

        double topPadding = diagramPadding.top() + voltageLevelPadding.top();
        double x = diagramPadding.left();
        double substationHeight = 0;

        for (VoltageLevelGraph vlGraph : getGraph().getVoltageLevels()) {

            // Calculate the objects coordinates inside the voltageLevel graph
            Layout vLayout = vLayoutFactory.create(vlGraph);
            vLayout.run(layoutParameters);

            x += voltageLevelPadding.left();
            vlGraph.setCoord(x, computeCoordY(layoutParameters, topPadding, vlGraph));

            x += vlGraph.getWidth() + voltageLevelPadding.right();

            double deltaY = vlGraph.getY() - topPadding;
            substationHeight = Math.max(substationHeight, deltaY + vlGraph.getHeight());
        }

        double substationWidth = x - diagramPadding.left();
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

        double topPadding = heightSnakeLinesTop + diagramPadding.top() + voltageLevelPadding.top();
        double x = diagramPadding.left();

        for (VoltageLevelGraph vlGraph : getGraph().getVoltageLevels()) {
            x += getWidthVerticalSnakeLines(vlGraph.getId(), layoutParameters, infosNbSnakeLines);
            vlGraph.setCoord(x + voltageLevelPadding.left(), computeCoordY(layoutParameters, topPadding, vlGraph));
            x += vlGraph.getWidth();
        }

        double substationWidth = x - diagramPadding.left();
        double heightSnakeLinesBottom = getHeightSnakeLines(layoutParameters, BOTTOM, infosNbSnakeLines);
        double substationHeight = getGraph().getHeight() + heightSnakeLinesTop + heightSnakeLinesBottom;

        getGraph().setSize(substationWidth, substationHeight);

        infosNbSnakeLines.reset();
        getGraph().getVoltageLevels().forEach(g -> manageSnakeLines(g, layoutParameters));
        manageSnakeLines(getGraph(), layoutParameters);
    }

    double computeCoordY(LayoutParameters layoutParameters, double topPadding, VoltageLevelGraph vlGraph) {
        double y;
        // Find maximum voltage level top height
        double maxTopExternCellHeight = getGraph().getVoltageLevelStream().mapToDouble(g -> g.getExternCellHeight(Direction.TOP)).max().orElse(0.0);
        // Get the gap between current voltage level and maximum height one
        double delta = maxTopExternCellHeight - vlGraph.getExternCellHeight(Direction.TOP);
        // Find maximum voltage level maxV
        double maxV = getGraph().getVoltageLevelStream().mapToDouble(VoltageLevelGraph::getMaxV).max().orElse(0.0);
        // Get all busbar section height
        double bbsHeight = layoutParameters.getVerticalSpaceBus() * (maxV - vlGraph.getMaxV());

        switch (layoutParameters.getBusbarsAlignment()) {
            case FIRST -> // Align on First busbar section
                y = topPadding + delta;
            case LAST -> // Align on Last busbar section
                y = topPadding + delta + bbsHeight;
            case MIDDLE -> // Align on middle of all busbar section
                y = topPadding + delta + bbsHeight / 2;
            case NONE -> // None alignment
                y = topPadding;
            default -> y = topPadding;
        }
        return y;
    }
}
