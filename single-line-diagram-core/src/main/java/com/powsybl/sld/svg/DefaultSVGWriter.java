/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.commons.exceptions.UncheckedTransformerException;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.AnchorOrientation;
import com.powsybl.sld.library.AnchorPoint;
import com.powsybl.sld.library.AnchorPointProvider;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ComponentMetadata;
import com.powsybl.sld.library.ComponentSize;
import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.Cell;
import com.powsybl.sld.model.Edge;
import com.powsybl.sld.model.ExternCell;
import com.powsybl.sld.model.FeederNode;
import com.powsybl.sld.model.FeederWithSideNode;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.LineEdge;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.SwitchNode;
import com.powsybl.sld.model.TwtEdge;
import com.powsybl.sld.model.VoltageLevelInfos;
import com.powsybl.sld.model.ZoneGraph;
import com.powsybl.sld.svg.DiagramLabelProvider.Direction;
import com.powsybl.sld.svg.GraphMetadata.ArrowMetadata;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.powsybl.sld.library.ComponentTypeName.ARROW;
import static com.powsybl.sld.library.ComponentTypeName.BREAKER;
import static com.powsybl.sld.library.ComponentTypeName.BUSBAR_SECTION;
import static com.powsybl.sld.library.ComponentTypeName.BUSBREAKER_CONNECTION;
import static com.powsybl.sld.library.ComponentTypeName.DISCONNECTOR;
import static com.powsybl.sld.library.ComponentTypeName.NODE;
import static com.powsybl.sld.library.ComponentTypeName.PHASE_SHIFT_TRANSFORMER;
import static com.powsybl.sld.library.ComponentTypeName.THREE_WINDINGS_TRANSFORMER;
import static com.powsybl.sld.library.ComponentTypeName.TWO_WINDINGS_TRANSFORMER;
import static com.powsybl.sld.model.Position.Dimension.H;
import static com.powsybl.sld.model.Position.Dimension.V;
import static com.powsybl.sld.svg.DiagramStyles.WIRE_STYLE_CLASS;
import static com.powsybl.sld.svg.DiagramStyles.escapeClassName;
import static com.powsybl.sld.svg.DiagramStyles.escapeId;

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
    protected static final int CIRCLE_RADIUS_NODE_INFOS_SIZE = 10;
    protected static final String FONT_FAMILY_ATTRIBUTE = "font-family";
    protected static final String FONT_SIZE_ATTRIBUTE = "font-size";

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

        drawVoltageLevel(prefixId, graph, root, metadata, initProvider, styleProvider, true);

        document.adoptNode(root);
        document.getDocumentElement().appendChild(root);

        return metadata;
    }

    protected void drawVoltageLevel(String prefixId,
                                    Graph graph,
                                    Element root,
                                    GraphMetadata metadata,
                                    DiagramLabelProvider initProvider,
                                    DiagramStyleProvider styleProvider,
                                    boolean useNodesInfosParam) {
        AnchorPointProvider anchorPointProvider = (type, id) -> {
            if (type.equals(BUSBAR_SECTION)) {
                BusNode busbarSectionNode = (BusNode) graph.getNode(id);
                List<AnchorPoint> result = new ArrayList<>();
                result.add(new AnchorPoint(0, 0, AnchorOrientation.HORIZONTAL));
                for (int i = 1; i < busbarSectionNode.getPosition().getSpan(H); i++) {
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

        if (useNodesInfosParam && layoutParameters.isAddNodesInfos()) {
            drawNodesInfos(prefixId, root, graph, styleProvider);
        }
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
            drawVoltageLevel(prefixId, vlGraph, root, metadata, initProvider, styleProvider, false);
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
                .mapToInt(nodeBus -> (nodeBus.getPosition().get(H) + nodeBus.getPosition().getSpan(H)) / 2)
                .max().orElse(0);
        int maxV = graph.getNodeBuses().stream()
                .mapToInt(nodeBus -> nodeBus.getPosition().get(V))
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
        label.setAttribute(FONT_FAMILY_ATTRIBUTE, FONT_FAMILY);
        label.setAttribute(FONT_SIZE_ATTRIBUTE, Integer.toString(fontSize));
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
                || node.getComponentType().equals(NODE)
                || node.getComponentType().equals(BUSBREAKER_CONNECTION));
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
                // permutation if cell direction is BOTTOM,
                // because in the svg component library, circle for winding1 is below circle for winding2
                node.setRotationAngle(180.);
            }
        } else {  // node outside any graph
            List<Node> adjacentNodes = node.getAdjacentNodes();
            adjacentNodes.sort(Comparator.comparingDouble(Node::getX));
            if (adjacentNodes.size() == 2) {  // 2 windings transformer
                FeederWithSideNode node1 = (FeederWithSideNode) adjacentNodes.get(0);
                FeederWithSideNode node2 = (FeederWithSideNode) adjacentNodes.get(1);
                List<Edge> edges = node.getAdjacentEdges();
                List<Double> pol1 = ((TwtEdge) edges.get(0)).getSnakeLine();
                List<Double> pol2 = ((TwtEdge) edges.get(1)).getSnakeLine();
                if (!(pol1.isEmpty() || pol2.isEmpty())) {
                    // get points for the line supporting the svg component
                    double x1 = pol1.get(pol1.size() - 4); // absciss of the first polyline second last point
                    double x2 = pol2.get(2);  // absciss of the second polyline third point

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
        insertSVGIntoDocumentSVG(node.getName(), componentType, g, componentDefsId, elementAttributesSetter);
    }

    protected void insertArrowSVGIntoDocumentSVG(String prefixId,
                                                 Element g, double angle,
                                                 ComponentSize componentSize,
                                                 String componentDefsId) {
        BiConsumer<Element, String> elementAttributesSetter
                = (e, subComponent) -> setArrowAttributes(prefixId, g, e, angle, componentSize);
        insertSVGIntoDocumentSVG("", ARROW, g, componentDefsId, elementAttributesSetter);
    }

    private void setArrowAttributes(String prefixId, Element g, Element e,
                                    double angle, ComponentSize componentSize) {
        replaceId(g, e, prefixId);
        if (Math.abs(angle) > 0) {
            double cx = componentSize.getWidth() / 2;
            double cy = componentSize.getHeight() / 2;
            e.setAttribute(TRANSFORM, ROTATE + "(" + angle + "," + cx + "," + cy + ")");
        }
    }

    protected void insertDecoratorSVGIntoDocumentSVG(String prefixId,
                                                     DiagramLabelProvider.NodeDecorator nodeDecorator,
                                                     Element g, Node node,
                                                     DiagramStyleProvider styleProvider) {
        BiConsumer<Element, String> elementAttributesSetter
                = (elt, subComponent) -> setDecoratorAttributes(prefixId, g, node, nodeDecorator, styleProvider, elt, subComponent);
        String nodeDecoratorType = nodeDecorator.getType();
        insertSVGIntoDocumentSVG(nodeDecoratorType, nodeDecoratorType, g, nodeDecoratorType, elementAttributesSetter);
    }

    protected void insertSVGIntoDocumentSVG(String name, String componentType, Element g, String componentDefsId,
                                            BiConsumer<Element, String> elementAttributesSetter) {
        addToolTip(name, g);
        Map<String, SVGOMDocument> subComponents = componentLibrary.getSvgDocument(componentType);
        subComponents.forEach(!layoutParameters.isAvoidSVGComponentsDuplication() ?
            (subComponentName, svgSubComponent) -> insertClonedSubcomponent(g, elementAttributesSetter, subComponentName, svgSubComponent) :
            (subComponentName, svgSubComponent) -> insertSubcomponentReference(g, componentDefsId, elementAttributesSetter, subComponentName, subComponents.size())
        );
    }

    private void insertClonedSubcomponent(Element g, BiConsumer<Element, String> elementAttributesSetter, String subComponentName, SVGOMDocument svgSubComponent) {
        // The following code work correctly considering SVG part describing the component is the first child of the SVGDocument.
        // If SVG are written differently, it will not work correctly.
        NodeList subComponentChildren = svgSubComponent.getChildNodes().item(0).getChildNodes();
        for (int i = 0; i < subComponentChildren.getLength(); i++) {
            org.w3c.dom.Node n = subComponentChildren.item(i).cloneNode(true);
            if (n instanceof Element) {
                setAttributesAndInsertElement(g, elementAttributesSetter, subComponentName, (Element) n);
            }
        }
    }

    private void insertSubcomponentReference(Element g, String componentDefsId, BiConsumer<Element, String> elementAttributesSetter, String subComponentName, int nbSubComponents) {
        // Adding <use> markup to reuse the svg defined in the <defs> part
        Element eltUse = g.getOwnerDocument().createElement("use");
        String hRefValue = nbSubComponents > 1 ? componentDefsId + "-" + subComponentName : componentDefsId;
        eltUse.setAttribute("href", "#" + hRefValue);
        setAttributesAndInsertElement(g, elementAttributesSetter, subComponentName, eltUse);
    }

    private void setAttributesAndInsertElement(Element g, BiConsumer<Element, String> elementAttributesSetter, String subComponentName, Element element) {
        elementAttributesSetter.accept(element, subComponentName);
        g.getOwnerDocument().adoptNode(element);
        g.appendChild(element);
    }

    private void addToolTip(String tooltip, Element g) {
        if (layoutParameters.isTooltipEnabled() && !tooltip.isEmpty()) {
            Document doc = g.getOwnerDocument();
            Element title = doc.createElement("title");
            title.appendChild(doc.createTextNode(tooltip));
            doc.adoptNode(title);
            g.appendChild(title);
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
        ComponentSize decoratorSize = componentLibrary.getSize(nodeDecorator.getType());
        LabelPosition decoratorPosition = nodeDecorator.getPosition();
        elt.setAttribute(TRANSFORM, getTransformStringDecorator(node, decoratorPosition, decoratorSize));
        Map<String, String> svgStyle = styleProvider.getSvgNodeStyleAttributes(node, decoratorSize, subComponentName, layoutParameters.isShowInternalNodes());
        svgStyle.forEach(elt::setAttribute);
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
        String transform;
        if (node.isRotated()) {
            double[] matrix = getDecoratorTransformMatrix(node, decoratorPosition, decoratorSize);
            transform = transformMatrixToString(matrix, 4);
        } else {
            ComponentSize componentSize = componentLibrary.getSize(node.getComponentType());
            double dX = componentSize.getWidth() / 2 + decoratorPosition.getdX();
            double dY = componentSize.getHeight() / 2 + decoratorPosition.getdY();
            if (decoratorPosition.isCentered()) {
                dX -= decoratorSize.getWidth() / 2;
                dY -= decoratorSize.getHeight() / 2;
            }
            transform = TRANSLATE + "(" + dX + "," + dY + ")";
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
        return new double[]{translateX, translateY};
    }

    private double[] getDecoratorTransformMatrix(Node node, LabelPosition decoratorPosition, ComponentSize decoratorSize) {
        ComponentSize componentSize = componentLibrary.getSize(node.getComponentType());
        double[] translateNode = getNodeTranslate(node);
        double[] matrixNode = getTransformMatrix(componentSize.getWidth(), componentSize.getHeight(), node.getRotationAngle() * Math.PI / 180,
                layoutParameters.getTranslateX() + node.getX(), layoutParameters.getTranslateY() + node.getY());
        double translateDecoratorX = translateNode[0] + componentSize.getWidth() / 2 + decoratorPosition.getdX();
        double translateDecoratorY = translateNode[1] + componentSize.getHeight() / 2 + decoratorPosition.getdY();
        if (decoratorPosition.isCentered()) {
            translateDecoratorX -= decoratorSize.getWidth() / 2;
            translateDecoratorY -= decoratorSize.getHeight() / 2;
        }
        double t1 = +matrixNode[3] * (translateDecoratorX - matrixNode[4]) - matrixNode[2] * (translateDecoratorY - matrixNode[5]);
        double t2 = -matrixNode[1] * (translateDecoratorX - matrixNode[4]) + matrixNode[0] * (translateDecoratorY - matrixNode[5]);
        return new double[]{matrixNode[3], -matrixNode[1], -matrixNode[2], matrixNode[0], t1, t2};
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
            // Calculate cos and sin of the angle between the wire line and the abscisse
            double cosAngle = dx / distancePoints;
            double sinAngle = dy / distancePoints;

            double dist = this.layoutParameters.getArrowDistance();
            double x = x1 + cosAngle * (dist + shift);
            double y = y1 + sinAngle * (dist + shift);

            double arrowRotationAngle = Math.atan(dy / dx) - Math.PI / 2;
            if (arrowRotationAngle < -Math.PI / 2) {
                arrowRotationAngle += Math.PI;
            }
            g.setAttribute(TRANSFORM, getTransformMatrixString(x, y, arrowRotationAngle, componentSize));
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

    protected void insertArrowsAndLabels(String prefixId,
                                         String wireId,
                                         List<Double> points,
                                         Element root,
                                         Node n,
                                         GraphMetadata metadata,
                                         DiagramLabelProvider initProvider,
                                         DiagramStyleProvider styleProvider) {
        InitialValue init = initProvider.getInitialValue(n);

        // we draw the arrow only if value 1 is present
        init.getLabel1()
                .ifPresent(lb ->
                        drawArrowAndLabel(prefixId, wireId, points, root, n, lb, init.getLabel3(), init.getArrowDirection1(), 0, 1, metadata, styleProvider));

        // we draw the arrow only if value 2 is present
        init.getLabel2()
                .ifPresent(lb -> {
                    double shiftArrow2 = 2 * metadata.getComponentMetadata(ARROW).getSize().getHeight();
                    drawArrowAndLabel(prefixId, wireId, points, root, n, lb, init.getLabel4(),
                            init.getArrowDirection2(), shiftArrow2, 2, metadata, styleProvider);
                });
    }

    private void drawArrowAndLabel(String prefixId, String wireId, List<Double> points, Element root, Node n,
                                   String labelR, Optional<String> labelL, Optional<Direction> dir, double shift, int iArrow,
                                   GraphMetadata metadata, DiagramStyleProvider styleProvider) {
        ComponentMetadata cd = metadata.getComponentMetadata(ARROW);

        double shX = cd.getSize().getWidth() + LABEL_OFFSET;
        double shY = cd.getSize().getHeight() - LABEL_OFFSET + (double) FONT_SIZE / 2;

        double y1 = points.get(1);
        double y2 = points.get(3);

        Element g = root.getOwnerDocument().createElement("g");
        String arrowWireId = wireId + "_ARROW" + iArrow;
        g.setAttribute("id", arrowWireId);
        transformArrow(points, cd.getSize(), shift, g);

        String defsId = dir.map(direction -> ARROW + "-arrow-" + direction.name().toLowerCase()).orElse(ARROW);
        insertArrowSVGIntoDocumentSVG(prefixId, g, y1 > y2 ? 180 : 0, cd.getSize(), defsId);
        drawLabel(null, StringUtils.rightPad(labelR, VALUE_MAX_NB_CHARS), false, shX, shY, g, FONT_SIZE, false, 0, true);

        if (dir.isPresent()) {
            g.setAttribute(CLASS, "ARROW" + iArrow + "_" + escapeClassName(n.getId()) + "_" + dir.get());
            if (layoutParameters.isAvoidSVGComponentsDuplication()) {
                styleProvider.getSvgArrowStyleAttributes(iArrow).forEach(((Element) g.getFirstChild())::setAttribute);
            }
        }

        labelL.ifPresent(s -> drawLabel(null, StringUtils.rightPad(s, VALUE_MAX_NB_CHARS), false, -(s.length() * (double) FONT_SIZE / 2 + LABEL_OFFSET), shY, g, FONT_SIZE, false, 0, true));

        root.appendChild(g);
        metadata.addArrowMetadata(new ArrowMetadata(arrowWireId, wireId, layoutParameters.getArrowDistance()));

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

   /*
     * Drawing the voltageLevel nodes infos
     */
    private void drawNodeInfos(ElectricalNodeInfo nodeInfo,
                               double xShift,
                               double yShift,
                               Element g,
                               String idNode,
                               int fontSize,
                               double circleRadiusSize) {
        Element circle = g.getOwnerDocument().createElement("circle");

        circle.setAttribute("id", idNode + "_circle");
        circle.setAttribute("cx", String.valueOf(xShift));
        circle.setAttribute("cy", String.valueOf(yShift));
        circle.setAttribute("r", String.valueOf(circleRadiusSize));
        circle.setAttribute("fill", nodeInfo.getColor());
        g.appendChild(circle);

        // v
        Element labelV = g.getOwnerDocument().createElement("text");
        labelV.setAttribute("id", idNode + "_v");
        String valueV = !Double.isNaN(nodeInfo.getV())
                ? String.valueOf(Precision.round(nodeInfo.getV(), 1))
                : "\u2014";  // em dash unicode for undefined value
        valueV += " kV";

        labelV.setAttribute("x", String.valueOf(xShift - circleRadiusSize));
        labelV.setAttribute("y", String.valueOf(yShift + 2.5 * circleRadiusSize));
        labelV.setAttribute(FONT_FAMILY_ATTRIBUTE, FONT_FAMILY);
        labelV.setAttribute(FONT_SIZE_ATTRIBUTE, Integer.toString(fontSize));
        labelV.setAttribute(CLASS, DiagramStyles.LABEL_STYLE_CLASS);
        Text textV = g.getOwnerDocument().createTextNode(valueV);
        labelV.appendChild(textV);
        g.appendChild(labelV);

        // angle
        Element labelAngle = g.getOwnerDocument().createElement("text");
        labelAngle.setAttribute("id", idNode + "_angle");
        String valueAngle = !Double.isNaN(nodeInfo.getAngle())
                ? String.valueOf(Precision.round(nodeInfo.getAngle(), 1))
                : "\u2014";  // em dash unicode for undefined value
        valueAngle += " \u00b0";  // degree sign unicode for degree symbol

        labelAngle.setAttribute("x", String.valueOf(xShift - circleRadiusSize));
        labelAngle.setAttribute("y", String.valueOf(yShift + 4 * circleRadiusSize));
        labelAngle.setAttribute(FONT_FAMILY_ATTRIBUTE, FONT_FAMILY);
        labelAngle.setAttribute(FONT_SIZE_ATTRIBUTE, Integer.toString(fontSize));
        labelAngle.setAttribute(CLASS, DiagramStyles.LABEL_STYLE_CLASS);
        Text textAngle = g.getOwnerDocument().createTextNode(valueAngle);
        labelAngle.appendChild(textAngle);
        g.appendChild(labelAngle);
    }

    private void drawNodesInfos(String prefixId,
                                Element root,
                                Graph graph,
                                DiagramStyleProvider styleProvider) {
        double xInitPos = graph.getNodes().stream()
                .filter(n -> n.getType() == Node.NodeType.BUS)
                .mapToDouble(Node::getX).min().getAsDouble() + layoutParameters.getTranslateX() + CIRCLE_RADIUS_NODE_INFOS_SIZE;

        double maxY = graph.getNodes().stream().mapToDouble(Node::getY).max().getAsDouble();
        double yPos = graph.getY() + layoutParameters.getInitialYBus() + maxY - 120;

        List<ElectricalNodeInfo> nodes = styleProvider.getElectricalNodesInfos(graph);

        IntStream.range(0, nodes.size()).forEach(i -> {
            String idNode = prefixId + "NODE_" + i + "_" + graph.getVoltageLevelInfos().getId();
            Element gNode = root.getOwnerDocument().createElement("g");
            gNode.setAttribute("id", idNode);

            drawNodeInfos(nodes.get(i), graph.getX() + xInitPos + (i * (2 * CIRCLE_RADIUS_NODE_INFOS_SIZE + 50)), yPos, gNode, idNode, FONT_SIZE, CIRCLE_RADIUS_NODE_INFOS_SIZE);
            root.appendChild(gNode);
        });
    }
}
