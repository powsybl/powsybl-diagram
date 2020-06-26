/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.commons.exceptions.UncheckedTransformerException;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.*;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.*;
import com.powsybl.sld.svg.DiagramLabelProvider.Direction;
import com.powsybl.sld.svg.GraphMetadata.ArrowMetadata;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.powsybl.sld.library.ComponentTypeName.*;
import static com.powsybl.sld.svg.DiagramStyles.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class DefaultSVGWriter implements SVGWriter {

    private static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";
    private static final String SVG_QUALIFIED_NAME = "svg";

    protected static final Logger LOGGER = LoggerFactory.getLogger(DefaultSVGWriter.class);

    protected static final String CLASS = "class";
    protected static final String TRANSFORM = "transform";
    protected static final String TRANSLATE = "translate";
    protected static final String ROTATE = "rotate";
    protected static final int FONT_SIZE = 8;
    protected static final String FONT_FAMILY = "Verdana";
    protected static final double LABEL_OFFSET = 5d;
    protected static final int FONT_VOLTAGE_LEVEL_LABEL_SIZE = 12;
    protected static final String POLYLINE = "polyline";
    protected static final String POINTS = "points";
    protected static final String TEXT_ANCHOR = "text-anchor";
    protected static final String MIDDLE = "middle";
    protected static final int VALUE_MAX_NB_CHARS = 5;

    protected final ComponentLibrary componentLibrary;

    protected final LayoutParameters layoutParameters;

    public DefaultSVGWriter(ComponentLibrary componentLibrary, LayoutParameters layoutParameters) {
        this.componentLibrary = Objects.requireNonNull(componentLibrary);
        this.layoutParameters = Objects.requireNonNull(layoutParameters);
    }

    /**
     * Create the SVGDocument corresponding to the graph
     *
     * @param graph   graph
     * @param svgFile file
     */
    @Override
    public GraphMetadata write(String prefixId,
                               Graph graph,
                               DiagramLabelProvider initProvider,
                               DiagramStyleProvider styleProvider,
                               Path svgFile) {
        try (Writer writer = Files.newBufferedWriter(svgFile)) {
            return write(prefixId, graph, initProvider, styleProvider, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Create the SVGDocument corresponding to the graph
     *
     * @param graph  graph
     * @param writer writer
     */
    @Override
    public GraphMetadata write(String prefixId,
                               Graph graph,
                               DiagramLabelProvider labelProvider,
                               DiagramStyleProvider styleProvider,
                               Writer writer) {
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        Document document = domImpl.createDocument(SVG_NAMESPACE, SVG_QUALIFIED_NAME, null);

        Set<String> listUsedComponentSVG = new HashSet<>();
        addStyle(document, styleProvider, labelProvider, Collections.singletonList(graph), listUsedComponentSVG);

        createDefsSVGComponents(document, listUsedComponentSVG);

        GraphMetadata metadata = writeGraph(prefixId, graph, document, labelProvider, styleProvider);

        transformDocument(document, writer);

        return metadata;
    }

    protected void addStyle(Document document, DiagramStyleProvider styleProvider, DiagramLabelProvider labelProvider,
                            List<Graph> graphs, Set<String> listUsedComponentSVG) {
        Element style = document.createElement("style");

        StringBuilder graphStyle = new StringBuilder("\n");
        graphStyle.append(componentLibrary.getStyleSheet());

        for (Graph graph : graphs) {
            graph.getNodes().forEach(n -> {
                if (!layoutParameters.isAvoidSVGComponentsDuplication()) {
                    Optional<String> nodeStyle = styleProvider.getCssNodeStyleAttributes(n, layoutParameters.isShowInternalNodes());
                    nodeStyle.ifPresent(s -> graphStyle.append(s).append("\n"));
                }
                listUsedComponentSVG.add(n.getComponentType());
                List<DiagramLabelProvider.NodeDecorator> nodeDecorators = labelProvider.getNodeDecorators(n);
                if (nodeDecorators != null) {
                    nodeDecorators.forEach(nodeDecorator -> listUsedComponentSVG.add(nodeDecorator.getType()));
                }
            });
        }

        String cssStr = graphStyle.toString()
                .replace("\r\n", "\n") // workaround for https://bugs.openjdk.java.net/browse/JDK-8133452
                .replace("\r", "\n");
        CDATASection cd = document.createCDATASection(cssStr);
        style.appendChild(cd);

        document.adoptNode(style);
        document.getDocumentElement().appendChild(style);
    }

    protected void transformDocument(Document document, Writer writer) {
        try {
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(writer);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new UncheckedTransformerException(e);
        }
    }

    /**
     * Create the SVGDocument corresponding to the graph
     */
    protected GraphMetadata writeGraph(String prefixId,
                                       Graph graph,
                                       Document document,
                                       DiagramLabelProvider initProvider,
                                       DiagramStyleProvider styleProvider) {
        GraphMetadata metadata = new GraphMetadata();

        Element root = document.createElement("g");

        if (layoutParameters.isShowGrid() && graph.isPositionNodeBusesCalculated()) {
            root.appendChild(drawGrid(prefixId, graph, document, metadata));
        }

        drawVoltageLevel(prefixId, graph, root, metadata, initProvider, styleProvider);

        document.adoptNode(root);
        document.getDocumentElement().appendChild(root);

        return metadata;
    }

    protected void drawVoltageLevel(String prefixId,
                                    Graph graph,
                                    Element root,
                                    GraphMetadata metadata,
                                    DiagramLabelProvider initProvider,
                                    DiagramStyleProvider styleProvider) {
        AnchorPointProvider anchorPointProvider = (type, id) -> {
            if (type.equals(BUSBAR_SECTION)) {
                BusNode busbarSectionNode = (BusNode) graph.getNode(id);
                List<AnchorPoint> result = new ArrayList<>();
                result.add(new AnchorPoint(0, 0, AnchorOrientation.HORIZONTAL));
                for (int i = 1; i < 2 * busbarSectionNode.getPosition().getHSpan(); i++) {
                    result.add(new AnchorPoint(
                            ((double) i / 2) * layoutParameters.getCellWidth() - layoutParameters.getHorizontalBusPadding() / 2,
                            0, AnchorOrientation.VERTICAL));
                }
                result.add(new AnchorPoint(busbarSectionNode.getPxWidth(), 0, AnchorOrientation.HORIZONTAL));
                return result;
            }
            return componentLibrary.getAnchorPoints(type);
        };

        List<Node> remainingNodes = graph.getNodes();

        List<Node> nodesToDraw = graph.getNodes().stream().filter(n -> n instanceof BusNode).collect(Collectors.toList());
        drawNodes(prefixId, root, graph, metadata, anchorPointProvider, initProvider, styleProvider, nodesToDraw);

        remainingNodes.removeAll(nodesToDraw);

        List<Edge> remainingEdges = graph.getEdges();

        for (Cell cell : graph.getCells()) {
            remainingEdges.removeAll(drawCell(prefixId, root, graph, cell, metadata, anchorPointProvider, initProvider,
                    styleProvider));
            remainingNodes.removeAll(cell.getNodes());
        }
        drawEdges(prefixId, root, graph, remainingEdges, metadata, anchorPointProvider, initProvider, styleProvider);
        drawNodes(prefixId, root, graph, metadata, anchorPointProvider, initProvider, styleProvider, remainingNodes);
    }

    private List<Edge> drawCell(String prefixId, Element root, Graph graph, Cell cell,
                                GraphMetadata metadata, AnchorPointProvider anchorPointProvider, DiagramLabelProvider initProvider,
                                DiagramStyleProvider styleProvider) {

        // To avoid overlapping lines over the switches, first, we draw all nodes except the switch nodes,
        // then we draw all the edges, and finally we draw the switch nodes

        String cellId = DiagramStyles.escapeId(prefixId + cell.getId());
        Element g = root.getOwnerDocument().createElement("g");
        g.setAttribute("id", cellId);
        g.setAttribute(CLASS, "cell " + cellId);

        drawNodes(prefixId, g, graph, metadata, anchorPointProvider, initProvider, styleProvider,
                cell.getNodes().stream()
                        .filter(n -> !(n instanceof BusNode || n instanceof SwitchNode)).collect(Collectors.toList()));

        List<Edge> edgesToDraw = cell.getNodes().stream().filter(n -> !(n instanceof BusNode))
                .flatMap(n -> n.getAdjacentEdges().stream())
                .distinct().collect(Collectors.toList());
        drawEdges(prefixId, g, graph, edgesToDraw, metadata, anchorPointProvider, initProvider, styleProvider);

        drawNodes(prefixId, g, graph, metadata, anchorPointProvider, initProvider, styleProvider,
                cell.getNodes().stream().filter(n -> n instanceof SwitchNode).collect(Collectors.toList()));
        root.appendChild(g);
        return edgesToDraw;
    }

    /**
     * Create the SVGDocument corresponding to the substation graph
     *
     * @param graph   substation graph
     * @param svgFile file
     */
    @Override
    public GraphMetadata write(String prefixId,
                               SubstationGraph graph,
                               DiagramLabelProvider initProvider,
                               DiagramStyleProvider styleProvider,
                               Path svgFile) {
        try (Writer writer = Files.newBufferedWriter(svgFile)) {
            return write(prefixId, graph, initProvider, styleProvider, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Create the SVGDocument corresponding to the substation graph
     *
     * @param graph  substation graph
     * @param writer writer
     */
    @Override
    public GraphMetadata write(String prefixId,
                               SubstationGraph graph,
                               DiagramLabelProvider labelProvider,
                               DiagramStyleProvider styleProvider,
                               Writer writer) {
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        Document document = domImpl.createDocument(SVG_NAMESPACE, SVG_QUALIFIED_NAME, null);

        Set<String> listUsedComponentSVG = new HashSet<>();
        addStyle(document, styleProvider, labelProvider, graph.getNodes(), listUsedComponentSVG);
        graph.getMultiTermNodes().forEach(n -> listUsedComponentSVG.add(n.getComponentType()));

        createDefsSVGComponents(document, listUsedComponentSVG);

        GraphMetadata metadata = writeGraph(prefixId, graph, document, labelProvider, styleProvider);

        transformDocument(document, writer);

        return metadata;
    }

    @Override
    public LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }

    @Override
    public ComponentLibrary getComponentLibrary() {
        return componentLibrary;
    }

    /**
     * Create the SVGDocument corresponding to the substation graph
     */
    protected GraphMetadata writeGraph(String prefixId,
                                       SubstationGraph graph,
                                       Document document,
                                       DiagramLabelProvider initProvider,
                                       DiagramStyleProvider styleProvider) {
        GraphMetadata metadata = new GraphMetadata();

        Element root = document.createElement("g");

        // Drawing grid lines
        if (layoutParameters.isShowGrid()) {
            for (Graph vlGraph : graph.getNodes()) {
                if (vlGraph.isPositionNodeBusesCalculated()) {
                    root.appendChild(drawGrid(prefixId, vlGraph, document, metadata));
                }
            }
        }

        drawSubstation(prefixId, graph, root, metadata, initProvider, styleProvider);

        // the drawing of the voltageLevel graph labels is done at the end in order to
        // facilitate the move of a voltageLevel in the diagram
        for (Graph vlGraph : graph.getNodes()) {
            drawGraphLabel(prefixId, root, vlGraph, metadata);
        }

        document.adoptNode(root);
        document.getDocumentElement().appendChild(root);

        return metadata;
    }

    protected void drawSubstation(String prefixId,
                                  SubstationGraph graph,
                                  Element root,
                                  GraphMetadata metadata,
                                  DiagramLabelProvider initProvider,
                                  DiagramStyleProvider styleProvider) {
        // Drawing the voltageLevel graphs
        for (Graph vlGraph : graph.getNodes()) {
            drawVoltageLevel(prefixId, vlGraph, root, metadata, initProvider, styleProvider);
        }

        AnchorPointProvider anchorPointProvider = (type, id) -> componentLibrary.getAnchorPoints(type);

        // Drawing the nodes outside the voltageLevel graphs (multi-terminal nodes)
        drawMultiTerminalNodes(prefixId, root, graph, metadata, styleProvider, anchorPointProvider);

        // Drawing the snake lines
        drawSnakeLines(prefixId, root, graph, metadata, styleProvider, anchorPointProvider);
    }

    /*
     * Drawing the grid lines (if required)
     */
    protected Element drawGrid(String prefixId, Graph graph, Document document, GraphMetadata metadata) {
        int maxH = graph.getNodeBuses().stream()
                .mapToInt(nodeBus -> nodeBus.getPosition().getH() + nodeBus.getPosition().getHSpan())
                .max().orElse(0);
        int maxV = graph.getNodeBuses().stream()
                .mapToInt(nodeBus -> nodeBus.getPosition().getV())
                .max().orElse(1) - 1;

        Element gridRoot = document.createElement("g");

        String gridId = prefixId + "GRID_" + graph.getVoltageLevelInfos().getId();
        gridRoot.setAttribute("id", gridId);
        gridRoot.setAttribute(CLASS, DiagramStyles.GRID_STYLE_CLASS);
        gridRoot.setAttribute(TRANSFORM,
                TRANSLATE + "(" + layoutParameters.getTranslateX() + "," + layoutParameters.getTranslateY() + ")");
        // vertical lines
        for (int i = 0; i < maxH + 1; i++) {
            gridRoot.appendChild(drawGridVerticalLine(document, graph, maxV,
                    graph.getX() + layoutParameters.getInitialXBus() + i * layoutParameters.getCellWidth()));
        }

        // StackHeight Horizontal lines
        gridRoot.appendChild(drawGridHorizontalLine(document, graph, maxH,
                graph.getY() + layoutParameters.getInitialYBus() - layoutParameters.getStackHeight()));
        gridRoot.appendChild(drawGridHorizontalLine(document, graph, maxH,
                graph.getY() + layoutParameters.getInitialYBus() + layoutParameters.getStackHeight()
                        + layoutParameters.getVerticalSpaceBus() * maxV));

        // internCellHeight Horizontal lines
        gridRoot.appendChild(drawGridHorizontalLine(document, graph, maxH,
                graph.getY() + layoutParameters.getInitialYBus() - layoutParameters.getInternCellHeight()));
        gridRoot.appendChild(drawGridHorizontalLine(document, graph, maxH,
                graph.getY() + layoutParameters.getInitialYBus() + layoutParameters.getInternCellHeight()
                        + layoutParameters.getVerticalSpaceBus() * maxV));

        metadata.addNodeMetadata(new GraphMetadata.NodeMetadata(gridId,
                graph.getVoltageLevelInfos().getId(),
                null,
                null,
                null,
                false,
                BusCell.Direction.UNDEFINED,
                false,
                null));

        return gridRoot;
    }

    protected Element drawGridHorizontalLine(Document document, Graph graph, int maxH, double y) {
        return drawGridLine(document,
                layoutParameters.getInitialXBus() + graph.getX(), y,
                layoutParameters.getInitialXBus() + maxH * layoutParameters.getCellWidth() + graph.getX(), y);
    }

    protected Element drawGridVerticalLine(Document document, Graph graph, int maxV, double x) {
        return drawGridLine(document,
                x, layoutParameters.getInitialYBus()
                        - layoutParameters.getStackHeight() - layoutParameters.getExternCellHeight() + graph.getY(),
                x, layoutParameters.getInitialYBus()
                        + layoutParameters.getStackHeight() + layoutParameters.getExternCellHeight()
                        + layoutParameters.getVerticalSpaceBus() * maxV + graph.getY());
    }

    protected Element drawGridLine(Document document, double x1, double y1, double x2, double y2) {
        Element line = document.createElement("line");
        line.setAttribute("x1", Double.toString(x1));
        line.setAttribute("x2", Double.toString(x2));
        line.setAttribute("y1", Double.toString(y1));
        line.setAttribute("y2", Double.toString(y2));
        return line;
    }

    /*
     * Drawing the voltageLevel graph nodes
     */
    protected void drawNodes(String prefixId,
                             Element root,
                             Graph graph,
                             GraphMetadata metadata,
                             AnchorPointProvider anchorPointProvider,
                             DiagramLabelProvider initProvider,
                             DiagramStyleProvider styleProvider,
                             List<Node> nodes) {
        nodes.stream().forEach(node -> {

            String nodeId = DiagramStyles.escapeId(prefixId + node.getId());
            Element g = root.getOwnerDocument().createElement("g");
            g.setAttribute("id", nodeId);

            g.setAttribute(CLASS, node.getComponentType() + " " + nodeId);

            if (node.getType() == Node.NodeType.BUS) {
                Element busElement = drawBus((BusNode) node, g);

                Map<String, String> svgStyle = styleProvider.getSvgNodeStyleAttributes(node, null, null, layoutParameters.isShowInternalNodes());
                svgStyle.forEach(busElement::setAttribute);
            } else {
                incorporateComponents(prefixId, node, g, styleProvider);
            }

            BusCell.Direction direction = (node instanceof FeederNode && node.getCell() != null) ? ((ExternCell) node.getCell()).getDirection() : BusCell.Direction.UNDEFINED;

            if (!node.isFictitious()) {
                drawNodeLabel(prefixId, g, node, initProvider);
                drawNodeDecorators(prefixId, g, node, initProvider, styleProvider);
            }
            root.appendChild(g);

            setMetadata(metadata, node, nodeId, graph, direction, anchorPointProvider);
        });
    }

    protected void setMetadata(GraphMetadata metadata, Node node, String nodeId, Graph graph, BusCell.Direction direction, AnchorPointProvider anchorPointProvider) {
        String nextVId = null;
        if (node instanceof FeederWithSideNode) {
            VoltageLevelInfos otherSideVoltageLevelInfos = ((FeederWithSideNode) node).getOtherSideVoltageLevelInfos();
            if (otherSideVoltageLevelInfos != null) {
                nextVId = otherSideVoltageLevelInfos.getId();
            }
        }

        metadata.addNodeMetadata(
                new GraphMetadata.NodeMetadata(nodeId, graph != null ? graph.getVoltageLevelInfos().getId() : "", nextVId,
                        node.getComponentType(), node.getRotationAngle(),
                        node.isOpen(), direction, false, node.getEquipmentId()));
        if (node.getType() == Node.NodeType.BUS) {
            metadata.addComponentMetadata(new ComponentMetadata(BUSBAR_SECTION,
                    nodeId,
                    anchorPointProvider.getAnchorPoints(BUSBAR_SECTION, node.getId()),
                    new ComponentSize(0, 0), true, null));
        } else {
            if (metadata.getComponentMetadata(node.getComponentType()) == null) {
                metadata.addComponentMetadata(new ComponentMetadata(node.getComponentType(),
                        null,
                        componentLibrary.getAnchorPoints(node.getComponentType()),
                        componentLibrary.getSize(node.getComponentType()), true, null));
            }
        }
    }

    protected void drawNodeLabel(String prefixId, Element g, Node node, DiagramLabelProvider labelProvider) {
        for (DiagramLabelProvider.NodeLabel nodeLabel : labelProvider.getNodeLabels(node)) {
            LabelPosition labelPosition = nodeLabel.getPosition();
            drawLabel(prefixId + labelPosition.getPositionName(), nodeLabel.getLabel(), node.isRotated(),
                    labelPosition.getdX(), labelPosition.getdY(), g, FONT_SIZE, labelPosition.isCentered(),
                    labelPosition.getShiftAngle(), false);
        }
    }

    protected void drawNodeDecorators(String prefixId, Element g, Node node, DiagramLabelProvider labelProvider,
                                      DiagramStyleProvider styleProvider) {
        for (DiagramLabelProvider.NodeDecorator nodeDecorator : labelProvider.getNodeDecorators(node)) {
            insertDecoratorSVGIntoDocumentSVG(prefixId, nodeDecorator, g, node, styleProvider);
        }
    }

    /*
     * Drawing the graph label
     */
    protected void drawGraphLabel(String prefixId, Element root, Graph graph, GraphMetadata metadata) {
        // drawing the label of the voltageLevel
        String idLabelVoltageLevel = prefixId + "LABEL_VL_" + graph.getVoltageLevelInfos().getId();
        Element gLabel = root.getOwnerDocument().createElement("g");
        gLabel.setAttribute("id", idLabelVoltageLevel);

        double decalYLabel = !layoutParameters.isAdaptCellHeightToContent()
                ? layoutParameters.getExternCellHeight()
                : graph.getMaxCalculatedCellHeight(BusCell.Direction.TOP);
        if (decalYLabel < 0) {
            decalYLabel = layoutParameters.getExternCellHeight();
        }

        double yPos = graph.getY() + layoutParameters.getInitialYBus() - decalYLabel - 20.;

        drawLabel(null, graph.isUseName()
                        ? graph.getVoltageLevelInfos().getName()
                        : graph.getVoltageLevelInfos().getId(),
                false, graph.getX(), yPos, gLabel, FONT_VOLTAGE_LEVEL_LABEL_SIZE, false, 0, false);
        root.appendChild(gLabel);

        metadata.addNodeMetadata(new GraphMetadata.NodeMetadata(idLabelVoltageLevel,
                graph.getVoltageLevelInfos().getId(),
                null,
                null,
                null,
                false,
                BusCell.Direction.UNDEFINED,
                true,
                null));
    }

    /*
     * Drawing the voltageLevel graph busbar sections
     */
    protected Element drawBus(BusNode node, Element g) {
        Element line = g.getOwnerDocument().createElement("line");
        line.setAttribute("x1", "0");
        line.setAttribute("y1", "0");
        if (node.isRotated()) {
            line.setAttribute("x2", "0");
            line.setAttribute("y2", String.valueOf(node.getPxWidth()));
        } else {
            line.setAttribute("x2", String.valueOf(node.getPxWidth()));
            line.setAttribute("y2", "0");
        }

        g.appendChild(line);

        g.setAttribute(TRANSFORM, TRANSLATE + "(" + (layoutParameters.getTranslateX() + node.getX()) + ","
                + (layoutParameters.getTranslateY() + node.getY()) + ")");

        return line;
    }

    /*
     * Drawing the voltageLevel graph busbar section names and feeder names
     */
    protected void drawLabel(String idLabel, String str, boolean rotated, double xShift, double yShift, Element g,
                             int fontSize, boolean centered, int shiftAngle, boolean adjustLength) {
        Element label = g.getOwnerDocument().createElement("text");
        if (!StringUtils.isEmpty(idLabel)) {
            label.setAttribute("id", idLabel);
        }
        label.setAttribute("x", String.valueOf(xShift));
        label.setAttribute("y", String.valueOf(yShift));
        label.setAttribute("font-family", FONT_FAMILY);
        label.setAttribute("font-size", Integer.toString(fontSize));
        if (adjustLength) {
            label.setAttribute("xml:space", "preserve");
            label.setAttribute("textLength", Integer.toString(str.length() * (FONT_SIZE - 3)));
        }
        label.setAttribute(CLASS, DiagramStyles.LABEL_STYLE_CLASS);
        Text text = g.getOwnerDocument().createTextNode(str);
        label.setAttribute(TRANSFORM, ROTATE + "(" + shiftAngle + "," + 0 + "," + 0 + ")");
        if (centered) {
            label.setAttribute(TEXT_ANCHOR, MIDDLE);
        }

        label.appendChild(text);
        g.appendChild(label);
    }

    protected boolean canInsertComponentSVG(Node node) {
        return (!node.isFictitious() && node.getType() != Node.NodeType.SHUNT)
                || (node.isFictitious()
                && node.getComponentType().equals(THREE_WINDINGS_TRANSFORMER)
                || node.getComponentType().equals(TWO_WINDINGS_TRANSFORMER)
                || node.getComponentType().equals(PHASE_SHIFT_TRANSFORMER)
                || node.getComponentType().equals(NODE));
    }

    protected void incorporateComponents(String prefixId, Node node, Element g, DiagramStyleProvider styleProvider) {
        String componentType = node.getComponentType();
        transformComponent(node, g);
        if (componentLibrary.getSvgDocument(componentType) != null && canInsertComponentSVG(node)) {
            String componentDefsId = node.getComponentType();
            if (node.getComponentType().equals(BREAKER)
                    || node.getComponentType().equals(DISCONNECTOR)) {
                componentDefsId += node.isOpen() ? "-open" : "-closed";
            }
            insertComponentSVGIntoDocumentSVG(prefixId, componentType, g, node, styleProvider, componentDefsId);
        }
    }

    private void handleNodeRotation(Node node) {
        if (node.getGraph() != null) { // node in voltage level graph
            if ((node.getComponentType().equals(TWO_WINDINGS_TRANSFORMER)
                    || node.getComponentType().equals(PHASE_SHIFT_TRANSFORMER)
                    || node.getComponentType().equals(THREE_WINDINGS_TRANSFORMER))
                    && node.getCell() != null
                    && ((ExternCell) node.getCell()).getDirection() == BusCell.Direction.BOTTOM) {
                node.setRotationAngle(180.);  // rotation if 3WT cell direction is BOTTOM
            }
        } else {  // node outside any graph
            List<Node> adjacentNodes = node.getAdjacentNodes();
            adjacentNodes.sort(Comparator.comparingDouble(Node::getX));
            if (adjacentNodes.size() == 2) {  // 2 windings transformer
                List<Edge> edges = node.getAdjacentEdges();
                List<Double> pol1 = ((TwtEdge) edges.get(0)).getSnakeLine();
                List<Double> pol2 = ((TwtEdge) edges.get(1)).getSnakeLine();
                if (!(pol1.isEmpty() || pol2.isEmpty())) {
                    double x1 = pol1.get(pol1.size() - 4); // absciss of the first polyline second last point
                    double x2 = pol2.get(2);  // absciss of the second polyline third point
                    if (x1 == x2) {
                        node.setRotationAngle(180.);  // rotation if points abscisses are the same
                    }
                }
            } else {  // 3 windings transformer
                Node n2 = adjacentNodes.get(1);
                if (n2.getCell() != null && ((ExternCell) n2.getCell()).getDirection() == BusCell.Direction.BOTTOM) {
                    node.setRotationAngle(180.);  // rotation if middle node cell orientation is BOTTOM
                }
            }
        }
    }

    protected void insertComponentSVGIntoDocumentSVG(String prefixId,
                                                     String componentType,
                                                     Element g, Node node,
                                                     DiagramStyleProvider styleProvider,
                                                     String componentDefsId) {
        handleNodeRotation(node);
        BiConsumer<Element, String> elementAttributesSetter
                = (elt, subComponent) -> setComponentAttributes(prefixId, g, node, styleProvider, elt, subComponent);
        insertSVGIntoDocumentSVG(componentType, g, componentDefsId, elementAttributesSetter);
    }

    protected void insertRotatedComponentSVGIntoDocumentSVG(String prefixId,
                                                            String componentType,
                                                            Element g, double angle,
                                                            ComponentSize componentSize,
                                                            String componentDefsId) {
        BiConsumer<Element, String> elementAttributesSetter
                = (e, subComponent) -> setRotatedComponentAttributes(prefixId, g, e, angle, componentSize);
        insertSVGIntoDocumentSVG(componentType, g, componentDefsId, elementAttributesSetter);
    }

    private void setRotatedComponentAttributes(String prefixId, Element g, Element e,
                                               double angle, ComponentSize componentSize) {
        replaceId(g, e, prefixId);
        double cx = componentSize.getWidth() / 2;
        double cy = componentSize.getHeight() / 2;
        e.setAttribute(TRANSFORM, ROTATE + "(" + angle + "," + cx + "," + cy + ")");
    }

    protected void insertDecoratorSVGIntoDocumentSVG(String prefixId,
                                                     DiagramLabelProvider.NodeDecorator nodeDecorator,
                                                     Element g, Node node,
                                                     DiagramStyleProvider styleProvider) {
        BiConsumer<Element, String> elementAttributesSetter
                = (elt, subComponent) -> setDecoratorAttributes(prefixId, g, node, nodeDecorator, styleProvider, elt, subComponent);
        insertSVGIntoDocumentSVG(nodeDecorator.getType(), g, nodeDecorator.getType(), elementAttributesSetter);
    }

    protected void insertSVGIntoDocumentSVG(String componentType, Element g, String componentDefsId,
                                            BiConsumer<Element, String> elementAttributesSetter) {

        Map<String, SVGOMDocument> subComponents = componentLibrary.getSvgDocument(componentType);

        if (!layoutParameters.isAvoidSVGComponentsDuplication()) {
            // The following code work correctly considering SVG part describing the component is the first child of the SVGDocument.
            // If SVG are written differently, it will not work correctly.
            for (Map.Entry<String, SVGOMDocument> subComponent : subComponents.entrySet()) {
                String subComponentName = subComponent.getKey();
                SVGOMDocument svgSubComponent = subComponent.getValue();

                for (int i = 0; i < svgSubComponent.getChildNodes().item(0).getChildNodes().getLength(); i++) {
                    org.w3c.dom.Node n = svgSubComponent.getChildNodes().item(0).getChildNodes().item(i).cloneNode(true);
                    if (n instanceof Element) {
                        elementAttributesSetter.accept((Element) n, subComponentName);
                    }
                    g.getOwnerDocument().adoptNode(n);
                    g.appendChild(n);
                }
            }
        } else {
            // Adding <use> markup to reuse the svg defined in the <defs> part
            String prefixHref = "#" + componentDefsId;
            if (subComponents != null) {
                Set<String> subCmpsName = subComponents.keySet();
                subCmpsName.forEach(s -> {
                    Element eltUse = g.getOwnerDocument().createElement("use");
                    eltUse.setAttribute("href", subCmpsName.size() > 1 ? prefixHref + "-" + s : prefixHref);
                    elementAttributesSetter.accept(eltUse, s);
                    g.getOwnerDocument().adoptNode(eltUse);
                    g.appendChild(eltUse);
                });
            }
        }
    }

    private void setComponentAttributes(String prefixId, Element g, Node node, DiagramStyleProvider styleProvider,
                                        Element elt, String subComponent) {
        replaceId(g, elt, prefixId);
        ComponentSize size = componentLibrary.getSize(node.getComponentType());
        if (!(node instanceof SwitchNode) && node.isRotated()) {
            elt.setAttribute(TRANSFORM, ROTATE + "(" + node.getRotationAngle() + "," + size.getWidth() / 2 + "," + size.getHeight() / 2 + ")");
        }
        Map<String, String> svgStyle = styleProvider.getSvgNodeStyleAttributes(node, size, subComponent, layoutParameters.isShowInternalNodes());
        svgStyle.forEach(elt::setAttribute);
    }

    private void setDecoratorAttributes(String prefixId, Element g, Node node, DiagramLabelProvider.NodeDecorator nodeDecorator,
                                        DiagramStyleProvider styleProvider, Element elt, String subComponentName) {
        replaceId(g, elt, prefixId);
        ComponentSize size = componentLibrary.getSize(node.getComponentType());
        LabelPosition decoratorPosition = nodeDecorator.getPosition();
        elt.setAttribute(TRANSFORM, getTransformStringDecorator(node, decoratorPosition));
        Map<String, String> svgStyle = styleProvider.getSvgNodeStyleAttributes(node, size, subComponentName, layoutParameters.isShowInternalNodes());
        svgStyle.forEach(elt::setAttribute);
    }

    /**
     * Ensures uniqueness of ids by adding prefixId and node id before id of elt (if existing)
     * @param g XML element for the node
     * @param elt XML element being duplicated
     * @param prefixId prefix string
     */
    private void replaceId(Element g, Element elt, String prefixId) {
        org.w3c.dom.Node nodeId = elt.getAttributes().getNamedItem("id");
        // the id is set only if elt had already an id
        if (nodeId != null) {
            String nodeIdValue = nodeId.getTextContent();
            String gIdValue = StringUtils.removeStart(g.getAttribute("id"), prefixId);
            nodeId.setTextContent(prefixId + gIdValue + "_" + nodeIdValue);
        }
    }

    private String getTransformStringDecorator(Node node, LabelPosition decoratorPosition) {
        String transform;
        if (node.isRotated()) {
            double[] matrix = getDecoratorTransformMatrix(node, decoratorPosition);
            transform = transformMatrixToString(matrix, 4);
        } else {
            transform = TRANSLATE + "(" + decoratorPosition.getdX() + "," + decoratorPosition.getdY() + ")";
        }
        return transform;
    }

    protected void transformComponent(Node node, Element g) {
        ComponentSize componentSize = componentLibrary.getSize(node.getComponentType());

        // For a node marked for rotation during the graph building, but with an svg component not allowed
        // to rotate (ex : disconnector in SVG component library), we cancel the rotation
        if (node.isRotated() && !componentLibrary.isAllowRotation(node.getComponentType())) {
            node.setRotationAngle(null);
        }

        String trans;
        if (!node.isRotated()) {
            double[] translate = getNodeTranslate(node);
            trans = TRANSLATE + "(" + translate[0] + "," + translate[1] + ")";
        } else {
            // afester javafx library does not handle more than one transformation, yet, so
            // combine the couple of transformations, translation+rotation, in a single matrix transformation
            trans = getTransformMatrixString(node.getX(), node.getY(), Math.toRadians(node.getRotationAngle()), componentSize);
        }
        g.setAttribute(TRANSFORM, trans);
    }

    private double[] getNodeTranslate(Node node) {
        ComponentSize componentSize = componentLibrary.getSize(node.getComponentType());
        double translateX = layoutParameters.getTranslateX() + node.getX() - componentSize.getWidth() / 2;
        double translateY = layoutParameters.getTranslateY() + node.getY() - componentSize.getHeight() / 2;
        return new double[] {translateX, translateY};
    }

    private double[] getDecoratorTransformMatrix(Node node, LabelPosition decoratorPosition) {
        ComponentSize componentSize = componentLibrary.getSize(node.getComponentType());
        double[] translateNode = getNodeTranslate(node);
        double[] matrixNode = getTransformMatrix(componentSize.getWidth(), componentSize.getHeight(), node.getRotationAngle() * Math.PI / 180,
            layoutParameters.getTranslateX() + node.getX(), layoutParameters.getTranslateY() + node.getY());
        double translateDecoratorX = translateNode[0] + decoratorPosition.getdX();
        double translateDecoratorY = translateNode[1] + decoratorPosition.getdY();
        double t1 = +matrixNode[3] * (translateDecoratorX - matrixNode[4]) - matrixNode[2] * (translateDecoratorY - matrixNode[5]);
        double t2 = -matrixNode[1] * (translateDecoratorX - matrixNode[4]) + matrixNode[0] * (translateDecoratorY - matrixNode[5]);
        return new double[] {matrixNode[3], -matrixNode[1], -matrixNode[2], matrixNode[0], t1, t2};
    }

    protected void transformArrow(List<Double> points, ComponentSize componentSize, double shift, Element g) {

        double x1 = points.get(0);
        double y1 = points.get(1);
        double x2 = points.get(2);
        double y2 = points.get(3);

        double dx = x2 - x1;
        double dy = y2 - y1;
        double distancePoints = Math.sqrt(dx * dx + dy * dy);

        // Case of wires with non-direct straight lines: if wire distance between first 2 points is too small to display
        // the arrow, checks if the distance between the 2nd and the 3rd points is big enough
        if (points.size() > 4 && distancePoints < 3 * componentSize.getHeight()) {
            double x3 = points.get(4);
            double y3 = points.get(5);
            double dx23 = x3 - x2;
            double dy23 = y3 - y2;
            double distancePoints23 = Math.sqrt(dx23 * dx23 + dy23 * dy23);
            if (distancePoints23 > 3 * componentSize.getHeight()) {
                distancePoints = distancePoints23;
                x1 = x2;
                y1 = y2;
                dx = dx23;
                dy = dy23;
            }
        }

        if (distancePoints > 0) {
            double cosAngle = dy / distancePoints;
            double sinAngle = dx / distancePoints;

            double dist = this.layoutParameters.getArrowDistance();
            double x = x1 + sinAngle * (dist + shift);
            double y = y1 + cosAngle * (dist + shift);

            g.setAttribute(TRANSFORM, getTransformMatrixString(x, y, Math.atan(dx / dy), componentSize));
        }
    }

    private String getTransformMatrixString(double centerPosX, double centerPosY, double angle, ComponentSize componentSize) {
        double centerPosTransX = layoutParameters.getTranslateX() + centerPosX;
        double centerPosTransY = layoutParameters.getTranslateY() + centerPosY;
        double[] matrix = getTransformMatrix(componentSize.getWidth(), componentSize.getHeight(), angle,
            centerPosTransX, centerPosTransY);
        return transformMatrixToString(matrix, 4);
    }

    private double[] getTransformMatrix(double width, double height, double angle,
                                        double centerPosX, double centerPosY) {

        double cosRo = Math.cos(angle);
        double sinRo = Math.sin(angle);
        double cdx = width / 2;
        double cdy = height / 2;

        double e1 = centerPosX - cdx * cosRo + cdy * sinRo;
        double f1 = centerPosY - cdx * sinRo - cdy * cosRo;

        return new double[] {+cosRo, sinRo, -sinRo, cosRo, e1, f1};
    }

    private static String transformMatrixToString(double[] matrix, int precision) {
        double[] matrix2 = new double[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            matrix2[i] = Precision.round(matrix[i], precision);
        }
        return "matrix("
            + matrix2[0] + "," + matrix2[1] + ","
            + matrix2[2] + "," + matrix2[3] + ","
            + matrix2[4] + "," + matrix2[5] + ")";
    }

    protected void insertArrowsAndLabels(String prefixId,
                                         String wireId,
                                         List<Double> points,
                                         Element root,
                                         Node n,
                                         GraphMetadata metadata,
                                         DiagramLabelProvider initProvider,
                                         DiagramStyleProvider styleProvider) {
        InitialValue init = initProvider.getInitialValue(n);
        ComponentMetadata cd = metadata.getComponentMetadata(ARROW);

        double shX = cd.getSize().getWidth() + LABEL_OFFSET;
        double shY = cd.getSize().getHeight() - LABEL_OFFSET + (double) FONT_SIZE / 2;

        String defsId = ARROW;
        double y1 = points.get(1);
        double y2 = points.get(3);

        Optional<String> label1 = init.getLabel1();

        if (label1.isPresent()) {  // we draw the arrow only if value 1 is present
            Element g1 = root.getOwnerDocument().createElement("g");
            String arrow1WireId = wireId + "_ARROW1";
            g1.setAttribute("id", arrow1WireId);
            transformArrow(points, cd.getSize(), 0, g1);

            Optional<Direction> dir1 = init.getArrowDirection1();
            if (dir1.isPresent()) {
                defsId += dir1.get() == Direction.UP ? "-arrow-up" : "-arrow-down";
            }

            if (y1 > y2) {
                insertRotatedComponentSVGIntoDocumentSVG(prefixId, ARROW, g1, 180, cd.getSize(), defsId);
            } else {
                insertComponentSVGIntoDocumentSVG(prefixId, ARROW, g1, n, styleProvider, defsId);
            }
            drawLabel(null, StringUtils.rightPad(label1.get(), VALUE_MAX_NB_CHARS), false, shX, shY, g1, FONT_SIZE, false, 0, true);

            if (dir1.isPresent()) {
                g1.setAttribute(CLASS, "ARROW1_" + escapeClassName(n.getId()) + "_" + dir1.get());
                if (layoutParameters.isAvoidSVGComponentsDuplication()) {
                    styleProvider.getSvgArrowStyleAttributes(1).forEach(((Element) g1.getFirstChild())::setAttribute);
                }
            }

            Optional<String> label3 = init.getLabel3();
            label3.ifPresent(s -> drawLabel(null, StringUtils.rightPad(s, VALUE_MAX_NB_CHARS), false, -(s.length() * (double) FONT_SIZE / 2 + LABEL_OFFSET), shY, g1, FONT_SIZE, false, 0, true));

            root.appendChild(g1);
            metadata.addArrowMetadata(new ArrowMetadata(arrow1WireId, wireId, layoutParameters.getArrowDistance()));
        }

        Optional<String> label2 = init.getLabel2();
        if (label2.isPresent()) {  // we draw the arrow only if value 2 is present
            Element g2 = root.getOwnerDocument().createElement("g");
            String arrow2WireId = wireId + "_ARROW2";
            g2.setAttribute("id", arrow2WireId);
            transformArrow(points, cd.getSize(), 2 * cd.getSize().getHeight(), g2);

            defsId = ARROW;
            Optional<Direction> dir2 = init.getArrowDirection2();
            if (dir2.isPresent()) {
                defsId += dir2.get() == Direction.UP ? "-arrow-up" : "-arrow-down";
            }

            if (y1 > y2) {
                insertRotatedComponentSVGIntoDocumentSVG(prefixId, ARROW, g2, 180, cd.getSize(), defsId);
            } else {
                insertComponentSVGIntoDocumentSVG(prefixId, ARROW, g2, n, styleProvider, defsId);
            }
            drawLabel(null, StringUtils.rightPad(label2.get(), VALUE_MAX_NB_CHARS), false, shX, shY, g2, FONT_SIZE, false, 0, true);

            if (dir2.isPresent()) {
                g2.setAttribute(CLASS, "ARROW2_" + escapeClassName(n.getId()) + "_" + dir2.get());
                if (layoutParameters.isAvoidSVGComponentsDuplication()) {
                    styleProvider.getSvgArrowStyleAttributes(2).forEach(((Element) g2.getFirstChild())::setAttribute);
                }
            }

            Optional<String> label4 = init.getLabel4();
            label4.ifPresent(s -> drawLabel(null, StringUtils.rightPad(s, VALUE_MAX_NB_CHARS), false, -(s.length() * (double) FONT_SIZE / 2 + LABEL_OFFSET), shY, g2, FONT_SIZE, false, 0, true));

            root.appendChild(g2);
            metadata.addArrowMetadata(new ArrowMetadata(arrow2WireId, wireId, layoutParameters.getArrowDistance()));
        }
    }

    /**
     * For global unicity in all type of container (voltage level, substation, zone), we prefix with the container Id and
     * we rely on the fact that node ids are unique inside a voltage level. We also prepend with a custom prefix id to
     * allow multiple diagrams unicity.
     */
    private static String getWireId(String prefixId, String containerId, Edge edge) {
        return escapeClassName(prefixId + "_" + containerId + "_" + edge.getNode1().getId() + "_" + edge.getNode2().getId());
    }

    /*
     * Drawing the voltageLevel graph edges
     */
    protected void drawEdges(String prefixId, Element root, Graph graph, List<Edge> edges, GraphMetadata metadata, AnchorPointProvider anchorPointProvider, DiagramLabelProvider initProvider, DiagramStyleProvider styleProvider) {
        String voltageLevelId = graph.getVoltageLevelInfos().getId();

        for (Edge edge : edges) {
            String wireId = getWireId(prefixId, voltageLevelId, edge);

            Element g = root.getOwnerDocument().createElement("g");
            g.setAttribute("id", wireId);
            g.setAttribute(CLASS, WIRE_STYLE_CLASS);
            root.appendChild(g);

            Element polyline = root.getOwnerDocument().createElement(POLYLINE);

            Map<String, String> styleAttributes = styleProvider.getSvgWireStyleAttributes(edge, layoutParameters.isHighlightLineState());
            styleAttributes.forEach(polyline::setAttribute);

            WireConnection anchorPoints = WireConnection.searchBetterAnchorPoints(anchorPointProvider, edge.getNode1(), edge.getNode2());

            // Determine points of the polyline
            List<Double> pol = anchorPoints.calculatePolylinePoints(edge.getNode1(), edge.getNode2(),
                    layoutParameters.isDrawStraightWires());

            polyline.setAttribute(POINTS, pointsListToString(pol));
            g.appendChild(polyline);

            metadata.addWireMetadata(new GraphMetadata.WireMetadata(wireId,
                    escapeId(edge.getNode1().getId()),
                    escapeId(edge.getNode2().getId()),
                    layoutParameters.isDrawStraightWires(),
                    false));

            if (metadata.getComponentMetadata(ARROW) == null) {
                metadata.addComponentMetadata(new ComponentMetadata(ARROW,
                        null,
                        componentLibrary.getAnchorPoints(ARROW),
                        componentLibrary.getSize(ARROW), true, null));
            }

            if (edge.getNode1() instanceof FeederNode) {
                if (!(edge.getNode2() instanceof FeederNode)) {
                    insertArrowsAndLabels(prefixId, wireId, pol, root, edge.getNode1(), metadata, initProvider, styleProvider);
                }
            } else if (edge.getNode2() instanceof FeederNode) {
                List<Double> reversePoints = new ArrayList<>();

                for (int i = pol.size() - 1; i >= 0; i--) {
                    if (i % 2 == 0) {
                        reversePoints.add(pol.get(i));
                        reversePoints.add(pol.get(i + 1));
                    }
                }

                insertArrowsAndLabels(prefixId, wireId, reversePoints, root, edge.getNode2(), metadata, initProvider, styleProvider);
            }
        }
    }

    /*
     * Drawing the substation graph edges (snakelines between voltageLevel diagram)
     */
    protected void drawSnakeLines(String prefixId, Element root, SubstationGraph graph,
                                  GraphMetadata metadata, DiagramStyleProvider styleProvider,
                                  AnchorPointProvider anchorPointProvider) {
        for (TwtEdge edge : graph.getEdges()) {
            Graph g1 = edge.getNode1().getGraph();
            Graph g2 = edge.getNode2().getGraph();

            if (g1 == null && g2 == null) {
                throw new AssertionError("Edge between two nodes outside any graph");
            }
            if (g1 != null && g2 != null) {
                throw new AssertionError("One node must be outside any graph");
            }

            String wireId = getWireId(prefixId, graph.getSubstationId(), edge);

            Element g = root.getOwnerDocument().createElement("g");
            g.setAttribute("id", wireId);
            g.setAttribute(CLASS, WIRE_STYLE_CLASS);
            root.appendChild(g);

            Element polyline = root.getOwnerDocument().createElement(POLYLINE);

            Map<String, String> styleAttributes = styleProvider.getSvgWireStyleAttributes(edge, layoutParameters.isHighlightLineState());
            styleAttributes.forEach(polyline::setAttribute);

            // Get the points of the snakeLine, already calculated during the layout application
            List<Double> pol = edge.getSnakeLine();
            if (!pol.isEmpty()) {
                adaptCoordSnakeLine(anchorPointProvider, edge, pol);
            }

            polyline.setAttribute(POINTS, pointsListToString(pol));

            g.appendChild(polyline);

            metadata.addWireMetadata(new GraphMetadata.WireMetadata(wireId,
                    escapeId(edge.getNode1().getId()),
                    escapeId(edge.getNode2().getId()),
                    layoutParameters.isDrawStraightWires(),
                    true));

            if (metadata.getComponentMetadata(ARROW) == null) {
                metadata.addComponentMetadata(new ComponentMetadata(ARROW,
                        null,
                        componentLibrary.getAnchorPoints(ARROW),
                        componentLibrary.getSize(ARROW), true, null));
            }
        }
    }

    /*
     * Adaptation of the previously calculated snakeLine points, in order to use the anchor points
     * of the node outside any graph
     */
    private void adaptCoordSnakeLine(AnchorPointProvider anchorPointProvider, TwtEdge edge, List<Double> pol) {
        Node n1 = edge.getNode1();
        Node n2 = edge.getNode2();

        Graph g1 = n1.getGraph();
        Graph g2 = n2.getGraph();

        // Getting the right polyline point from where we need to compute the best anchor point
        double x;
        double y;
        if (g2 == null) {
            if (pol.size() <= 4) {
                x = pol.get(0);
                y = pol.get(1);
            } else if (pol.size() <= 6) {
                x = pol.get(2);
                y = pol.get(3);
            } else {
                x = pol.get(pol.size() - 4);
                y = pol.get(pol.size() - 3);
            }
        } else {
            x = pol.get(2);
            y = pol.get(3);
        }

        WireConnection wireC = WireConnection.searchBetterAnchorPoints(anchorPointProvider, g1 == null ? n1 : n2, x, y);
        AnchorPoint anc1 = wireC.getAnchorPoint1();

        int n = pol.size();

        // Replacing the right points coordinates in the original polyline
        if (g2 == null) {
            double xOld = pol.get(n - 2);
            double yOld = pol.get(n - 1);
            pol.set(n - 2, xOld + anc1.getX());
            pol.set(n - 1, yOld + anc1.getY());
            if (xOld == x) {
                pol.set(n - 4, xOld + anc1.getX());
            } else {
                pol.set(n - 3, yOld + anc1.getY());
            }
        } else {
            double xOld = pol.get(0);
            double yOld = pol.get(1);
            pol.set(0, xOld + anc1.getX());
            pol.set(1, yOld + anc1.getY());
            if (xOld == x) {
                pol.set(2, xOld + anc1.getX());
            } else {
                pol.set(3, yOld + anc1.getY());
            }
        }
    }

    protected String pointsListToString(List<Double> pol) {
        return IntStream.range(0, pol.size())
                .mapToObj(n -> n % 2 == 0 ? pol.get(n) + layoutParameters.getTranslateX() : pol.get(n) + layoutParameters.getTranslateY())
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    /**
     * Creation of the defs area for the SVG components
     */
    protected void createDefsSVGComponents(Document document, Set<String> listUsedComponentSVG) {
        if (layoutParameters.isAvoidSVGComponentsDuplication()) {
            listUsedComponentSVG.add(ARROW);  // adding also arrows

            Element defs = document.createElement("defs");

            listUsedComponentSVG.forEach(c -> {
                Map<String, SVGOMDocument> subComponents = componentLibrary.getSvgDocument(c);
                if (subComponents != null) {
                    Element group = document.createElement("g");
                    group.setAttribute("id", c);

                    insertSVGComponentIntoDefsArea(group, subComponents);

                    defs.getOwnerDocument().adoptNode(group);
                    defs.appendChild(group);
                }
            });

            document.adoptNode(defs);
            document.getDocumentElement().appendChild(defs);
        }
    }

    protected void insertSVGComponentIntoDefsArea(Element group, Map<String, SVGOMDocument> subComponents) {
        for (SVGOMDocument subComponent : subComponents.values()) {
            for (int i = 0; i < subComponent.getChildNodes().item(0).getChildNodes().getLength(); i++) {
                org.w3c.dom.Node n = subComponent.getChildNodes().item(0).getChildNodes().item(i).cloneNode(true);
                group.getOwnerDocument().adoptNode(n);
                group.appendChild(n);
            }
        }
    }

    @Override
    public GraphMetadata write(String prefixId,
                               ZoneGraph graph,
                               DiagramLabelProvider initProvider,
                               DiagramStyleProvider styleProvider,
                               Path svgFile) {
        try (Writer writer = Files.newBufferedWriter(svgFile)) {
            return write(prefixId, graph, initProvider, styleProvider, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public GraphMetadata write(String prefixId,
                               ZoneGraph graph,
                               DiagramLabelProvider labelProvider,
                               DiagramStyleProvider styleProvider,
                               Writer writer) {
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        Document document = domImpl.createDocument(SVG_NAMESPACE, SVG_QUALIFIED_NAME, null);

        List<Graph> vlGraphs = graph.getNodes().stream().map(SubstationGraph::getNodes).flatMap(Collection::stream).collect(Collectors.toList());

        Set<String> listUsedComponentSVG = new HashSet<>();
        addStyle(document, styleProvider, labelProvider, vlGraphs, listUsedComponentSVG);

        createDefsSVGComponents(document, listUsedComponentSVG);

        GraphMetadata metadata = writeGraph(prefixId, graph, vlGraphs, document, labelProvider, styleProvider);

        transformDocument(document, writer);

        return metadata;
    }

    private GraphMetadata writeGraph(String prefixId,
                                     ZoneGraph graph,
                                     List<Graph> vlGraphs,
                                     Document document,
                                     DiagramLabelProvider initProvider,
                                     DiagramStyleProvider styleProvider) {
        GraphMetadata metadata = new GraphMetadata();

        Element root = document.createElement("g");

        // Drawing grid lines
        if (layoutParameters.isShowGrid()) {
            for (Graph vlGraph : vlGraphs) {
                if (vlGraph.isPositionNodeBusesCalculated()) {
                    root.appendChild(drawGrid(prefixId, vlGraph, document, metadata));
                }
            }
        }

        drawZone(prefixId, graph, root, metadata, initProvider, styleProvider);

        // the drawing of the voltageLevel graph labels is done at the end in order to
        // facilitate the move of a voltageLevel in the diagram
        for (Graph vlGraph : vlGraphs) {
            drawGraphLabel(prefixId, root, vlGraph, metadata);
        }

        document.adoptNode(root);
        document.getDocumentElement().appendChild(root);

        return metadata;
    }

    private void drawZone(String prefixId,
                          ZoneGraph graph,
                          Element root,
                          GraphMetadata metadata,
                          DiagramLabelProvider initProvider,
                          DiagramStyleProvider styleProvider) {
        for (SubstationGraph sGraph : graph.getNodes()) {
            drawSubstation(prefixId, sGraph, root, metadata, initProvider, styleProvider);
        }

        drawLines(prefixId, root, graph, metadata);
    }

    private void drawLines(String prefixId, Element root, ZoneGraph graph, GraphMetadata metadata) {
        for (LineEdge edge : graph.getEdges()) {
            String lineId = escapeId(prefixId + edge.getLineId());

            Element g = root.getOwnerDocument().createElement(POLYLINE);
            g.setAttribute("id", lineId);
            String polyline = edge.getPoints()
                    .stream()
                    .map(point -> (point.getX() + layoutParameters.getTranslateX()) + "," + (point.getY() + layoutParameters.getTranslateY()))
                    .collect(Collectors.joining(","));
            g.setAttribute(POINTS, polyline);
            g.setAttribute(CLASS, DiagramStyles.LINE_STYLE_CLASS + " " + escapeClassName(edge.getLineId()));
            root.appendChild(g);

            metadata.addLineMetadata(new GraphMetadata.LineMetadata(lineId,
                    escapeId(edge.getNode1().getId()),
                    escapeId(edge.getNode2().getId())));
        }
    }

    /*
     * Drawing the multi-terminal nodes
     */
    protected void drawMultiTerminalNodes(String prefixId,
                                          Element root,
                                          SubstationGraph graph,
                                          GraphMetadata metadata,
                                          DiagramStyleProvider styleProvider,
                                          AnchorPointProvider anchorPointProvider) {
        graph.getMultiTermNodes().stream().forEach(node -> {

            String nodeId = DiagramStyles.escapeId(prefixId + node.getId());
            Element g = root.getOwnerDocument().createElement("g");
            g.setAttribute("id", nodeId);

            g.setAttribute(CLASS, node.getComponentType() + " " + nodeId);

            incorporateComponents(prefixId, node, g, styleProvider);

            root.appendChild(g);

            setMetadata(metadata, node, nodeId, null, BusCell.Direction.UNDEFINED, anchorPointProvider);
        });
    }
}
