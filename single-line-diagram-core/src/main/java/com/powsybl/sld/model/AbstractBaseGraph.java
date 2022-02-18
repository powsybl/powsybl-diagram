/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.model.coordinate.Point;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public abstract class AbstractBaseGraph extends AbstractGraph implements BaseGraph {

    private static final String EDGE_PREFIX = "EDGE_";

    protected List<BranchEdge> twtEdges = new ArrayList<>();

    protected List<MiddleTwtNode> multiTermNodes = new ArrayList<>();

    AbstractBaseGraph(Graph parentGraph) {
        super(parentGraph);
    }

    @Override
    public List<BranchEdge> getTwtEdges() {
        return new ArrayList<>(twtEdges);
    }

    @Override
    public List<MiddleTwtNode> getMultiTermNodes() {
        return multiTermNodes;
    }

    @Override
    public BranchEdge addTwtEdge(FeederTwtLegNode legNode, MiddleTwtNode twtNode) {
        BranchEdge edge = new BranchEdge(EDGE_PREFIX + legNode.getId(), legNode, twtNode);
        twtNode.addAdjacentEdge(edge);
        twtEdges.add(edge);
        return edge;
    }

    @Override
    public void addMultiTermNode(MiddleTwtNode node) {
        multiTermNodes.add(node);
    }

    protected void writeBranchFields(JsonGenerator generator, boolean includeCoordinates) throws IOException {
        generator.writeArrayFieldStart("multitermNodes");
        for (MiddleTwtNode multitermNode : multiTermNodes) {
            multitermNode.writeJson(generator, includeCoordinates);
        }
        generator.writeEndArray();
        generator.writeArrayFieldStart("twtEdges");
        for (BranchEdge edge : twtEdges) {
            edge.writeJson(generator, includeCoordinates);
        }
        generator.writeEndArray();
        generator.writeArrayFieldStart("lineEdges");
        for (BranchEdge edge : getLineEdges()) {
            edge.writeJson(generator, includeCoordinates);
        }
        generator.writeEndArray();
    }

    public void handleMultiTermsNodeRotation() {
        // node outside any graph
        for (MiddleTwtNode node : getMultiTermNodes()) {
            if (node.getAdjacentEdges().size() == 2) {  // 2 windings transformer
                handle2wtNodeRotation(node);
            } else {  // 3 windings transformer
                handle3wtNodeRotation(node);
            }
        }
    }

    private void handle2wtNodeRotation(Node node) {
        List<Node> adjacentNodes = node.getAdjacentNodes();
        adjacentNodes.sort(Comparator.comparingDouble(Node::getX));
        FeederWithSideNode node1 = (FeederWithSideNode) adjacentNodes.get(0);
        FeederWithSideNode node2 = (FeederWithSideNode) adjacentNodes.get(1);

        List<Edge> edges = node.getAdjacentEdges();
        List<Point> pol1 = ((BranchEdge) edges.get(0)).getSnakeLine();
        List<Point> pol2 = ((BranchEdge) edges.get(1)).getSnakeLine();

        if (pol1.isEmpty() || pol2.isEmpty()) {
            return;
        }

        // get points for the line supporting the svg component
        double x1 = pol1.get(pol1.size() - 2).getX(); // absciss of the first polyline second last point
        double x2 = pol2.get(pol2.size() - 2).getX();  // absciss of the second polyline second last point

        if (x1 == x2) {
            // vertical line supporting the svg component
            FeederWithSideNode nodeWinding1 = node1.getSide() == FeederWithSideNode.Side.ONE ? node1 : node2;
            FeederWithSideNode nodeWinding2 = node1.getSide() == FeederWithSideNode.Side.TWO ? node1 : node2;
            if (nodeWinding2.getY() > nodeWinding1.getY()) {
                // permutation here, because in the svg component library, circle for winding1 is below circle for winding2
                node.setRotationAngle(180.);
            }
        } else {
            // horizontal line supporting the svg component,
            // so we rotate the component by 90 or 270 (the component is vertical in the library)
            if (node1.getSide() == FeederWithSideNode.Side.ONE) {
                // rotation by 90 to get circle for winding1 at the left side
                node.setRotationAngle(90.);
            } else {
                // rotation by 90 to get circle for winding1 at the right side
                node.setRotationAngle(270.);
            }
        }
    }

    private void handle3wtNodeRotation(Node node) {

        List<Edge> edges = node.getAdjacentEdges();
        List<Point> pol1 = ((BranchEdge) edges.get(0)).getSnakeLine();
        List<Point> pol2 = ((BranchEdge) edges.get(1)).getSnakeLine();
        List<Point> pol3 = ((BranchEdge) edges.get(2)).getSnakeLine();
        if (pol1.isEmpty() || pol2.isEmpty() || pol3.isEmpty()) {
            return;
        }

        // get points for the line supporting the svg component
        Point coord1 = pol1.get(pol1.size() - 2); // abscissa of the first polyline second last point
        Point coord2 = pol2.get(pol2.size() - 2);  // abscissa of the second polyline second last point
        Point coord3 = pol3.get(pol3.size() - 2);  // abscissa of the third polyline second last point
        if (coord1.getY() == coord3.getY()) {
            if (coord2.getY() < coord1.getY()) {
                node.setRotationAngle(180.);  // rotation if middle node cell orientation is BOTTOM
            }
        } else {
            if (coord2.getX() == coord1.getX()) {
                node.setRotationAngle(coord3.getX() > coord1.getX() ? 270. : 90.);
            } else if (coord2.getX() == coord3.getX()) {
                node.setRotationAngle(coord1.getX() > coord3.getX() ? 270. : 90.);
            }
        }
    }
}
