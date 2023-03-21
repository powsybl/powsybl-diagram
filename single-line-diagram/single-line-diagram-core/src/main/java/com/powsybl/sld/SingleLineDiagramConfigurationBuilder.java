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

public class SingleLineDiagramConfigurationBuilder {

    SvgParameters svgParameters = new SvgParameters();
    LayoutParameters layoutParameters = new LayoutParameters();
    ComponentLibrary componentLibrary = new ConvergenceComponentLibrary();
    DiagramLabelProvider diagramLabelProvider;
    DiagramStyleProvider diagramStyleProvider;
    VoltageLevelLayoutFactory voltageLevelLayoutFactory;
    SubstationLayoutFactory substationLayoutFactory = new HorizontalSubstationLayoutFactory();
    Network network;
    boolean defaultLabelProvider;

    public SingleLineDiagramConfigurationBuilder(Network network) {
        this.network = network;
        voltageLevelLayoutFactory = new SmartVoltageLevelLayoutFactory(network);
        diagramLabelProvider = new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters);
        diagramStyleProvider = new TopologicalStyleProvider(network);
        defaultLabelProvider = true;
    }

    public SingleLineDiagramConfigurationBuilder withSvgParameters(SvgParameters svgParameters) {
        this.svgParameters = svgParameters;
        return this;
    }

    public SingleLineDiagramConfigurationBuilder withLayoutParameters(LayoutParameters layoutParameters) {
        this.layoutParameters = layoutParameters;
        if (defaultLabelProvider) {
            diagramLabelProvider = new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters);
        }
        return this;
    }

    public SingleLineDiagramConfigurationBuilder withComponentLibrary(ComponentLibrary componentLibrary) {
        this.componentLibrary = componentLibrary;
        if (defaultLabelProvider) {
            diagramLabelProvider = new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters);
        }
        return this;
    }

    public SingleLineDiagramConfigurationBuilder withDiagramLabelProvider(DiagramLabelProvider diagramLabelProvider) {
        this.diagramLabelProvider = diagramLabelProvider;
        defaultLabelProvider = false;
        return this;
    }

    public SingleLineDiagramConfigurationBuilder withDiagramStyleProvider(DiagramStyleProvider diagramStyleProvider) {
        this.diagramStyleProvider = diagramStyleProvider;
        return this;
    }

    public SingleLineDiagramConfigurationBuilder withVoltageLevelLayoutFactory(VoltageLevelLayoutFactory voltageLevelLayoutFactory) {
        this.voltageLevelLayoutFactory = voltageLevelLayoutFactory;
        return this;
    }

    public SingleLineDiagramConfigurationBuilder withSubstationLayoutFactory(SubstationLayoutFactory substationLayoutFactory) {
        this.substationLayoutFactory = substationLayoutFactory;
        return this;
    }

    public SingleLineDiagramConfiguration build() {
        return new SingleLineDiagramConfiguration(svgParameters, layoutParameters, componentLibrary, diagramLabelProvider, diagramStyleProvider, substationLayoutFactory, voltageLevelLayoutFactory);
    }
}
