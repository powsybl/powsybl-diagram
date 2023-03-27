package com.powsybl.sld.svg;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;

public class DefaultDiagramLabelProviderFactory implements DiagramLabelProviderFactory {

    @Override
    public DiagramLabelProvider create(Network network, ComponentLibrary componentLibrary, LayoutParameters layoutParameters) {
        return new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters);
    }
}
