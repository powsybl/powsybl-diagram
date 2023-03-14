package com.powsybl.nad;

import com.powsybl.nad.build.iidm.IdProvider;
import com.powsybl.nad.layout.LayoutFactory;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.LabelProvider;
import com.powsybl.nad.svg.StyleProvider;
import com.powsybl.nad.svg.SvgParameters;

public class NetworkAreaDiagramConfiguration {

    SvgParameters svgParameters;
    LayoutParameters layoutParameters;
    StyleProvider styleProvider;
    LabelProvider labelProvider;
    LayoutFactory layoutFactory;
    IdProvider idProvider;

    public NetworkAreaDiagramConfiguration(SvgParameters svgParameters, LayoutParameters layoutParameters, StyleProvider styleProvider, LabelProvider labelProvider, LayoutFactory layoutFactory, IdProvider idProvider) {
        this.svgParameters = svgParameters;
        this.layoutParameters = layoutParameters;
        this.styleProvider = styleProvider;
        this.labelProvider = labelProvider;
        this.layoutFactory = layoutFactory;
        this.idProvider = idProvider;
    }

    public SvgParameters getSvgParameters() {
        return svgParameters;
    }

    public LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }

    public StyleProvider getStyleProvider() {
        return styleProvider;
    }

    public LabelProvider getLabelProvider() {
        return labelProvider;
    }

    public LayoutFactory getLayoutFactory() {
        return layoutFactory;
    }

    public IdProvider getIdProvider() {
        return idProvider;
    }

}
