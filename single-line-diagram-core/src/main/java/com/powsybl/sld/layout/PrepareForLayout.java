/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.graphs.VoltageLevelGraph;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
*/

public final class PrepareForLayout {

    public static void run(VoltageLevelGraph graph, LayoutParameters layoutParameters, boolean removeUnnecessaryFictitiousNodes, boolean substituteSingularFictitiousByFeederNode) {
        graph.substituteFictitiousNodesMirroringBusNodes();
        if (removeUnnecessaryFictitiousNodes) {
            graph.removeUnnecessaryFictitiousNodes();
        }
        if (substituteSingularFictitiousByFeederNode) {
            graph.substituteSingularFictitiousByFeederNode();
        }
        graph.insertFictitiousNodesAtFeeders();
        graph.extendNodeConnectedToBus();
        graph.extendSwitchBetweenBuses();
        graph.extendFirstOutsideNode();
        graph.extendBusConnectedToBus();
    }

    public static void run(VoltageLevelGraph graph, LayoutParameters layoutParameters) {
        run(graph, layoutParameters, true, true);
    }
}
