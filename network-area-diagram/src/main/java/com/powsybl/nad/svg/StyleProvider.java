/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.nad.model.*;

import java.util.List;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public interface StyleProvider {

    String CLASSES_PREFIX = "nad-";
    String VOLTAGE_LEVEL_NODES_CLASS = CLASSES_PREFIX + "vl-nodes";
    String TEXT_NODES_CLASS = CLASSES_PREFIX + "text-nodes";
    String THREE_WT_NODES_CLASS = CLASSES_PREFIX + "3wt-nodes";
    String BOUNDARY_NODE_CLASS = CLASSES_PREFIX + "boundary-node";
    String DISCONNECTED_CLASS = CLASSES_PREFIX + "disconnected";
    String BRANCH_EDGES_CLASS = CLASSES_PREFIX + "branch-edges";
    String HVDC_EDGE_CLASS = CLASSES_PREFIX + "hvdc-edge";
    String THREE_WT_EDGES_CLASS = CLASSES_PREFIX + "3wt-edges";
    String DANGLING_LINE_EDGE_CLASS = CLASSES_PREFIX + "dangling-line-edge";
    String TIE_LINE_EDGE_CLASS = CLASSES_PREFIX + "tie-line-edge";
    String TEXT_EDGES_CLASS = CLASSES_PREFIX + "text-edges";
    String EDGE_INFOS_CLASS = CLASSES_PREFIX + "edge-infos";
    String EDGE_LABEL_CLASS = CLASSES_PREFIX + "edge-label";
    String ARROW_IN_CLASS = CLASSES_PREFIX + "arrow-in";
    String ARROW_OUT_CLASS = CLASSES_PREFIX + "arrow-out";
    String HVDC_CLASS = CLASSES_PREFIX + "hvdc";
    String UNKNOWN_BUSNODE_CLASS = CLASSES_PREFIX + "unknown-busnode";
    String LINE_OVERLOADED_CLASS = CLASSES_PREFIX + "overload";
    String VL_OVERVOLTAGE_CLASS = CLASSES_PREFIX + "overvoltage";
    String VL_UNDERVOLTAGE_CLASS = CLASSES_PREFIX + "undervoltage";
    String EDGE_PATH_CLASS = CLASSES_PREFIX + "edge-path";
    String WINDING_CLASS = CLASSES_PREFIX + "winding";
    String BUSNODE_CLASS = CLASSES_PREFIX + "busnode";
    String LABEL_BOX_CLASS = CLASSES_PREFIX + "label-box";
    String LEGEND_SQUARE_CLASS = CLASSES_PREFIX + "legend-square";
    String PST_ARROW_CLASS = CLASSES_PREFIX + "pst-arrow";

    List<String> getCssFilenames();

    String getStyleDefs();

    List<String> getNodeStyleClasses(Node node);

    List<String> getNodeStyleClasses(BusNode busNode);

    List<String> getEdgeStyleClasses(Edge edge);

    List<String> getSideEdgeStyleClasses(BranchEdge edge, BranchEdge.Side side);

    List<String> getEdgeInfoStyles(EdgeInfo info);

    List<String> getThreeWtNodeStyle(ThreeWtNode threeWtNode, ThreeWtEdge.Side one);
}
