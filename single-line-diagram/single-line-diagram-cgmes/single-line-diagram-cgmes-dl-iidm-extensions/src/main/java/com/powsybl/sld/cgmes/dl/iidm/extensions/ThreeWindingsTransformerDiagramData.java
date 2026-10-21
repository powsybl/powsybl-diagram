/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.iidm.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.ThreeWindingsTransformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class ThreeWindingsTransformerDiagramData extends AbstractExtension<ThreeWindingsTransformer> {

    static final String NAME = "three-windings-transformer-diagram-data";
    private final Map<String, ThreeWindingsTransformerDiagramDataDetails> diagramsDetails = new HashMap<>();

    public ThreeWindingsTransformerDiagramData(ThreeWindingsTransformer transformer) {
        super(transformer);
    }

    @Override
    public String getName() {
        return NAME;
    }

    public void addData(String diagramName, ThreeWindingsTransformerDiagramDataDetails nodeDetails) {
        Objects.requireNonNull(diagramName);
        Objects.requireNonNull(nodeDetails);
        diagramsDetails.put(diagramName, nodeDetails);
    }

    public ThreeWindingsTransformerDiagramDataDetails getData(String diagramName) {
        Objects.requireNonNull(diagramName);
        return diagramsDetails.get(diagramName);
    }

    public List<String> getDiagramsNames() {
        return new ArrayList<>(diagramsDetails.keySet());
    }

    public static class ThreeWindingsTransformerDiagramDataDetails {
        private final DiagramPoint point;
        private final double rotation;
        private final List<DiagramPoint> terminal1Points = new ArrayList<>();
        private final List<DiagramPoint> terminal2Points = new ArrayList<>();
        private final List<DiagramPoint> terminal3Points = new ArrayList<>();

        public ThreeWindingsTransformerDiagramDataDetails(DiagramPoint point, double rotation) {
            this.point = Objects.requireNonNull(point);
            this.rotation = rotation;
        }

        public void addTerminalPoint(DiagramTerminal terminal, DiagramPoint point) {
            Objects.requireNonNull(terminal);
            Objects.requireNonNull(point);
            switch (terminal) {
                case TERMINAL1 -> terminal1Points.add(point);
                case TERMINAL2 -> terminal2Points.add(point);
                case TERMINAL3 -> terminal3Points.add(point);
                default -> throw new AssertionError("Unexpected terminal: " + terminal);
            }
        }

        public List<DiagramPoint> getTerminalPoints(DiagramTerminal terminal) {
            Objects.requireNonNull(terminal);
            return switch (terminal) {
                case TERMINAL1 -> terminal1Points.stream().sorted().collect(Collectors.toList());
                case TERMINAL2 -> terminal2Points.stream().sorted().collect(Collectors.toList());
                case TERMINAL3 -> terminal3Points.stream().sorted().collect(Collectors.toList());
            };
        }

        public DiagramPoint getPoint() {
            return point;
        }

        public double getRotation() {
            return rotation;
        }
    }

}
