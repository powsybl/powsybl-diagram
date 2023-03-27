package com.powsybl.sld;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.layout.*;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ConvergenceComponentLibrary;
import com.powsybl.sld.svg.*;
import com.powsybl.sld.util.TopologicalStyleProvider;

public class SingleLineDiagramConfigurationBuilder {

    SvgParameters svgParameters = new SvgParameters();
    LayoutParameters layoutParameters = new LayoutParameters();
    ComponentLibrary componentLibrary = new ConvergenceComponentLibrary();
    DiagramLabelProvider diagramLabelProvider;
    DiagramLabelProviderFactory diagramLabelProviderFactory;
    DiagramStyleProvider diagramStyleProvider;
    VoltageLevelLayoutFactory voltageLevelLayoutFactory;
    SubstationLayoutFactory substationLayoutFactory = new HorizontalSubstationLayoutFactory();
    Network network;

    public SingleLineDiagramConfigurationBuilder(Network network) {
        this.network = network;
        voltageLevelLayoutFactory = network != null ? new SmartVoltageLevelLayoutFactory(network) : new PositionVoltageLevelLayoutFactory();
        diagramLabelProviderFactory = new DefaultDiagramLabelProviderFactory();
        diagramLabelProvider = network != null ? diagramLabelProviderFactory.create(network, componentLibrary, layoutParameters) : null;
        diagramStyleProvider = new TopologicalStyleProvider(network);
    }

    public SingleLineDiagramConfigurationBuilder withSvgParameters(SvgParameters svgParameters) {
        this.svgParameters = svgParameters;
        return this;
    }

    public SingleLineDiagramConfigurationBuilder withLayoutParameters(LayoutParameters layoutParameters) {
        this.layoutParameters = layoutParameters;
        this.diagramLabelProvider = network != null ? diagramLabelProviderFactory.create(network, componentLibrary, layoutParameters) : null;
        return this;
    }

    public SingleLineDiagramConfigurationBuilder withComponentLibrary(ComponentLibrary componentLibrary) {
        this.componentLibrary = componentLibrary;
        this.diagramLabelProvider = network != null ? diagramLabelProviderFactory.create(network, componentLibrary, layoutParameters) : null;
        return this;
    }

    public SingleLineDiagramConfigurationBuilder withDiagramLabelProviderFactory(DiagramLabelProviderFactory diagramLabelProviderFactory) {
        this.diagramLabelProvider = diagramLabelProviderFactory.create(network, componentLibrary, layoutParameters);
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
