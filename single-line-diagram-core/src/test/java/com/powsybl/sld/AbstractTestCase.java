/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.google.common.io.ByteStreams;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ResourcesComponentLibrary;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.svg.DefaultNodeLabelConfiguration;
import com.powsybl.sld.svg.DefaultSubstationDiagramInitialValueProvider;
import com.powsybl.sld.svg.DefaultSubstationDiagramStyleProvider;
import com.powsybl.sld.svg.DefaultSVGWriter;
import com.powsybl.sld.svg.SubstationDiagramStyleProvider;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public abstract class AbstractTestCase {

    protected Network network;

    protected VoltageLevel vl;
    protected Substation substation;

    protected final ResourcesComponentLibrary componentLibrary = new ResourcesComponentLibrary("/ConvergenceLibrary");

    protected final SubstationDiagramStyleProvider styleProvider = new DefaultSubstationDiagramStyleProvider();

    protected static String normalizeLineSeparator(String str) {
        return str.replace("\r\n", "\n")
                .replace("\r", "\n");
    }

    abstract void setUp() throws IOException;

    String getName() {
        return getClass().getSimpleName();
    }

    VoltageLevel getVl() {
        return vl;
    }

    Substation getSubstation() {
        return substation;
    }

    public void compareSvg(Graph graph, LayoutParameters layoutParameters, String refSvgName) {
        try (StringWriter writer = new StringWriter()) {
            new DefaultSVGWriter(componentLibrary, layoutParameters)
                    .write("", graph,
                            new DefaultSubstationDiagramInitialValueProvider(network),
                            styleProvider,
                            new DefaultNodeLabelConfiguration(componentLibrary),
                            writer);
            writer.flush();

//            FileWriter fw = new FileWriter(System.getProperty("user.home") + refSvgName);
//            fw.write(writer.toString());
//            fw.close();

            String refSvg = normalizeLineSeparator(new String(ByteStreams.toByteArray(getClass().getResourceAsStream(refSvgName)), StandardCharsets.UTF_8));
            String svg = normalizeLineSeparator(writer.toString());
            assertEquals(refSvg, svg);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void compareSvg(Graph graph, LayoutParameters layoutParameters, String refSvgName, SubstationDiagramStyleProvider myStyleProvider) {
        try (StringWriter writer = new StringWriter()) {
            new DefaultSVGWriter(componentLibrary, layoutParameters)
                    .write("", graph,
                            new DefaultSubstationDiagramInitialValueProvider(network),
                            myStyleProvider,
                            new DefaultNodeLabelConfiguration(componentLibrary),
                            writer);
            writer.flush();

//            FileWriter fw = new FileWriter(System.getProperty("user.home") + refSvgName);
//            fw.write(writer.toString());
//            fw.close();

            String refSvg = normalizeLineSeparator(new String(ByteStreams.toByteArray(getClass().getResourceAsStream(refSvgName)), StandardCharsets.UTF_8));
            String svg = normalizeLineSeparator(writer.toString());
            assertEquals(refSvg, svg);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void compareSvg(SubstationGraph graph, LayoutParameters layoutParameters, String refSvgName) {
        try (StringWriter writer = new StringWriter()) {
            new DefaultSVGWriter(componentLibrary, layoutParameters)
                    .write("", graph,
                            new DefaultSubstationDiagramInitialValueProvider(network),
                            styleProvider,
                            new DefaultNodeLabelConfiguration(componentLibrary),
                            writer);
            writer.flush();

//            FileWriter fw = new FileWriter(System.getProperty("user.home") + refSvgName);
//            fw.write(writer.toString());
//            fw.close();

            String refSvg = normalizeLineSeparator(new String(ByteStreams.toByteArray(getClass().getResourceAsStream(refSvgName)), StandardCharsets.UTF_8));
            String svg = normalizeLineSeparator(writer.toString());
            assertEquals(refSvg, svg);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void compareSvg(SubstationGraph graph, LayoutParameters layoutParameters,
                           String refSvgName, SubstationDiagramStyleProvider myStyleProvider) {
        try (StringWriter writer = new StringWriter()) {
            new DefaultSVGWriter(componentLibrary, layoutParameters)
                    .write("", graph,
                            new DefaultSubstationDiagramInitialValueProvider(network),
                            myStyleProvider,
                            new DefaultNodeLabelConfiguration(componentLibrary),
                            writer);
            writer.flush();

//            FileWriter fw = new FileWriter(System.getProperty("user.home") + refSvgName);
//            fw.write(writer.toString());
//            fw.close();

            String refSvg = normalizeLineSeparator(new String(ByteStreams.toByteArray(getClass().getResourceAsStream(refSvgName)), StandardCharsets.UTF_8));
            String svg = normalizeLineSeparator(writer.toString());
            assertEquals(refSvg, svg);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
