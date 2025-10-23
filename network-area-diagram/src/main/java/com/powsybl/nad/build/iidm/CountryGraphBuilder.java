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
import com.powsybl.nad.model.*;
import com.powsybl.nad.svg.EdgeInfo;

import java.util.*;
import java.util.stream.Collectors;

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
     * @param network the network
     * @param idProvider the ID provider
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
        Set<Country> countries = getCountries();
        
        // Create a VoltageLevelNode with one BusNode for each country
        Map<Country, VoltageLevelNode> countryToVlNode = new HashMap<>();

        for (Country country : countries) {
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
     * @return set of countries
     */
    private Set<Country> getCountries() {
        return network.getSubstationStream()
            .map(Substation::getNullableCountry)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
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
        Map<CountryPair, AllLines> allBordersLines = new HashMap<>();

        // Process all lines
        network.getLineStream().forEach(line -> processBranch(line, allBordersLines));
        network.getTieLineStream().forEach(tieLine -> processBranch(tieLine, allBordersLines));

        // Process HVDC lines
        network.getHvdcLineStream().forEach(hvdcLine -> processHvdcLine(hvdcLine, allBordersLines));
        
        // Create BranchEdges for each country pair with connections
        allBordersLines.forEach((countryPair, allLines) -> {
            Country country1 = countryPair.country1();
            Country country2 = countryPair.country2();
            
            VoltageLevelNode vlNode1 = countryToVlNode.get(country1);
            VoltageLevelNode vlNode2 = countryToVlNode.get(country2);

            if (vlNode1 != null && vlNode2 != null) {
                createCountryEdge(graph, country1, country2, allLines, vlNode1, vlNode2);
            }
        });
    }

    /**
     * Processes a line to aggregate active power between countries.
     */
    private void processBranch(Branch<?> branch, Map<CountryPair, AllLines> allBorderLines) {
        Country country1 = getCountryFromTerminal(branch.getTerminal1());
        Country country2 = getCountryFromTerminal(branch.getTerminal2());
        
        if (country1 != null && country2 != null && country1 != country2) {
            CountryPair pair = new CountryPair(country1, country2);
            allBorderLines.computeIfAbsent(pair, k -> new AllLines())
                    .addBranch(branch);
        }
    }

    /**
     * Processes a tie line to aggregate active power between countries.
     */
    private void processHvdcLine(HvdcLine hvdcLine, Map<CountryPair, AllLines> allBorderLines) {
        Country country1 = getCountryFromTerminal(hvdcLine.getConverterStation1().getTerminal());
        Country country2 = getCountryFromTerminal(hvdcLine.getConverterStation2().getTerminal());
        
        if (country1 != null && country2 != null && country1 != country2) {
            CountryPair pair = new CountryPair(country1, country2);
            allBorderLines.computeIfAbsent(pair, k -> new AllLines())
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
    private void createCountryEdge(Graph graph, Country country1, Country country2, AllLines allLines,
                                   VoltageLevelNode vlNode1, VoltageLevelNode vlNode2) {
        
        String edgeId = country1.name() + "-" + country2.name();
        Optional<EdgeInfo> edgeInfo1 = labelProvider.getCountryEdgeInfo(country1, country2, allLines.lines, allLines.tieLines, allLines.hvdcLines, BranchEdge.Side.ONE);
        Optional<EdgeInfo> edgeInfo2 = labelProvider.getCountryEdgeInfo(country1, country2, allLines.lines, allLines.tieLines, allLines.hvdcLines, BranchEdge.Side.TWO);
        String label = labelProvider.getBranchLabel(country1, country2, allLines.lines, allLines.tieLines, allLines.hvdcLines);
        
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
    private record CountryPair(Country country1, Country country2) {
        CountryPair {
            // Ensure consistent ordering to avoid duplicate pairs
            if (country1.compareTo(country2) > 0) {
                Country temp = country1;
                country1 = country2;
                country2 = temp;
            }
        }
    }

    private record AllLines(List<Line> lines, List<TieLine> tieLines, List<HvdcLine> hvdcLines) {
        private AllLines() {
            this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }

        public void addBranch(Branch<?> branch) {
            if (branch instanceof Line line) {
                lines.add(line);
            } else if  (branch instanceof TieLine tieLine) {
                tieLines.add(tieLine);
            } else {
                throw new PowsyblException("Unexcepted branch class: " + branch.getClass());
            }
        }
    }
}
