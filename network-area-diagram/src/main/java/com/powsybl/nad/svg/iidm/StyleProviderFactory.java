package com.powsybl.nad.svg.iidm;

import com.powsybl.iidm.network.Network;
import com.powsybl.nad.svg.StyleProvider;

public interface StyleProviderFactory {
    StyleProvider create(Network network);
}
