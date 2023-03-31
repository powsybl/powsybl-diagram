/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad;

import com.google.common.io.ByteStreams;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.nad.build.iidm.NetworkGraphBuilder;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.layout.BasicForceLayout;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.StyleProvider;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.SvgWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractTest {

    protected boolean debugSvg = false;
    protected boolean overrideTestReferences = false;

    private SvgParameters svgParameters;

    private LayoutParameters layoutParameters;

    protected abstract StyleProvider getStyleProvider(Network network);

    protected abstract LabelProvider getLabelProvider(Network network);

    protected String generateSvgString(Network network, String refFilename) {
        return generateSvgString(network, VoltageLevelFilter.NO_FILTER, refFilename);
    }

    protected String generateSvgString(Network network, Predicate<VoltageLevel> voltageLevelFilter, String refFilename) {
        Graph graph = new NetworkGraphBuilder(network, voltageLevelFilter).buildGraph();
        new BasicForceLayout().run(graph, getLayoutParameters());
        StringWriter writer = new StringWriter();
        new SvgWriter(getSvgParameters(), getStyleProvider(network), getLabelProvider(network)).writeSvg(graph, writer);
        String svgString = writer.toString();
        if (debugSvg) {
            writeToHomeDir(refFilename, svgString);
        }
        if (overrideTestReferences) {
            overrideTestReference(refFilename, svgString);
        }
        return svgString;
    }

    private void writeToHomeDir(String refFilename, String svgString) {
        Path debugFolder = Path.of(System.getProperty("user.home"), ".powsybl", "debug-nad");
        try {
            Files.createDirectories(debugFolder);
            Path debugFile = debugFolder.resolve(refFilename.startsWith("/") ? refFilename.substring(1) : refFilename);
            try (BufferedWriter bw = Files.newBufferedWriter(debugFile, StandardCharsets.UTF_8)) {
                bw.write(normalizeLineSeparator(svgString));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void overrideTestReference(String filename, String svgString) {
        Path testReference = Path.of("src", "test", "resources", filename);
        if (!Files.exists(testReference)) {
            return;
        }
        try (BufferedWriter bw = Files.newBufferedWriter(testReference, StandardCharsets.UTF_8)) {
            bw.write(normalizeLineSeparator(svgString));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected String toString(String resourceName) {
        try {
            InputStream in = Objects.requireNonNull(getClass().getResourceAsStream(resourceName));
            return normalizeLineSeparator(new String(ByteStreams.toByteArray(in), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String normalizeLineSeparator(String str) {
        return str.replace("\r\n", "\n")
                .replace("\r", "\n");
    }

    protected LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }

    protected SvgParameters getSvgParameters() {
        return svgParameters;
    }

    protected void setLayoutParameters(LayoutParameters layoutParameters) {
        this.layoutParameters = layoutParameters;
    }

    protected void setSvgParameters(SvgParameters svgParameters) {
        this.svgParameters = svgParameters;
    }
}
