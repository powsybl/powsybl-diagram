/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.powsybl.commons.config.BaseVoltagesConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.sld.model.graphs.VoltageLevelInfos;
import com.powsybl.sld.model.nodes.*;
import com.powsybl.sld.model.nodes.feeders.FeederTwLeg;
import com.powsybl.sld.model.nodes.feeders.FeederWithSides;
import com.powsybl.sld.svg.DiagramStyles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class NominalVoltageDiagramStyleProvider extends AbstractIidmBaseVoltageDiagramStyleProvider {

    public NominalVoltageDiagramStyleProvider(Network network) {
        this(BaseVoltagesConfig.fromPlatformConfig(), network);
    }

    public NominalVoltageDiagramStyleProvider(BaseVoltagesConfig baseVoltagesConfig, Network network) {
        super(baseVoltagesConfig, network);
    }

    @Override
    protected boolean isNodeSeparatingStyles(Node node) {
        if (node instanceof FeederNode) {
            // filtering out leg nodes as they are nodes with the same voltage level at each side
            Feeder feeder = ((FeederNode) node).getFeeder();
            return feeder instanceof FeederWithSides && !(feeder instanceof FeederTwLeg);
        } else {
            return node instanceof Middle3WTNode;
        }
    }

    @Override
    public Optional<String> getVoltageLevelNodeStyle(VoltageLevelInfos vlInfo, Node node) {
        return baseVoltagesConfig.getBaseVoltageName(vlInfo.getNominalVoltage(), BASE_VOLTAGE_PROFILE)
                .map(bvName -> DiagramStyles.STYLE_PREFIX + bvName);
    }

    @Override
    public Optional<String> getVoltageLevelNodeStyle(VoltageLevelInfos vlInfo, Node node, NodeSide side) {
        return getVoltageLevelNodeStyle(vlInfo, node);
    }

    @Override
    public List<String> getCssFilenames() {
        return Arrays.asList("tautologies.css", "baseVoltages.css", "highlightLineStates.css");
    }
}
