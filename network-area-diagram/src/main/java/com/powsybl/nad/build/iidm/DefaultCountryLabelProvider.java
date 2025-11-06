/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nad.build.iidm;

import com.powsybl.iidm.network.*;
import com.powsybl.nad.model.BranchEdge;
import com.powsybl.nad.svg.EdgeInfo;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class DefaultCountryLabelProvider implements CountryLabelProvider {
    @Override
    public CountryLegend getCountryLegend(Country country) {
        return new CountryLegend(List.of(country.getName()), List.of());
    }

    @Override
    public Optional<EdgeInfo> getCountryEdgeInfo(Country country1, Country country2, List<Line> lines, List<TieLine> tieLines, List<HvdcLine> hvdcLines, BranchEdge.Side side) {
        if (country1 != Country.FR && country2 != Country.FR && side == BranchEdge.Side.TWO) {
            return Optional.empty();
        }
        if (country2 == Country.FR && side == BranchEdge.Side.ONE || country1 == Country.FR && side == BranchEdge.Side.TWO) {
            return Optional.empty();
        }
        Country edgeInfoCountry = side == BranchEdge.Side.ONE ? country1 : country2;
        double p12Lines = lines.stream().mapToDouble(l -> getBranchActivePower(edgeInfoCountry, l.getTerminal1(), l.getTerminal2())).sum();
        double p12TieLines = tieLines.stream().mapToDouble(tl -> getBranchActivePower(edgeInfoCountry, tl.getTerminal1(), tl.getTerminal2())).sum();
        double p12HvdcLines = hvdcLines.stream().mapToDouble(hvdcLine -> getBranchActivePower(edgeInfoCountry, hvdcLine.getConverterStation1().getTerminal(), hvdcLine.getConverterStation2().getTerminal())).sum();
        double totalActivePower = p12Lines + p12TieLines + p12HvdcLines;
        return Optional.of(new EdgeInfo(EdgeInfo.ACTIVE_POWER, totalActivePower, value -> String.format(Locale.US, "%.1f MW", value)));
    }

    /**
     * Gets the active power from a line, handling NaN values.
     */
    private double getBranchActivePower(Country country1, Terminal terminal1, Terminal terminal2) {
        Country countryTerminal1 = terminal1.getVoltageLevel().getSubstation()
                .flatMap(Substation::getCountry).orElse(null);
        double p = (country1 == countryTerminal1 ? terminal1 : terminal2).getP();
        return Double.isNaN(p) ? 0.0 : p;
    }

    @Override
    public String getBranchLabel(Country country1, Country country2, List<Line> lines, List<TieLine> tieLines, List<HvdcLine> hvdcLines) {
        return "";
    }
}
