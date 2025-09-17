/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg;

import com.powsybl.diagram.components.ComponentSize;
import com.powsybl.diagram.util.CssUtil;
import com.powsybl.diagram.util.DomUtil;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.AnchorPoint;
import com.powsybl.sld.library.SldComponent;
import com.powsybl.sld.library.SldComponentLibrary;
import com.powsybl.sld.model.cells.Cell;
import com.powsybl.sld.model.coordinate.Direction;
import com.powsybl.sld.model.coordinate.Orientation;
import com.powsybl.sld.model.coordinate.Point;
import com.powsybl.sld.model.coordinate.Side;
import com.powsybl.sld.model.graphs.*;
import com.powsybl.sld.model.nodes.*;
import com.powsybl.sld.model.nodes.Node.NodeType;
import com.powsybl.sld.model.nodes.feeders.FeederWithSides;
import com.powsybl.sld.svg.GraphMetadata.FeederInfoMetadata;
import com.powsybl.sld.svg.styles.StyleClassConstants;
import com.powsybl.sld.svg.styles.StyleProvider;
import com.powsybl.sld.util.IdUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.io.Writer;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.powsybl.sld.library.SldComponentTypeName.*;
import static com.powsybl.sld.model.coordinate.Direction.*;
import static com.powsybl.sld.util.IdUtil.escapeClassName;
import static com.powsybl.sld.util.IdUtil.escapeId;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class DefaultSVGWriter implements SVGWriter {

    private static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";
    private static final String SVG_QUALIFIED_NAME = "svg";

    protected static final Logger LOGGER = LoggerFactory.getLogger(DefaultSVGWriter.class);

    protected static final String GROUP = "g";
    protected static final String CLASS = "class";
    protected static final String STYLE = "style";
    protected static final String TRANSFORM = "transform";
    protected static final String TRANSLATE = "translate";
    protected static final String ROTATE = "rotate";
    protected static final String SCALE = "scale";
    protected static final double LABEL_OFFSET = 5d;
    protected static final String POLYLINE = "polyline";
    protected static final String POINTS = "points";
    protected static final String TEXT_ANCHOR = "text-anchor";
    protected static final String MIDDLE = "middle";
    protected static final int CIRCLE_RADIUS_NODE_INFOS_SIZE = 10;
    protected static final String WIDTH = "width";
    protected static final String HEIGHT = "height";

    protected final SldComponentLibrary componentLibrary;

    protected final LayoutParameters layoutParameters;
    protected final SvgParameters svgParameters;

    public DefaultSVGWriter(SldComponentLibrary componentLibrary, LayoutParameters layoutParameters, SvgParameters svgParameters) {
        this.componentLibrary = Objects.requireNonNull(componentLibrary);
        this.layoutParameters = Objects.requireNonNull(layoutParameters);
        this.svgParameters = svgParameters;
    }

    /**
     * Create the SVGDocument corresponding to the graph
     *
     * @param graph  zone, voltage level or substation graph
     * @param writer writer for the SVG content
     */
    @Override
    public GraphMetadata write(Graph graph, LabelProvider labelProvider, StyleProvider styleProvider, Writer writer) {
        DOMImplementation domImpl = DomUtil.getDocumentBuilder().getDOMImplementation();

        Document document = domImpl.createDocument(SVG_NAMESPACE, SVG_QUALIFIED_NAME, null);
        setDocumentSize(graph, document);

        Set<String> listUsedComponentSVG = new HashSet<>();
        addStyle(document, styleProvider, labelProvider, graph, listUsedComponentSVG);
        if (graph instanceof BaseGraph) {
            ((BaseGraph) graph).getMultiTermNodes().forEach(n -> listUsedComponentSVG.add(n.getComponentType()));
        }

        createDefsSVGComponents(document, listUsedComponentSVG);

        addFrame(document);
        GraphMetadata metadata = writeGraph(graph, document, labelProvider, styleProvider);

        DomUtil.transformDocument(document, writer);

        return metadata;
    }

    private void setDocumentSize(Graph graph, Document document) {
        document.getDocumentElement().setAttribute("viewBox", "0 0 " + getDiagramWidth(graph, layoutParameters) + " " + getDiagramHeight(graph, layoutParameters));
        if (svgParameters.isSvgWidthAndHeightAdded()) {
            document.getDocumentElement().setAttribute(WIDTH, Double.toString(getDiagramWidth(graph, layoutParameters)));
            document.getDocumentElement().setAttribute(HEIGHT, Double.toString(getDiagramHeight(graph, layoutParameters)));
        }
    }

    private double getDiagramWidth(Graph graph, LayoutParameters layoutParameters) {
        return graph.getWidth() + layoutParameters.getDiagramPadding().getLeft() + layoutParameters.getDiagramPadding().getRight();
    }

    private double getDiagramHeight(Graph graph, LayoutParameters layoutParameters) {
        double height = graph.getHeight() + layoutParameters.getDiagramPadding().getTop() + layoutParameters.getDiagramPadding().getBottom();
        if (graph instanceof VoltageLevelGraph && svgParameters.isBusesLegendAdded()) {
            height += 6 * CIRCLE_RADIUS_NODE_INFOS_SIZE;
        }
        return height;
    }

    protected void addStyle(Document document, StyleProvider styleProvider, LabelProvider labelProvider,
                            Graph graph, Set<String> listUsedComponentSVG) {

        graph.getAllNodesStream().forEach(n -> {
            listUsedComponentSVG.add(n.getComponentType());
            List<LabelProvider.NodeDecorator> nodeDecorators = labelProvider.getNodeDecorators(n, graph.getDirection(n));
            if (nodeDecorators != null) {
                nodeDecorators.forEach(nodeDecorator -> listUsedComponentSVG.add(nodeDecorator.getType()));
            }
        });

        Element style = document.createElement(STYLE);
        switch (svgParameters.getCssLocation()) {
            case INSERTED_IN_SVG:
                String cssContent = CssUtil.getFilesContent(styleProvider.getCssUrls())
                        + CssUtil.getFilesContent(componentLibrary.getCssUrls());
                style.appendChild(document.createCDATASection(cssContent));
                document.adoptNode(style);
                document.getDocumentElement().appendChild(style);
                break;
            case EXTERNAL_IMPORTED:
                String cssImports = CssUtil.getImportCssString(styleProvider.getCssFilenames())
                        + CssUtil.getImportCssString(componentLibrary.getCssFilenames());
                style.appendChild(document.createTextNode(cssImports));
                document.adoptNode(style);
                document.getDocumentElement().appendChild(style);
                break;
            case EXTERNAL_NO_IMPORT:
                // Nothing to do
                break;
            default:
                throw new AssertionError("Unexpected CSS location: " + svgParameters.getCssLocation());
        }
    }

    private void addFrame(Document document) {
        Element rect = document.createElement("rect");
        rect.setAttribute(WIDTH, "100%");
        rect.setAttribute(HEIGHT, "100%");
        rect.setAttribute(CLASS, StyleClassConstants.FRAME_CLASS);
        document.adoptNode(rect);
        document.getDocumentElement().appendChild(rect);
    }

    /**
     * Create the SVGDocument corresponding to the graph
     */
    protected GraphMetadata writeGraph(Graph graph, Document document, LabelProvider initProvider, StyleProvider styleProvider) {
        GraphMetadata metadata = new GraphMetadata(layoutParameters, svgParameters);

        Element root = document.createElement(GROUP);

        drawGrid(graph, document, metadata, root);

        if (graph instanceof VoltageLevelGraph) {
            drawVoltageLevel((VoltageLevelGraph) graph, root, metadata, initProvider, styleProvider);
        } else if (graph instanceof SubstationGraph) {
            drawSubstation((SubstationGraph) graph, root, metadata, initProvider, styleProvider);
        } else if (graph instanceof ZoneGraph) {
            drawZone((ZoneGraph) graph, root, metadata, initProvider, styleProvider);
        }

        document.adoptNode(root);
        document.getDocumentElement().appendChild(root);

        return metadata;
    }

    private void drawGrid(Graph graph, Document document, GraphMetadata metadata, Element root) {
        if (svgParameters.isShowGrid()) {
            for (VoltageLevelGraph vlGraph : graph.getVoltageLevels()) {
                if (vlGraph.isPositionNodeBusesCalculated()) {
                    drawGrid(vlGraph, document, metadata, root);
                }
            }
        }
    }

    protected void drawVoltageLevel(VoltageLevelGraph graph,
                                    Element root,
                                    GraphMetadata metadata,
                                    LabelProvider initProvider,
                                    StyleProvider styleProvider) {
        Element g = root.getOwnerDocument().createElement(GROUP);
        g.setAttribute("id", IdUtil.escapeId(graph.getId()));
        g.setAttribute(CLASS, StyleClassConstants.VOLTAGE_LEVEL_CLASS);

        if (!graph.isForVoltageLevelDiagram()) {
            drawGraphLabel(g, graph, metadata);
        }

        Set<Node> remainingNodesToDraw = graph.getNodeSet();
        Set<Edge> remainingEdgesToDraw = graph.getEdgeSet();

        drawBuses(g, graph, metadata, initProvider, styleProvider, remainingNodesToDraw);
        graph.getCellStream().forEach(cell ->
                drawCell(g, graph, cell, metadata, initProvider, styleProvider,
                        remainingEdgesToDraw, remainingNodesToDraw));

        drawEdges(g, graph, metadata, initProvider, styleProvider, remainingEdgesToDraw);

        drawNodes(g, graph, graph.getCoord(), metadata, initProvider, styleProvider, remainingNodesToDraw);

        // Drawing the snake lines before multi-terminal nodes to hide the 3WT connections
        drawSnakeLines(g, graph, metadata, styleProvider);

        // Drawing the nodes outside the voltageLevel graphs (multi-terminal nodes)
        drawNodes(g, graph, new Point(0, 0), metadata, initProvider, styleProvider, graph.getMultiTermNodes());

        if (graph.isForVoltageLevelDiagram() && svgParameters.isBusesLegendAdded()) {
            drawBusesLegend(g, graph, metadata, initProvider, styleProvider);
        }
        root.appendChild(g);
    }

    private void drawCell(Element root, VoltageLevelGraph graph, Cell cell,
                          GraphMetadata metadata, LabelProvider initProvider, StyleProvider styleProvider,
                          Set<Edge> remainingEdgesToDraw, Set<Node> remainingNodesToDraw) {

        // To avoid overlapping lines over the switches, first, we draw all nodes except the switch nodes and bus connections,
        // then we draw all the edges, and finally we draw the switch nodes and bus connections
        String prefixId = metadata.getSvgParameters().getPrefixId();

        String cellId = IdUtil.escapeId(prefixId + cell.getId());
        Element g = root.getOwnerDocument().createElement(GROUP);
        g.setAttribute("id", cellId);
        writeStyleClasses(g, styleProvider.getCellStyles(cell));

        List<Node> cellNodes = cell.getNodes();
        List<Node> nodesToDraw = cellNodes.stream().filter(n -> !(n instanceof BusNode)).collect(Collectors.toList());
        Collection<Edge> edgesToDraw = nodesToDraw.stream().flatMap(n -> n.getAdjacentEdges().stream())
                .filter(e -> cellNodes.contains(e.getNode1()) && cellNodes.contains(e.getNode2()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        drawEdges(g, graph, metadata, initProvider, styleProvider, edgesToDraw);
        drawNodes(g, graph, graph.getCoord(), metadata, initProvider, styleProvider, nodesToDraw);

        remainingEdgesToDraw.removeAll(edgesToDraw);
        remainingNodesToDraw.removeAll(nodesToDraw);

        root.appendChild(g);
    }

    protected void drawSubstation(SubstationGraph graph,
                                  Element root,
                                  GraphMetadata metadata,
                                  LabelProvider initProvider,
                                  StyleProvider styleProvider) {
        // Drawing the voltageLevel graphs
        for (VoltageLevelGraph vlGraph : graph.getVoltageLevels()) {
            drawVoltageLevel(vlGraph, root, metadata, initProvider, styleProvider);
        }

        // Drawing the snake lines before multi-terminal nodes to hide the 3WT connections
        drawSnakeLines(root, graph, metadata, styleProvider);

        // Drawing the nodes outside the voltageLevel graphs (multi-terminal nodes)
        drawNodes(root, graph, new Point(0, 0), metadata, initProvider, styleProvider, graph.getMultiTermNodes());
    }

    /*
     * Drawing the grid lines (if required)
     */
    protected void drawGrid(VoltageLevelGraph graph, Document document, GraphMetadata metadata, Element root) {
        int maxH = graph.getMaxH();
        int maxV = graph.getMaxV();

        Element gridRoot = document.createElement(GROUP);
        String prefixId = metadata.getSvgParameters().getPrefixId();

        String gridId = prefixId + "GRID_" + graph.getVoltageLevelInfos().getId();
        gridRoot.setAttribute("id", IdUtil.escapeId(gridId));
        gridRoot.setAttribute(CLASS, StyleClassConstants.GRID_STYLE_CLASS);

        // vertical lines
        for (int iCell = 0; iCell < maxH / 2 + 1; iCell++) {
            drawGridVerticalLine(document, graph, maxV, graph.getX() + iCell * layoutParameters.getCellWidth(), gridRoot);
        }

        // TOP - Horizontal lines
        if (graph.getExternCellHeight(TOP) > 0.) {
            // StackHeight
            drawGridHorizontalLine(document, graph, maxH, graph.getY() + graph.getFirstBusY() - layoutParameters.getStackHeight(), gridRoot);
            // internCellHeight
            drawGridHorizontalLine(document, graph, maxH, graph.getY() + graph.getFirstBusY() - layoutParameters.getInternCellHeight(), gridRoot);
            // FeederSpan
            drawGridHorizontalLine(document, graph, maxH, graph.getY() + layoutParameters.getFeederSpan(), gridRoot);
        }

        // BOTTOM - Horizontal lines
        if (graph.getExternCellHeight(BOTTOM) > 0.) {
            // StackHeight
            drawGridHorizontalLine(document, graph, maxH, graph.getY() + graph.getFirstBusY() + layoutParameters.getStackHeight() + layoutParameters.getVerticalSpaceBus() * maxV, gridRoot);
            // internCellHeight
            drawGridHorizontalLine(document, graph, maxH, graph.getY() + graph.getFirstBusY() + layoutParameters.getInternCellHeight() + layoutParameters.getVerticalSpaceBus() * maxV, gridRoot);
            // FeederSpan
            drawGridHorizontalLine(document, graph, maxH, graph.getY() + graph.getFirstBusY() + graph.getExternCellHeight(BOTTOM) - layoutParameters.getFeederSpan() + layoutParameters.getVerticalSpaceBus() * maxV, gridRoot);
        }

        metadata.addNodeMetadata(new GraphMetadata.NodeMetadata(null, gridId,
                graph.getVoltageLevelInfos().getId(),
                null,
                null,
                false,
                UNDEFINED,
                false,
                null,
                Collections.emptyList()));

        root.appendChild(gridRoot);
    }

    protected void drawGridHorizontalLine(Document document, VoltageLevelGraph graph, int maxH, double y, Element root) {
        drawGridLine(document, graph.getX(), y, maxH / 2. * layoutParameters.getCellWidth() + graph.getX(), y, root);
    }

    protected void drawGridVerticalLine(Document document, VoltageLevelGraph graph, int maxV, double x, Element root) {
        drawGridLine(document,
                x, graph.getY() + graph.getFirstBusY() - graph.getExternCellHeight(TOP),
                x, graph.getY() + graph.getFirstBusY() + graph.getExternCellHeight(BOTTOM)
                        + layoutParameters.getVerticalSpaceBus() * maxV, root);
    }

    protected void drawGridLine(Document document, double x1, double y1, double x2, double y2, Element root) {
        Element line = document.createElement("line");
        line.setAttribute("x1", Double.toString(x1));
        line.setAttribute("x2", Double.toString(x2));
        line.setAttribute("y1", Double.toString(y1));
        line.setAttribute("y2", Double.toString(y2));
        root.appendChild(line);
    }

    /*
     * Drawing the voltageLevel graph nodes
     */
    protected void drawBuses(Element root,
                             VoltageLevelGraph graph,
                             GraphMetadata metadata,
                             LabelProvider initProvider,
                             StyleProvider styleProvider,
                             Set<Node> remainingNodesToDraw) {

        String prefixId = metadata.getSvgParameters().getPrefixId();

        for (BusNode busNode : graph.getNodeBuses()) {

            String nodeId = IdUtil.escapeId(prefixId + busNode.getId());

            Element g = root.getOwnerDocument().createElement(GROUP);
            g.setAttribute("id", nodeId);
            writeStyleClasses(g, styleProvider.getNodeStyles(graph, busNode, componentLibrary, svgParameters.isShowInternalNodes()));

            drawBus(graph, busNode, g);
            List<LabelProvider.NodeLabel> nodeLabels = initProvider.getNodeLabels(busNode, graph.getDirection(busNode));
            drawNodeLabel(prefixId, g, busNode, nodeLabels);
            drawNodeDecorators(prefixId, g, graph, busNode, initProvider, styleProvider);

            insertBusInfo(prefixId, g, busNode, metadata, initProvider, styleProvider);

            root.appendChild(g);

            metadata.addNodeMetadata(
                new GraphMetadata.NodeMetadata(null, nodeId, graph.getVoltageLevelInfos().getId(), null, BUSBAR_SECTION,
                    false, UNDEFINED, false, busNode.getEquipmentId(), createNodeLabelMetadata(prefixId, busNode, nodeLabels)));
            if (metadata.getComponentMetadata(BUSBAR_SECTION) == null) {
                metadata.addComponent(new SldComponent(BUSBAR_SECTION,
                        null, null,
                        componentLibrary.getComponentStyleClass(BUSBAR_SECTION).orElse(null),
                        componentLibrary.getTransformations(BUSBAR_SECTION), null));
            }

            remainingNodesToDraw.remove(busNode);
        }
    }

    protected List<GraphMetadata.NodeLabelMetadata> createNodeLabelMetadata(String prefixId, Node node, List<LabelProvider.NodeLabel> nodeLabels) {
        List<GraphMetadata.NodeLabelMetadata> labelsMetadata = new ArrayList<>();
        for (LabelProvider.NodeLabel nodeLabel : nodeLabels) {
            LabelPosition labelPosition = nodeLabel.getPosition();
            String svgId = getNodeLabelId(prefixId, node, labelPosition);
            labelsMetadata.add(new GraphMetadata.NodeLabelMetadata(svgId, labelPosition.getPositionName(), nodeLabel.getUserDefinedId()));
        }
        return labelsMetadata;
    }

    /*
     * Drawing the voltageLevel graph nodes
     */
    protected void drawNodes(Element root,
                             BaseGraph graph,
                             Point shift,
                             GraphMetadata metadata,
                             LabelProvider labelProvider,
                             StyleProvider styleProvider,
                             Collection<? extends Node> nodes) {

        String prefixId = metadata.getSvgParameters().getPrefixId();

        for (Node node : nodes) {
            String nodeEscapedId = IdUtil.escapeId(prefixId + node.getId());
            Element g = root.getOwnerDocument().createElement(GROUP);
            g.setAttribute("id", nodeEscapedId);
            writeStyleClasses(g, styleProvider.getNodeStyles(graph.getVoltageLevelGraph(node), node, componentLibrary, svgParameters.isShowInternalNodes()));

            incorporateComponents(prefixId, graph, node, shift, g, labelProvider, styleProvider);
            List<LabelProvider.NodeLabel> nodeLabels = labelProvider.getNodeLabels(node, graph.getDirection(node));
            drawNodeLabel(prefixId, g, node, nodeLabels);
            drawNodeDecorators(prefixId, g, graph, node, labelProvider, styleProvider);

            root.appendChild(g);

            Direction direction = node instanceof FeederNode ? graph.getDirection(node) : Direction.UNDEFINED;
            setMetadata(metadata, node, nodeEscapedId, graph, direction, nodeLabels);
        }
    }

    protected void setMetadata(GraphMetadata metadata, Node node, String nodeEscapedId, BaseGraph graph, Direction direction, List<LabelProvider.NodeLabel> nodeLabels) {
        String nextVId = null;
        if (node instanceof FeederNode && ((FeederNode) node).getFeeder() instanceof FeederWithSides) {
            FeederWithSides feederWs = (FeederWithSides) ((FeederNode) node).getFeeder();
            VoltageLevelInfos otherSideVoltageLevelInfos = feederWs.getOtherSideVoltageLevelInfos();
            if (otherSideVoltageLevelInfos != null) {
                nextVId = otherSideVoltageLevelInfos.getId();
            }
        }

        String prefixId = metadata.getSvgParameters().getPrefixId();

        String vId = graph instanceof VoltageLevelGraph ? ((VoltageLevelGraph) graph).getVoltageLevelInfos().getId() : "";

        boolean isOpen = node.getType() == NodeType.SWITCH && ((SwitchNode) node).isOpen();

        metadata.addNodeMetadata(
                new GraphMetadata.NodeMetadata(getUnescapedId(node), nodeEscapedId, vId, nextVId, node.getComponentType(), isOpen, direction, false,
                        node instanceof EquipmentNode ? ((EquipmentNode) node).getEquipmentId() : null,
                        createNodeLabelMetadata(prefixId, node, nodeLabels)));

        addInfoComponentMetadata(metadata, node.getComponentType());
    }

    private String getUnescapedId(Node node) {
        String unescapedId = null;
        if (node.getComponentType().equals(VSC_CONVERTER_STATION) ||
            node.getComponentType().equals(LCC_CONVERTER_STATION)) {
            unescapedId = node.getId();
        }
        return unescapedId;
    }

    protected void drawNodeLabel(String prefixId, Element g, Node node, List<LabelProvider.NodeLabel> nodeLabels) {
        for (LabelProvider.NodeLabel nodeLabel : nodeLabels) {

            LabelPosition labelPosition = nodeLabel.getPosition();
            Element label = createLabelElement(nodeLabel.getLabel(), labelPosition.getdX(), labelPosition.getdY(), labelPosition.getShiftAngle(), g);
            String svgId = getNodeLabelId(prefixId, node, labelPosition);
            label.setAttribute("id", IdUtil.escapeId(svgId));
            if (labelPosition.isCentered()) {
                label.setAttribute(TEXT_ANCHOR, MIDDLE);
            }
            g.appendChild(label);
        }
    }

    protected void drawNodeDecorators(String prefixId, Element root, Graph graph, Node node, LabelProvider labelProvider,
                                      StyleProvider styleProvider) {
        for (LabelProvider.NodeDecorator nodeDecorator : labelProvider.getNodeDecorators(node, graph.getDirection(node))) {
            Element g = root.getOwnerDocument().createElement(GROUP);
            writeStyleClasses(g, styleProvider.getNodeDecoratorStyles(nodeDecorator, node, componentLibrary));
            insertDecoratorSVGIntoDocumentSVG(prefixId, nodeDecorator, g, graph, node, styleProvider);
            root.appendChild(g);
        }
    }

    /*
     * Drawing the graph label
     */
    protected void drawGraphLabel(Element root, VoltageLevelGraph graph, GraphMetadata metadata) {
        // drawing the label of the voltageLevel
        String unescapedId = metadata.getSvgParameters().getPrefixId() + "LABEL_VL_" + graph.getVoltageLevelInfos().getId();
        String idLabelVoltageLevel = IdUtil.escapeId(unescapedId);
        Element gLabel = root.getOwnerDocument().createElement(GROUP);
        gLabel.setAttribute("id", idLabelVoltageLevel);

        double yPos = graph.getY() - 20.;

        String graphName = svgParameters.isUseName() ? graph.getVoltageLevelInfos().getName() : graph.getVoltageLevelInfos().getId();
        Element label = createLabelElement(graphName, graph.getX(), yPos, 0, gLabel);
        label.setAttribute(CLASS, StyleClassConstants.GRAPH_LABEL_STYLE_CLASS);
        gLabel.appendChild(label);
        root.appendChild(gLabel);

        metadata.addNodeMetadata(new GraphMetadata.NodeMetadata(unescapedId, idLabelVoltageLevel,
                graph.getVoltageLevelInfos().getId(),
                null,
                null,
                false,
                UNDEFINED,
                true,
                null,
                Collections.emptyList()));
    }

    /*
     * Drawing the voltageLevel graph busbar sections
     */
    protected void drawBus(VoltageLevelGraph graph, BusNode node, Element g) {
        Element line = g.getOwnerDocument().createElement("line");
        line.setAttribute("x1", "0");
        line.setAttribute("y1", "0");
        if (node.getOrientation().isHorizontal()) {
            line.setAttribute("x2", String.valueOf(node.getPxWidth()));
            line.setAttribute("y2", "0");
        } else {
            line.setAttribute("x2", "0");
            line.setAttribute("y2", String.valueOf(node.getPxWidth()));
        }
        g.appendChild(line);
        g.setAttribute(TRANSFORM, String.format("%s(%s,%s)", TRANSLATE, graph.getX() + node.getX(), graph.getY() + node.getY()));
    }

    /*
     * Create a label text element at the given position
     */
    protected Element createLabelElement(String str, double xShift, double yShift, int shiftAngle, Element g) {
        Element label = g.getOwnerDocument().createElement("text");
        label.setAttribute("x", String.valueOf(xShift));
        label.setAttribute("y", String.valueOf(yShift));
        if (shiftAngle != 0) {
            label.setAttribute(TRANSFORM, ROTATE + "(" + shiftAngle + "," + 0 + "," + 0 + ")");
        }
        label.setAttribute(CLASS, StyleClassConstants.LABEL_STYLE_CLASS);
        Text text = g.getOwnerDocument().createTextNode(str);
        label.appendChild(text);
        return label;
    }

    protected void incorporateComponents(String prefixId, Graph graph, Node node, Point shift, Element g,
                                         LabelProvider labelProvider, StyleProvider styleProvider) {
        String componentType = node.getComponentType();
        transformComponent(node, shift, g);
        if (componentLibrary.getSvgElements(componentType) != null) {
            insertComponentSVGIntoDocumentSVG(prefixId, componentType, g, graph, node, labelProvider, styleProvider);
        }
    }

    protected void insertComponentSVGIntoDocumentSVG(String prefixId, String componentType, Element g, Graph graph, Node node,
                                                     LabelProvider labelProvider, StyleProvider styleProvider) {
        BiConsumer<Element, String> elementAttributesSetter
                = (elt, subComponent) -> setComponentAttributes(prefixId, g, graph, node, styleProvider, elt, componentType, subComponent);
        String tooltipContent = svgParameters.isTooltipEnabled() ? labelProvider.getTooltip(node) : null;
        insertSVGIntoDocumentSVG(componentType, g, tooltipContent, elementAttributesSetter);
    }

    protected void insertFeederInfoSVGIntoDocumentSVG(FeederInfo feederInfo, String prefixId, Element g, double angle) {
        BiConsumer<Element, String> elementAttributesSetter
                = (e, subComponent) -> setInfoAttributes(feederInfo.getComponentType(), prefixId, g, e, subComponent, angle);
        insertSVGIntoDocumentSVG(feederInfo.getComponentType(), g, null, elementAttributesSetter);
    }

    protected void insertBusInfoSVGIntoDocumentSVG(BusInfo busInfo, String prefixId, Element g) {
        BiConsumer<Element, String> elementAttributesSetter
                = (e, subComponent) -> setInfoAttributes(busInfo.getComponentType(), prefixId, g, e, subComponent, 0.);
        insertSVGIntoDocumentSVG(busInfo.getComponentType(), g, null, elementAttributesSetter);
    }

    private void setInfoAttributes(String infoType, String prefixId, Element g, Element e, String subComponent, double angle) {
        replaceId(g, e, prefixId);
        componentLibrary.getSubComponentStyleClass(infoType, subComponent).ifPresent(style -> e.setAttribute(CLASS, style));
        if (Math.abs(angle) > 0) {
            ComponentSize componentSize = componentLibrary.getSize(infoType);
            double cx = componentSize.width() / 2;
            double cy = componentSize.height() / 2;
            e.setAttribute(TRANSFORM, ROTATE + "(" + angle + "," + cx + "," + cy + ")");
        }
    }

    protected void insertDecoratorSVGIntoDocumentSVG(String prefixId, LabelProvider.NodeDecorator nodeDecorator,
                                                     Element g, Graph graph, Node node, StyleProvider styleProvider) {
        BiConsumer<Element, String> elementAttributesSetter
                = (elt, subComponent) -> setDecoratorAttributes(prefixId, g, graph, node, nodeDecorator, styleProvider, elt, subComponent);
        String nodeDecoratorType = nodeDecorator.getType();
        insertSVGIntoDocumentSVG(nodeDecoratorType, g, null, elementAttributesSetter);
    }

    protected void insertSVGIntoDocumentSVG(String componentType, Element g, String tooltip,
                                            BiConsumer<Element, String> elementAttributesSetter) {
        addToolTip(g, tooltip);
        Map<String, List<Element>> subComponents = componentLibrary.getSvgElements(componentType);
        subComponents.forEach(svgParameters.isAvoidSVGComponentsDuplication() ?
            (subComponentName, svgSubComponent) -> insertSubcomponentReference(g, elementAttributesSetter, componentType, subComponentName, subComponents.size()) :
            (subComponentName, svgSubComponent) -> insertDuplicatedSubcomponent(g, elementAttributesSetter, subComponentName, svgSubComponent)
        );
    }

    private void insertDuplicatedSubcomponent(Element g, BiConsumer<Element, String> elementAttributesSetter, String subComponentName, List<Element> svgSubComponent) {
        svgSubComponent.forEach(e -> {
            Element clonedElement = (Element) e.cloneNode(true);
            setAttributesAndInsertElement(g, elementAttributesSetter, subComponentName, clonedElement);
        });
    }

    private void insertSubcomponentReference(Element g, BiConsumer<Element, String> elementAttributesSetter, String componentType, String subComponentName, int nbSubComponents) {
        // Adding <use> markup to reuse the svg defined in the <defs> part
        Element eltUse = g.getOwnerDocument().createElement("use");
        eltUse.setAttribute("href", "#" + getHRefValue(nbSubComponents, componentType, subComponentName));
        setAttributesAndInsertElement(g, elementAttributesSetter, subComponentName, eltUse);
    }

    private static String getHRefValue(int nbSubComponents, String componentType, String subComponentName) {
        return nbSubComponents > 1 ? componentType + "-" + subComponentName : componentType;
    }

    private void setAttributesAndInsertElement(Element g, BiConsumer<Element, String> elementAttributesSetter, String subComponentName, Element element) {
        elementAttributesSetter.accept(element, subComponentName);
        g.getOwnerDocument().adoptNode(element);
        g.appendChild(element);
    }

    private void addToolTip(Element g, String tooltip) {
        if (!StringUtils.isEmpty(tooltip)) {
            Document doc = g.getOwnerDocument();
            Element title = doc.createElement("title");
            title.appendChild(doc.createTextNode(tooltip));
            g.appendChild(title);
        }
    }

    private void setComponentAttributes(String prefixId, Element g, Graph graph, Node node, StyleProvider styleProvider,
                                        Element elt, String componentType, String subComponent) {
        replaceId(g, elt, prefixId);
        ComponentSize size = componentLibrary.getSize(componentType);

        // Checking if svg component is allowed to be transformed (rotate or flip)
        // (ex : disconnector in SVG component library not allowed to rotate)
        Orientation nodeOrientation = node.getOrientation();
        SldComponent.Transformation transformation = componentLibrary.getTransformations(node.getComponentType()).get(nodeOrientation);
        if (transformation != null) {
            switch (transformation) {
                case ROTATION: {
                    elt.setAttribute(TRANSFORM, ROTATE + "(" + nodeOrientation.toRotationAngle() + "," + size.width() / 2 + "," + size.height() / 2 + ")");
                    break;
                }
                case FLIP: {
                    if (nodeOrientation.isVertical()) {
                        elt.setAttribute(TRANSFORM, SCALE + "(1, -1)" + " " + TRANSLATE + "(0, " + -size.height() + ")");
                    } else {
                        elt.setAttribute(TRANSFORM, SCALE + "(-1, 1)" + " " + TRANSLATE + "(" + -size.width() + ", 0)");
                    }
                    break;
                }
                case NONE:
                default: {
                    // No transformation
                }
            }
        }

        writeStyleClasses(elt, styleProvider.getNodeSubcomponentStyles(graph, node, subComponent),
                componentLibrary.getSubComponentStyleClass(componentType, subComponent).orElse(null));
    }

    private void setDecoratorAttributes(String prefixId, Element g, Graph graph, Node node, LabelProvider.NodeDecorator nodeDecorator,
                                        StyleProvider styleProvider, Element elt, String subComponentName) {
        replaceId(g, elt, prefixId);
        ComponentSize decoratorSize = componentLibrary.getSize(nodeDecorator.getType());
        LabelPosition decoratorPosition = nodeDecorator.getPosition();
        elt.setAttribute(TRANSFORM, getTransformStringDecorator(node, decoratorPosition, decoratorSize));
        writeStyleClasses(elt, styleProvider.getNodeSubcomponentStyles(graph, node, subComponentName),
                componentLibrary.getSubComponentStyleClass(nodeDecorator.getType(), subComponentName).orElse(null));
    }

    /**
     * Ensures uniqueness of ids by adding prefixId and node id before id of elt (if existing)
     *
     * @param g        XML element for the node
     * @param elt      XML element being duplicated
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

    private String getTransformStringDecorator(Node node, LabelPosition decoratorPosition, ComponentSize decoratorSize) {
        ComponentSize componentSize = componentLibrary.getSize(node.getComponentType());
        double dX = componentSize.width() / 2 + decoratorPosition.getdX();
        double dY = componentSize.height() / 2 + decoratorPosition.getdY();
        if (decoratorPosition.isCentered()) {
            dX -= decoratorSize.width() / 2;
            dY -= decoratorSize.height() / 2;
        }
        return TRANSLATE + "(" + dX + "," + dY + ")";
    }

    protected void transformComponent(Node node, Point shift, Element g) {
        double[] translate = getNodeTranslate(node, shift);
        g.setAttribute(TRANSFORM, TRANSLATE + "(" + translate[0] + "," + translate[1] + ")");
    }

    private double[] getNodeTranslate(Node node, Point shift) {
        ComponentSize componentSize = componentLibrary.getSize(node.getComponentType());
        double translateX = node.getX() + shift.getX() - componentSize.width() / 2;
        double translateY = node.getY() + shift.getY() - componentSize.height() / 2;
        return new double[] {translateX, translateY};
    }

    protected void transformFeederInfo(List<Point> points, ComponentSize componentSize, double shift, Element g) {
        Point pointA = points.get(0);
        Point pointB = points.get(1);
        double distancePoints = pointA.distance(pointB);

        // Case of wires with non-direct straight lines: if wire distance between first 2 points is too small to display
        // the feeder info, checks if the distance between the 2nd and the 3rd points is big enough
        if (points.size() > 2 && distancePoints < 3 * componentSize.height()) {
            double distancePoints23 = points.get(1).distance(points.get(2));
            if (distancePoints23 > 3 * componentSize.height()) {
                distancePoints = distancePoints23;
                pointA = points.get(1);
                pointB = points.get(2);
            }
        }

        if (distancePoints > 0) {
            double dx = pointB.getX() - pointA.getX();
            double dy = pointB.getY() - pointA.getY();

            // Calculate cos and sin of the angle between the wire line and the abscisse
            double cosAngle = dx / distancePoints;
            double sinAngle = dy / distancePoints;

            double x = pointA.getX() + cosAngle * (svgParameters.getFeederInfosOuterMargin() + shift);
            double y = pointA.getY() + sinAngle * (svgParameters.getFeederInfosOuterMargin() + shift);

            double feederInfoRotationAngle = Math.atan(dy / dx) - Math.PI / 2;
            if (feederInfoRotationAngle < -Math.PI / 2) {
                feederInfoRotationAngle += Math.PI;
            }
            g.setAttribute(TRANSFORM, getTransformString(x, y, feederInfoRotationAngle, componentSize));
        }
    }

    private String getTransformString(double centerPosX, double centerPosY, double angle, ComponentSize componentSize) {
        if (angle == 0) {
            double translateX = centerPosX - componentSize.width() / 2;
            double translateY = centerPosY - componentSize.height() / 2;
            return TRANSLATE + "(" + translateX + "," + translateY + ")";
        } else {
            double[] matrix = getTransformMatrix(componentSize.width(), componentSize.height(), angle,
                centerPosX, centerPosY);
            return transformMatrixToString(matrix, 4);
        }
    }

    private double[] getTransformMatrix(double width, double height, double angle,
                                        double centerPosX, double centerPosY) {

        double cosRo = Math.cos(angle);
        double sinRo = Math.sin(angle);
        double cdx = width / 2;
        double cdy = height / 2;

        double e1 = centerPosX - cdx * cosRo + cdy * sinRo;
        double f1 = centerPosY - cdx * sinRo - cdy * cosRo;

        return new double[]{+cosRo, sinRo, -sinRo, cosRo, e1, f1};
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

    protected void insertFeederInfos(String prefixId,
                                      List<Point> points,
                                      Element root,
                                      VoltageLevelGraph graph,
                                      FeederNode feederNode,
                                      GraphMetadata metadata,
                                      LabelProvider labelProvider,
                                      StyleProvider styleProvider) {
        if (points.isEmpty()) {
            points.add(graph.getShiftedPoint(feederNode));
            points.add(graph.getShiftedPoint(feederNode));
        }

        double shiftFeederInfo = 0;
        for (FeederInfo feederInfo : labelProvider.getFeederInfos(feederNode)) {
            drawFeederInfo(prefixId, feederNode, points, root, feederInfo, shiftFeederInfo, metadata, styleProvider);
            addInfoComponentMetadata(metadata, feederInfo.getComponentType());

            double height = componentLibrary.getSize(feederInfo.getComponentType()).height();
            shiftFeederInfo += svgParameters.getFeederInfosIntraMargin() + height;
        }
    }

    private void addInfoComponentMetadata(GraphMetadata metadata, String componentType) {
        if (metadata.getComponentMetadata(componentType) == null) {
            metadata.addComponent(new SldComponent(componentType,
                    componentLibrary.getAnchorPoints(componentType),
                    componentLibrary.getSize(componentType),
                    componentLibrary.getComponentStyleClass(componentType).orElse(null),
                    componentLibrary.getTransformations(componentType), null));
        }
    }

    private void drawFeederInfo(String prefixId, FeederNode feederNode, List<Point> points, Element root,
                                FeederInfo feederInfo, double shift, GraphMetadata metadata,
                                StyleProvider styleProvider) {

        Element g = root.getOwnerDocument().createElement(GROUP);
        ComponentSize size = componentLibrary.getSize(feederInfo.getComponentType());

        double shX = size.width() + LABEL_OFFSET;
        double shY = size.height() / 2;

        transformFeederInfo(points, size, shift, g);

        String svgId = escapeId(feederNode.getId() + "_" + feederInfo.getComponentType());
        g.setAttribute("id", svgId);
        String componentType = feederInfo.getComponentType();

        String side = feederNode.getFeeder() instanceof FeederWithSides ? ((FeederWithSides) feederNode.getFeeder()).getSide().name() : null;
        metadata.addFeederInfoMetadata(new FeederInfoMetadata(svgId, feederNode.getEquipmentId(), side, componentType, feederInfo.getUserDefinedId()));

        // we draw the feeder info
        double rotationAngle = points.get(0).getY() > points.get(1).getY() ? 180 : 0;
        insertFeederInfoSVGIntoDocumentSVG(feederInfo, prefixId, g, rotationAngle);

        // we draw the right label only if present
        feederInfo.getRightLabel().ifPresent(s -> {
            Element labelRight = createLabelElement(s, shX, shY, 0, g);
            g.appendChild(labelRight);
        });

        // we draw the left label only if present
        feederInfo.getLeftLabel().ifPresent(s -> {
            Element labelLeft = createLabelElement(s, -LABEL_OFFSET, shY, 0, g);
            labelLeft.setAttribute(STYLE, "text-anchor:end");
            g.appendChild(labelLeft);
        });

        writeStyleClasses(g, styleProvider.getFeederInfoStyles(feederInfo),
                componentLibrary.getComponentStyleClass(feederInfo.getComponentType()).orElse(null));
        root.appendChild(g);
    }

    protected void insertBusInfo(String prefixId, Element root, BusNode busNode,
                                 GraphMetadata metadata, LabelProvider labelProvider, StyleProvider styleProvider) {
        Optional<BusInfo> busInfo = labelProvider.getBusInfo(busNode);
        busInfo.ifPresent(info -> {
            drawBusInfo(prefixId, busNode, root, info, styleProvider, metadata);
            addInfoComponentMetadata(metadata, busInfo.get().getComponentType());
        });
    }

    private void drawBusInfo(String prefixId, BusNode busNode, Element root, BusInfo busInfo,
                             StyleProvider styleProvider, GraphMetadata metadata) {
        Element g = root.getOwnerDocument().createElement(GROUP);

        // Position
        ComponentSize size = componentLibrary.getSize(busInfo.getComponentType());
        double shiftX = svgParameters.getBusInfoMargin();
        double dy = -size.height() / 2;
        double dx = busInfo.getAnchor() == Side.RIGHT ? busNode.getPxWidth() - shiftX - size.width() : shiftX;
        g.setAttribute(TRANSFORM, TRANSLATE + "(" + dx + "," + dy + ")");

        // Styles
        writeStyleClasses(g, styleProvider.getBusInfoStyle(busInfo),
                componentLibrary.getComponentStyleClass(busInfo.getComponentType()).orElse(null));

        // Identity
        String svgId = escapeId(busNode.getId() + "_" + busInfo.getComponentType());
        g.setAttribute("id", svgId);

        // Metadata
        metadata.addBusInfoMetadata(new GraphMetadata.BusInfoMetadata(svgId, busNode.getId(), busInfo.getUserDefinedId()));

        // Append indicator to SVG
        insertBusInfoSVGIntoDocumentSVG(busInfo, prefixId, g);
        double shY = size.height() + LABEL_OFFSET;

        // We draw the bottom label only if present
        busInfo.getBottomLabel().ifPresent(s -> {
            Element labelBottom = createLabelElement(s, 0, shY, 0, g);
            g.appendChild(labelBottom);
        });

        // We draw the top label only if present
        busInfo.getTopLabel().ifPresent(s -> {
            Element labelTop = createLabelElement(s, 0, -LABEL_OFFSET, 0, g);
            g.appendChild(labelTop);
        });
        root.appendChild(g);
    }

    /**
     * For global unicity in all type of container (voltage level, substation, zone), we prefix with the container Id and
     * we rely on the fact that node ids are unique inside a voltage level. We also prepend with a custom prefix id to
     * allow multiple diagrams unicity.
     */
    private static String getWireId(String prefixId, String containerId, Edge edge) {
        return escapeClassName(prefixId + "_" + containerId + "_" + edge.getNode1().getId() + "_" + edge.getNode2().getId());
    }

    private static String getNodeLabelId(String prefixId, Node node, LabelPosition labelPosition) {
        return prefixId + node.getId() + "_" + labelPosition.getPositionName();
    }

    /*
     * Drawing the voltageLevel graph edges
     */
    protected void drawEdges(Element root, VoltageLevelGraph graph, GraphMetadata metadata,
                             LabelProvider initProvider, StyleProvider styleProvider, Collection<Edge> edges) {
        String voltageLevelId = graph.getVoltageLevelInfos().getId();
        String prefixId = metadata.getSvgParameters().getPrefixId();

        for (Edge edge : edges) {
            String wireId = getWireId(prefixId, voltageLevelId, edge);

            List<Point> pol = new ArrayList<>();
            if (!edge.isZeroLength()) {
                // Determine points of the polyline
                Point shift = graph.getCoord();
                pol = WireConnection.searchBestAnchorPoints(componentLibrary, graph, edge.getNode1(), edge.getNode2())
                        .calculatePolylinePoints(edge.getNode1(), edge.getNode2(), svgParameters.isDrawStraightWires(), shift);

                if (!pol.isEmpty()) {
                    Element g = root.getOwnerDocument().createElement(GROUP);

                    g.setAttribute("id", wireId);
                    writeStyleClasses(g, styleProvider.getEdgeStyles(graph, edge));

                    Element polyline = root.getOwnerDocument().createElement(POLYLINE);
                    polyline.setAttribute(POINTS, pointsListToString(pol));

                    g.appendChild(polyline);
                    root.appendChild(g);
                }
            }

            metadata.addWireMetadata(new GraphMetadata.WireMetadata(wireId,
                    escapeId(edge.getNode1().getId()),
                    escapeId(edge.getNode2().getId()),
                    svgParameters.isDrawStraightWires(),
                    false));

            if (edge.getNode1() instanceof FeederNode) {
                if (!(edge.getNode2() instanceof FeederNode)) {
                    insertFeederInfos(prefixId, pol, root, graph, (FeederNode) edge.getNode1(), metadata, initProvider, styleProvider);
                }
            } else if (edge.getNode2() instanceof FeederNode) {
                Collections.reverse(pol);
                insertFeederInfos(prefixId, pol, root, graph, (FeederNode) edge.getNode2(), metadata, initProvider, styleProvider);
            }
        }
    }

    /*
     * Drawing the zone graph edges (snakelines between station diagram)
     */
    protected void drawSnakeLines(Element root, ZoneGraph graph,
                                  GraphMetadata metadata, StyleProvider styleProvider) {
        for (BranchEdge edge : graph.getLineEdges()) {
            drawSnakeLines(graph, edge, root, metadata, styleProvider);
        }
    }

    /*
     * Drawing the substation graph edges (snakelines between voltageLevel diagram)
     */
    protected void drawSnakeLines(Element root, BaseGraph graph,
                                  GraphMetadata metadata, StyleProvider styleProvider) {
        for (BranchEdge edge : graph.getLineEdges()) {
            drawSnakeLines(graph, edge, root, metadata, styleProvider);
        }

        for (BranchEdge edge : graph.getTwtEdges()) {
            drawSnakeLines(graph, edge, root, metadata, styleProvider);
        }
    }

    private void drawSnakeLines(Graph graph, BranchEdge edge, Element root, GraphMetadata metadata, StyleProvider styleProvider) {
        Element g = root.getOwnerDocument().createElement(GROUP);
        String snakeLineId = escapeId(metadata.getSvgParameters().getPrefixId() + edge.getId());
        g.setAttribute("id", snakeLineId);
        writeStyleClasses(g, styleProvider.getEdgeStyles(graph, edge));
        root.appendChild(g);

        // Get the points of the snakeLine, already calculated during the layout application
        List<Point> pol = edge.getSnakeLine();
        if (!pol.isEmpty() && graph.getVoltageLevelGraph(edge.getNode2()) == null) {
            // Note that edge.getNode2() might be outside the voltageLevelGraph (multiTermNode between voltage levels),
            // whereas edge.getNode1() is supposed to always be a FeederNode in a voltageLevelGraph
            // Snakeline between two feeder nodes, no need to adapt
            adaptCoordSnakeLine(edge, pol, graph);
        }

        Element polyline = root.getOwnerDocument().createElement(POLYLINE);
        polyline.setAttribute(POINTS, pointsListToString(pol));
        g.appendChild(polyline);

        metadata.addWireMetadata(new GraphMetadata.WireMetadata(snakeLineId,
                escapeId(edge.getNode1().getId()),
                escapeId(edge.getNode2().getId()),
                svgParameters.isDrawStraightWires(),
                true));
    }

    /*
     * Adaptation of the previously calculated snakeLine points, in order to use the anchor points
     * if a node is outside any graph
     */
    private void adaptCoordSnakeLine(BranchEdge edge, List<Point> pol, Graph graph) {
        // Getting the right polyline point from where we need to compute the best anchor point
        Point multiTermPoint = pol.get(pol.size() - 1);
        Point pointBeforeNode = pol.get(Math.max(pol.size() - 2, 0));

        AnchorPoint bestAnchorPoint = WireConnection.getBestAnchorPoint(componentLibrary, graph, edge.getNode2(), pointBeforeNode);

        if (multiTermPoint.getX() == pointBeforeNode.getX()) {
            pointBeforeNode.shiftX(bestAnchorPoint.getX()); // vertical line remains vertical
        } else if (multiTermPoint.getY() == pointBeforeNode.getY()) {
            pointBeforeNode.shiftY(bestAnchorPoint.getY()); // horizontal line remains horizontal
        }
        multiTermPoint.shift(bestAnchorPoint);
    }

    protected String pointsListToString(List<Point> polyline) {
        return polyline.stream()
            .map(pt -> pt.getX() + "," + pt.getY())
            .collect(Collectors.joining(","));
    }

    /**
     * Creation of the defs area for the SVG components
     */
    protected void createDefsSVGComponents(Document document, Set<String> listUsedComponentSVG) {
        if (svgParameters.isAvoidSVGComponentsDuplication()) {
            // adding also arrows
            listUsedComponentSVG.add(ARROW_ACTIVE);
            listUsedComponentSVG.add(ARROW_REACTIVE);

            Element defs = document.createElement("defs");

            listUsedComponentSVG.forEach(c -> {
                Map<String, List<Element>> subComponents = componentLibrary.getSvgElements(c);
                if (subComponents != null) {
                    Element group = document.createElement(GROUP);
                    group.setAttribute("id", c);

                    insertSVGComponentIntoDefsArea(c, group, subComponents);

                    defs.getOwnerDocument().adoptNode(group);
                    defs.appendChild(group);
                }
            });

            document.adoptNode(defs);
            document.getDocumentElement().appendChild(defs);
        }
    }

    protected void insertSVGComponentIntoDefsArea(String componentType, Element group, Map<String, List<Element>> subComponents) {
        for (Map.Entry<String, List<Element>> subComponent : subComponents.entrySet()) {
            if (subComponents.size() > 1) {
                Element subComponentGroup = group.getOwnerDocument().createElement("g");
                subComponentGroup.setAttribute("id", getHRefValue(subComponents.size(), componentType, subComponent.getKey()));
                addSvgSubComponentsToElement(subComponent.getValue(), subComponentGroup);
                group.getOwnerDocument().adoptNode(subComponentGroup);
                group.appendChild(subComponentGroup);
            } else {
                addSvgSubComponentsToElement(subComponent.getValue(), group);
            }
        }
    }

    private void addSvgSubComponentsToElement(List<Element> subComponentElements, Element group) {
        for (Element subComponentElement : subComponentElements) {
            org.w3c.dom.Node n = subComponentElement.cloneNode(true);
            group.getOwnerDocument().adoptNode(n);
            group.appendChild(n);
        }
    }

    private void drawZone(ZoneGraph graph,
                          Element root,
                          GraphMetadata metadata,
                          LabelProvider initProvider,
                          StyleProvider styleProvider) {
        for (SubstationGraph sGraph : graph.getSubstations()) {
            drawSubstation(sGraph, root, metadata, initProvider, styleProvider);
        }

        drawSnakeLines(root, graph, metadata, styleProvider);
    }

    /*
     * Drawing the voltageLevel nodes infos
     */
    private void drawBusLegendInfo(BusLegendInfo busLegendInfo, double xShift, double yShift,
                                   Element g, String idNode, VoltageLevelGraph graph, StyleProvider styleProvider) {

        Element circle = g.getOwnerDocument().createElement("circle");

        // colored circle
        circle.setAttribute("id", IdUtil.escapeId(idNode + "_circle"));
        circle.setAttribute("cx", String.valueOf(xShift));
        circle.setAttribute("cy", String.valueOf(yShift));
        circle.setAttribute("r", String.valueOf(CIRCLE_RADIUS_NODE_INFOS_SIZE));
        circle.setAttribute("stroke-width", String.valueOf(CIRCLE_RADIUS_NODE_INFOS_SIZE));
        writeStyleClasses(circle, styleProvider.getBusStyles(busLegendInfo.busId(), graph));
        g.appendChild(circle);

        // legend nodes
        double padding = 2.5;
        for (BusLegendInfo.Caption caption : busLegendInfo.captions()) {
            Element label = g.getOwnerDocument().createElement("text");
            writeStyleClasses(label, styleProvider.getBusLegendCaptionStyles(caption), StyleClassConstants.BUS_LEGEND_INFO);
            label.setAttribute("id", IdUtil.escapeId(idNode + "_" + caption.type()));
            label.setAttribute("x", String.valueOf(xShift - CIRCLE_RADIUS_NODE_INFOS_SIZE));
            label.setAttribute("y", String.valueOf(yShift + padding * CIRCLE_RADIUS_NODE_INFOS_SIZE));
            Text textNode = g.getOwnerDocument().createTextNode(caption.label());
            label.appendChild(textNode);
            g.appendChild(label);

            padding += 1.5;
        }
    }

    private void drawBusesLegend(Element root, VoltageLevelGraph graph,
                                 GraphMetadata metadata, LabelProvider labelProvider, StyleProvider styleProvider) {

        Element nodesInfosNode = root.getOwnerDocument().createElement(GROUP);
        root.appendChild(nodesInfosNode);
        nodesInfosNode.setAttribute(CLASS, StyleClassConstants.LEGEND);

        double xInitPos = layoutParameters.getDiagramPadding().getLeft() + CIRCLE_RADIUS_NODE_INFOS_SIZE;
        double yPos = graph.getY() - layoutParameters.getVoltageLevelPadding().getTop() + graph.getHeight() + CIRCLE_RADIUS_NODE_INFOS_SIZE;

        double xShift = graph.getX() + xInitPos;
        for (BusLegendInfo busLegendInfo : labelProvider.getBusLegendInfos(graph)) {
            String idNode = metadata.getSvgParameters().getPrefixId() + "NODE_" + busLegendInfo.busId();
            String escapedIdNode = IdUtil.escapeId(idNode);
            Element gNode = nodesInfosNode.getOwnerDocument().createElement(GROUP);
            gNode.setAttribute("id", escapedIdNode);

            drawBusLegendInfo(busLegendInfo, xShift, yPos, gNode, idNode, graph, styleProvider);

            nodesInfosNode.appendChild(gNode);

            metadata.addBusLegendInfoMetadata(new GraphMetadata.BusLegendInfoMetadata(escapedIdNode));

            xShift += 2 * CIRCLE_RADIUS_NODE_INFOS_SIZE + 50;
        }
    }

    private void writeStyleClasses(Element element, List<String> styleClasses) {
        writeStyleClasses(element, styleClasses, null);
    }

    private void writeStyleClasses(Element element, List<String> styleClasses, String additionalClass) {
        if (styleClasses.isEmpty() && additionalClass == null) {
            return;
        }
        List<String> allClasses = styleClasses;
        if (additionalClass != null) {
            allClasses = new ArrayList<>(styleClasses);
            allClasses.add(additionalClass);
        }
        element.setAttribute(CLASS, String.join(" ", allClasses));
    }
}
