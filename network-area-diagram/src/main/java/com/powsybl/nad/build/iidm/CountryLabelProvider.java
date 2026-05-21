/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.build.iidm;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.nad.model.BranchEdge;
import com.powsybl.nad.svg.EdgeInfo;

import java.util.List;
import java.util.Optional;

/**
 * Interface for providing labels and legends for countries.
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public interface CountryLabelProvider {
    /**
     * Gets the voltage level legend for a country.
     */
    CountryLegend getCountryLegend(Country country);

    /**
     * Gets EdgeInfo for the connection between two countries.
     */
    Optional<EdgeInfo> getCountryEdgeInfo(Country country1, Country country2, List<Line> lines, List<TieLine> tieLines, List<HvdcLine> hvdcLines, BranchEdge.Side side);

    /**
     * Gets the branch label for the connection between two countries.
     */
    String getBranchLabel(Country country1, Country country2, List<Line> lines, List<TieLine> tieLines, List<HvdcLine> hvdcLines);

    record CountryLegend(List<String> header, List<String> footer) {
    }
}
