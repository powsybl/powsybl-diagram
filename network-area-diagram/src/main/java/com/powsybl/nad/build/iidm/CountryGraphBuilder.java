/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.build.iidm;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.nad.build.GraphBuilder;
import com.powsybl.nad.model.BranchEdge;
import com.powsybl.nad.model.BusNode;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.model.VoltageLevelNode;
import com.powsybl.nad.svg.EdgeInfo;

import java.util.*;

/**
 * Graph builder that creates a graph based on substation countries.
 * Creates one VoltageLevelNode with one BusNode for each country found in the network substations,
 * and BranchEdges between countries representing the existing lines between countries.
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class CountryGraphBuilder implements GraphBuilder {

    private final Network network;
    private final IdProvider idProvider;
    private final CountryLabelProvider labelProvider;

    /**
     * Creates a new CountryGraphBuilder.
     *
     * @param network       the network
     * @param idProvider    the ID provider
     * @param labelProvider the label provider
     */
    public CountryGraphBuilder(Network network, IdProvider idProvider, CountryLabelProvider labelProvider) {
        this.network = Objects.requireNonNull(network);
        this.idProvider = Objects.requireNonNull(idProvider);
        this.labelProvider = Objects.requireNonNull(labelProvider);
    }

    @Override
    public Graph buildGraph() {
        Graph graph = new Graph();

        // Get all countries from substations

        // Create a VoltageLevelNode with one BusNode for each country
        Map<Country, VoltageLevelNode> countryToVlNode = new EnumMap<>(Country.class);

        for (Country country : getCountries()) {
            CountryLabelProvider.CountryLegend legend = labelProvider.getCountryLegend(country);
            VoltageLevelNode vlNode = new VoltageLevelNode(
                    idProvider,
                    country.name(),
                    country.name(),
                    false,
                    true,
                    legend.header(),
                    legend.footer()
            );

            BusNode busNode = new BusNode(idProvider, country.name(), Collections.emptyList(), null);

            vlNode.addBusNode(busNode);
            graph.addNode(vlNode);
            graph.addTextNode(vlNode);

            countryToVlNode.put(country, vlNode);
        }

        // Create edges between countries based on lines
        createCountryConnections(graph, countryToVlNode);

        return graph;
    }

    /**
     * Gets all countries from substations in the network.
     *
     * @return list of countries
     */
    private List<Country> getCountries() {
        return network.getSubstationStream()
                .map(Substation::getNullableCountry)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * Creates connections (BranchEdges) between countries based on lines in the network.
     *
     * @param graph           the graph to add edges to
     * @param countryToVlNode mapping from country to voltage level node
     */
    private void createCountryConnections(Graph graph,
                                          Map<Country, VoltageLevelNode> countryToVlNode) {

        // Map to store aggregated active powers between countries
        Map<Border, BorderEdges> borderEdgesMap = new LinkedHashMap<>();

        // Process all lines
        network.getLineStream().forEach(line -> fillBorderEdgesMap(line, borderEdgesMap));
        network.getTieLineStream().forEach(tieLine -> fillBorderEdgesMap(tieLine, borderEdgesMap));

        // Process HVDC lines
        network.getHvdcLineStream().forEach(hvdcLine -> fillBorderEdgesMap(hvdcLine, borderEdgesMap));

        // Create BranchEdges for each country pair with connections
        borderEdgesMap.forEach((border, borderEdges) -> {
            Country country1 = border.country1();
            Country country2 = border.country2();

            VoltageLevelNode vlNode1 = countryToVlNode.get(country1);
            VoltageLevelNode vlNode2 = countryToVlNode.get(country2);

            if (vlNode1 != null && vlNode2 != null) {
                createCountryEdge(graph, country1, country2, borderEdges, vlNode1, vlNode2);
            }
        });
    }

    /**
     * Processes a branch to aggregate active power between countries.
     */
    private void fillBorderEdgesMap(Branch<?> branch, Map<Border, BorderEdges> allBorderLines) {
        Country country1 = getCountryFromTerminal(branch.getTerminal1());
        Country country2 = getCountryFromTerminal(branch.getTerminal2());

        if (country1 != null && country2 != null && country1 != country2) {
            Border pair = new Border(country1, country2);
            allBorderLines.computeIfAbsent(pair, k -> new BorderEdges())
                    .addBranch(branch);
        }
    }

    /**
     * Processes a tie line to aggregate active power between countries.
     */
    private void fillBorderEdgesMap(HvdcLine hvdcLine, Map<Border, BorderEdges> allBorderLines) {
        Country country1 = getCountryFromTerminal(hvdcLine.getConverterStation1().getTerminal());
        Country country2 = getCountryFromTerminal(hvdcLine.getConverterStation2().getTerminal());

        if (country1 != null && country2 != null && country1 != country2) {
            Border pair = new Border(country1, country2);
            allBorderLines.computeIfAbsent(pair, k -> new BorderEdges())
                    .hvdcLines().add(hvdcLine);
        }
    }

    /**
     * Gets the country from a terminal's substation.
     */
    private Country getCountryFromTerminal(Terminal terminal) {
        return terminal.getVoltageLevel().getSubstation().map(Substation::getNullableCountry).orElse(null);
    }

    /**
     * Creates a BranchEdge between two countries.
     */
    private void createCountryEdge(Graph graph, Country country1, Country country2, BorderEdges borderEdges,
                                   VoltageLevelNode vlNode1, VoltageLevelNode vlNode2) {

        String edgeId = country1.name() + "-" + country2.name();
        Optional<EdgeInfo> edgeInfo1 = labelProvider.getCountryEdgeInfo(country1, country2, borderEdges.lines, borderEdges.tieLines, borderEdges.hvdcLines, BranchEdge.Side.ONE);
        Optional<EdgeInfo> edgeInfo2 = labelProvider.getCountryEdgeInfo(country1, country2, borderEdges.lines, borderEdges.tieLines, borderEdges.hvdcLines, BranchEdge.Side.TWO);
        String label = labelProvider.getBranchLabel(country1, country2, borderEdges.lines, borderEdges.tieLines, borderEdges.hvdcLines);

        BranchEdge edge = new BranchEdge(
                idProvider,
                edgeId,
                edgeId,
                BranchEdge.LINE_EDGE,
                edgeInfo1.orElse(null),
                edgeInfo2.orElse(null),
                label
        );

        graph.addEdge(vlNode1, vlNode1.getBusNodes().getFirst(), vlNode2, vlNode2.getBusNodes().getFirst(), edge);
    }

    /**
     * Record to represent a pair of countries, ensuring consistent ordering.
     */
    private record Border(Country country1, Country country2) {
        Border {
            // Ensure consistent ordering to avoid duplicate pairs
            if (country1.compareTo(country2) > 0) {
                Country temp = country1;
                country1 = country2;
                country2 = temp;
            }
        }
    }

    private record BorderEdges(List<Line> lines, List<TieLine> tieLines, List<HvdcLine> hvdcLines) {
        private BorderEdges() {
            this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }

        public void addBranch(Branch<?> branch) {
            if (branch instanceof Line line) {
                lines.add(line);
            } else if (branch instanceof TieLine tieLine) {
                tieLines.add(tieLine);
            } else {
                throw new PowsyblException("Unexcepted branch class: " + branch.getClass());
            }
        }
    }
}
