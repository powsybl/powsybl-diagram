package com.powsybl.sld.svg.styles;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.svg.styles.iidm.HighlightLineStateStyleProvider;
import com.powsybl.sld.svg.styles.iidm.TopologicalStyleProvider;

public class DefaultStyleProviderFactory implements StyleProviderFactory {
    @Override
    public StyleProvider create(Network network) {
        return new StyleProvidersList(new TopologicalStyleProvider(network), new HighlightLineStateStyleProvider(network));
    }
}
