/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public interface IdProvider {

    /**
     * Creates a new unique identifier for an SVG object related to the given
     * equipment identifier. The equipment identifier is contextual information
     * only: implementations must not rely on repeated calls returning the same
     * value.
     *
     * @param equipmentId equipment identifier
     * @return a new SVG identifier
     */
    String createSvgId(String equipmentId);

    /**
     * Creates a new unique identifier for an SVG object related to an
     * equipment subtype.
     *
     * @param equipmentId equipment identifier
     * @param subType subtype or label position
     * @return a new SVG identifier
     */
    String createSvgId(String equipmentId, String subType);

    /**
     * Creates a new unique identifier for an SVG object contained in a graph.
     *
     * @param containerId graph/container identifier
     * @param id1 first related object identifier
     * @param id2 second related object identifier
     * @return a new SVG identifier
     */
    String createSvgId(String containerId, String id1, String id2);
}
