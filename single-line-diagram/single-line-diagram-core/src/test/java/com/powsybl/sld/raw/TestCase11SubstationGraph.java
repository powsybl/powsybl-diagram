/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.builders.SubstationRawBuilder;
import com.powsybl.sld.builders.VoltageLevelRawBuilder;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.layout.VerticalSubstationLayoutFactory;
import com.powsybl.sld.model.graphs.SubstationGraph;
import com.powsybl.sld.model.nodes.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.powsybl.sld.model.coordinate.Direction.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/*
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
class TestCase11SubstationGraph extends AbstractTestCaseRaw {

    private SubstationRawBuilder ssb1;
    private VoltageLevelRawBuilder vlb1;
    private VoltageLevelRawBuilder vlb2;
    private VoltageLevelRawBuilder vlb3;

    private SubstationRawBuilder ssb2;
    private VoltageLevelRawBuilder vlsubst2;
    private BusNode bbs1;
    private BusNode bbs2;
    private BusNode bbs6;
    private BusNode bbs7;
    private BusNode bbs12;

    @BeforeEach
    public void setUp() {
        ssb1 = rawGraphBuilder.createSubstationBuilder("subst");
        vlb1 = rawGraphBuilder.createVoltageLevelBuilder("vl1", 380, ssb1);

        bbs1 = vlb1.createBusBarSection("bbs1", 1, 1);
        bbs2 = vlb1.createBusBarSection("bbs2", 1, 2);
        BusNode bbs3 = vlb1.createBusBarSection("bbs3", 2, 1);
        BusNode bbs4 = vlb1.createBusBarSection("bbs4", 2, 2);

        SwitchNode dsect11 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dsect11", false, false);
        SwitchNode dtrct11 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "dtrct11", false, false);
        SwitchNode dsect12 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dsect12", false, false);
        vlb1.connectNode(bbs1, dsect11);
        vlb1.connectNode(dsect11, dtrct11);
        vlb1.connectNode(dtrct11, dsect12);
        vlb1.connectNode(dsect12, bbs2);

        SwitchNode dsect21 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dsect21", false, false);
        SwitchNode dtrct21 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "dtrct21", false, false);
        SwitchNode dsect22 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dsect22", false, false);
        vlb1.connectNode(bbs3, dsect21);
        vlb1.connectNode(dsect21, dtrct21);
        vlb1.connectNode(dtrct21, dsect22);
        vlb1.connectNode(dsect22, bbs4);

        FeederNode load1 = vlb1.createLoad("load1", 0, TOP);
        SwitchNode dload1 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dload1", false, false);
        SwitchNode bload1 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bload1", false, false);
        vlb1.connectNode(bbs1, dload1);
        vlb1.connectNode(dload1, bload1);
        vlb1.connectNode(load1, bload1);

        FeederNode gen1 = vlb1.createGenerator("gen1", 2, BOTTOM);
        SwitchNode dgen1 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dgen1", false, false);
        SwitchNode bgen1 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bgen1", false, false);
        vlb1.connectNode(bbs3, dgen1);
        vlb1.connectNode(dgen1, bgen1);
        vlb1.connectNode(gen1, bgen1);

        FeederNode load2 = vlb1.createLoad("load2", 8, TOP);
        SwitchNode dload2 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dload2", false, false);
        SwitchNode bload2 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bload2", false, false);
        vlb1.connectNode(bbs2, dload2);
        vlb1.connectNode(dload2, bload2);
        vlb1.connectNode(load2, bload2);

        FeederNode gen2 = vlb1.createGenerator("gen2", 12, BOTTOM);
        SwitchNode dgen2 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dgen2", false, false);
        SwitchNode bgen2 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bgen2", false, false);
        vlb1.connectNode(bbs4, dgen2);
        vlb1.connectNode(dgen2, bgen2);
        vlb1.connectNode(gen2, bgen2);

        vlb2 = rawGraphBuilder.createVoltageLevelBuilder("vl2", 225, ssb1);

        BusNode bbs5 = vlb2.createBusBarSection("bbs5", 1, 1);
        bbs6 = vlb2.createBusBarSection("bbs6", 2, 1);

        SwitchNode dscpl1 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dscpl1", false, false);
        SwitchNode ddcpl1 = vlb2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "ddcpl1", false, false);
        SwitchNode dscpl2 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dscpl2", false, false);
        vlb2.connectNode(bbs5, dscpl1);
        vlb2.connectNode(dscpl1, ddcpl1);
        vlb2.connectNode(ddcpl1, dscpl2);
        vlb2.connectNode(dscpl2, bbs6);

        FeederNode load3 = vlb2.createLoad("load3", 0, TOP);
        SwitchNode dload3 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dload3", false, false);
        SwitchNode bload3 = vlb2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bload3", false, false);
        vlb2.connectNode(bbs5, dload3);
        vlb2.connectNode(load3, bload3);
        vlb2.connectNode(bload3, dload3);

        FeederNode gen4 = vlb2.createGenerator("gen4", 2, BOTTOM);
        SwitchNode dgen4 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dgen4", false, false);
        SwitchNode bgen4 = vlb2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bgen4", false, false);
        vlb2.connectNode(bbs6, dgen4);
        vlb2.connectNode(gen4, bgen4);
        vlb2.connectNode(bgen4, dgen4);

        // third voltage level
        vlb3 = rawGraphBuilder.createVoltageLevelBuilder("vl3", 225, ssb1);

        bbs7 = vlb3.createBusBarSection("bbs7", 1, 1);

        FeederNode load4 = vlb3.createLoad("load4", 0, TOP);
        SwitchNode dload4 = vlb3.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dload4", false, false);
        SwitchNode bload4 = vlb3.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bload4", false, false);
        vlb3.connectNode(bbs7, dload4);
        vlb3.connectNode(bload4, load4);
        vlb3.connectNode(bload4, dload4);

        /*
        // two windings transformers between voltage levels
        //
        */

        Map<VoltageLevelRawBuilder, FeederNode> feeder2WTs1 = ssb1.createFeeder2WT("trf1", vlb1, vlb2,
                1, 1, TOP, TOP);
        SwitchNode dtrf11 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf11", false, false);
        SwitchNode btrf11 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf11", false, false);
        vlb1.connectNode(bbs1, dtrf11);
        vlb1.connectNode(dtrf11, btrf11);
        vlb1.connectNode(btrf11, feeder2WTs1.get(vlb1));

        SwitchNode dtrf21 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf21", false, false);
        SwitchNode btrf21 = vlb2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf21", false, false);
        vlb2.connectNode(bbs5, dtrf21);
        vlb2.connectNode(dtrf21, btrf21);
        vlb2.connectNode(btrf21, feeder2WTs1.get(vlb2));

        Map<VoltageLevelRawBuilder, FeederNode> feeder2WTs2 = ssb1.createFeeder2WT("trf2", vlb1, vlb2,
                11, 7, TOP, BOTTOM);
        SwitchNode dtrf12 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf12", false, false);
        SwitchNode btrf12 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf12", false, false);
        vlb1.connectNode(bbs2, dtrf12);
        vlb1.connectNode(dtrf12, btrf12);
        vlb1.connectNode(btrf12, feeder2WTs2.get(vlb1));

        SwitchNode dtrf22 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf22", false, false);
        SwitchNode btrf22 = vlb2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf22", false, false);
        vlb2.connectNode(bbs6, dtrf22);
        vlb2.connectNode(dtrf22, btrf22);
        vlb2.connectNode(btrf22, feeder2WTs2.get(vlb2));

        Map<VoltageLevelRawBuilder, FeederNode> feeder2WTs3 = ssb1.createFeeder2WT("trf3", vlb1, vlb2,
                3, 8, BOTTOM, BOTTOM);

        SwitchNode dtrf13 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf13", false, false);
        SwitchNode btrf13 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf13", false, false);
        vlb1.connectNode(bbs3, dtrf13);
        vlb1.connectNode(dtrf13, btrf13);
        vlb1.connectNode(btrf13, feeder2WTs3.get(vlb1));

        SwitchNode dtrf23 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf23", false, false);
        SwitchNode btrf23 = vlb2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf23", false, false);
        vlb2.connectNode(bbs6, dtrf23);
        vlb2.connectNode(dtrf23, btrf23);
        vlb2.connectNode(btrf23, feeder2WTs3.get(vlb2));

        Map<VoltageLevelRawBuilder, FeederNode> feeder2WTs4 = ssb1.createFeeder2WT("trf4", vlb1, vlb2,
                10, 3, BOTTOM, TOP);

        SwitchNode dtrf14 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf14", false, false);
        SwitchNode btrf14 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf14", false, false);
        vlb1.connectNode(bbs4, dtrf14);
        vlb1.connectNode(dtrf14, btrf14);
        vlb1.connectNode(btrf14, feeder2WTs4.get(vlb1));

        SwitchNode dtrf24 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf24", false, false);
        SwitchNode btrf24 = vlb2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf24", false, false);
        vlb2.connectNode(bbs5, dtrf24);
        vlb2.connectNode(dtrf24, btrf24);
        vlb2.connectNode(btrf24, feeder2WTs4.get(vlb2));

        Map<VoltageLevelRawBuilder, FeederNode> feeder2WTs5 = ssb1.createFeeder2WT("trf5", vlb1, vlb3,
                4, 1, TOP, BOTTOM);

        SwitchNode dtrf15 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf15", false, false);
        SwitchNode btrf15 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf15", false, false);
        vlb1.connectNode(bbs1, dtrf15);
        vlb1.connectNode(dtrf15, btrf15);
        vlb1.connectNode(btrf15, feeder2WTs5.get(vlb1));

        SwitchNode dtrf25 = vlb3.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf25", false, false);
        SwitchNode btrf25 = vlb3.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf25", false, false);
        vlb3.connectNode(bbs7, dtrf25);
        vlb3.connectNode(dtrf25, btrf25);
        vlb3.connectNode(btrf25, feeder2WTs5.get(vlb3));
        /*
        // three windings transformers between voltage levels
        //
       */

        Map<VoltageLevelRawBuilder, FeederNode> feeder3WTs6 = ssb1.createFeeder3WT("trf6", vlb1, vlb2, vlb3,
                5, 5, 2, TOP, TOP, TOP);

        SwitchNode dtrf16 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf16", false, false);
        SwitchNode btrf16 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf16", false, false);
        vlb1.connectNode(bbs1, dtrf16);
        vlb1.connectNode(dtrf16, btrf16);
        vlb1.connectNode(btrf16, feeder3WTs6.get(vlb1));

        SwitchNode dtrf26 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf26", false, false);
        SwitchNode btrf26 = vlb2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf26", false, false);
        vlb2.connectNode(bbs6, dtrf26);
        vlb2.connectNode(dtrf26, btrf26);
        vlb2.connectNode(btrf26, feeder3WTs6.get(vlb2));

        SwitchNode dtrf36 = vlb3.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf36", false, false);
        SwitchNode btrf36 = vlb3.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf36", false, false);
        vlb3.connectNode(bbs7, dtrf36);
        vlb3.connectNode(dtrf36, btrf36);
        vlb3.connectNode(btrf36, feeder3WTs6.get(vlb3));

        Map<VoltageLevelRawBuilder, FeederNode> feeder3WTs7 = ssb1.createFeeder3WT("trf7", vlb1, vlb2, vlb3,
                6, 4, 3, BOTTOM, TOP, BOTTOM);

        SwitchNode dtrf17 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf17", false, false);
        SwitchNode btrf17 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf17", false, false);
        vlb1.connectNode(bbs3, dtrf17);
        vlb1.connectNode(dtrf17, btrf17);
        vlb1.connectNode(btrf17, feeder3WTs7.get(vlb1));

        SwitchNode dtrf27 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf27", false, false);
        SwitchNode btrf27 = vlb2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf27", false, false);
        vlb2.connectNode(bbs5, dtrf27);
        vlb2.connectNode(dtrf27, btrf27);
        vlb2.connectNode(btrf27, feeder3WTs7.get(vlb2));

        SwitchNode dtrf37 = vlb3.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf37", false, false);
        SwitchNode btrf37 = vlb3.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf37", false, false);
        vlb3.connectNode(bbs7, dtrf37);
        vlb3.connectNode(dtrf37, btrf37);
        vlb3.connectNode(btrf37, feeder3WTs7.get(vlb3));

        Map<VoltageLevelRawBuilder, FeederNode> feeder3WTs8 = ssb1.createFeeder3WT("trf8", vlb1, vlb2, vlb3,
                9, 6, 4, TOP, BOTTOM, TOP);

        SwitchNode dtrf18 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf18", false, false);
        SwitchNode btrf18 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf18", false, false);
        vlb1.connectNode(bbs2, dtrf18);
        vlb1.connectNode(dtrf18, btrf18);
        vlb1.connectNode(btrf18, feeder3WTs8.get(vlb1));

        SwitchNode dtrf28 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf28", false, false);
        SwitchNode btrf28 = vlb2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf28", false, false);
        vlb2.connectNode(bbs6, dtrf28);
        vlb2.connectNode(dtrf28, btrf28);
        vlb2.connectNode(btrf28, feeder3WTs8.get(vlb2));

        SwitchNode dtrf38 = vlb3.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf38", false, false);
        SwitchNode btrf38 = vlb3.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf38", false, false);
        vlb3.connectNode(bbs7, dtrf38);
        vlb3.connectNode(dtrf38, btrf38);
        vlb3.connectNode(btrf38, feeder3WTs8.get(vlb3));

        /*
        // Creation of another substation, another voltageLevel
        // - a line between the two substations
        //
        */
        ssb2 = rawGraphBuilder.createSubstationBuilder("subst2");
        vlsubst2 = rawGraphBuilder.createVoltageLevelBuilder("vlSubst2", 380, ssb2);

        bbs12 = vlsubst2.createBusBarSection("bbs1_2", 1, 1);

        SwitchNode dline112 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dline11_2", false, false);
        SwitchNode bline112 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bline11_2", false, false);

        SwitchNode dline212 = vlsubst2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dline21_2", false, false);
        SwitchNode bline212 = vlsubst2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bline21_2", false, false);
        Map<VoltageLevelRawBuilder, FeederNode> line1 =
                ssb1.createLine("line1", vlb1, vlsubst2, 7, 1, TOP, TOP);
        vlb1.connectNode(bbs1, dline112);
        vlb1.connectNode(dline112, bline112);
        vlb1.connectNode(bline112, line1.get(vlb1));
        vlsubst2.connectNode(bbs12, dline212);
        vlsubst2.connectNode(dline212, bline212);
        vlsubst2.connectNode(bline212, line1.get(vlsubst2));

        /*
        // - a two windings transformers between the two substations
        */
        Map<VoltageLevelRawBuilder, FeederNode> feeder2WTs211 = ssb1.createFeeder2WT("trf211", vlb3, vlsubst2);
        SwitchNode dtrf231 = vlb3.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf231", false, false);
        SwitchNode btrf231 = vlb3.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf231", false, false);
        vlb3.connectNode(bbs7, dtrf231);
        vlb3.connectNode(dtrf231, btrf231);
        vlb3.connectNode(btrf231, feeder2WTs211.get(vlb3));

        SwitchNode dtrf211 = vlsubst2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf211", false, false);
        SwitchNode btrf211 = vlsubst2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf211", false, false);
        vlsubst2.connectNode(bbs12, dtrf211);
        vlsubst2.connectNode(dtrf211, btrf211);
        vlsubst2.connectNode(btrf211, feeder2WTs211.get(vlsubst2));

        /*
        // - a three windings transformers between the two substations
        */
        Map<VoltageLevelRawBuilder, FeederNode> feeder3WTs311 = ssb1.createFeeder3WT("trf311", vlb1, vlb2, vlsubst2);

        SwitchNode dtrf318 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf318", false, false);
        SwitchNode btrf318 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf318", false, false);
        vlb1.connectNode(bbs2, dtrf318);
        vlb1.connectNode(dtrf318, btrf318);
        vlb1.connectNode(btrf318, feeder3WTs311.get(vlb1));

        SwitchNode dtrf328 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf328", false, false);
        SwitchNode btrf328 = vlb2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf328", false, false);
        vlb2.connectNode(bbs6, dtrf328);
        vlb2.connectNode(dtrf328, btrf328);
        vlb2.connectNode(btrf328, feeder3WTs311.get(vlb2));

        SwitchNode dtrf338 = vlsubst2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf338", false, false);
        SwitchNode btrf338 = vlsubst2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf338", false, false);
        vlsubst2.connectNode(bbs12, dtrf338);
        vlsubst2.connectNode(dtrf338, btrf338);
        vlsubst2.connectNode(btrf338, feeder3WTs311.get(vlsubst2));
    }

    private void appendLines() {
        /*
        // Creation of another voltageLevel (vl22)
        */
        VoltageLevelRawBuilder vl22 = rawGraphBuilder.createVoltageLevelBuilder("vl2_2", 380, ssb2);
        BusNode bbs13 = vl22.createBusBarSection("bbs1_3", 1, 1);

        /*
        // - a line between the voltageLevels: vlsubst2 & vlb1
        */
        SwitchNode dline21 = vlsubst2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dline21_1", false, false);
        SwitchNode bline21 = vlsubst2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bline21_1", false, false);

        SwitchNode dline22 = vl22.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dline21_2", false, false);
        SwitchNode bline22 = vl22.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bline21_2", false, false);
        Map<VoltageLevelRawBuilder, FeederNode> line2 = ssb2.createLine("line2", vlsubst2, vl22);
        vlsubst2.connectNode(bbs12, dline21);
        vlsubst2.connectNode(dline21, bline21);
        vlsubst2.connectNode(bline21, line2.get(vlsubst2));
        vl22.connectNode(bbs13, dline22);
        vl22.connectNode(dline22, bline22);
        vl22.connectNode(bline22, line2.get(vl22));

        /*
        // - a line between the voltageLevels: vlsubst2 & vlb1
        */
        SwitchNode dline112 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dline11_2", false, false);
        SwitchNode bline112 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bline11_2", false, false);

        SwitchNode dline212 = vlsubst2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dline21_2", false, false);
        SwitchNode bline212 = vlsubst2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bline21_2", false, false);
        Map<VoltageLevelRawBuilder, FeederNode> line1 =
                ssb1.createLine("line1", vlsubst2, vlb1, 7, 1, TOP, TOP);
        vlb1.connectNode(bbs1, dline112);
        vlb1.connectNode(dline112, bline112);
        vlb1.connectNode(bline112, line1.get(vlb1));
        vlsubst2.connectNode(bbs12, dline212);
        vlsubst2.connectNode(dline212, bline212);
        vlsubst2.connectNode(bline212, line1.get(vlsubst2));
    }

    private void append2wt() {
        /*
        // - a two windings transformers between the two substations
        */
        Map<VoltageLevelRawBuilder, FeederNode> feeder2WTs211 = ssb2.createFeeder2WT("trf211", vlb3, vlsubst2);
        SwitchNode dtrf231 = vlb3.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf231", false, false);
        SwitchNode btrf231 = vlb3.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf231", false, false);
        vlb3.connectNode(bbs7, dtrf231);
        vlb3.connectNode(dtrf231, btrf231);
        vlb3.connectNode(btrf231, feeder2WTs211.get(vlb3));

        SwitchNode dtrf211 = vlsubst2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf211", false, false);
        SwitchNode btrf211 = vlsubst2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf211", false, false);
        vlsubst2.connectNode(bbs12, dtrf211);
        vlsubst2.connectNode(dtrf211, btrf211);
        vlsubst2.connectNode(btrf211, feeder2WTs211.get(vlsubst2));
    }

    private void append3wts() {
        /*
        // - two three windings transformers between the two substations
        */
        Map<VoltageLevelRawBuilder, FeederNode> feeder3WTs312 = ssb2.createFeeder3WT("trf312", vlb1, vlsubst2, vlb2);

        SwitchNode dtrf319 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf319", false, false);
        SwitchNode btrf319 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf319", false, false);
        vlb1.connectNode(bbs2, dtrf319);
        vlb1.connectNode(dtrf319, btrf319);
        vlb1.connectNode(btrf319, feeder3WTs312.get(vlb1));

        SwitchNode dtrf329 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf329", false, false);
        SwitchNode btrf329 = vlb2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf328", false, false);
        vlb2.connectNode(bbs6, dtrf329);
        vlb2.connectNode(dtrf329, btrf329);
        vlb2.connectNode(btrf329, feeder3WTs312.get(vlb2));

        SwitchNode dtrf339 = vlsubst2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf339", false, false);
        SwitchNode btrf339 = vlsubst2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf339", false, false);
        vlsubst2.connectNode(bbs12, dtrf339);
        vlsubst2.connectNode(dtrf339, btrf339);
        vlsubst2.connectNode(btrf339, feeder3WTs312.get(vlsubst2));

        Map<VoltageLevelRawBuilder, FeederNode> feeder3WTs313 = ssb2.createFeeder3WT("trf313", vlsubst2, vlb1, vlb2);

        SwitchNode dtrf3110 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf318", false, false);
        SwitchNode btrf3110 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf318", false, false);
        vlb1.connectNode(bbs2, dtrf3110);
        vlb1.connectNode(dtrf3110, btrf3110);
        vlb1.connectNode(btrf3110, feeder3WTs313.get(vlb1));

        SwitchNode dtrf3210 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf328", false, false);
        SwitchNode btrf3210 = vlb2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf328", false, false);
        vlb2.connectNode(bbs6, dtrf3210);
        vlb2.connectNode(dtrf3210, btrf3210);
        vlb2.connectNode(btrf3210, feeder3WTs313.get(vlb2));

        SwitchNode dtrf3310 = vlsubst2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf338", false, false);
        SwitchNode btrf3310 = vlsubst2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf338", false, false);
        vlsubst2.connectNode(bbs12, dtrf3310);
        vlsubst2.connectNode(dtrf3310, btrf3310);
        vlsubst2.connectNode(btrf3310, feeder3WTs313.get(vlsubst2));
    }

    @Test
    void testH() {
        SubstationGraph g = rawGraphBuilder.buildSubstationGraph("subst");
        substationGraphLayout(g);
        assertEquals(toString("/TestCase11SubstationGraphHRaw.json"), toJson(g, "/TestCase11SubstationGraphHRaw.json"));
    }

    @Test
    void testV() {
        SubstationGraph g = rawGraphBuilder.buildSubstationGraph("subst");
        new VerticalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
        assertEquals(toString("/TestCase11SubstationGraphVRaw.json"), toJson(g, "/TestCase11SubstationGraphVRaw.json"));
    }

    @Test
    void testH2() {
        SubstationGraph g = rawGraphBuilder.buildSubstationGraph("subst2");
        substationGraphLayout(g);
        assertEquals(toString("/TestCase11SubstationGraphHRaw2.json"), toJson(g, "/TestCase11SubstationGraphHRaw2.json"));
    }

    @Test
    void testV2() {
        SubstationGraph g = rawGraphBuilder.buildSubstationGraph("subst2");
        new VerticalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
        assertEquals(toString("/TestCase11SubstationGraphVRaw2.json"), toJson(g, "/TestCase11SubstationGraphVRaw2.json"));
    }

    @Test
    void testH2WithLines() {
        appendLines();

        SubstationGraph g = rawGraphBuilder.buildSubstationGraph("subst2");
        substationGraphLayout(g);
        assertEquals(toString("/TestCase11SubstationGraphHRaw2WithLines.json"), toJson(g, "/TestCase11SubstationGraphHRaw2WithLines.json"));
    }

    @Test
    void testV2WithLines() {
        appendLines();

        SubstationGraph g = rawGraphBuilder.buildSubstationGraph("subst2");
        new VerticalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
        assertEquals(toString("/TestCase11SubstationGraphVRaw2WithLines.json"), toJson(g, "/TestCase11SubstationGraphVRaw2WithLines.json"));
    }

    @Test
    void testH2With2wts() {
        append2wt();

        SubstationGraph g = rawGraphBuilder.buildSubstationGraph("subst2");
        substationGraphLayout(g);
        assertEquals(toString("/TestCase11SubstationGraphHRaw2With2wts.json"), toJson(g, "/TestCase11SubstationGraphHRaw2With2wts.json"));
    }

    @Test
    void testV2With2wts() {
        append2wt();

        SubstationGraph g = rawGraphBuilder.buildSubstationGraph("subst2");
        new VerticalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
        assertEquals(toString("/TestCase11SubstationGraphVRaw2With2wts.json"), toJson(g, "/TestCase11SubstationGraphVRaw2With2wts.json"));
    }

    @Test
    void testH2With3wts() {
        append3wts();

        SubstationGraph g = rawGraphBuilder.buildSubstationGraph("subst2");
        substationGraphLayout(g);
        assertEquals(toString("/TestCase11SubstationGraphHRaw2With3wts.json"), toJson(g, "/TestCase11SubstationGraphHRaw2With3wts.json"));
    }

    @Test
    void testV2With3wts() {
        append3wts();

        SubstationGraph g = rawGraphBuilder.buildSubstationGraph("subst2");
        new VerticalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
        assertEquals(toString("/TestCase11SubstationGraphVRaw2With3wts.json"), toJson(g, "/TestCase11SubstationGraphVRaw2With3wts.json"));
    }
}
