/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg;

import com.powsybl.diagram.util.ValueFormatter;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.svg.styles.StyleClassConstants;
import com.powsybl.sld.svg.styles.StyleProvider;
import com.powsybl.sld.util.IdUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.util.List;
import java.util.Objects;

import static com.powsybl.diagram.util.CssUtil.writeStyleClasses;
import static com.powsybl.sld.svg.DefaultSVGWriter.CIRCLE_RADIUS_NODE_INFOS_SIZE;
import static com.powsybl.sld.svg.DefaultSVGWriter.GROUP;

/**
 * @author Slimane Amar {@literal <slimane.amar at rte-france.com>}
 */
public class DefaultLegendProvider implements LegendProvider {

    protected final Network network;
    protected final SvgParameters svgParameters;
    protected final ValueFormatter valueFormatter;

    public DefaultLegendProvider(Network net, SvgParameters svgParameters) {
        this.network = Objects.requireNonNull(net);
        this.svgParameters = Objects.requireNonNull(svgParameters);
        this.valueFormatter = svgParameters.createValueFormatter();
    }

    @Override
    public void drawLegend(VoltageLevelGraph graph, GraphMetadata metadata, StyleProvider styleProvider, Element legendRootElement, double positionX, double positionY) {
        double shiftX = positionX;
        for (BusLegendInfo busLegendInfo : getBusLegendInfos(graph)) {
            String idNode = metadata.getSvgParameters().getPrefixId() + "NODE_" + busLegendInfo.busId();
            String escapedIdNode = IdUtil.escapeId(idNode);
            Element gNode = legendRootElement.getOwnerDocument().createElement(GROUP);
            gNode.setAttribute("id", escapedIdNode);

            drawBusLegendInfo(busLegendInfo, shiftX, positionY, gNode, idNode, graph, styleProvider);

            legendRootElement.appendChild(gNode);

            metadata.addBusLegendInfoMetadata(new GraphMetadata.BusLegendInfoMetadata(escapedIdNode));

            shiftX += 2 * CIRCLE_RADIUS_NODE_INFOS_SIZE + 50;
        }
    }

    protected List<BusLegendInfo> getBusLegendInfos(VoltageLevelGraph graph) {
        VoltageLevel vl = network.getVoltageLevel(graph.getVoltageLevelInfos().getId());
        return vl.getBusView().getBusStream()
            .map(b -> new BusLegendInfo(b.getId(), List.of(
                new BusLegendInfo.Caption(valueFormatter.formatVoltage(b.getV(), "kV"), "v"),
                new BusLegendInfo.Caption(valueFormatter.formatAngleInDegrees(b.getAngle()), "angle")
            )))
            .toList();
    }

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
}
