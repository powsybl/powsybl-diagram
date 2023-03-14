package com.powsybl.nad;

import com.powsybl.iidm.network.Network;
import com.powsybl.nad.build.iidm.IdProvider;
import com.powsybl.nad.build.iidm.IntIdProvider;
import com.powsybl.nad.layout.BasicForceLayoutFactory;
import com.powsybl.nad.layout.LayoutFactory;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.StyleProvider;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.TopologicalStyleProvider;

public class NetworkAreaDiagramConfigurationAdder {
    SvgParameters svgParameters = new SvgParameters();
    LayoutParameters layoutParameters = new LayoutParameters();
    StyleProvider styleProvider;
    LabelProvider labelProvider;
    LayoutFactory layoutFactory = new BasicForceLayoutFactory();
    IdProvider idProvider = new IntIdProvider();
    Network network;
    boolean defaultLabelProvider;

    public NetworkAreaDiagramConfigurationAdder(Network network) {
        this.network = network;
        this.styleProvider = new TopologicalStyleProvider(network);
        this.labelProvider = new DefaultLabelProvider(network, svgParameters);
        defaultLabelProvider = true;
    }

    public NetworkAreaDiagramConfigurationAdder setSvgParameters(SvgParameters svgParameters) {
        this.svgParameters = svgParameters;
        if (defaultLabelProvider) {
            this.labelProvider = new DefaultLabelProvider(network, svgParameters);
        }
        return this;
    }

    public NetworkAreaDiagramConfigurationAdder setLayoutParameters(LayoutParameters layoutParameters) {
        this.layoutParameters = layoutParameters;
        return this;
    }

    public NetworkAreaDiagramConfigurationAdder setStyleProvider(StyleProvider styleProvider) {
        this.styleProvider = styleProvider;
        return this;
    }

    public NetworkAreaDiagramConfigurationAdder setLabelProvider(LabelProvider labelProvider) {
        this.labelProvider = labelProvider;
        defaultLabelProvider = false;
        return this;
    }

    public NetworkAreaDiagramConfigurationAdder setLayoutFactory(LayoutFactory layoutFactory) {
        this.layoutFactory = layoutFactory;
        return this;
    }

    public NetworkAreaDiagramConfigurationAdder setIdProvider(IdProvider idProvider) {
        this.idProvider = idProvider;
        return this;
    }

    public NetworkAreaDiagramConfiguration add() {
        return new NetworkAreaDiagramConfiguration(svgParameters, layoutParameters, styleProvider, labelProvider, layoutFactory, idProvider);
    }

}
