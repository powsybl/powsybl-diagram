/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.svg;

import java.util.List;

import com.powsybl.nad.model.Point;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro at soft.it>}
 */
public record EdgePoints(List<Point> points1, List<Point> points2) {
}
