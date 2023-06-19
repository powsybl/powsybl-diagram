package com.powsybl.nad.svg.iidm;

import com.powsybl.iidm.network.Network;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.SvgParameters;

public interface LabelProviderFactory {
    LabelProvider create(Network network, SvgParameters svgParameters);

}
