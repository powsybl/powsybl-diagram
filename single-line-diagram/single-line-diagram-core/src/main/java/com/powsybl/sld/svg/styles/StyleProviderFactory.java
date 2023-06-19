package com.powsybl.sld.svg.styles;

import com.powsybl.iidm.network.Network;

public interface StyleProviderFactory {
    StyleProvider create(Network network);
}
