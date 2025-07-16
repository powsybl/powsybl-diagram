/**
 * Copyright (c) 2021-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.diagram.util.CssUtil;
import com.powsybl.nad.library.NadComponentLibrary;
import com.powsybl.nad.model.*;
import com.powsybl.nad.routing.EdgeRouting;
import com.powsybl.nad.utils.RadiusUtils;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.jgrapht.alg.util.Pair;
import org.w3c.dom.Element;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class SvgWriter {

    private static final String INDENT = "    ";
    private static final String SVG_NAMESPACE_URI = "http://www.w3.org/2000/svg";
    private static final String XHTML_NAMESPACE_URI = "http://www.w3.org/1999/xhtml";
    private static final String SVG_ROOT_ELEMENT_NAME = "svg";
    private static final String STYLE_ELEMENT_NAME = "style";
    private static final String GROUP_ELEMENT_NAME = "g";
    private static final String POLYLINE_ELEMENT_NAME = "polyline";
    private static final String PATH_ELEMENT_NAME = "path";
    private static final String CIRCLE_ELEMENT_NAME = "circle";
    private static final String TEXT_ELEMENT_NAME = "text";
    private static final String FOREIGN_OBJECT_ELEMENT_NAME = "foreignObject";
    private static final String DIV_ELEMENT_NAME = "div";
    private static final String SPAN_ELEMENT_NAME = "span";
    private static final String USE_ELEMENT_NAME = "use";
    private static final String ID_ATTRIBUTE = "id";
    private static final String WIDTH_ATTRIBUTE = "width";
    private static final String HEIGHT_ATTRIBUTE = "height";
    private static final String VIEW_BOX_ATTRIBUTE = "viewBox";
    private static final String DESCRIPTION_ATTRIBUTE = "desc";
    private static final String CLASS_ATTRIBUTE = "class";
    private static final String STYLE_ATTRIBUTE = "style";
    private static final String TRANSFORM_ATTRIBUTE = "transform";
    private static final String CIRCLE_RADIUS_ATTRIBUTE = "r";
    private static final String PATH_D_ATTRIBUTE = "d";
    private static final String X_ATTRIBUTE = "x";
    private static final String Y_ATTRIBUTE = "y";
    private static final String POINTS_ATTRIBUTE = "points";
    private static final String HREF_ATTRIBUTE = "href";

    private final SvgParameters svgParameters;
    private final StyleProvider styleProvider;
    private final LabelProvider labelProvider;
    private final EdgeRouting edgeRouting;
    private final NadComponentLibrary componentLibrary;

    public SvgWriter(SvgParameters svgParameters, StyleProvider styleProvider, LabelProvider labelProvider,
                     NadComponentLibrary componentLibrary, EdgeRouting edgeRouting) {
        this.svgParameters = Objects.requireNonNull(svgParameters);
        this.styleProvider = Objects.requireNonNull(styleProvider);
        this.labelProvider = Objects.requireNonNull(labelProvider);
        this.componentLibrary = Objects.requireNonNull(componentLibrary);
        this.edgeRouting = Objects.requireNonNull(edgeRouting);
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
        edgeRouting.run(graph, svgParameters);

        try {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, INDENT, svgOs);
            addSvgRoot(graph, writer);
            addStyle(writer);
            if (this.svgParameters.isHighlightGraph()) {
                drawHighlightedSection(graph, writer);
            }
            drawVoltageLevelNodes(graph, writer);
            drawInjections(graph, writer);
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

    private void drawHighlightedSection(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.HIGHLIGHT_CLASS);
        drawHighlightVoltageLevelNodes(graph, writer);
        drawHighlightBranchEdges(graph, writer);
        drawHighlightThreeWtEdges(graph, writer);
        writer.writeEndElement();
    }

    private void drawInjections(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        if (graph.getBusNodesStream().mapToInt(BusNode::getInjectionCount).anyMatch(nb -> nb > 0)) {
            writer.writeStartElement(GROUP_ELEMENT_NAME);
            writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.INJECTIONS_CLASS);
            for (VoltageLevelNode vlNode : graph.getVoltageLevelNodesStream().filter(VoltageLevelNode::isVisible).toList()) {
                drawInjections(graph, vlNode, writer);
            }
            writer.writeEndElement();
        }
    }

    private void drawInjections(Graph graph, VoltageLevelNode vlNode, XMLStreamWriter writer) throws XMLStreamException {
        if (vlNode.getBusNodes().stream().mapToInt(BusNode::getInjectionCount).anyMatch(nb -> nb > 0)) {
            writer.writeStartElement(GROUP_ELEMENT_NAME);
            for (BusNode busNode : vlNode.getBusNodes()) {
                drawInjections(graph, busNode, writer);
            }
            writer.writeEndElement();
        }
    }

    private void drawInjections(Graph graph, BusNode busNode, XMLStreamWriter writer) throws XMLStreamException {
        if (busNode.getInjectionCount() == 0) {
            return;
        }

        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writeStyleClasses(writer, styleProvider.getBusNodeStyleClasses(busNode));
        for (Injection injection : busNode.getInjections()) {
            writer.writeStartElement(GROUP_ELEMENT_NAME);
            writeId(writer, injection);
            writeStyleClasses(writer, styleProvider.getInjectionStyleClasses(injection));
            writeStyleAttribute(writer, styleProvider.getInjectionStyle(injection));
            insertName(writer, injection::getName);
            drawInjectionEdge(graph, injection, writer);
            drawInjectionIcon(injection, writer);
            writer.writeEndElement();
        }
        writer.writeEndElement();

    }

    private void drawInjectionEdge(Graph graph, Injection injection, XMLStreamWriter writer) throws XMLStreamException {
        Optional<EdgeInfo> edgeInfo = labelProvider.getEdgeInfo(graph, injection);
        writer.writeEmptyElement(POLYLINE_ELEMENT_NAME);
        writeStyleClasses(writer, StyleProvider.EDGE_PATH_CLASS);
        writer.writeAttribute(POINTS_ATTRIBUTE, getPolylinePointsString(injection.getEdge()));
        if (edgeInfo.isPresent()) {
            drawInjectionEdgeInfo(writer, injection, edgeInfo.get());
        }
    }

    private void drawInjectionIcon(Injection injection, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(GROUP_ELEMENT_NAME);

        writer.writeEmptyElement(CIRCLE_ELEMENT_NAME);
        double radius = svgParameters.getInjectionCircleRadius();
        Point circleCenter = injection.getInjectionPoint().atDistance(-radius, injection.getBusNodePoint());
        writer.writeAttribute("cx", getFormattedValue(circleCenter.getX()));
        writer.writeAttribute("cy", getFormattedValue(circleCenter.getY()));
        writer.writeAttribute(CIRCLE_RADIUS_ATTRIBUTE, getFormattedValue(radius));

        insertSvgComponent(injection, writer);

        writer.writeEndElement();
    }

    private void insertSvgComponent(Injection injection, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(TRANSFORM_ATTRIBUTE, getTranslateString(injection.getIconOrigin(svgParameters.getInjectionCircleRadius())));

        Result result = new SAXResult(new SvgContentHandlerToXMLStreamWriter(writer));
        String componentType = injection.getComponentType();
        writeStyleClasses(writer, componentLibrary.getComponentStyleClass(componentType).map(List::of).orElse(List.of()));

        try {
            Transformer transformer = componentLibrary.getSvgTransformer();
            Map<String, List<Element>> subComponents = componentLibrary.getSvgElements(componentType);
            for (Map.Entry<String, List<Element>> scEntry : subComponents.entrySet()) {
                List<String> edgeStyleClasses = componentLibrary.getSubComponentStyleClass(componentType, scEntry.getKey())
                        .map(List::of).orElse(List.of());
                writeStyleClasses(writer, edgeStyleClasses);
                for (Element element : scEntry.getValue()) {
                    transformer.transform(new DOMSource(element), result);
                }
            }
        } catch (TransformerException e) {
            throw new PowsyblException("Cannot insert SVG for injection of type " + injection.getType(), e);
        }
        writer.writeEndElement();
    }

    private void drawBranchEdges(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.BRANCH_EDGES_CLASS);
        for (BranchEdge edge : graph.getBranchEdges()) {
            writer.writeStartElement(GROUP_ELEMENT_NAME);
            writeId(writer, edge);
            writeStyleClasses(writer, styleProvider.getBranchEdgeStyleClasses(edge));
            insertName(writer, edge::getName);
            drawHalfEdge(graph, writer, edge, BranchEdge.Side.ONE);
            drawHalfEdge(graph, writer, edge, BranchEdge.Side.TWO);
            drawEdgeCenter(writer, edge);
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void drawHighlightBranchEdges(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.BRANCH_EDGES_CLASS);
        for (BranchEdge edge : graph.getBranchEdges()) {
            drawHighlightHalfEdge(graph, writer, edge, BranchEdge.Side.ONE);
            drawHighlightHalfEdge(graph, writer, edge, BranchEdge.Side.TWO);
        }
        writer.writeEndElement();
    }

    private void drawEdgeLabel(XMLStreamWriter writer, BranchEdge edge, String edgeLabel) throws XMLStreamException {

        if (edgeLabel == null || edgeLabel.isEmpty()) {
            return;
        }

        List<Point> points1 = edge.getPoints1();
        List<Point> points2 = edge.getPoints2();
        Point anchorPoint = Point.createMiddlePoint(points1.get(points1.size() - 1), points2.get(points2.size() - 1));

        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.EDGE_LABEL_CLASS);
        writer.writeAttribute(TRANSFORM_ATTRIBUTE, getTranslateString(anchorPoint));

        if (edge.isVisible(BranchEdge.Side.ONE) && edge.isVisible(BranchEdge.Side.TWO)) {
            drawEdgeMiddleLabel(edgeLabel, edge, writer);
        } else if (edge.isVisible(BranchEdge.Side.ONE)) {
            drawHalfEdgeLabel(edgeLabel, edge, BranchEdge.Side.ONE, writer);
        } else {
            drawHalfEdgeLabel(edgeLabel, edge, BranchEdge.Side.TWO, writer);
        }

        writer.writeEndElement();
    }

    private void drawEdgeMiddleLabel(String edgeLabel, BranchEdge edge, XMLStreamWriter writer) throws XMLStreamException {
        double edgeEndAngle = edge.getEdgeEndAngle(BranchEdge.Side.ONE);
        drawLabel(writer, edgeLabel, 0, "text-anchor:middle", computeTextAngle(edgeEndAngle), X_ATTRIBUTE);
    }

    private void drawHalfEdgeLabel(String edgeLabel, BranchEdge edge, BranchEdge.Side side, XMLStreamWriter writer) throws XMLStreamException {
        double edgeEndAngle = edge.getEdgeEndAngle(side);
        String style = Math.cos(edgeEndAngle) < 0 ? "text-anchor:end" : "text-anchor:start";
        drawLabel(writer, edgeLabel, 0, style, computeTextAngle(edgeEndAngle), X_ATTRIBUTE);
    }

    private double computeTextAngle(double edgeEndAngle) {
        return Math.cos(edgeEndAngle) < 0 ? edgeEndAngle - Math.PI : edgeEndAngle;
    }

    private void drawEdgeCenter(XMLStreamWriter writer, BranchEdge edge) throws XMLStreamException {
        if (BranchEdge.DANGLING_LINE_EDGE.equals(edge.getType())) {
            return;
        }
        String edgeLabel = labelProvider.getLabel(edge);
        if (!BranchEdge.LINE_EDGE.equals(edge.getType()) || !StringUtils.isEmpty(edgeLabel)) {
            writer.writeStartElement(GROUP_ELEMENT_NAME);
            switch (edge.getType()) {
                case BranchEdge.PST_EDGE, BranchEdge.TWO_WT_EDGE:
                    draw2Wt(writer, edge);
                    break;
                case BranchEdge.HVDC_LINE_EDGE:
                    drawConverterStation(writer, edge);
                    break;
                default:
                    break;
            }
            drawEdgeLabel(writer, edge, edgeLabel);
            writer.writeEndElement();
        }
    }

    private void draw2Wt(XMLStreamWriter writer, BranchEdge edge) throws XMLStreamException {
        draw2WtWinding(writer, edge, BranchEdge.Side.ONE);
        draw2WtWinding(writer, edge, BranchEdge.Side.TWO);
        if (BranchEdge.PST_EDGE.equals(edge.getType())) {
            drawPstArrow(writer, edge);
        }
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

    private void drawHighlightThreeWtEdges(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        List<ThreeWtEdge> threeWtEdges = graph.getThreeWtEdges();
        if (threeWtEdges.isEmpty()) {
            return;
        }

        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.THREE_WT_EDGES_CLASS);
        for (ThreeWtEdge edge : threeWtEdges) {
            drawHighlightThreeWtEdge(writer, edge);
        }
        writer.writeEndElement();
    }

    private void drawHalfEdge(Graph graph, XMLStreamWriter writer, BranchEdge edge, BranchEdge.Side side) throws XMLStreamException {
        // the half edge is only drawn if visible, but if the edge is a TwoWtEdge, the transformer is still drawn
        if (!edge.isVisible(side) && !(edge.isTransformerEdge())) {
            return;
        }
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(ID_ATTRIBUTE, getPrefixedId(edge.getDiagramId() + "." + side.getNum()));
        writeStyleClasses(writer, styleProvider.getSideEdgeStyleClasses(edge, side));
        if (edge.isVisible(side)) {
            Optional<EdgeInfo> edgeInfo = labelProvider.getEdgeInfo(graph, edge, side);
            if (!graph.isLoop(edge)) {
                drawHalfEdge(writer, edge, side, edgeInfo.orElse(null));
            } else {
                drawLoopEdge(writer, edge, side, edgeInfo.orElse(null));
            }
        }
        writer.writeEndElement();
    }

    private void drawHalfEdge(XMLStreamWriter writer, BranchEdge edge, BranchEdge.Side side, EdgeInfo edgeInfo) throws XMLStreamException {
        writer.writeEmptyElement(POLYLINE_ELEMENT_NAME);
        writeStyleClasses(writer, StyleProvider.EDGE_PATH_CLASS);
        writeStyleAttribute(writer, styleProvider.getSideEdgeStyle(edge, side));
        writer.writeAttribute(POINTS_ATTRIBUTE, getPolylinePointsString(edge, side));
        if (edgeInfo != null) {
            drawBranchEdgeInfo(writer, edge, side, edgeInfo);
        }
    }

    private void drawHighlightHalfEdge(Graph graph, XMLStreamWriter writer, BranchEdge edge, BranchEdge.Side side) throws XMLStreamException {
        if (edge.isVisible(side) && !graph.isLoop(edge)) {
            drawHighlightHalfEdge(writer, edge, side);
        }
    }

    private void drawHighlightHalfEdge(XMLStreamWriter writer, BranchEdge edge, BranchEdge.Side side) throws XMLStreamException {
        writer.writeEmptyElement(POLYLINE_ELEMENT_NAME);
        writeStyleClasses(writer, styleProvider.getHighlightSideEdgeStyleClasses(edge, side));
        writer.writeAttribute(POINTS_ATTRIBUTE, getPolylinePointsString(edge, side));
    }

    private void drawLoopEdge(XMLStreamWriter writer, BranchEdge edge, BranchEdge.Side side, EdgeInfo edgeInfo) throws XMLStreamException {
        writer.writeEmptyElement(PATH_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.EDGE_PATH_CLASS);
        writer.writeAttribute(PATH_D_ATTRIBUTE, getLoopPathString(edge, side));
        writeStyleAttribute(writer, styleProvider.getSideEdgeStyle(edge, side));
        if (edgeInfo != null) {
            drawLoopEdgeInfo(writer, edge, side, edgeInfo);
        }
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
        writeId(writer, edge);
        writeStyleClasses(writer, styleProvider.getThreeWtEdgeStyleClasses(edge));
        insertName(writer, edge::getName);
        writer.writeEmptyElement(POLYLINE_ELEMENT_NAME);
        writeStyleClasses(writer, StyleProvider.EDGE_PATH_CLASS);
        writeStyleAttribute(writer, styleProvider.getThreeWtEdgeStyle(edge));
        writer.writeAttribute(POINTS_ATTRIBUTE, getPolylinePointsString(edge));

        Optional<EdgeInfo> edgeInfo = labelProvider.getEdgeInfo(graph, edge);
        if (edgeInfo.isPresent()) {
            drawThreeWtEdgeInfo(writer, edge, edgeInfo.get());
        }
        writer.writeEndElement();
    }

    private void drawHighlightThreeWtEdge(XMLStreamWriter writer, ThreeWtEdge edge) throws XMLStreamException {
        if (!edge.isVisible()) {
            return;
        }
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writeStyleClasses(writer, styleProvider.getThreeWtEdgeStyleClasses(edge));
        writer.writeEmptyElement(POLYLINE_ELEMENT_NAME);
        writeStyleClasses(writer, styleProvider.getHighlightThreeWtEdgStyleClasses(edge));
        writer.writeAttribute(POINTS_ATTRIBUTE, getPolylinePointsString(edge));
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
            writeId(writer, threeWtNode);
            writer.writeAttribute(TRANSFORM_ATTRIBUTE, getTranslateString(threeWtNode.getPosition()));
            writeStyleClasses(writer, styleProvider.getNodeStyleClasses(threeWtNode));
            List<ThreeWtEdge> edges = graph.getThreeWtEdgeStream(threeWtNode).collect(Collectors.toList());
            for (ThreeWtEdge edge : edges) {
                draw3WtWinding(edge, threeWtNode, writer);
                if (ThreeWtEdge.PST_EDGE.equals(edge.getType())) {
                    drawPstArrow(writer, threeWtNode, edge);
                }
            }
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void draw3WtWinding(ThreeWtEdge edge, ThreeWtNode threeWtNode, XMLStreamWriter writer) throws XMLStreamException {
        double radius = svgParameters.getTransformerCircleRadius();
        Point circleCenter = edge.getPoints().get(1).atDistance(radius, threeWtNode.getPosition());
        writer.writeEmptyElement(CIRCLE_ELEMENT_NAME);
        writeStyleClasses(writer, styleProvider.getThreeWtEdgeStyleClasses(edge), StyleProvider.WINDING_CLASS);
        writeStyleAttribute(writer, styleProvider.getThreeWtEdgeStyle(edge));
        writer.writeAttribute("cx", getFormattedValue(circleCenter.getX() - threeWtNode.getX()));
        writer.writeAttribute("cy", getFormattedValue(circleCenter.getY() - threeWtNode.getY()));
        writer.writeAttribute(CIRCLE_RADIUS_ATTRIBUTE, getFormattedValue(svgParameters.getTransformerCircleRadius()));
    }

    private void drawPstArrow(XMLStreamWriter writer, ThreeWtNode threeWtNode, ThreeWtEdge edge) throws XMLStreamException {
        double arrowSize = 3 * svgParameters.getTransformerCircleRadius();

        double delta = switch (edge.getSide()) {
            case ONE -> 1.5 * Math.PI;
            case TWO -> 0.75 * Math.PI;
            case THREE -> Math.PI;
        };

        double rotationAngle = edge.getEdgeAngle() + delta;

        double radius = svgParameters.getTransformerCircleRadius();
        Point circleCenter = edge.getPoints().get(1).atDistance(radius, threeWtNode.getPosition());
        Point p = new Point(circleCenter.getX() - threeWtNode.getX(), circleCenter.getY() - threeWtNode.getY());
        double[] matrix = getTransformMatrix(arrowSize, arrowSize, rotationAngle, p);

        writer.writeEmptyElement(PATH_ELEMENT_NAME);
        writer.writeAttribute(PATH_D_ATTRIBUTE, getPstArrowPath(arrowSize));
        writer.writeAttribute(TRANSFORM_ATTRIBUTE, getMatrixString(matrix));
        writeStyleClasses(writer, styleProvider.getThreeWtEdgeStyleClasses(edge), StyleProvider.WINDING_CLASS);
        writeStyleAttribute(writer, styleProvider.getThreeWtEdgeStyle(edge));
    }

    private void drawLoopEdgeInfo(XMLStreamWriter writer, BranchEdge edge, BranchEdge.Side side, EdgeInfo edgeInfo) throws XMLStreamException {
        drawEdgeInfo(writer, edgeInfo, edge.getPoints(side).get(1), edge.getEdgeStartAngle(side));
    }

    private void drawBranchEdgeInfo(XMLStreamWriter writer, BranchEdge edge, BranchEdge.Side side, EdgeInfo edgeInfo) throws XMLStreamException {
        drawEdgeInfo(writer, edgeInfo, edge.getArrow(side), edge.getArrowAngle(side));
    }

    private void drawThreeWtEdgeInfo(XMLStreamWriter writer, ThreeWtEdge edge, EdgeInfo edgeInfo) throws XMLStreamException {
        drawEdgeInfo(writer, edgeInfo, edge.getArrowPoint(), edge.getEdgeAngle());
    }

    private void drawInjectionEdgeInfo(XMLStreamWriter writer, Injection injection, EdgeInfo edgeInfo) throws XMLStreamException {
        drawEdgeInfo(writer, edgeInfo, injection.getArrowPoint(), injection.getAngle());
    }

    private void drawEdgeInfo(XMLStreamWriter writer, EdgeInfo edgeInfo, Point infoCenter, double edgeAngle) throws XMLStreamException {
        drawEdgeInfo(writer, Collections.emptyList(), edgeInfo, infoCenter, edgeAngle);
    }

    private void drawEdgeInfo(XMLStreamWriter writer, List<String> additionalStyles, EdgeInfo edgeInfo, Point infoCenter, double edgeAngle) throws XMLStreamException {

        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writeStyleClasses(writer, additionalStyles, StyleProvider.EDGE_INFOS_CLASS);
        writer.writeAttribute(TRANSFORM_ATTRIBUTE, getTranslateString(infoCenter));

        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writeStyleClasses(writer, styleProvider.getEdgeInfoStyleClasses(edgeInfo));
        drawInAndOutArrows(writer, edgeAngle);
        Optional<String> externalLabel = edgeInfo.getExternalLabel();
        if (externalLabel.isPresent()) {
            drawLabel(writer, externalLabel.get(), edgeAngle, true);
        }
        Optional<String> internalLabel = edgeInfo.getInternalLabel();
        if (internalLabel.isPresent()) {
            drawLabel(writer, internalLabel.get(), edgeAngle, false);
        }
        writer.writeEndElement();

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
        writeStyleAttribute(writer, style);
        writer.writeCharacters(label);
        writer.writeEndElement();
    }

    private String getRotateString(double angleRad) {
        return "rotate(" + getFormattedValue(Math.toDegrees(angleRad)) + ")";
    }

    private String getScaleString(double scale) {
        return "scale(" + getFormattedValue(scale) + ")";
    }

    private String getMatrixString(double[] matrix) {
        return "matrix("
                + getFormattedValue(matrix[0]) + "," + getFormattedValue(matrix[1]) + ","
                + getFormattedValue(matrix[2]) + "," + getFormattedValue(matrix[3]) + ","
                + getFormattedValue(matrix[4]) + "," + getFormattedValue(matrix[5]) + ")";
    }

    private double[] getTransformMatrix(double width, double height, double angle, Point center) {
        double centerPosX = center.getX();
        double centerPosY = center.getY();

        double cosRo = Math.cos(angle);
        double sinRo = Math.sin(angle);
        double cdx = width / 2;
        double cdy = height / 2;

        double e1 = centerPosX - cdx * cosRo + cdy * sinRo;
        double f1 = centerPosY - cdx * sinRo - cdy * cosRo;

        return new double[]{+cosRo, sinRo, -sinRo, cosRo, e1, f1};
    }

    private void draw2WtWinding(XMLStreamWriter writer, BranchEdge edge, BranchEdge.Side side) throws XMLStreamException {
        writer.writeEmptyElement(CIRCLE_ELEMENT_NAME);
        writeStyleClasses(writer, styleProvider.getSideEdgeStyleClasses(edge, side), StyleProvider.WINDING_CLASS);
        writeStyleAttribute(writer, styleProvider.getSideEdgeStyle(edge, side));
        List<Point> halfPoints = edge.getPoints(side);
        Point point1 = halfPoints.get(halfPoints.size() - 1); // point near 2wt
        Point point2 = halfPoints.get(halfPoints.size() - 2); // point near voltage level, or control point for loops
        double radius = svgParameters.getTransformerCircleRadius();
        Point circleCenter = point1.atDistance(-radius, point2);
        writer.writeAttribute("cx", getFormattedValue(circleCenter.getX()));
        writer.writeAttribute("cy", getFormattedValue(circleCenter.getY()));
        writer.writeAttribute(CIRCLE_RADIUS_ATTRIBUTE, getFormattedValue(radius));
    }

    private void drawPstArrow(XMLStreamWriter writer, BranchEdge edge) throws XMLStreamException {
        double arrowSize = 3 * svgParameters.getTransformerCircleRadius();
        double rotationAngle = edge.getEdgeEndAngle(BranchEdge.Side.ONE);

        List<Point> points1 = edge.getPoints1();
        List<Point> points2 = edge.getPoints2();
        Point middle = Point.createMiddlePoint(points1.get(points1.size() - 1), points2.get(points2.size() - 1));
        double[] matrix = getTransformMatrix(arrowSize, arrowSize, rotationAngle, middle);

        writer.writeEmptyElement(PATH_ELEMENT_NAME);
        writer.writeAttribute(PATH_D_ATTRIBUTE, getPstArrowPath(arrowSize));
        writer.writeAttribute(TRANSFORM_ATTRIBUTE, getMatrixString(matrix));
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.PST_ARROW_CLASS);
    }

    private String getPstArrowPath(double arrowSize) {
        String d1 = getFormattedValue(arrowSize); // arrow size
        String dh = getFormattedValue(svgParameters.getPstArrowHeadSize()); // arrow head size
        String d2 = getFormattedValue(arrowSize - svgParameters.getPstArrowHeadSize()); // arrow size without the arrow head
        return String.format("M%s,0 0,%s M%s,0 %s,0 %s,%s", d1, d1, d2, d1, d1, dh);
    }

    private void drawVoltageLevelNodes(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.VOLTAGE_LEVEL_NODES_CLASS);
        for (VoltageLevelNode vlNode : graph.getVoltageLevelNodesStream().filter(VoltageLevelNode::isVisible).toList()) {
            writer.writeStartElement(GROUP_ELEMENT_NAME);
            writer.writeAttribute(TRANSFORM_ATTRIBUTE, getTranslateString(vlNode));
            drawNode(graph, writer, vlNode);
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void drawHighlightVoltageLevelNodes(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(GROUP_ELEMENT_NAME);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.VOLTAGE_LEVEL_NODES_CLASS);
        for (VoltageLevelNode vlNode : graph.getVoltageLevelNodesStream().filter(VoltageLevelNode::isVisible).collect(Collectors.toList())) {
            drawHighlightedNode(writer, vlNode);
        }
        writer.writeEndElement();
    }

    private void drawHighlightedNode(XMLStreamWriter writer, VoltageLevelNode vlNode) throws XMLStreamException {
        writer.writeStartElement(USE_ELEMENT_NAME);
        writer.writeAttribute(HREF_ATTRIBUTE, "#" + getPrefixedId(vlNode.getDiagramId()));
        writeStyleClasses(writer, styleProvider.getHighlightNodeStyleClasses(vlNode));
        writer.writeEndElement();
    }

    private void drawTextNodes(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        List<Pair<VoltageLevelNode, TextNode>> textNodes = graph.getVoltageLevelTextPairs().stream()
                .filter(nodePair -> nodePair.getSecond() != null)
                .toList();

        if (!textNodes.isEmpty()) {
            writeForeignObject(writer);
            writer.writeStartElement("", DIV_ELEMENT_NAME, XHTML_NAMESPACE_URI);
            writer.writeDefaultNamespace(XHTML_NAMESPACE_URI);
            for (Pair<VoltageLevelNode, TextNode> nodePair : textNodes) {
                writeDetailedTextNode(writer, nodePair.getSecond(), nodePair.getFirst());
            }
            writer.writeEndElement();
            writer.writeEndElement();
        }
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

    private void writeForeignObject(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(FOREIGN_OBJECT_ELEMENT_NAME);
        // width and height can be set neither to auto nor 0, due to firefox not displaying it in those cases
        // using a fixed size of 1x1 and CSS {overflow: visible} to display it
        writer.writeAttribute(HEIGHT_ATTRIBUTE, "1");
        writer.writeAttribute(WIDTH_ATTRIBUTE, "1");
        writeStyleClasses(writer, StyleProvider.TEXT_NODES_CLASS);
    }

    private void writeDetailedTextNode(XMLStreamWriter writer, TextNode textNode, VoltageLevelNode vlNode) throws XMLStreamException {
        writer.writeStartElement("", DIV_ELEMENT_NAME, XHTML_NAMESPACE_URI);
        writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.LABEL_BOX_CLASS);
        long top = Math.round(textNode.getY());
        long left = Math.round(textNode.getX());
        writeStyleAttribute(writer, String.format("position: absolute; top: %spx; left: %spx", top, left));
        writeId(writer, textNode);

        List<String> vlDescription = labelProvider.getVoltageLevelDescription(vlNode);
        writeLines(vlDescription, writer);

        writeBusNodeLegend(writer, vlNode);

        List<String> vlDetails = labelProvider.getVoltageLevelDetails(vlNode);
        writeLines(vlDetails, writer);

        writer.writeEndElement();
    }

    private void writeLines(List<String> lines, XMLStreamWriter writer) throws XMLStreamException {
        for (String line : lines) {
            writer.writeStartElement(DIV_ELEMENT_NAME);
            writer.writeCharacters(line);
            writer.writeEndElement();
        }
    }

    private void writeBusNodeLegend(XMLStreamWriter writer, VoltageLevelNode vlNode) throws XMLStreamException {
        List<BusNode> notEmptyDescrBusNodes = vlNode.getBusNodeStream()
                .filter(busNode -> StringUtils.isNotEmpty(labelProvider.getBusDescription(busNode)))
                .toList();
        for (BusNode busNode : notEmptyDescrBusNodes) {
            writer.writeStartElement(DIV_ELEMENT_NAME);
            writer.writeAttribute(CLASS_ATTRIBUTE, StyleProvider.BUS_DESCR_CLASS);
            writer.writeEmptyElement(SPAN_ELEMENT_NAME);
            writeStyleClasses(writer, styleProvider.getBusNodeStyleClasses(busNode), StyleProvider.LEGEND_SQUARE_CLASS);
            writeStyleAttribute(writer, styleProvider.getBusNodeStyle(busNode));
            writer.writeCharacters(labelProvider.getBusDescription(busNode));
            writer.writeEndElement();
        }
    }

    private void drawNode(Graph graph, XMLStreamWriter writer, VoltageLevelNode vlNode) throws XMLStreamException {
        writeId(writer, vlNode);
        writeStyleClasses(writer, styleProvider.getNodeStyleClasses(vlNode));
        insertName(writer, vlNode::getName);

        double nodeOuterRadius = RadiusUtils.getVoltageLevelCircleRadius(vlNode, svgParameters);

        if (vlNode.hasUnknownBusNode()) {
            writer.writeEmptyElement(CIRCLE_ELEMENT_NAME);
            writeStyleClasses(writer, styleProvider.getBusNodeStyleClasses(BusNode.UNKNOWN));
            writeStyleAttribute(writer, styleProvider.getBusNodeStyle(BusNode.UNKNOWN));
            writer.writeAttribute(CIRCLE_RADIUS_ATTRIBUTE, getFormattedValue(nodeOuterRadius + svgParameters.getUnknownBusNodeExtraRadius()));
        }

        List<Edge> traversingEdges = new ArrayList<>();
        List<Injection> traversingInjections = new ArrayList<>();

        for (BusNode busNode : vlNode.getBusNodes()) {
            double busInnerRadius = RadiusUtils.getBusAnnulusInnerRadius(busNode, vlNode, svgParameters);
            double busOuterRadius = RadiusUtils.getBusAnnulusOuterRadius(busNode, vlNode, svgParameters);
            if (busInnerRadius == 0) {
                if (busNode instanceof BoundaryBusNode) {
                    // Boundary nodes are always at side two of a dangling line edge, dangling line is its only edge
                    double edgeStartAngle = getEdgeStartAngle(graph.getBusEdges(busNode).iterator().next(), BranchEdge.Side.TWO);
                    drawBoundarySemicircle(writer, busOuterRadius, edgeStartAngle);
                } else {
                    writer.writeEmptyElement(CIRCLE_ELEMENT_NAME);
                    writer.writeAttribute(CIRCLE_RADIUS_ATTRIBUTE, getFormattedValue(busOuterRadius));
                }
            } else {
                writer.writeEmptyElement(PATH_ELEMENT_NAME);
                String path = getFragmentedAnnulusPath(busInnerRadius, busOuterRadius, traversingEdges, traversingInjections, graph, vlNode, busNode);
                writer.writeAttribute(PATH_D_ATTRIBUTE, path);
            }
            writeId(writer, busNode);
            writeStyleClasses(writer, styleProvider.getBusNodeStyleClasses(busNode), StyleProvider.BUSNODE_CLASS);
            writeStyleAttribute(writer, styleProvider.getBusNodeStyle(busNode));

            traversingEdges.addAll(graph.getBusEdges(busNode));
            traversingInjections.addAll(busNode.getInjections());
        }
    }

    private void drawBoundarySemicircle(XMLStreamWriter writer, double radius, double edgeStartAngle) throws XMLStreamException {
        writer.writeEmptyElement(PATH_ELEMENT_NAME);
        double startAngle = -Math.PI / 2 + edgeStartAngle;
        String semiCircle = "M" + getCirclePath(radius, startAngle, startAngle + Math.PI, true);
        writer.writeAttribute(PATH_D_ATTRIBUTE, semiCircle);
    }

    private String getFragmentedAnnulusPath(double innerRadius, double outerRadius, List<Edge> traversingBusEdges, List<Injection> traversingInjections,
                                            Graph graph, VoltageLevelNode vlNode, BusNode busNode) {
        if (traversingBusEdges.isEmpty() && traversingInjections.isEmpty()) {
            String path = "M" + getCirclePath(outerRadius, 0, Math.PI, true)
                    + " M" + getCirclePath(outerRadius, Math.PI, 0, true);
            if (innerRadius > 0) { // going the other way around (counter-clockwise) to subtract the inner circle
                path += "M" + getCirclePath(innerRadius, 0, Math.PI, false)
                        + "M" + getCirclePath(innerRadius, Math.PI, 0, false);
            }
            return path;
        }

        List<Double> angles = createTraversingEdgesAnglesList(traversingBusEdges, graph, vlNode, busNode);
        traversingInjections.forEach(ti -> angles.add(ti.getAngle()));
        Collections.sort(angles);

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

    private List<Double> createTraversingEdgesAnglesList(List<Edge> traversingBusEdges, Graph graph, VoltageLevelNode vlNode, BusNode busNode) {
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
        return angles;
    }

    private boolean isBusNodeDrawn(Node busGraphNode, BusNode busNodeCurrentlyDrawn) {
        if (busGraphNode == BusNode.UNKNOWN) {
            return false;
        }
        if (busGraphNode instanceof BusNode busGraphBusNode) {
            return busGraphBusNode.getRingIndex() < busNodeCurrentlyDrawn.getRingIndex();
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
            drawTextEdge(writer, edge);
        }
        writer.writeEndElement();
    }

    private void drawTextEdge(XMLStreamWriter writer, TextEdge edge) throws XMLStreamException {
        writer.writeEmptyElement(POLYLINE_ELEMENT_NAME);
        writeId(writer, edge);
        List<Point> points = edge.getPoints();
        String lineFormatted1 = points.stream()
                .map(point -> getFormattedValue(point.getX()) + "," + getFormattedValue(point.getY()))
                .collect(Collectors.joining(" "));
        writer.writeAttribute(POINTS_ATTRIBUTE, lineFormatted1);
    }

    private void writeStyleClasses(XMLStreamWriter writer, String... additionalClasses) throws XMLStreamException {
        writeStyleClasses(writer, Collections.emptyList(), additionalClasses);
    }

    private void writeStyleClasses(XMLStreamWriter writer, List<String> edgeStyleClasses, String... additionalClasses) throws XMLStreamException {
        if (edgeStyleClasses.isEmpty() && additionalClasses.length == 0) {
            return;
        }
        List<String> allClasses = new ArrayList<>(edgeStyleClasses);
        allClasses.addAll(Arrays.asList(additionalClasses));
        writer.writeAttribute(CLASS_ATTRIBUTE, String.join(" ", allClasses));
    }

    private void writeStyleAttribute(XMLStreamWriter writer, String style) throws XMLStreamException {
        if (!StringUtils.isEmpty(style)) {
            writer.writeAttribute(STYLE_ATTRIBUTE, style);
        }
    }

    private void writeId(XMLStreamWriter writer, Identifiable identifiable) throws XMLStreamException {
        writer.writeAttribute(ID_ATTRIBUTE, getPrefixedId(identifiable.getDiagramId()));
    }

    private void addSvgRoot(Graph graph, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("", SVG_ROOT_ELEMENT_NAME, SVG_NAMESPACE_URI);
        if (svgParameters.isSvgWidthAndHeightAdded()) {
            double[] diagramDimension = getDiagramDimensions(graph);
            writer.writeAttribute(WIDTH_ATTRIBUTE, getFormattedValue(diagramDimension[0]));
            writer.writeAttribute(HEIGHT_ATTRIBUTE, getFormattedValue(diagramDimension[1]));
        }
        writer.writeAttribute(VIEW_BOX_ATTRIBUTE, getViewBoxValue(graph));
        writer.writeDefaultNamespace(SVG_NAMESPACE_URI);
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
                String cssContent = CssUtil.getFilesContent(styleProvider.getCssUrls())
                        + CssUtil.getFilesContent(componentLibrary.getCssUrls());
                writer.writeCData(cssContent);
                writer.writeEndElement();
                break;
            case EXTERNAL_IMPORTED:
                writer.writeStartElement(STYLE_ELEMENT_NAME);
                String cssImports = CssUtil.getImportCssString(styleProvider.getCssFilenames())
                        + CssUtil.getImportCssString(componentLibrary.getCssFilenames());
                writer.writeCharacters(cssImports);
                writer.writeEndElement();
                break;
            case EXTERNAL_NO_IMPORT:
                // nothing to do
                break;
        }
    }

    private static String getFormattedValue(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    public String getPrefixedId(String id) {
        return svgParameters.getSvgPrefix() + id;
    }
}
