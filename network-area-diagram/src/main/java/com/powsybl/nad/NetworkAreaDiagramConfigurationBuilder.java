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

import java.util.function.BiFunction;

public class NetworkAreaDiagramConfigurationBuilder {
    SvgParameters svgParameters = new SvgParameters();
    LayoutParameters layoutParameters = new LayoutParameters();
    StyleProvider styleProvider;
    LabelProvider labelProvider;
    BiFunction<Network, SvgParameters, LabelProvider> labelProviderCreator = DefaultLabelProvider::new;
    LayoutFactory layoutFactory = new BasicForceLayoutFactory();
    IdProvider idProvider = new IntIdProvider();
    Network network;

    private static <R extends LabelProvider> R factory(Network network, SvgParameters svgParameters, BiFunction<Network, SvgParameters, R> function) {
        return function.apply(network, svgParameters);
    }

    public NetworkAreaDiagramConfigurationBuilder(Network network) {
        this.network = network;
        this.styleProvider = new TopologicalStyleProvider(network);
        this.labelProvider = factory(network, svgParameters, labelProviderCreator);
    }

    public NetworkAreaDiagramConfigurationBuilder withSvgParameters(SvgParameters svgParameters) {
        this.svgParameters = svgParameters;
        this.labelProvider = factory(network, svgParameters, labelProviderCreator);
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

    public NetworkAreaDiagramConfigurationBuilder withLabelProviderCreator(BiFunction<Network, SvgParameters, LabelProvider> labelProviderCreator) {
        this.labelProviderCreator = labelProviderCreator;
        this.labelProvider = factory(network, svgParameters, labelProviderCreator);
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
