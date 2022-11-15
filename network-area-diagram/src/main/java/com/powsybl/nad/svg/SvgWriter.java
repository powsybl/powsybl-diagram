/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.nad.model.*;
import org.apache.commons.io.output.WriterOutputStream;
import org.jgrapht.alg.util.Pair;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class SvgWriter {

    private static final String INDENT = "    ";
    public static final String NAMESPACE_URI = "http://www.w3.org/2000/svg";
    private static final String SVG_ROOT_ELEMENT_NAME = "svg";
    private static final String STYLE_ELEMENT_NAME = "style";
    private static final String DEFS_ELEMENT_NAME = "defs";
    private static final String GROUP_ELEMENT_NAME = "g";
    private static final String POLYLINE_ELEMENT_NAME = "polyline";
    private static final String PATH_ELEMENT_NAME = "path";
    private static final String CIRCLE_ELEMENT_NAME = "circle";
    private static final String TEXT_ELEMENT_NAME = "text";
    private static final String TSPAN_ELEMENT_NAME = "tspan";
    private static final String ID_ATTRIBUTE = "id";
    private static final String WIDTH_ATTRIBUTE = "width";
    private static final String HEIGHT_ATTRIBUTE = "height";
    private static final String VIEW_BOX_ATTRIBUTE = "viewBox";
    private static final String DESCRIPTION_ATTRIBUTE = "desc";
    private static final String CLASS_ATTRIBUTE = "class";
    private static final String TRANSFORM_ATTRIBUTE = "transform";
    private static final String CIRCLE_RADIUS_ATTRIBUTE = "r";
    private static final String PATH_D_ATTRIBUTE = "d";
    private static final String X_ATTRIBUTE = "x";
    private static final String Y_ATTRIBUTE = "y";
    private static final String DY_ATTRIBUTE = "dy";
    private static final String POINTS_ATTRIBUTE = "points";
    private static final String FILTER_ELEMENT_NAME = "filter";
    private static final String FE_FLOOD_ELEMENT_NAME = "feFlood";
    private static final String FE_COMPOSITE_ELEMENT_NAME = "feComposite";
    private static final String FE_IN_ATTRIBUTE = "in";
    private static final String FE_OPERATOR_ATTRIBUTE = "operator";
    public static final String TEXT_BG_FILTER_ID = "textBgFilter";

    private final SvgParameters svgParameters;
    private final StyleProvider styleProvider;
    private final LabelProvider labelProvider;
    private final EdgeRendering edgeRendering;

    public SvgWriter(SvgParameters svgParameters, StyleProvider styleProvider, LabelProvider labelProvider) {
        this.svgParameters = Objects.requireNonNull(svgParameters);
        this.styleProvider = Objects.requireNonNull(styleProvider);
        this.labelProvider = Objects.requireNonNull(labelProvider);
        this.edgeRendering = new DefaultEdgeRendering();
    }

    public void writeSvg(Graph graph, Path svgFile) {
        Objects.requireNonNull(svgFile);
        Path dir = svgFile.toAbsolutePath().getParent();
        String svgFileName = svgFile.getFileName().toString();
        if (!svgFileName.endsWith(".svg")) {
            svgFileName = svgFileName + ".svg";
        }
        try (OutputStream svgOs = new BufferedOutputStream(Files.newOutputStream(dir.resolve(svgFileName)))) {
            writeSvg(graph, svgOs);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeSvg(Graph graph, Writer svgWriter) {
        try (WriterOutputStream svgOs = new WriterOutputStream(svgWriter, StandardCharsets.UTF_8)) {
            writeSvg(graph, svgOs);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeSvg(Graph graph, OutputStream svgOs) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(svgOs);

        // Edge coordinates need to be computed first, based on svg parameters
        edgeRendering.run(graph, svgParameters);

        try {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, INDENT, svgOs);
            addSvgRoot(graph, writer);
            addStyle(writer);
            addMetadata(graph, writer);
            addDefs(writer);
            drawVoltageLevelNodes(graph, writer);
            drawBranchEdges(graph, writer);
            drawThreeWtEdges(graph, writer);
            drawThreeWtNodes(graph, writer);
            drawTextEdges(graph, writer);
            drawTextNodes(graph, writer);
            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private void drawBranchEdges(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.BRANCH_EDGES_CLASS);
        for (BranchEdge edge : graph.getBranchEdges()) {
            writer.writeStartElement(GROUP_ELEMENT_NAME);
            writer.writeAttribute(ID_ATTRIBUTE, getPrefixedId(edge.getDiagramId()));
            addStylesIfAny(writer, styleProvider.getEdgeStyleClasses(edge));
            insertName(writer, edge::getName);

            drawHalfEdge(graph, writer, edge, BranchEdge.Side.ONE);
            drawHalfEdge(graph, writer, edge, BranchEdge.Side.TWO);

            if (edge.getType().equals(BranchEdge.HVDC_LINE_EDGE)) {
                drawConverterStation(writer, edge);
            }

            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void drawConverterStation(XMLStreamWriter writer, BranchEdge edge) throws XMLStreamException {
        writer.writeEmptyElement(POLYLINE_ELEMENT_NAME);
        List<Point> line1 = edge.getPoints(BranchEdge.Side.ONE);
        List<Point> line2 = edge.getPoints(BranchEdge.Side.TWO);
        List<Point> points = new ArrayList<>(2);
        double halfWidth = svgParameters.getConverterStationWidth() / 2;
        if (line1.size() > 2) {
            points.add(line1.get(2).atDistance(halfWidth, line1.get(1)));
            points.add(line2.get(2).atDistance(halfWidth, line2.get(1)));
        } else {
            points.add(line1.get(1).atDistance(halfWidth, line1.get(0)));
            points.add(line2.get(1).atDistance(halfWidth, line2.get(0)));
        }
        String lineFormatted = points.stream()
                .map(point -> getFormattedValue(point.getX()) + "," + getFormattedValue(point.getY()))
                .collect(Collectors.joining(" "));
        writer.writeAttribute(POINTS_ATTRIBUTE, lineFormatted);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.HVDC_CLASS);
    }

    private void drawThreeWtEdges(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        List<ThreeWtEdge> threeWtEdges = graph.getThreeWtEdges();
        if (threeWtEdges.isEmpty()) {
            return;
        }

        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.THREE_WT_EDGES_CLASS);
        for (ThreeWtEdge edge : threeWtEdges) {
            drawThreeWtEdge(graph, writer, edge);
        }
        writer.writeEndElement();
    }

    private void drawHalfEdge(Graph graph, XMLStreamWriter writer, BranchEdge edge, BranchEdge.Side side) throws XMLStreamException {
        // the half edge is only drawn if visible, but if the edge is a TwoWtEdge, the transformer is still drawn
        if (!edge.isVisible(side) && !(edge.getType().equals(BranchEdge.TWO_WT_EDGE))) {
            return;
        }
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        addStylesIfAny(writer, styleProvider.getSideEdgeStyleClasses(edge, side));
        if (edge.isVisible(side)) {
            if (!graph.isLoop(edge)) {
                writer.writeEmptyElement(POLYLINE_ELEMENT_NAME);
                writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.EDGE_PATH_CLASS);
                writer.writeAttribute(POINTS_ATTRIBUTE, getPolylinePointsString(edge, side));
                drawBranchEdgeInfo(graph, writer, edge, side, labelProvider.getEdgeInfos(graph, edge, side));
            } else {
                writer.writeEmptyElement(PATH_ELEMENT_NAME);
                writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.EDGE_PATH_CLASS);
                writer.writeAttribute(PATH_D_ATTRIBUTE, getLoopPathString(edge, side));
                drawLoopEdgeInfo(writer, edge, side, labelProvider.getEdgeInfos(graph, edge, side));
            }
        }
        if (edge.getType().equals(BranchEdge.TWO_WT_EDGE)) {
            draw2WtWinding(writer, edge.getPoints(side));
        }
        writer.writeEndElement();
    }

    private String getPolylinePointsString(BranchEdge edge, BranchEdge.Side side) {
        return getPolylinePointsString(edge.getPoints(side));
    }

    private String getPolylinePointsString(ThreeWtEdge edge) {
        return getPolylinePointsString(edge.getPoints());
    }

    private String getPolylinePointsString(List<Point> points) {
        return points.stream()
                .map(point -> getFormattedValue(point.getX()) + "," + getFormattedValue(point.getY()))
                .collect(Collectors.joining(" "));
    }

    private String getLoopPathString(BranchEdge edge, BranchEdge.Side side) {
        Object[] points = edge.getPoints(side).stream().flatMap(p -> Stream.of(p.getX(), p.getY())).toArray();
        return String.format(Locale.US, "M%.2f,%.2f L%.2f,%.2f C%.2f,%.2f %.2f,%.2f %.2f,%.2f", points);
    }

    private void drawThreeWtEdge(Graph graph, XMLStreamWriter writer, ThreeWtEdge edge) throws XMLStreamException {
        if (!edge.isVisible()) {
            return;
        }
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(ID_ATTRIBUTE, getPrefixedId(edge.getDiagramId()));
        addStylesIfAny(writer, styleProvider.getEdgeStyleClasses(edge));
        insertName(writer, edge::getName);
        writer.writeEmptyElement(POLYLINE_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.EDGE_PATH_CLASS);
        writer.writeAttribute(POINTS_ATTRIBUTE, getPolylinePointsString(edge));
        drawThreeWtEdgeInfo(graph, writer, edge, labelProvider.getEdgeInfos(graph, edge));
        writer.writeEndElement();
    }

    private void drawThreeWtNodes(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        List<ThreeWtNode> threeWtNodes = graph.getThreeWtNodesStream().collect(Collectors.toList());
        if (threeWtNodes.isEmpty()) {
            return;
        }

        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.THREE_WT_NODES_CLASS);
        for (ThreeWtNode threeWtNode : threeWtNodes) {
            writer.writeStartElement(GROUP_ELEMENT_NAME);
            addStylesIfAny(writer, styleProvider.getNodeStyleClasses(threeWtNode));
            List<ThreeWtEdge> edges = graph.getThreeWtEdgeStream(threeWtNode).collect(Collectors.toList());
            for (ThreeWtEdge edge : edges) {
                draw3WtWinding(edge, threeWtNode, writer);
            }
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void draw3WtWinding(ThreeWtEdge edge, ThreeWtNode threeWtNode, XMLStreamWriter writer) throws XMLStreamException {
        List<String> styles = styleProvider.getThreeWtNodeStyle(threeWtNode, edge.getSide());
        styles.add(StyleProvider.WINDING_CLASS);
        double radius = svgParameters.getTransformerCircleRadius();
        Point circleCenter = edge.getPoints().get(1).atDistance(radius, threeWtNode.getPosition());
        writer.writeEmptyElement(CIRCLE_ELEMENT_NAME);
        addStylesIfAny(writer, styles);
        writer.writeAttribute("cx", getFormattedValue(circleCenter.getX()));
        writer.writeAttribute("cy", getFormattedValue(circleCenter.getY()));
        writer.writeAttribute(CIRCLE_RADIUS_ATTRIBUTE, getFormattedValue(svgParameters.getTransformerCircleRadius()));
    }

    private void drawLoopEdgeInfo(XMLStreamWriter writer, BranchEdge edge, BranchEdge.Side side, List<EdgeInfo> edgeInfos) throws XMLStreamException {
        drawEdgeInfo(writer, edgeInfos, edge.getPoints(side).get(1), edge.getEdgeStartAngle(side));
    }

    private void drawBranchEdgeInfo(Graph graph, XMLStreamWriter writer, BranchEdge edge, BranchEdge.Side side, List<EdgeInfo> edgeInfos) throws XMLStreamException {
        VoltageLevelNode vlNode = graph.getVoltageLevelNode(edge, side);
        BusNode busNode = graph.getBusGraphNode(edge, side);
        drawEdgeInfo(writer, edgeInfos, getArrowCenter(vlNode, busNode, edge.getPoints(side)), edge.getEdgeEndAngle(side));
    }

    private void drawThreeWtEdgeInfo(Graph graph, XMLStreamWriter writer, ThreeWtEdge edge, List<EdgeInfo> edgeInfos) throws XMLStreamException {
        VoltageLevelNode vlNode = graph.getVoltageLevelNode(edge);
        BusNode busNode = graph.getBusGraphNode(edge);
        drawEdgeInfo(writer, edgeInfos, getArrowCenter(vlNode, busNode, edge.getPoints()), edge.getEdgeAngle());
    }

    private void drawEdgeInfo(XMLStreamWriter writer, List<EdgeInfo> edgeInfos, Point infoCenter, double edgeAngle) throws XMLStreamException {
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.EDGE_INFOS_CLASS);
        writer.writeAttribute(TRANSFORM_ATTRIBUTE, getTranslateString(infoCenter));
        for (EdgeInfo info : edgeInfos) {
            writer.writeStartElement(GROUP_ELEMENT_NAME);
            addStylesIfAny(writer, styleProvider.getEdgeInfoStyles(info));
            drawInAndOutArrows(writer, edgeAngle);
            Optional<String> externalLabel = info.getExternalLabel();
            if (externalLabel.isPresent()) {
                drawLabel(writer, externalLabel.get(), edgeAngle, true);
            }
            Optional<String> internalLabel = info.getInternalLabel();
            if (internalLabel.isPresent()) {
                drawLabel(writer, internalLabel.get(), edgeAngle, false);
            }
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void drawInAndOutArrows(XMLStreamWriter writer, double edgeAngle) throws XMLStreamException {
        double rotationAngle = edgeAngle + (edgeAngle > Math.PI / 2 ? -3 * Math.PI / 2 : Math.PI / 2);
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(TRANSFORM_ATTRIBUTE, getRotateString(rotationAngle));
        writer.writeEmptyElement(PATH_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.ARROW_IN_CLASS);
        writer.writeAttribute(TRANSFORM_ATTRIBUTE, getScaleString(svgParameters.getArrowHeight()));
        writer.writeAttribute(PATH_D_ATTRIBUTE, labelProvider.getArrowPathDIn());
        writer.writeEmptyElement(PATH_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.ARROW_OUT_CLASS);
        writer.writeAttribute(TRANSFORM_ATTRIBUTE, getScaleString(svgParameters.getArrowHeight()));
        writer.writeAttribute(PATH_D_ATTRIBUTE, labelProvider.getArrowPathDOut());
        writer.writeEndElement();
    }

    private void drawLabel(XMLStreamWriter writer, String label, double edgeAngle, boolean externalLabel) throws XMLStreamException {
        if (svgParameters.isEdgeInfoAlongEdge()) {
            drawLabelAlongEdge(writer, label, edgeAngle, externalLabel);
        } else {
            drawLabelPerpendicularToEdge(writer, label, edgeAngle, externalLabel);
        }
    }

    private void drawLabelAlongEdge(XMLStreamWriter writer, String label, double edgeAngle, boolean externalLabel) throws XMLStreamException {
        boolean textFlipped = Math.cos(edgeAngle) < 0;
        String style = externalLabel == textFlipped ? "text-anchor:end" : null;
        double textAngle = textFlipped ? edgeAngle - Math.PI : edgeAngle;
        double shift = svgParameters.getArrowLabelShift() * (externalLabel ? 1 : -1);
        drawLabel(writer, label, textFlipped ? -shift : shift, style, textAngle, X_ATTRIBUTE);
    }

    private void drawLabelPerpendicularToEdge(XMLStreamWriter writer, String label, double edgeAngle, boolean externalLabel) throws XMLStreamException {
        boolean textFlipped = Math.sin(edgeAngle) > 0;
        double textAngle = textFlipped ? -Math.PI / 2 + edgeAngle : Math.PI / 2 + edgeAngle;
        double shift = svgParameters.getArrowLabelShift();
        double shiftAdjusted = externalLabel == textFlipped ? shift * 1.15 : -shift; // to have a nice compact rendering, shift needs to be adjusted, because of dominant-baseline:middle (text is expected to be a number, hence not below the line)
        drawLabel(writer, label, shiftAdjusted, "text-anchor:middle", textAngle, Y_ATTRIBUTE);
    }

    private void drawLabel(XMLStreamWriter writer, String label, double shift, String style, double textAngle, String shiftAxis) throws XMLStreamException {
        writer.writeStartElement(TEXT_ELEMENT_NAME);
        writer.writeAttribute(TRANSFORM_ATTRIBUTE, getRotateString(textAngle));
        writer.writeAttribute(shiftAxis, getFormattedValue(shift));
        if (style != null) {
            writer.writeAttribute(STYLE_ELEMENT_NAME, style);
        }
        writer.writeCharacters(label);
        writer.writeEndElement();
    }

    private String getRotateString(double angleRad) {
        return "rotate(" + getFormattedValue(Math.toDegrees(angleRad)) + ")";
    }

    private String getScaleString(double scale) {
        return "scale(" + getFormattedValue(scale) + ")";
    }

    private Point getArrowCenter(VoltageLevelNode vlNode, BusNode busNode, List<Point> line) {
        double shift = svgParameters.getArrowShift();
        if (line.size() == 2) { // straight line; in case of a forking line it is the middle point which is the starting point
            double nodeOuterRadius = getVoltageLevelCircleRadius(vlNode);
            double busAnnulusOuterRadius = getBusAnnulusOuterRadius(busNode, vlNode, svgParameters);
            shift += nodeOuterRadius - busAnnulusOuterRadius;
        }
        return line.get(line.size() - 2).atDistance(shift, line.get(line.size() - 1));
    }

    private void draw2WtWinding(XMLStreamWriter writer, List<Point> half) throws XMLStreamException {
        writer.writeEmptyElement(CIRCLE_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.WINDING_CLASS);
        Point point1 = half.get(half.size() - 1); // point near 2wt
        Point point2 = half.get(half.size() - 2); // point near voltage level, or control point for loops
        double radius = svgParameters.getTransformerCircleRadius();
        Point circleCenter = point1.atDistance(-radius, point2);
        writer.writeAttribute("cx", getFormattedValue(circleCenter.getX()));
        writer.writeAttribute("cy", getFormattedValue(circleCenter.getY()));
        writer.writeAttribute(CIRCLE_RADIUS_ATTRIBUTE, getFormattedValue(radius));
    }

    private void drawVoltageLevelNodes(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.VOLTAGE_LEVEL_NODES_CLASS);
        for (VoltageLevelNode vlNode : graph.getVoltageLevelNodesStream().filter(VoltageLevelNode::isVisible).collect(Collectors.toList())) {
            writer.writeStartElement(GROUP_ELEMENT_NAME);
            writer.writeAttribute(TRANSFORM_ATTRIBUTE, getTranslateString(vlNode));
            drawNode(graph, writer, vlNode);
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void drawTextNodes(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.TEXT_NODES_CLASS);
        for (Pair<VoltageLevelNode, TextNode> nodePair : graph.getVoltageLevelTextPairs()) {
            writeTextNode(writer, nodePair.getSecond(), labelProvider.getVoltageLevelDescription(nodePair.getFirst()));
        }
        writer.writeEndElement();
    }

    private String getTranslateString(Node node) {
        return getTranslateString(node.getPosition());
    }

    private String getTranslateString(Point point) {
        return getTranslateString(point.getX(), point.getY());
    }

    private String getTranslateString(double x, double y) {
        return "translate(" + getFormattedValue(x) + "," + getFormattedValue(y) + ")";
    }

    private void writeTextNode(XMLStreamWriter writer, TextNode textNode, List<String> content) throws XMLStreamException {
        if (textNode == null) {
            return;
        }
        writer.writeStartElement(TEXT_ELEMENT_NAME);
        if (svgParameters.isTextNodeBackground()) {
            writer.writeAttribute(FILTER_ELEMENT_NAME, "url(#" + TEXT_BG_FILTER_ID + ")");
        }
        writer.writeAttribute(Y_ATTRIBUTE, getFormattedValue(textNode.getY()));
        if (content.size() == 1) {
            writer.writeAttribute(X_ATTRIBUTE, getFormattedValue(textNode.getX()));
            writer.writeCharacters(content.get(0));
        } else {
            for (int i = 0; i < content.size(); i++) {
                String line = content.get(i);
                writer.writeStartElement(TSPAN_ELEMENT_NAME);
                writer.writeAttribute(X_ATTRIBUTE, getFormattedValue(textNode.getX()));
                if (i > 0) {
                    writer.writeAttribute(DY_ATTRIBUTE, "1.1em");
                }
                writer.writeCharacters(line);
                writer.writeEndElement();
            }
        }
        writer.writeEndElement();
    }

    private void drawNode(Graph graph, XMLStreamWriter writer, VoltageLevelNode vlNode) throws XMLStreamException {
        writer.writeAttribute(ID_ATTRIBUTE, getPrefixedId(vlNode.getDiagramId()));
        addStylesIfAny(writer, styleProvider.getNodeStyleClasses(vlNode));
        insertName(writer, vlNode::getName);

        double nodeOuterRadius = getVoltageLevelCircleRadius(vlNode);

        if (vlNode.hasUnknownBusNode()) {
            writer.writeEmptyElement(CIRCLE_ELEMENT_NAME);
            addStylesIfAny(writer, styleProvider.getNodeStyleClasses(BusNode.UNKNOWN));
            writer.writeAttribute(CIRCLE_RADIUS_ATTRIBUTE, getFormattedValue(nodeOuterRadius + svgParameters.getUnknownBusNodeExtraRadius()));
        }

        List<Edge> traversingBusEdges = new ArrayList<>();

        for (BusNode busNode : vlNode.getBusNodes()) {
            double busInnerRadius = getBusAnnulusInnerRadius(busNode, vlNode, svgParameters);
            double busOuterRadius = getBusAnnulusOuterRadius(busNode, vlNode, svgParameters);
            if (busInnerRadius == 0) {
                writer.writeEmptyElement(CIRCLE_ELEMENT_NAME);
                writer.writeAttribute(CIRCLE_RADIUS_ATTRIBUTE, getFormattedValue(busOuterRadius));
            } else {
                writer.writeEmptyElement(PATH_ELEMENT_NAME);
                writer.writeAttribute(PATH_D_ATTRIBUTE, getFragmentedAnnulusPath(busInnerRadius, busOuterRadius, traversingBusEdges, graph, vlNode, busNode));
            }
            writer.writeAttribute(ID_ATTRIBUTE, getPrefixedId(busNode.getDiagramId()));

            List<String> nodeStyleClasses = styleProvider.getNodeStyleClasses(busNode);
            nodeStyleClasses.add(StyleProvider.BUSNODE_CLASS);
            addStylesIfAny(writer, nodeStyleClasses);

            traversingBusEdges.addAll(graph.getBusEdges(busNode));
        }
    }

    private String getFragmentedAnnulusPath(double innerRadius, double outerRadius, List<Edge> traversingBusEdges, Graph graph, VoltageLevelNode vlNode, BusNode busNode) {
        if (traversingBusEdges.isEmpty()) {
            String path = "M" + getCirclePath(outerRadius, 0, Math.PI, true)
                    + " M" + getCirclePath(outerRadius, Math.PI, 0, true);
            if (innerRadius > 0) { // going the other way around (counter-clockwise) to subtract the inner circle
                path += "M" + getCirclePath(innerRadius, 0, Math.PI, false)
                        + "M" + getCirclePath(innerRadius, Math.PI, 0, false);
            }
            return path;
        }

        List<Double> angles = createSortedTraversingAnglesList(traversingBusEdges, graph, vlNode, busNode);

        // adding first angle to close the circle annulus, and adding 360° to keep the list ordered
        angles.add(angles.get(0) + 2 * Math.PI);

        double halfWidth = svgParameters.getNodeHollowWidth() / 2;
        double deltaAngle0 = halfWidth / outerRadius;
        double deltaAngle1 = halfWidth / innerRadius;

        StringBuilder path = new StringBuilder();
        for (int i = 0; i < angles.size() - 1; i++) {
            double outerArcStart = angles.get(i) + deltaAngle0;
            double outerArcEnd = angles.get(i + 1) - deltaAngle0;
            double innerArcStart = angles.get(i + 1) - deltaAngle1;
            double innerArcEnd = angles.get(i) + deltaAngle1;
            if (outerArcEnd > outerArcStart && innerArcEnd < innerArcStart) {
                path.append("M").append(getCirclePath(outerRadius, outerArcStart, outerArcEnd, true))
                        .append(" L").append(getCirclePath(innerRadius, innerArcStart, innerArcEnd, false))
                        .append(" Z ");
            }
        }

        return path.toString();
    }

    private List<Double> createSortedTraversingAnglesList(List<Edge> traversingBusEdges, Graph graph, VoltageLevelNode vlNode, BusNode busNode) {
        List<Double> angles = new ArrayList<>(traversingBusEdges.size());
        for (Edge edge : traversingBusEdges) {
            Node node1 = graph.getNode1(edge);
            Node node2 = graph.getNode2(edge);
            if (node1 == node2) {
                // For looping edges we need to consider the two angles
                if (isBusNodeDrawn(graph.getBusGraphNode1(edge), busNode)) {
                    angles.add(getEdgeStartAngle(edge, BranchEdge.Side.ONE));
                }
                if (isBusNodeDrawn(graph.getBusGraphNode2(edge), busNode)) {
                    angles.add(getEdgeStartAngle(edge, BranchEdge.Side.TWO));
                }
            } else {
                angles.add(getEdgeStartAngle(edge, node1 == vlNode ? BranchEdge.Side.ONE : BranchEdge.Side.TWO));
            }
        }
        Collections.sort(angles);

        return angles;
    }

    private boolean isBusNodeDrawn(Node busGraphNode, BusNode busNodeCurrentlyDrawn) {
        if (busGraphNode instanceof BusNode) {
            return ((BusNode) busGraphNode).getIndex() < busNodeCurrentlyDrawn.getIndex();
        }
        return true;
    }

    private double getEdgeStartAngle(Edge edge, BranchEdge.Side side) {
        if (edge instanceof ThreeWtEdge) {
            return ((ThreeWtEdge) edge).getEdgeAngle();
        } else if (edge instanceof BranchEdge) {
            return ((BranchEdge) edge).getEdgeStartAngle(side);
        }
        return 0;
    }

    private String getCirclePath(double radius, double angleStart, double angleEnd, boolean clockWise) {
        double arcAngle = angleEnd - angleStart;
        double xStart = radius * Math.cos(angleStart);
        double yStart = radius * Math.sin(angleStart);
        double xEnd = radius * Math.cos(angleEnd);
        double yEnd = radius * Math.sin(angleEnd);
        int largeArc = Math.abs(arcAngle) > Math.PI ? 1 : 0;
        return String.format(Locale.US, "%.3f,%.3f A%.3f,%.3f %.3f %d %d %.3f,%.3f",
                xStart, yStart, radius, radius, Math.toDegrees(arcAngle), largeArc, clockWise ? 1 : 0, xEnd, yEnd);
    }

    private void insertName(XMLStreamWriter writer, Supplier<Optional<String>> getName) throws XMLStreamException {
        if (svgParameters.isInsertNameDesc()) {
            Optional<String> nodeName = getName.get();
            if (nodeName.isPresent()) {
                writer.writeStartElement(DESCRIPTION_ATTRIBUTE);
                writer.writeCharacters(nodeName.get());
                writer.writeEndElement();
            }
        }
    }

    private void drawTextEdges(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.TEXT_EDGES_CLASS);
        for (TextEdge edge : graph.getTextEdges()) {
            drawTextEdge(writer, edge, graph.getVoltageLevelNode(edge));
        }
        writer.writeEndElement();
    }

    private void drawTextEdge(XMLStreamWriter writer, TextEdge edge, VoltageLevelNode vlNode) throws XMLStreamException {
        writer.writeEmptyElement(POLYLINE_ELEMENT_NAME);
        writer.writeAttribute(ID_ATTRIBUTE, getPrefixedId(edge.getDiagramId()));
        addStylesIfAny(writer, styleProvider.getEdgeStyleClasses(edge));
        List<Point> points = edge.getPoints();
        shiftEdgeStart(points, vlNode);
        String lineFormatted1 = points.stream()
                .map(point -> getFormattedValue(point.getX()) + "," + getFormattedValue(point.getY()))
                .collect(Collectors.joining(" "));
        writer.writeAttribute(POINTS_ATTRIBUTE, lineFormatted1);
    }

    private void addStylesIfAny(XMLStreamWriter writer, List<String> edgeStyleClasses) throws XMLStreamException {
        if (!edgeStyleClasses.isEmpty()) {
            writer.writeAttribute(CLASS_ATTRIBUTE, String.join(" ", edgeStyleClasses));
        }
    }

    private void shiftEdgeStart(List<Point> points, VoltageLevelNode vlNode) {
        double circleRadius = getVoltageLevelCircleRadius(vlNode);
        points.set(0, points.get(0).atDistance(circleRadius, points.get(1)));
    }

    private void addSvgRoot(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("", SVG_ROOT_ELEMENT_NAME, NAMESPACE_URI);
        if (svgParameters.isSvgWidthAndHeightAdded()) {
            double[] diagramDimension = getDiagramDimensions(graph);
            writer.writeAttribute(WIDTH_ATTRIBUTE, getFormattedValue(diagramDimension[0]));
            writer.writeAttribute(HEIGHT_ATTRIBUTE, getFormattedValue(diagramDimension[1]));
        }
        writer.writeAttribute(VIEW_BOX_ATTRIBUTE, getViewBoxValue(graph));
        writer.writeDefaultNamespace(NAMESPACE_URI);
    }

    private double[] getDiagramDimensions(Graph graph) {
        double width = getDiagramWidth(graph);
        double height = getDiagramHeight(graph);
        double scale;
        switch (svgParameters.getSizeConstraint()) {
            case FIXED_WIDTH:
                scale = svgParameters.getFixedWidth() / width;
                break;
            case FIXED_HEIGHT:
                scale = svgParameters.getFixedHeight() / height;
                break;
            case FIXED_SCALE:
                scale = svgParameters.getFixedScale();
                break;
            default:
                scale = 1;
                break;
        }
        return new double[] {width * scale, height * scale};
    }

    private double getDiagramHeight(Graph graph) {
        Padding diagramPadding = svgParameters.getDiagramPadding();
        return graph.getHeight() + diagramPadding.getTop() + diagramPadding.getBottom();
    }

    private double getDiagramWidth(Graph graph) {
        Padding diagramPadding = svgParameters.getDiagramPadding();
        return graph.getWidth() + diagramPadding.getLeft() + diagramPadding.getRight();
    }

    private String getViewBoxValue(Graph graph) {
        Padding diagramPadding = svgParameters.getDiagramPadding();
        return getFormattedValue(graph.getMinX() - diagramPadding.getLeft()) + " "
                + getFormattedValue(graph.getMinY() - diagramPadding.getTop()) + " "
                + getFormattedValue(getDiagramWidth(graph)) + " " + getFormattedValue(getDiagramHeight(graph));
    }

    private void addStyle(XMLStreamWriter writer) throws XMLStreamException {
        switch (svgParameters.getCssLocation()) {
            case INSERTED_IN_SVG:
                writer.writeStartElement(STYLE_ELEMENT_NAME);
                writer.writeCData(styleProvider.getStyleDefs());
                writer.writeEndElement();
                break;
            case EXTERNAL_IMPORTED:
                writer.writeStartElement(STYLE_ELEMENT_NAME);
                for (String cssFilename : styleProvider.getCssFilenames()) {
                    writer.writeCharacters("@import url(" + cssFilename + ");");
                }
                writer.writeEndElement();
                break;
            case EXTERNAL_NO_IMPORT:
                // nothing to do
                break;
        }
    }

    private void addMetadata(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        GraphMetadata metadata = new GraphMetadata();

        graph.getBusNodesStream().forEach(bn -> metadata.addBusNode(bn, this::getPrefixedId));
        graph.getNodesStream().forEach(bn -> metadata.addNode(bn, this::getPrefixedId));
        graph.getEdgesStream().forEach(bn -> metadata.addEdge(bn, this::getPrefixedId));

        metadata.writeXml(writer);
    }

    private void addDefs(XMLStreamWriter writer) throws XMLStreamException {
        if (svgParameters.isTextNodeBackground()) {
            writer.writeStartElement(DEFS_ELEMENT_NAME);
            writer.writeStartElement(FILTER_ELEMENT_NAME);
            writer.writeAttribute(ID_ATTRIBUTE, TEXT_BG_FILTER_ID);
            writer.writeAttribute(X_ATTRIBUTE, String.valueOf(0));
            writer.writeAttribute(Y_ATTRIBUTE, String.valueOf(0));
            writer.writeAttribute(WIDTH_ATTRIBUTE, String.valueOf(1));
            writer.writeAttribute(HEIGHT_ATTRIBUTE, String.valueOf(1));
            writer.writeEmptyElement(FE_FLOOD_ELEMENT_NAME);
            writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.TEXT_BACKGROUND_CLASS);
            writer.writeEmptyElement(FE_COMPOSITE_ELEMENT_NAME);
            writer.writeAttribute(FE_IN_ATTRIBUTE, "SourceGraphic");
            writer.writeAttribute(FE_OPERATOR_ATTRIBUTE, "over");
            writer.writeEndElement();
            writer.writeEndElement();
        }
    }

    private static String getFormattedValue(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    protected double getVoltageLevelCircleRadius(VoltageLevelNode vlNode) {
        return getVoltageLevelCircleRadius(vlNode, svgParameters);
    }

    protected static double getVoltageLevelCircleRadius(VoltageLevelNode vlNode, SvgParameters svgParameters) {
        if (vlNode.isFictitious()) {
            return svgParameters.getFictitiousVoltageLevelCircleRadius();
        }
        int nbBuses = vlNode.getBusNodes().size();
        return Math.min(Math.max(nbBuses, 1), 2) * svgParameters.getVoltageLevelCircleRadius();
    }

    public static double getBusAnnulusInnerRadius(BusNode node, VoltageLevelNode vlNode, SvgParameters svgParameters) {
        if (node.getIndex() == 0) {
            return 0;
        }
        int nbNeighbours = node.getNbNeighbouringBusNodes();
        double unitaryRadius = SvgWriter.getVoltageLevelCircleRadius(vlNode, svgParameters) / (nbNeighbours + 1);
        return node.getIndex() * unitaryRadius + svgParameters.getInterAnnulusSpace() / 2;
    }

    public static double getBusAnnulusOuterRadius(BusNode node, VoltageLevelNode vlNode, SvgParameters svgParameters) {
        int nbNeighbours = node.getNbNeighbouringBusNodes();
        double unitaryRadius = SvgWriter.getVoltageLevelCircleRadius(vlNode, svgParameters) / (nbNeighbours + 1);
        return (node.getIndex() + 1) * unitaryRadius - svgParameters.getInterAnnulusSpace() / 2;
    }

    public String getPrefixedId(String id) {
        return svgParameters.getSvgPrefix() + id;
    }
}
