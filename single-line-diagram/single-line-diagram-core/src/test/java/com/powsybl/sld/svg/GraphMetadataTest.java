/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.AnchorOrientation;
import com.powsybl.sld.library.AnchorPoint;
import com.powsybl.sld.library.SldComponent;
import com.powsybl.diagram.components.ComponentSize;
import com.powsybl.sld.model.coordinate.Direction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static com.powsybl.sld.library.SldComponentTypeName.*;

/**
 * @author Benoit Jeanson {@literal <benoit.jeanson at rte-france.com>}
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
class GraphMetadataTest {

    private FileSystem fileSystem;
    private Path tmpDir;

    @BeforeEach
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        tmpDir = Files.createDirectory(fileSystem.getPath("/tmp"));
    }

    @AfterEach
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    void test() throws IOException {
        GraphMetadata metadata = new GraphMetadata(new LayoutParameters(), new SvgParameters());
        metadata.addComponent(new SldComponent(BREAKER, ImmutableList.of(new AnchorPoint(5, 4, AnchorOrientation.NONE)),
            new ComponentSize(10, 12), "breaker", Collections.emptyMap(), null));

        List<GraphMetadata.NodeLabelMetadata> labels = Collections.singletonList(new GraphMetadata.NodeLabelMetadata("id", "position_name", "user_id"));

        metadata.addNodeMetadata(new GraphMetadata.NodeMetadata(null, "id1", "vid1", null, BREAKER, false, Direction.UNDEFINED, false, null, labels));
        metadata.addNodeMetadata(new GraphMetadata.NodeMetadata(null, "id2", "vid2", null, BUSBAR_SECTION, false, Direction.UNDEFINED, false, null, labels));
        metadata.addWireMetadata(new GraphMetadata.WireMetadata("id3", "id1", "id2", false, false));
        metadata.addFeederInfoMetadata(new GraphMetadata.FeederInfoMetadata("id1", "id3", "ONE", "COMPONENT_TYPE", "user_id"));
        metadata.addBusInfoMetadata(new GraphMetadata.BusInfoMetadata("id6", "busNodeId1", "user_id"));

        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                                  .writeValueAsString(metadata);

        GraphMetadata metadata2 = objectMapper.readValue(json, GraphMetadata.class);
        assertEquals(1, metadata2.getComponentMetadata().size());
        assertNotNull(metadata2.getComponentMetadata(BREAKER));
        assertEquals(1, metadata2.getComponentMetadata(BREAKER).getAnchorPoints().size());
        assertEquals(5, metadata2.getComponentMetadata(BREAKER).getAnchorPoints().get(0).getX(), 0);
        assertEquals(4, metadata2.getComponentMetadata(BREAKER).getAnchorPoints().get(0).getY(), 0);
        assertEquals(AnchorOrientation.NONE, metadata2.getComponentMetadata(BREAKER).getAnchorPoints().get(0).getOrientation());
        assertEquals(10, metadata2.getComponentMetadata(BREAKER).getSize().getWidth(), 0);
        assertEquals(12, metadata2.getComponentMetadata(BREAKER).getSize().getHeight(), 0);
        assertEquals(2, metadata2.getNodeMetadata().size());
        assertNotNull(metadata2.getNodeMetadata("id1"));
        assertEquals("id1", metadata2.getNodeMetadata("id1").getId());
        assertEquals("vid1", metadata2.getNodeMetadata("id1").getVId());
        assertEquals(BREAKER, metadata2.getNodeMetadata("id1").getComponentType());
        assertEquals(BUSBAR_SECTION, metadata2.getNodeMetadata("id2").getComponentType());
        assertEquals("id2", metadata2.getNodeMetadata("id2").getId());
        assertEquals("vid2", metadata2.getNodeMetadata("id2").getVId());
        assertNotNull(metadata2.getNodeMetadata("id1").getLabels());
        assertEquals("id", metadata2.getNodeMetadata("id1").getLabels().get(0).getId());
        assertEquals("user_id", metadata2.getNodeMetadata("id1").getLabels().get(0).getUserDefinedId());
        assertEquals("position_name", metadata2.getNodeMetadata("id1").getLabels().get(0).getPositionName());
        assertNotNull(metadata2.getNodeMetadata("id2"));
        assertEquals(1, metadata2.getWireMetadata().size());
        assertNotNull(metadata2.getWireMetadata("id3"));
        assertEquals("id3", metadata2.getWireMetadata("id3").getId());
        assertEquals("id1", metadata2.getWireMetadata("id3").getNodeId1());
        assertEquals("id2", metadata2.getWireMetadata("id3").getNodeId2());
        assertFalse(metadata2.getWireMetadata("id3").isStraight());
        assertNotNull(metadata2.getFeederInfoMetadata("id1"));
        assertEquals("id3", metadata2.getFeederInfoMetadata("id1").getEquipmentId());
        assertEquals("ONE", metadata2.getFeederInfoMetadata("id1").getSide());
        assertEquals("user_id", metadata2.getFeederInfoMetadata("id1").getUserDefinedId());
        assertEquals("COMPONENT_TYPE", metadata2.getFeederInfoMetadata("id1").getComponentType());

        assertNotNull(metadata2.getBusInfoMetadata("id6"));
        assertEquals("busNodeId1", metadata2.getBusInfoMetadata("id6").getBusNodeId());
        assertEquals("user_id", metadata2.getBusInfoMetadata("id6").getUserDefinedId());

        assertEquals(AnchorOrientation.NONE, metadata2.getAnchorPoints(BREAKER).get(0).getOrientation());
        assertEquals(5, metadata2.getAnchorPoints(BREAKER).get(0).getX(), 0);
        assertEquals(4, metadata2.getAnchorPoints(BREAKER).get(0).getY(), 0);

        Path meta = tmpDir.resolve("meta.json");
        metadata.writeJson(meta);
        GraphMetadata metadata3 = GraphMetadata.parseJson(meta);
        assertEquals(1, metadata3.getComponentMetadata().size());
        assertNotNull(metadata3.getComponentMetadata(BREAKER));
        assertEquals(1, metadata3.getComponentMetadata(BREAKER).getAnchorPoints().size());
        assertEquals(5, metadata3.getComponentMetadata(BREAKER).getAnchorPoints().get(0).getX(), 0);
        assertEquals(4, metadata3.getComponentMetadata(BREAKER).getAnchorPoints().get(0).getY(), 0);
        assertEquals(AnchorOrientation.NONE, metadata3.getComponentMetadata(BREAKER).getAnchorPoints().get(0).getOrientation());
        assertEquals(10, metadata3.getComponentMetadata(BREAKER).getSize().getWidth(), 0);
        assertEquals(12, metadata3.getComponentMetadata(BREAKER).getSize().getHeight(), 0);
        assertEquals(2, metadata3.getNodeMetadata().size());
        assertNotNull(metadata3.getNodeMetadata("id1"));
        assertEquals("id1", metadata3.getNodeMetadata("id1").getId());
        assertEquals("vid1", metadata3.getNodeMetadata("id1").getVId());
        assertEquals(BREAKER, metadata3.getNodeMetadata("id1").getComponentType());
        assertEquals(BUSBAR_SECTION, metadata3.getNodeMetadata("id2").getComponentType());
        assertEquals("id2", metadata3.getNodeMetadata("id2").getId());
        assertEquals("vid2", metadata3.getNodeMetadata("id2").getVId());
        assertNotNull(metadata3.getNodeMetadata("id2"));
        assertEquals(1, metadata3.getWireMetadata().size());
        assertNotNull(metadata3.getWireMetadata("id3"));
        assertEquals("id3", metadata3.getWireMetadata("id3").getId());
        assertEquals("id1", metadata3.getWireMetadata("id3").getNodeId1());
        assertEquals("id2", metadata3.getWireMetadata("id3").getNodeId2());
        assertFalse(metadata3.getWireMetadata("id3").isStraight());
        assertEquals("id3", metadata3.getFeederInfoMetadata("id1").getEquipmentId());
    }

    @Test
    void testGraphMetadataWithLine() throws IOException {
        GraphMetadata metadata = new GraphMetadata(new LayoutParameters(), new SvgParameters());
        metadata.addNodeMetadata(new GraphMetadata.NodeMetadata("STATION_EXAMPLE", "idSTATION_95_EXAMPLE", "vid1", null, LCC_CONVERTER_STATION, false, Direction.UNDEFINED, false, null, Collections.emptyList()));
        metadata.addNodeMetadata(new GraphMetadata.NodeMetadata(null, "bid1", "vid1", null, BUSBAR_SECTION, false, Direction.UNDEFINED, false, null, Collections.emptyList()));
        metadata.addNodeMetadata(new GraphMetadata.NodeMetadata(null, "lid1", "vid1", null, LINE, false, Direction.UNDEFINED, false, null, Collections.emptyList()));
        metadata.addWireMetadata(new GraphMetadata.WireMetadata("wid1", "bid1", "lid1", false, false));
        metadata.addNodeMetadata(new GraphMetadata.NodeMetadata(null, "bid2", "vid2", null, BUSBAR_SECTION, false, Direction.UNDEFINED, false, null, Collections.emptyList()));
        metadata.addNodeMetadata(new GraphMetadata.NodeMetadata(null, "lid2", "vid2", null, LINE, false, Direction.UNDEFINED, false, null, Collections.emptyList()));
        metadata.addWireMetadata(new GraphMetadata.WireMetadata("wid2", "bid2", "lid2", false, false));
        metadata.addLineMetadata(new GraphMetadata.LineMetadata("lid", "lid1", "lid2"));

        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                                  .writeValueAsString(metadata);
        GraphMetadata metadata2 = objectMapper.readValue(json, GraphMetadata.class);
        checkMetadata(metadata2);

        Path meta = tmpDir.resolve("meta.json");
        metadata.writeJson(meta);
        GraphMetadata metadata3 = GraphMetadata.parseJson(meta);
        checkMetadata(metadata3);
    }

    private void checkMetadata(GraphMetadata metadata) {
        assertEquals(5, metadata.getNodeMetadata().size());
        assertEquals("STATION_EXAMPLE", metadata.getNodeMetadata("idSTATION_95_EXAMPLE").getUnescapedId());
        assertNotNull(metadata.getNodeMetadata("bid1"));
        assertNull(metadata.getNodeMetadata("bid1").getUnescapedId());
        assertEquals("bid1", metadata.getNodeMetadata("bid1").getId());
        assertEquals("vid1", metadata.getNodeMetadata("bid1").getVId());
        assertEquals(BUSBAR_SECTION, metadata.getNodeMetadata("bid1").getComponentType());
        assertEquals("lid1", metadata.getNodeMetadata("lid1").getId());
        assertEquals("vid1", metadata.getNodeMetadata("lid1").getVId());
        assertEquals(LINE, metadata.getNodeMetadata("lid1").getComponentType());
        assertEquals("bid2", metadata.getNodeMetadata("bid2").getId());
        assertEquals("vid2", metadata.getNodeMetadata("bid2").getVId());
        assertEquals(BUSBAR_SECTION, metadata.getNodeMetadata("bid2").getComponentType());
        assertEquals("lid2", metadata.getNodeMetadata("lid2").getId());
        assertEquals("vid2", metadata.getNodeMetadata("lid2").getVId());
        assertEquals(LINE, metadata.getNodeMetadata("lid2").getComponentType());

        assertEquals(2, metadata.getWireMetadata().size());
        assertNotNull(metadata.getWireMetadata("wid1"));
        assertEquals("wid1", metadata.getWireMetadata("wid1").getId());
        assertEquals("bid1", metadata.getWireMetadata("wid1").getNodeId1());
        assertEquals("lid1", metadata.getWireMetadata("wid1").getNodeId2());
        assertNotNull(metadata.getWireMetadata("wid2"));
        assertEquals("wid2", metadata.getWireMetadata("wid2").getId());
        assertEquals("bid2", metadata.getWireMetadata("wid2").getNodeId1());
        assertEquals("lid2", metadata.getWireMetadata("wid2").getNodeId2());

        assertEquals(1, metadata.getLineMetadata().size());
        assertNotNull(metadata.getLineMetadata("lid"));
        assertEquals("lid", metadata.getLineMetadata("lid").getId());
        assertEquals("lid1", metadata.getLineMetadata("lid").getNodeId1());
        assertEquals("lid2", metadata.getLineMetadata("lid").getNodeId2());
    }

}
