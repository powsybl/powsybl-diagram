package com.powsybl.sld;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.layout.*;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ConvergenceComponentLibrary;
import com.powsybl.sld.svg.DefaultDiagramLabelProvider;
import com.powsybl.sld.svg.DiagramLabelProvider;
import com.powsybl.sld.svg.DiagramStyleProvider;
import com.powsybl.sld.svg.SvgParameters;
import com.powsybl.sld.util.TopologicalStyleProvider;

public class SingleLineDiagramConfigurationAdder {

    SvgParameters svgParameters = new SvgParameters();
    LayoutParameters layoutParameters = new LayoutParameters();
    ComponentLibrary componentLibrary = new ConvergenceComponentLibrary();
    DiagramLabelProvider diagramLabelProvider;
    DiagramStyleProvider diagramStyleProvider;
    VoltageLevelLayoutFactory voltageLevelLayoutFactory;
    SubstationLayoutFactory substationLayoutFactory = new HorizontalSubstationLayoutFactory();
    Network network;
    boolean defaultLabelProvider;

    public SingleLineDiagramConfigurationAdder(Network network) {
        this.network = network;
        voltageLevelLayoutFactory = new SmartVoltageLevelLayoutFactory(network);
        diagramLabelProvider = new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters);
        diagramStyleProvider = new TopologicalStyleProvider(network);
        defaultLabelProvider = true;
    }

    public SingleLineDiagramConfigurationAdder setSvgParameters(SvgParameters svgParameters) {
        this.svgParameters = svgParameters;
        return this;
    }

    public SingleLineDiagramConfigurationAdder setLayoutParameters(LayoutParameters layoutParameters) {
        this.layoutParameters = layoutParameters;
        if (defaultLabelProvider) {
            diagramLabelProvider = new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters);
        }
        return this;
    }

    public SingleLineDiagramConfigurationAdder setComponentLibrary(ComponentLibrary componentLibrary) {
        this.componentLibrary = componentLibrary;
        if (defaultLabelProvider) {
            diagramLabelProvider = new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters);
        }
        return this;
    }

    public SingleLineDiagramConfigurationAdder setDiagramLabelProvider(DiagramLabelProvider diagramLabelProvider) {
        this.diagramLabelProvider = diagramLabelProvider;
        defaultLabelProvider = false;
        return this;
    }

    public SingleLineDiagramConfigurationAdder setDiagramStyleProvider(DiagramStyleProvider diagramStyleProvider) {
        this.diagramStyleProvider = diagramStyleProvider;
        return this;
    }

    public SingleLineDiagramConfigurationAdder setVoltageLevelLayoutFactory(VoltageLevelLayoutFactory voltageLevelLayoutFactory) {
        this.voltageLevelLayoutFactory = voltageLevelLayoutFactory;
        return this;
    }

    public SingleLineDiagramConfigurationAdder setSubstationLayoutFactory(SubstationLayoutFactory substationLayoutFactory) {
        this.substationLayoutFactory = substationLayoutFactory;
        return this;
    }

    public SingleLineDiagramConfiguration add() {
        return new SingleLineDiagramConfiguration(svgParameters, layoutParameters, componentLibrary, diagramLabelProvider, diagramStyleProvider, substationLayoutFactory, voltageLevelLayoutFactory);
    }
}
