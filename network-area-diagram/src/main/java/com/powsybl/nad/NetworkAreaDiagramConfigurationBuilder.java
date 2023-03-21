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

public class NetworkAreaDiagramConfigurationBuilder {
    SvgParameters svgParameters = new SvgParameters();
    LayoutParameters layoutParameters = new LayoutParameters();
    StyleProvider styleProvider;
    LabelProvider labelProvider;
    LayoutFactory layoutFactory = new BasicForceLayoutFactory();
    IdProvider idProvider = new IntIdProvider();
    Network network;
    boolean defaultLabelProvider;

    public NetworkAreaDiagramConfigurationBuilder(Network network) {
        this.network = network;
        this.styleProvider = new TopologicalStyleProvider(network);
        this.labelProvider = new DefaultLabelProvider(network, svgParameters);
        defaultLabelProvider = true;
    }

    public NetworkAreaDiagramConfigurationBuilder withSvgParameters(SvgParameters svgParameters) {
        this.svgParameters = svgParameters;
        if (defaultLabelProvider) {
            this.labelProvider = new DefaultLabelProvider(network, svgParameters);
        }
        return this;
    }

    public NetworkAreaDiagramConfigurationBuilder withLayoutParameters(LayoutParameters layoutParameters) {
        this.layoutParameters = layoutParameters;
        return this;
    }

    public NetworkAreaDiagramConfigurationBuilder withStyleProvider(StyleProvider styleProvider) {
        this.styleProvider = styleProvider;
        return this;
    }

    public NetworkAreaDiagramConfigurationBuilder withLabelProvider(LabelProvider labelProvider) {
        this.labelProvider = labelProvider;
        defaultLabelProvider = false;
        return this;
    }

    public NetworkAreaDiagramConfigurationBuilder withLayoutFactory(LayoutFactory layoutFactory) {
        this.layoutFactory = layoutFactory;
        return this;
    }

    public NetworkAreaDiagramConfigurationBuilder withIdProvider(IdProvider idProvider) {
        this.idProvider = idProvider;
        return this;
    }

    public NetworkAreaDiagramConfiguration build() {
        return new NetworkAreaDiagramConfiguration(svgParameters, layoutParameters, styleProvider, labelProvider, layoutFactory, idProvider);
    }

}
