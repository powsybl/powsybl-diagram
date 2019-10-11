/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.view;

import afester.javafx.svg.SvgLoader;
import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.svg.GraphMetadata;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Polyline;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.powsybl.sld.library.ComponentTypeName.BREAKER;
import static com.powsybl.sld.library.ComponentTypeName.DISCONNECTOR;
import static com.powsybl.sld.library.ComponentTypeName.LOAD_BREAK_SWITCH;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public abstract class AbstractContainerDiagramView extends BorderPane {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContainerDiagramView.class);

    private double pressedX;
    private double pressedY;
    private final Group svgImage;

    protected AbstractContainerDiagramView(Group svgImage) {
        super(svgImage);
        this.svgImage = svgImage;

        registerEvents();
    }

    /*
     * Resizing the group of nodes to fit the viewport scrollpane dimensions
     */
    public void fitToContent(double viewportWidth, double dWidth,
                             double viewportHeight, double dHeight) {
        double boundsWidth = svgImage.getBoundsInParent().getWidth();
        double boundsHeight = svgImage.getBoundsInParent().getHeight();

        double scaleX = 1.;
        double scaleY = 1.;
        if (boundsWidth > boundsHeight) {
            scaleX = (viewportWidth - dWidth) / boundsWidth;
            scaleY = scaleX;
        } else {
            scaleY = (viewportHeight - dHeight) / boundsHeight;
            scaleX = scaleY;
        }

        svgImage.setScaleX(svgImage.getScaleX() * scaleX);
        svgImage.setScaleY(svgImage.getScaleY() * scaleY);
        svgImage.setTranslateX(svgImage.getTranslateX() - svgImage.getBoundsInParent().getMinX() + dWidth / 2);
        svgImage.setTranslateY(svgImage.getTranslateY() - svgImage.getBoundsInParent().getMinY() + dHeight / 2);
    }

    private void registerEvents() {
        setOnScroll(event -> {
            double zoomFactor = 1.05;
            double deltaY = event.getDeltaY();
            if (deltaY < 0) {
                zoomFactor = 2.0 - zoomFactor;
            }
            svgImage.setScaleX(svgImage.getScaleX() * zoomFactor);
            svgImage.setScaleY(svgImage.getScaleY() * zoomFactor);

            event.consume();
        });

        setOnMousePressed(event -> {
            if (event.getButton().equals(MouseButton.SECONDARY)) {
                pressedX = -svgImage.getTranslateX() + event.getX();
                pressedY = -svgImage.getTranslateY() + event.getY();
            }
            event.consume();
        });
        setOnMouseDragged(event -> {
            if (event.getButton().equals(MouseButton.SECONDARY)) {
                svgImage.setTranslateX(event.getX() - pressedX);
                svgImage.setTranslateY(event.getY() - pressedY);
            }
            event.consume();
        });
    }

    private static void installHandlers(Node node, GraphMetadata metadata,
                                        Map<String, WireHandler> wireHandlers,
                                        Map<String, NodeHandler> nodeHandlers,
                                        Map<String, VoltageLevelHandler> vlHandlers,
                                        DisplayVoltageLevel displayVL) {
        if (node == null) {
            return;
        }

        if (!StringUtils.isEmpty(node.getId())) {
            GraphMetadata.NodeMetadata nodeMetadata = metadata.getNodeMetadata(node.getId());
            if (nodeMetadata != null) {
                if (node instanceof Group &&
                        (nodeMetadata.getComponentType() != null) &&
                        (nodeMetadata.getComponentType().equals(BREAKER) || nodeMetadata.getComponentType().equals(DISCONNECTOR) || nodeMetadata.getComponentType().equals(LOAD_BREAK_SWITCH))) {
                    setNodeVisibility((Group) node, nodeMetadata);
                }
                installNodeHandlers(node, metadata, nodeMetadata, nodeHandlers, vlHandlers, displayVL);
            }
            GraphMetadata.WireMetadata wireMetadata = metadata.getWireMetadata(node.getId());
            if (wireMetadata != null) {
                installWireHandlers(node, metadata, wireMetadata, nodeHandlers, wireHandlers);
            }
            GraphMetadata.ArrowMetadata arrowMetadata = metadata.getArrowMetadata(node.getId());
            if (arrowMetadata != null) {
                WireHandler wireHandler = wireHandlers.get(arrowMetadata.getWireId());
                wireHandler.addArrow((Group) node);
            }
        }

        // propagate to children
        if (node instanceof Group) {
            ((Group) node).getChildren().forEach(child -> installHandlers(child, metadata, wireHandlers, nodeHandlers, vlHandlers, displayVL));
        }
    }

    private static void installNodeHandlers(Node node, GraphMetadata metadata,
                                            GraphMetadata.NodeMetadata nodeMetadata,
                                            Map<String, NodeHandler> nodeHandlers,
                                            Map<String, VoltageLevelHandler> vlHandlers,
                                            DisplayVoltageLevel displayVL) {
        if (!nodeMetadata.isVLabel()) {
            NodeHandler nodeHandler = new NodeHandler(node, nodeMetadata.getComponentType(),
                                                      nodeMetadata.getRotationAngle(),
                                                      metadata,
                                                      nodeMetadata.getVId(), nodeMetadata.getNextVId(),
                                                      nodeMetadata.getDirection());
            nodeHandler.setDisplayVL(displayVL);
            LOGGER.trace("Add handler to node {} in voltageLevel {}", node.getId(), nodeMetadata.getVId());
            nodeHandlers.put(node.getId(), nodeHandler);
        } else {  // handler for voltageLevel label
            VoltageLevelHandler vlHandler = new VoltageLevelHandler(node, metadata, nodeMetadata.getVId());
            LOGGER.trace("Add handler to voltageLvel label {}", node.getId());
            vlHandlers.put(nodeMetadata.getVId(), vlHandler);
        }
    }

    private static void installWireHandlers(Node node, GraphMetadata metadata, GraphMetadata.WireMetadata wireMetadata, Map<String, NodeHandler> nodeHandlers, Map<String, WireHandler> wireHandlers) {
        NodeHandler nodeHandler1 = nodeHandlers.get(wireMetadata.getNodeId1());
        if (nodeHandler1 == null) {
            throw new PowsyblException("Node 1 " + wireMetadata.getNodeId1() + " not found");
        }
        NodeHandler nodeHandler2 = nodeHandlers.get(wireMetadata.getNodeId2());
        if (nodeHandler2 == null) {
            throw new PowsyblException("Node 2 " + wireMetadata.getNodeId2() + " not found");
        }
        WireHandler wireHandler = new WireHandler((Polyline) node, nodeHandler1, nodeHandler2, wireMetadata.isStraight(),
                wireMetadata.isSnakeLine(), metadata);
        LOGGER.trace(" Added handler to wire between {} and {}", wireMetadata.getNodeId1(), wireMetadata.getNodeId2());
        wireHandlers.put(node.getId(), wireHandler);
    }

    private static void setNodeVisibility(Group node, GraphMetadata.NodeMetadata nodeMetadata) {
        node.getChildren().forEach(child ->
                child.setVisible((nodeMetadata.isOpen() && child.getId().endsWith("open"))
                        || (!nodeMetadata.isOpen() && child.getId().endsWith("closed"))));
    }

    private static void installHandlers(Node node, GraphMetadata metadata,
                                        DisplayVoltageLevel displayVL) {
        Map<String, WireHandler> wireHandlers = new HashMap<>();
        Map<String, NodeHandler> nodeHandlers = new HashMap<>();
        Map<String, VoltageLevelHandler> vlHandlers = new HashMap<>();

        installHandlers(node, metadata, wireHandlers, nodeHandlers, vlHandlers, displayVL);

        // resolve links
        for (WireHandler wireHandler : wireHandlers.values()) {
            wireHandler.getNodeHandler1().addWire(wireHandler);
            wireHandler.getNodeHandler2().addWire(wireHandler);
        }

        // resolve voltageLevel handler
        vlHandlers.values().forEach(v -> v.addNodeHandlers(nodeHandlers.values().stream().collect(Collectors.toList())));
    }

    protected static Group loadSvgAndMetadata(InputStream svgInputStream,
                                              InputStream metadataInputStream,
                                              DisplayVoltageLevel displayVL) {
        // convert svg file to JavaFX components
        Group svgImage = null;
        try {
            svgImage = new SvgLoader().loadSvg(svgInputStream);
        } catch (Exception e) {
            // to feed the content of the 'SVG' and 'Metadata' tab, even if the
            // svg diagram cannot be loaded by svg loader
        }

        // load metadata
        GraphMetadata metadata = GraphMetadata.parseJson(metadataInputStream);

        // install node and wire handlers to allow diagram edition
        installHandlers(svgImage, metadata, displayVL);

        return svgImage;
    }
}
