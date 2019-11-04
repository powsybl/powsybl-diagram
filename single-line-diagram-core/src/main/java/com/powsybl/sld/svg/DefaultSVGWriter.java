/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import static com.powsybl.sld.library.ComponentTypeName.ARROW;
import static com.powsybl.sld.library.ComponentTypeName.BREAKER;
import static com.powsybl.sld.library.ComponentTypeName.BUSBAR_SECTION;
import static com.powsybl.sld.library.ComponentTypeName.DISCONNECTOR;
import static com.powsybl.sld.library.ComponentTypeName.INDUCTOR;
import static com.powsybl.sld.library.ComponentTypeName.THREE_WINDINGS_TRANSFORMER;
import static com.powsybl.sld.library.ComponentTypeName.TWO_WINDINGS_TRANSFORMER;
import static com.powsybl.sld.library.ComponentTypeName.NODE;
import static com.powsybl.sld.svg.DiagramStyles.WIRE_STYLE_CLASS;
import static com.powsybl.sld.svg.DiagramStyles.escapeClassName;
import static com.powsybl.sld.svg.DiagramStyles.escapeId;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
import org.w3c.dom.Text;
import org.w3c.dom.svg.SVGElement;

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
import com.powsybl.sld.model.Edge;
import com.powsybl.sld.model.ExternCell;
import com.powsybl.sld.model.Feeder2WTNode;
import com.powsybl.sld.model.FeederBranchNode;
import com.powsybl.sld.model.FeederNode;
import com.powsybl.sld.model.Fictitious3WTNode;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.SwitchNode;
import com.powsybl.sld.model.TwtEdge;
import com.powsybl.sld.svg.DiagramInitialValueProvider.Direction;
import com.powsybl.sld.svg.GraphMetadata.ArrowMetadata;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class DefaultSVGWriter implements SVGWriter {

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
    protected static final String STROKE = "stroke";
    protected static final String WINDING1 = "WINDING1";
    protected static final String WINDING2 = "WINDING2";
    protected static final String WINDING3 = "WINDING3";

    protected final ComponentLibrary componentLibrary;

    protected final LayoutParameters layoutParameters;

    Function<Node, BusCell.Direction> nodeDirection = node ->
            (node instanceof FeederNode && node.getCell() != null) ? ((ExternCell) node.getCell()).getDirection() : BusCell.Direction.UNDEFINED;

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
                               DiagramInitialValueProvider initProvider,
                               DiagramStyleProvider styleProvider,
                               NodeLabelConfiguration nodeLabelConfiguration,
                               Path svgFile) {
        try (Writer writer = Files.newBufferedWriter(svgFile)) {
            return write(prefixId, graph, initProvider, styleProvider, nodeLabelConfiguration, writer);
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
                               DiagramInitialValueProvider initProvider,
                               DiagramStyleProvider styleProvider,
                               NodeLabelConfiguration nodeLabelConfiguration,
                               Writer writer) {
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        Document document = domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);

        Set<String> listUsedComponentSVG = new HashSet<>();
        addStyle(document, styleProvider, Collections.singletonList(graph), listUsedComponentSVG);

        createDefsSVGComponents(document, listUsedComponentSVG);

        GraphMetadata metadata = writegraph(prefixId, graph, document, initProvider, styleProvider, nodeLabelConfiguration);

        transformDocument(document, writer);

        return metadata;
    }

    protected void addStyle(Document document, DiagramStyleProvider styleProvider, List<Graph> graphs, Set<String> listUsedComponentSVG) {
        Element style = document.createElement("style");

        StringBuilder graphStyle = new StringBuilder();
        graphStyle.append(componentLibrary.getStyleSheet());

        for (Graph graph : graphs) {
            graph.getNodes().forEach(n -> {
                Optional<String> nodeStyle = styleProvider.getNodeStyle(n, layoutParameters.isAvoidSVGComponentsDuplication(), layoutParameters.isShowInternalNodes());
                nodeStyle.ifPresent(graphStyle::append);
                listUsedComponentSVG.add(n.getComponentType());
            });
            graph.getEdges().forEach(e -> {
                Optional<String> wireStyle = styleProvider.getWireStyle(e);
                wireStyle.ifPresent(graphStyle::append);
            });
        }
        String cssStr = graphStyle.toString()
                .replace("\r\n", "\n") // workaround for https://bugs.openjdk.java.net/browse/JDK-8133452
                .replace("\r", "\n");
        CDATASection cd = document.createCDATASection(cssStr);
        style.appendChild(cd);
        style.appendChild(cd);

        document.adoptNode(style);
        document.getDocumentElement().appendChild(style);
    }

    protected void transformDocument(Document document, Writer writer) {
        try {
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(writer);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
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
    protected GraphMetadata writegraph(String prefixId,
                                       Graph graph,
                                       Document document,
                                       DiagramInitialValueProvider initProvider,
                                       DiagramStyleProvider styleProvider,
                                       NodeLabelConfiguration nodeLabelConfiguration) {
        GraphMetadata metadata = new GraphMetadata();

        Element root = document.createElement("g");

        if (layoutParameters.isShowGrid()) {
            root.appendChild(drawGrid(prefixId, graph, document, metadata));
        }

        drawVoltageLevel(prefixId, graph, root, metadata, initProvider, styleProvider, nodeLabelConfiguration);

        // the drawing of the voltageLevel graph label is done at the end in order to
        // facilitate the move of a voltageLevel in the diagram
        drawGraphLabel(prefixId, root, graph, metadata);

        document.adoptNode(root);
        document.getDocumentElement().appendChild(root);

        return metadata;
    }

    protected void drawVoltageLevel(String prefixId,
                                    Graph graph,
                                    Element root,
                                    GraphMetadata metadata,
                                    DiagramInitialValueProvider initProvider,
                                    DiagramStyleProvider styleProvider,
                                    NodeLabelConfiguration nodeLabelConfiguration) {
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

        if (layoutParameters.isShiftFeedersPosition()) {
            shiftFeedersPosition(graph, layoutParameters.getScaleShiftFeedersPosition());
        }

        // To avoid overlapping lines over the switches, first, we draw all nodes except the switch nodes,
        // then we draw all the edges, and finally we draw the switch nodes
        drawNodes(prefixId, root, graph, metadata, anchorPointProvider, initProvider, styleProvider, nodeLabelConfiguration, n -> !(n instanceof SwitchNode));
        drawEdges(prefixId, root, graph, metadata, anchorPointProvider, initProvider, styleProvider);
        drawNodes(prefixId, root, graph, metadata, anchorPointProvider, initProvider, styleProvider, nodeLabelConfiguration, n -> n instanceof SwitchNode);
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
                               DiagramInitialValueProvider initProvider,
                               DiagramStyleProvider styleProvider,
                               NodeLabelConfiguration nodeLabelConfiguration,
                               Path svgFile) {
        try (Writer writer = Files.newBufferedWriter(svgFile)) {
            return write(prefixId, graph, initProvider, styleProvider, nodeLabelConfiguration, writer);
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
                               DiagramInitialValueProvider initProvider,
                               DiagramStyleProvider styleProvider,
                               NodeLabelConfiguration nodeLabelConfiguration,
                               Writer writer) {
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        Document document = domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);

        Set<String> listUsedComponentSVG = new HashSet<>();
        addStyle(document, styleProvider, graph.getNodes(), listUsedComponentSVG);

        createDefsSVGComponents(document, listUsedComponentSVG);

        GraphMetadata metadata = writegraph(prefixId, graph, document, initProvider, styleProvider, nodeLabelConfiguration);

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
    protected GraphMetadata writegraph(String prefixId,
                                       SubstationGraph graph,
                                       Document document,
                                       DiagramInitialValueProvider initProvider,
                                       DiagramStyleProvider styleProvider,
                                       NodeLabelConfiguration nodeLabelConfiguration) {
        GraphMetadata metadata = new GraphMetadata();

        Element root = document.createElement("g");

        // Drawing grid lines
        if (layoutParameters.isShowGrid()) {
            for (Graph vlGraph : graph.getNodes()) {
                root.appendChild(drawGrid(prefixId, vlGraph, document, metadata));
            }
        }

        drawSubstation(prefixId, graph, root, metadata, initProvider, styleProvider, nodeLabelConfiguration);

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
                                  DiagramInitialValueProvider initProvider,
                                  DiagramStyleProvider styleProvider,
                                  NodeLabelConfiguration nodeLabelConfiguration) {
        // Drawing the voltageLevels
        for (Graph vlGraph : graph.getNodes()) {
            drawVoltageLevel(prefixId, vlGraph, root, metadata, initProvider, styleProvider, nodeLabelConfiguration);
        }

        drawSnakeLines(prefixId, root, graph, metadata);
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

        String gridId = prefixId + "GRID_" + graph.getVoltageLevelId();
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
                graph.getVoltageLevelId(),
                null,
                null,
                null,
                false,
                BusCell.Direction.UNDEFINED,
                false));

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
    protected void drawNodes(String prefixId, Element root, Graph graph, GraphMetadata metadata,
                             AnchorPointProvider anchorPointProvider,
                             DiagramInitialValueProvider initProvider,
                             DiagramStyleProvider styleProvider,
                             NodeLabelConfiguration nodeLabelConfiguration,
                             Predicate<Node> predicate) {
        graph.getNodes().stream().filter(predicate).forEach(node -> {

            String nodeId = DiagramStyles.escapeId(prefixId + node.getId());
            Element g = root.getOwnerDocument().createElement("g");
            g.setAttribute("id", nodeId);

            g.setAttribute(CLASS, node.getComponentType() + " " + nodeId);

            if (node.getType() == Node.NodeType.BUS) {
                drawBus((BusNode) node, g);
            } else {
                incorporateComponents(prefixId, node, g, styleProvider);
            }

            BusCell.Direction direction = (node instanceof FeederNode && node.getCell() != null) ? ((ExternCell) node.getCell()).getDirection() : BusCell.Direction.UNDEFINED;

            if (!node.isFictitious()) {
                drawNodeLabel(prefixId, g, node, initProvider, nodeLabelConfiguration);
            }
            root.appendChild(g);

            setMetadata(metadata, node, nodeId, graph, direction, anchorPointProvider);
        });
    }

    protected void setMetadata(GraphMetadata metadata, Node node, String nodeId, Graph graph, BusCell.Direction direction, AnchorPointProvider anchorPointProvider) {
        String nextVId = null;
        if (node instanceof FeederBranchNode) {
            nextVId = ((FeederBranchNode) node).getVIdOtherSide();
        }

        metadata.addNodeMetadata(
                new GraphMetadata.NodeMetadata(nodeId, graph.getVoltageLevelId(), nextVId,
                        node.getComponentType(), node.getRotationAngle(),
                        node.isOpen(), direction, false));
        if (node.getType() == Node.NodeType.BUS) {
            metadata.addComponentMetadata(new ComponentMetadata(BUSBAR_SECTION,
                    nodeId,
                    anchorPointProvider.getAnchorPoints(BUSBAR_SECTION, node.getId()),
                    new ComponentSize(0, 0), true));
        } else {
            if (metadata.getComponentMetadata(node.getComponentType()) == null) {
                metadata.addComponentMetadata(new ComponentMetadata(node.getComponentType(),
                        null,
                        componentLibrary.getAnchorPoints(node.getComponentType()),
                        componentLibrary.getSize(node.getComponentType()), true));
            }
        }
    }

    protected void drawNodeLabel(String prefixId, Element g, Node node,
                                 DiagramInitialValueProvider initProvider,
                                 NodeLabelConfiguration nodeLabelConfiguration) {
        List<String> labelsNode = initProvider.getNodeLabelValue(node);
        List<LabelPosition> labelsPosition = nodeLabelConfiguration.getLabelsPosition(node);

        if (labelsPosition.size() != labelsNode.size()) {
            throw new AssertionError("Number of node labels <> Number of labels positions");
        }

        for (int i = 0; i < labelsNode.size(); ++i) {
            drawLabel(prefixId + labelsPosition.get(i).getPositionName(), labelsNode.get(i), node.isRotated(),
                    labelsPosition.get(i).getdX(), labelsPosition.get(i).getdY(),
                    g, FONT_SIZE);
        }
    }

    /*
     * Drawing the graph label
     */
    protected void drawGraphLabel(String prefixId, Element root, Graph graph, GraphMetadata metadata) {
        // drawing the label of the voltageLevel
        String idLabelVoltageLevel = prefixId + "LABEL_VL_" + graph.getVoltageLevelId();
        Element gLabel = root.getOwnerDocument().createElement("g");
        gLabel.setAttribute("id", idLabelVoltageLevel);

        drawLabel(null, graph.isUseName()
                     ? graph.getVoltageLevelName()
                     : graph.getVoltageLevelId(),
                  false, graph.getX(), graph.getY(), gLabel, FONT_VOLTAGE_LEVEL_LABEL_SIZE);
        root.appendChild(gLabel);

        metadata.addNodeMetadata(new GraphMetadata.NodeMetadata(idLabelVoltageLevel,
                graph.getVoltageLevelId(),
                null,
                null,
                null,
                false,
                BusCell.Direction.UNDEFINED,
                true));
    }

    /*
     * Drawing the voltageLevel graph busbar sections
     */
    protected void drawBus(BusNode node, Element g) {
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
    }

    /*
     * Drawing the voltageLevel graph busbar section names and feeder names
     */
    protected void drawLabel(String idLabel, String str, boolean rotated, double xShift, double yShift, Element g,
                             int fontSize) {
        Element label = g.getOwnerDocument().createElement("text");
        if (!StringUtils.isEmpty(idLabel)) {
            label.setAttribute("id", idLabel);
        }
        label.setAttribute("x", String.valueOf(xShift));
        label.setAttribute("y", String.valueOf(yShift));
        label.setAttribute("font-family", FONT_FAMILY);
        label.setAttribute("font-size", Integer.toString(fontSize));
        label.setAttribute(CLASS, DiagramStyles.LABEL_STYLE_CLASS);
        Text text = g.getOwnerDocument().createTextNode(str);
        label.setAttribute(TRANSFORM, ROTATE + "(" + (rotated ? -90 : 0) + "," + 0 + "," + 0 + ")");
        label.appendChild(text);
        g.appendChild(label);
    }

    protected boolean canInsertComponentSVG(Node node) {
        return (!node.isFictitious() && node.getType() != Node.NodeType.SHUNT) ||
                        (node.isFictitious() && node.getComponentType().equals(THREE_WINDINGS_TRANSFORMER) ||
                                (node.isFictitious() && node.getComponentType().equals(NODE)));

    }

    protected void incorporateComponents(String prefixId, Node node, Element g, DiagramStyleProvider styleProvider) {
        SVGOMDocument obj = componentLibrary.getSvgDocument(node.getComponentType());
        transformComponent(node, g);
        if (obj != null && canInsertComponentSVG(node)) {
            String componentDefsId = node.getComponentType();
            if (node.getComponentType().equals(BREAKER)
                    || node.getComponentType().equals(DISCONNECTOR)) {
                componentDefsId += node.isOpen() ? "-open" : "-closed";
            }
            insertComponentSVGIntoDocumentSVG(prefixId, obj, g, node, styleProvider, componentLibrary.getSize(node.getComponentType()), componentDefsId);
        }
    }

    protected Map<String, String> getAttributesTransformer(Node node,
                                                           String idWinding,
                                                           DiagramStyleProvider styleProvider,
                                                           ComponentSize size) {
        Map<String, String> attributes = new HashMap<>();
        Optional<String> color = Optional.empty();
        String vlId = node.getGraph().getVoltageLevelId();

        // We will rotate the 3WT SVG, if cell orientation is BOTTOM
        boolean rotateSVG = node instanceof Fictitious3WTNode
                && node.getCell() != null
                && ((ExternCell) node.getCell()).getDirection() == BusCell.Direction.BOTTOM;

        if (node instanceof Fictitious3WTNode) {
            color = styleProvider.getNode3WTStyle((Fictitious3WTNode) node, rotateSVG, vlId, idWinding);
        } else {
            color = styleProvider.getNode2WTStyle((Feeder2WTNode) node, idWinding);
        }

        if (color.isPresent()) {
            attributes.put(STROKE, color.get());
        }
        if (rotateSVG) {  // SVG element rotation
            attributes.put(TRANSFORM, ROTATE + "(" + 180 + "," + size.getWidth() / 2 + "," + size.getHeight() / 2 + ")");
            // We store the rotation angle in order to transform correctly the anchor points when further drawing the edges
            node.setRotationAngle(180.);
        }
        return attributes;
    }

    protected Map<String, String> getAttributesInductor(Node node, DiagramStyleProvider styleProvider) {
        Map<String, String> attributes = new HashMap<>();
        Optional<String> color = styleProvider.getColor(((Feeder2WTNode) node).getNominalVOtherSide());
        if (color.isPresent()) {
            attributes.put(STROKE, color.get());
        }
        return attributes;
    }

    protected Map<String, String> getAttributesHiddenNode(Node node, DiagramStyleProvider styleProvider) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("stroke-opacity", "0");
        attributes.put("fill-opacity", "0");
        return attributes;
    }

    /*
     * Handling the transformer SVG part (rotation, colorization)
     */
    protected void handleTransformerSvgDocument(Node node, DiagramStyleProvider styleProvider,
                                              ComponentSize size, org.w3c.dom.Node n) {
        String idWinding = StringUtils.substringAfterLast(((SVGElement) n).getId(), "-");
        getAttributesTransformer(node, idWinding, styleProvider, size).entrySet().stream().forEach(e -> {
            ((Element) n).removeAttribute(e.getKey());
            ((Element) n).setAttribute(e.getKey(), e.getValue());
        });
    }

    /*
     * Handling the inductor SVG part (colorization)
     */
    protected void handleInductorSvgDocument(Node node, DiagramStyleProvider styleProvider, org.w3c.dom.Node n) {
        getAttributesInductor(node, styleProvider).entrySet().stream().forEach(e -> {
            ((Element) n).removeAttribute(e.getKey());
            ((Element) n).setAttribute(e.getKey(), e.getValue());
        });
    }

    protected void insertComponentSVGIntoDocumentSVG(String prefixId,
                                                     SVGOMDocument obj, Element g, Node node,
                                                     DiagramStyleProvider styleProvider,
                                                     ComponentSize size,
                                                     String componentDefsId) {
        if (!layoutParameters.isAvoidSVGComponentsDuplication()) {
            // The following code work correctly considering SVG part describing the component is the first child of "obj" the SVGDocument.
            // If SVG are written otherwise, it will not work correctly.
            for (int i = 0; i < obj.getChildNodes().item(0).getChildNodes().getLength(); i++) {
                org.w3c.dom.Node n = obj.getChildNodes().item(0).getChildNodes().item(i).cloneNode(true);

                if (n instanceof SVGElement) {
                    if (node instanceof Fictitious3WTNode ||
                            (node instanceof Feeder2WTNode && node.getComponentType().equals(TWO_WINDINGS_TRANSFORMER))) {
                        handleTransformerSvgDocument(node, styleProvider, size, n);
                    } else if (node instanceof Feeder2WTNode && node.getComponentType().equals(INDUCTOR)) {
                        handleInductorSvgDocument(node, styleProvider, n);
                    }
                }

                // Adding prefixId and node id before id of n : to ensure unicity of ids
                if (n.getAttributes() != null) {
                    org.w3c.dom.Node nodeId = n.getAttributes().getNamedItem("id");
                    if (nodeId != null) {
                        String nodeIdValue = nodeId.getTextContent();
                        String gIdValue = StringUtils.removeStart(g.getAttribute("id"), prefixId);
                        if (StringUtils.isEmpty(prefixId)) {
                            nodeId.setTextContent(gIdValue + "_" + nodeIdValue);
                        } else {
                            nodeId.setTextContent(prefixId + gIdValue + "_" + nodeIdValue);
                        }
                    }
                }

                g.getOwnerDocument().adoptNode(n);
                g.appendChild(n);
            }
        } else {
            // Adding <use> markup to reuse the svg defined in the <defs> part
            if (node instanceof Fictitious3WTNode || (node instanceof Feeder2WTNode && node.getComponentType().equals(TWO_WINDINGS_TRANSFORMER))) {
                List<String> lsWindings = node instanceof Fictitious3WTNode
                        ? Arrays.asList(WINDING1, WINDING2, WINDING3)
                        : Arrays.asList(WINDING1, WINDING2);
                lsWindings.stream().forEach(s -> {
                    Element eltUse = g.getOwnerDocument().createElement("use");
                    eltUse.setAttribute("href", "#" + componentDefsId + "-" + s);
                    getAttributesTransformer(node, s, styleProvider, size).entrySet().stream().forEach(e -> eltUse.setAttribute(e.getKey(), e.getValue()));
                    g.getOwnerDocument().adoptNode(eltUse);
                    g.appendChild(eltUse);
                });
            } else {
                Element eltUse = g.getOwnerDocument().createElement("use");
                eltUse.setAttribute("href", "#" + componentDefsId);

                if (node instanceof Feeder2WTNode && node.getComponentType().equals(INDUCTOR)) {
                    getAttributesInductor(node, styleProvider).entrySet().stream().forEach(e -> eltUse.setAttribute(e.getKey(), e.getValue()));
                }
                if (!layoutParameters.isShowInternalNodes() && node.getComponentType().equals(NODE)) {
                    getAttributesHiddenNode(node, styleProvider).entrySet().stream().forEach(e -> eltUse.setAttribute(e.getKey(), e.getValue()));
                }
                g.getOwnerDocument().adoptNode(eltUse);
                g.appendChild(eltUse);
            }
        }
    }

    protected void insertRotatedComponentSVGIntoDocumentSVG(String prefixId,
                                                            SVGOMDocument obj, Element g, double angle,
                                                            double cx, double cy,
                                                            String componentDefsId) {
        if (!layoutParameters.isAvoidSVGComponentsDuplication()) {
            // The following code work correctly considering SVG part describing the component is the first child of "obj" the SVGDocument.
            // If SVG are written otherwise, it will not work correctly.
            for (int i = 0; i < obj.getChildNodes().item(0).getChildNodes().getLength(); i++) {
                org.w3c.dom.Node n = obj.getChildNodes().item(0).getChildNodes().item(i).cloneNode(true);
                if (n.getNodeName().equals("g") && n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element e = (Element) n;
                    e.setAttribute(TRANSFORM, ROTATE + "(" + angle + "," + cx + "," + cy + ")");
                }

                // Adding prefixId and g id before id of n : to ensure unicity of ids
                if (n.getAttributes() != null) {
                    org.w3c.dom.Node nodeId = n.getAttributes().getNamedItem("id");
                    if (nodeId != null) {
                        String nodeIdValue = nodeId.getTextContent();
                        String gIdValue = StringUtils.removeStart(g.getAttribute("id"), prefixId);
                        if (StringUtils.isEmpty(prefixId)) {
                            nodeId.setTextContent(gIdValue + "_" + nodeIdValue);
                        } else {
                            nodeId.setTextContent(prefixId + gIdValue + "_" + nodeIdValue);
                        }
                    }
                }

                g.getOwnerDocument().adoptNode(n);
                g.appendChild(n);
            }
        } else {
            // Adding <use> markup to reuse the svg defined in the <defs> part
            Element eltUse = g.getOwnerDocument().createElement("use");
            eltUse.setAttribute("href", "#" + componentDefsId);
            eltUse.setAttribute(TRANSFORM, ROTATE + "(" + angle + "," + cx + "," + cy + ")");

            g.getOwnerDocument().adoptNode(eltUse);
            g.appendChild(eltUse);
        }
    }

    protected void transformComponent(Node node, Element g) {
        ComponentSize componentSize = componentLibrary.getSize(node.getComponentType());

        // For a node marked for rotation during the graph building, but with an svg component not allowed
        // to rotate (ex : disconnector in SVG component library), we cancel the rotation
        if (node.isRotated() && !componentLibrary.isAllowRotation(node.getComponentType())) {
            node.setRotationAngle(null);
        }

        if (!node.isRotated()) {
            g.setAttribute(TRANSFORM,
                    TRANSLATE + "(" + (layoutParameters.getTranslateX() + node.getX() - componentSize.getWidth() / 2) + ","
                            + (layoutParameters.getTranslateY() + node.getY() - componentSize.getHeight() / 2) + ")");
            return;
        }

/*
        afester javafx library does not handle more than one transformation, yet, so
        combine the couple of transformations, translation+rotation, in a single matrix transformation
*/
        int precision = 4;

        double angle = Math.toRadians(node.isRotated() ? 90 : 0);
        double cosRo = Math.cos(angle);
        double sinRo = Math.sin(angle);
        double cdx = componentSize.getWidth() / 2;
        double cdy = componentSize.getHeight() / 2;

        double e1 = layoutParameters.getTranslateX() - cdx * cosRo + cdy * sinRo + node.getX();
        double f1 = layoutParameters.getTranslateY() - cdx * sinRo - cdy * cosRo + node.getY();

        g.setAttribute(TRANSFORM,
                "matrix(" + Precision.round(cosRo, precision) + "," + Precision.round(sinRo, precision)
                        + "," + Precision.round(-sinRo, precision) + "," + Precision.round(cosRo,
                        precision) + ","
                        + Precision.round(e1, precision) + "," + Precision.round(f1, precision) + ")");
    }

    protected void transformArrow(List<Double> points, ComponentSize componentSize, double shift, Element g) {

        double x1 = points.get(0);
        double y1 = points.get(1);
        double x2 = points.get(2);
        double y2 = points.get(3);

        if (points.size() > 4 && Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)) < 3 * componentSize.getHeight()) {
            double x3 = points.get(4);
            double y3 = points.get(5);
            if (Math.sqrt((x3 - x2) * (x3 - x2) + (y3 - y2) * (y3 - y2)) > 3 * componentSize.getHeight()) {
                x1 = x2;
                y1 = y2;
                x2 = x3;
                y2 = y3;
            }
        }
        double dx = x2 - x1;
        double dy = y2 - y1;

        double angle = Math.atan(dx / dy);
        if (!Double.isNaN(angle)) {
            double cosRo = Math.cos(angle);
            double sinRo = Math.sin(angle);
            double cdx = componentSize.getWidth() / 2;
            double cdy = componentSize.getHeight() / 2;

            double dist = this.layoutParameters.getArrowDistance();

            double x = x1 + sinRo * (dist + shift);
            double y = y1 + cosRo * (y1 > y2 ? -(dist + shift) : (dist + shift));

            double e1 = layoutParameters.getTranslateX() - cdx * cosRo + cdy * sinRo + x;
            double f1 = layoutParameters.getTranslateY() - cdx * sinRo - cdy * cosRo + y;

            int precision = 4;
            g.setAttribute(TRANSFORM,
                    "matrix(" + Precision.round(cosRo, precision) + "," + Precision.round(sinRo, precision)
                            + "," + Precision.round(-sinRo, precision) + "," + Precision.round(cosRo,
                            precision) + ","
                            + Precision.round(e1, precision) + "," + Precision.round(f1, precision) + ")");
        }
    }

    protected void insertArrowsAndLabels(String prefixId, String wireId, List<Double> points, Element root, Node n, GraphMetadata metadata, DiagramInitialValueProvider initProvider, DiagramStyleProvider styleProvider) {
        InitialValue init = initProvider.getInitialValue(n);
        ComponentMetadata cd = metadata.getComponentMetadata(ARROW);

        double shX = cd.getSize().getWidth() + LABEL_OFFSET;
        double shY = cd.getSize().getHeight() - LABEL_OFFSET + (double) FONT_SIZE / 2;

        Element g1 = root.getOwnerDocument().createElement("g");
        String arrow1WireId = wireId + "_ARROW1";
        g1.setAttribute("id", arrow1WireId);
        SVGOMDocument arr = componentLibrary.getSvgDocument(ARROW);
        transformArrow(points, cd.getSize(), 0, g1);
        double y1 = points.get(1);
        double y2 = points.get(3);
        String defsId = ARROW;

        Optional<Direction> dir1 = init.getArrowDirection1();
        if (dir1.isPresent()) {
            defsId += dir1.get() == Direction.UP ? "-arrow-up" : "-arrow-down";
        }

        if (y1 > y2) {
            insertRotatedComponentSVGIntoDocumentSVG(prefixId, arr, g1, 180, cd.getSize().getWidth() / 2, cd.getSize().getHeight() / 2, defsId);
        } else {
            insertComponentSVGIntoDocumentSVG(prefixId, arr, g1, n, styleProvider, componentLibrary.getSize(n.getComponentType()), defsId);
        }
        Optional<String> label1 = init.getLabel1();
        if (label1.isPresent()) {
            drawLabel(null, label1.get(), false, shX, shY, g1, FONT_SIZE);
        }
        if (dir1.isPresent()) {
            g1.setAttribute(CLASS, "ARROW1_" + escapeClassName(n.getId()) + "_" + dir1.get());
            if (layoutParameters.isAvoidSVGComponentsDuplication()) {
                styleProvider.getAttributesArrow(1).entrySet().stream().forEach(e -> ((Element) g1.getFirstChild()).setAttribute(e.getKey(), e.getValue()));
            }
        }
        root.appendChild(g1);
        metadata.addArrowMetadata(new ArrowMetadata(arrow1WireId, wireId, layoutParameters.getArrowDistance()));

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
            insertRotatedComponentSVGIntoDocumentSVG(prefixId, arr, g2, 180, 5, 5, defsId);
        } else {
            insertComponentSVGIntoDocumentSVG(prefixId, arr, g2, n, styleProvider, componentLibrary.getSize(n.getComponentType()), defsId);
        }
        Optional<String> label2 = init.getLabel2();
        if (label2.isPresent()) {
            drawLabel(null, label2.get(), false, shX, shY, g2, FONT_SIZE);
        }
        if (dir2.isPresent()) {
            g2.setAttribute(CLASS, "ARROW2_" + escapeClassName(n.getId()) + "_" + dir2.get());
            if (layoutParameters.isAvoidSVGComponentsDuplication()) {
                styleProvider.getAttributesArrow(2).entrySet().stream().forEach(e -> ((Element) g2.getFirstChild()).setAttribute(e.getKey(), e.getValue()));
            }
        }
        Optional<String> label3 = init.getLabel3();
        if (label3.isPresent()) {
            drawLabel(null, label3.get(), false, -(label3.get().length() * (double) FONT_SIZE / 2 + LABEL_OFFSET), shY, g1, FONT_SIZE);
        }
        Optional<String> label4 = init.getLabel4();
        if (label4.isPresent()) {
            drawLabel(null, label4.get(), false, -(label4.get().length() * (double) FONT_SIZE / 2 + LABEL_OFFSET), shY, g2, FONT_SIZE);
        }

        root.appendChild(g2);
        metadata.addArrowMetadata(new ArrowMetadata(arrow2WireId, wireId, layoutParameters.getArrowDistance()));
    }

    /*
     * Drawing the voltageLevel graph edges
     */
    protected void drawEdges(String prefixId, Element root, Graph graph, GraphMetadata metadata, AnchorPointProvider anchorPointProvider, DiagramInitialValueProvider initProvider, DiagramStyleProvider styleProvider) {
        String vId = graph.getVoltageLevelId();

        for (Edge edge : graph.getEdges()) {
            // for unicity purpose (in substation diagram), we prefix the id of the WireMetadata with the voltageLevel id and "_"
            String wireId = escapeId(prefixId + vId + "_Wire" + graph.getEdges().indexOf(edge));

            Element g = root.getOwnerDocument().createElement(POLYLINE);
            g.setAttribute("id", wireId);

            WireConnection anchorPoints = WireConnection.searchBetterAnchorPoints(anchorPointProvider, edge.getNode1(), edge.getNode2());

            // Determine points of the polyline
            List<Double> pol = anchorPoints.calculatePolylinePoints(edge.getNode1(), edge.getNode2(),
                    layoutParameters.isDrawStraightWires());

            g.setAttribute(POINTS, pointsListToString(pol));
            g.setAttribute(CLASS, WIRE_STYLE_CLASS + " " + styleProvider.getIdWireStyle(edge));
            root.appendChild(g);

            metadata.addWireMetadata(new GraphMetadata.WireMetadata(wireId,
                    escapeId(edge.getNode1().getId()),
                    escapeId(edge.getNode2().getId()),
                    layoutParameters.isDrawStraightWires(),
                    false));

            if (metadata.getComponentMetadata(ARROW) == null) {
                metadata.addComponentMetadata(new ComponentMetadata(ARROW,
                        null,
                        componentLibrary.getAnchorPoints(ARROW),
                        componentLibrary.getSize(ARROW), true));
            }

            if (edge.getNode1() instanceof FeederNode) {
                if (!(edge.getNode2() instanceof FeederNode)) {
                    insertArrowsAndLabels(prefixId, wireId, pol, root, edge.getNode1(), metadata, initProvider, styleProvider);
                }
            } else if (edge.getNode2() instanceof FeederNode) {
                insertArrowsAndLabels(prefixId, wireId, pol, root, edge.getNode2(), metadata, initProvider, styleProvider);
            }
        }
    }

    /*
     * Drawing the substation graph edges (snakelines between voltageLevel diagram)
     */
    protected void drawSnakeLines(String prefixId, Element root, SubstationGraph graph, GraphMetadata metadata) {

        for (TwtEdge edge : graph.getEdges()) {
            String vId1 = edge.getNode1().getGraph().getVoltageLevelId();
            String vId2 = edge.getNode2().getGraph().getVoltageLevelId();

            String wireId = escapeId(prefixId + vId1 + "_" + vId2 + "_" + "Wire" + graph.getEdges().indexOf(edge));
            Element g = root.getOwnerDocument().createElement(POLYLINE);
            g.setAttribute("id", wireId);

            // Get points of the snakeLine
            List<Double> pol = edge.getSnakeLine();

            g.setAttribute(POINTS, pointsListToString(pol));

            String vId;
            if (edge.getNode1().getGraph().getVoltageLevelNominalV() > edge.getNode2().getGraph().getVoltageLevelNominalV()) {
                vId = vId1;
            } else {
                vId = vId2;
            }

            g.setAttribute(CLASS, DiagramStyles.WIRE_STYLE_CLASS + " " + DiagramStyles.WIRE_STYLE_CLASS + "_" + escapeClassName(vId));
            root.appendChild(g);

            metadata.addWireMetadata(new GraphMetadata.WireMetadata(wireId,
                    escapeId(edge.getNode1().getId()),
                    escapeId(edge.getNode2().getId()),
                    layoutParameters.isDrawStraightWires(),
                    true));

            if (metadata.getComponentMetadata(ARROW) == null) {
                metadata.addComponentMetadata(new ComponentMetadata(ARROW,
                        null,
                        componentLibrary.getAnchorPoints(ARROW),
                        componentLibrary.getSize(ARROW), true));
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
     * Adjust feeders height, positioning them on a descending/ascending ramp
     * (depending on their BusCell direction)
     */
    private void shiftFeedersPosition(Graph graph, double scaleShiftFeederNames) {
        Map<BusCell.Direction, List<Node>> orderedFeederNodesByDirection = graph.getNodes().stream()
                .filter(node -> !node.isFictitious() && node instanceof FeederNode && node.getCell() != null)
                .sorted(Comparator.comparing(Node::getX))
                .collect(Collectors.groupingBy(node -> nodeDirection.apply(node)));

        Map<BusCell.Direction, Double> mapLev = Arrays.stream(BusCell.Direction.values()).collect(Collectors.toMap(d -> d, d -> 0.0));

        Stream.of(BusCell.Direction.values())
                .filter(direction -> orderedFeederNodesByDirection.get(direction) != null)
                .forEach(direction -> {
                    orderedFeederNodesByDirection.get(direction).stream().skip(1).forEach(node -> {
                        int componentHeight = (int) (componentLibrary.getSize(node.getComponentType()).getHeight());
                        double oldY = node.getY() - graph.getY();
                        double newY = mapLev.get(direction) + scaleShiftFeederNames * FONT_SIZE + (componentHeight == 0 ? LABEL_OFFSET : componentHeight);
                        node.setY(oldY + ((direction == BusCell.Direction.TOP) ? 1 : -1) * newY);
                        mapLev.put(direction, newY);
                    });
                });
    }

    /**
     * Creation of the defs area for the SVG components
     */
    protected void createDefsSVGComponents(Document document, Set<String> listUsedComponentSVG) {
        if (layoutParameters.isAvoidSVGComponentsDuplication()) {
            Element defs = document.createElement("defs");

            listUsedComponentSVG.stream().forEach(c -> {
                SVGOMDocument obj = componentLibrary.getSvgDocument(c);
                if (obj != null) {
                    Element group = document.createElement("g");
                    group.setAttribute("id", c);

                    insertSVGComponentIntoDefsArea(group, obj);

                    defs.getOwnerDocument().adoptNode(group);
                    defs.appendChild(group);
                }
            });

            // Adding also arrows
            Element group = document.createElement("g");
            group.setAttribute("id", ARROW);
            SVGOMDocument obj = componentLibrary.getSvgDocument(ARROW);
            insertSVGComponentIntoDefsArea(group, obj);
            defs.getOwnerDocument().adoptNode(group);
            defs.appendChild(group);

            document.adoptNode(defs);
            document.getDocumentElement().appendChild(defs);
        }
    }

    protected void insertSVGComponentIntoDefsArea(Element defs, SVGOMDocument obj) {
        for (int i = 0; i < obj.getChildNodes().item(0).getChildNodes().getLength(); i++) {
            org.w3c.dom.Node n = obj.getChildNodes().item(0).getChildNodes().item(i).cloneNode(true);
            defs.getOwnerDocument().adoptNode(n);
            defs.appendChild(n);
        }
    }
}
