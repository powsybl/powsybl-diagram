/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad;

import com.google.common.io.ByteStreams;
import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.nad.build.iidm.IntIdProvider;
import com.powsybl.nad.build.iidm.NetworkGraphBuilder;
import com.powsybl.nad.build.iidm.VoltageLevelFilter;
import com.powsybl.nad.layout.BasicForceLayout;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.library.DefaultComponentLibrary;
import com.powsybl.nad.library.NadComponentLibrary;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.routing.EdgeRouting;
import com.powsybl.nad.routing.StraightEdgeRouting;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.StyleProvider;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.SvgWriter;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTest.class);
    private static final Pattern SVG_ID_PATTERN = Pattern.compile("(.*id=\")(\\w+)(\".*)");
    private static final Pattern HREF_PATTERN = Pattern.compile("(.*href=\")(#\\w+)(\".*)");
    protected boolean debugSvg = true;
    protected boolean overrideTestReferences = false;
    protected boolean throwOnIdChange = false;

    private SvgParameters svgParameters;

    private LayoutParameters layoutParameters;

    protected abstract StyleProvider getStyleProvider(Network network);

    protected abstract LabelProvider getLabelProvider(Network network);

    protected NadComponentLibrary getComponentLibrary() {
        return new DefaultComponentLibrary();
    }

    protected EdgeRouting getEdgeRouting() {
        return new StraightEdgeRouting();
    }

    protected void assertFileEquals(String resourceNameExpected, Path generatedFile) {
        try {
            assertStringEquals(resourceNameExpected, Files.readString(generatedFile));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected void assertSvgEquals(String resourceName, Network network) {
        assertSvgEquals(resourceName, network, VoltageLevelFilter.NO_FILTER);
    }

    protected void assertSvgEquals(String resourceName, Network network, Predicate<VoltageLevel> voltageLevelFilter) {
        Graph graph = new NetworkGraphBuilder(network, voltageLevelFilter, getLabelProvider(network), getLayoutParameters(), new IntIdProvider()).buildGraph();
        new BasicForceLayout().run(graph, getLayoutParameters());
        StringWriter writer = new StringWriter();
        new SvgWriter(getSvgParameters(), getStyleProvider(network), getComponentLibrary(), getEdgeRouting()).writeSvg(graph, writer);
        assertStringEquals(resourceName, writer.toString());
    }

    protected void assertStringEquals(String resourceNameExpected, String generated) {
        if (debugSvg) {
            writeToHomeDir(resourceNameExpected, generated);
        }
        if (overrideTestReferences) {
            overrideTestReference(resourceNameExpected, generated);
        }
        String expected = toString(resourceNameExpected);
        String actual = normalizeLineSeparator(generated);
        try {
            assertEquals(expected, actual);
        } catch (AssertionFailedError exception) {
            if (checkIfOnlyIdsAreDifferent(expected, actual)) {
                LOGGER.error("Only ids and/or hrefs are different between the expected and the actual svg.");
                if (throwOnIdChange) {
                    throw exception;
                }
            } else {
                throw exception;
            }
        }
    }

    private static boolean checkIfOnlyIdsAreDifferent(String expected, String actual) {
        String[] expectedLines = expected.split("\n");
        String[] actualLines = actual.split("\n");
        for (int i = 0; i < expectedLines.length; i++) {
            String expectedLine = expectedLines[i];
            String actualLine = actualLines[i];
            if (!expectedLine.equals(actualLine) && checkAroundSvgIds(expectedLine, actualLine) && checkAroundHref(expectedLine, actualLine)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkAroundHref(String expectedLine, String actualLine) {
        return checkPattern(expectedLine, actualLine, HREF_PATTERN);
    }

    private static boolean checkAroundSvgIds(String expectedLine, String actualLine) {
        return checkPattern(expectedLine, actualLine, SVG_ID_PATTERN);
    }

    private static boolean checkPattern(String expectedLine, String actualLine, Pattern pattern) {
        Matcher expectedMatcher = pattern.matcher(expectedLine);
        Matcher actualMatcher = pattern.matcher(actualLine);
        return !expectedMatcher.matches() || !actualMatcher.matches()
            || !expectedMatcher.group(1).equals(actualMatcher.group(1))
            || !expectedMatcher.group(3).equals(actualMatcher.group(3));

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

    protected static String normalizeLineSeparator(String str) {
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
