package com.powsybl.sld;

import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.SubstationLayoutFactory;
import com.powsybl.sld.layout.VoltageLevelLayoutFactory;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.svg.DiagramLabelProvider;
import com.powsybl.sld.svg.DiagramStyleProvider;
import com.powsybl.sld.svg.SvgParameters;

public class SingleLineDiagramConfiguration {

    SvgParameters svgParameters;
    LayoutParameters layoutParameters;
    ComponentLibrary componentLibrary;
    DiagramLabelProvider diagramLabelProvider;
    DiagramStyleProvider diagramStyleProvider;
    SubstationLayoutFactory substationLayoutFactory;
    VoltageLevelLayoutFactory voltageLevelLayoutFactory;

    public SingleLineDiagramConfiguration(SvgParameters svgParameters, LayoutParameters layoutParameters, ComponentLibrary componentLibrary, DiagramLabelProvider diagramLabelProvider, DiagramStyleProvider diagramStyleProvider, SubstationLayoutFactory substationLayoutFactory, VoltageLevelLayoutFactory voltageLevelLayoutFactory) {
        this.svgParameters = svgParameters;
        this.layoutParameters = layoutParameters;
        this.componentLibrary = componentLibrary;
        this.diagramLabelProvider = diagramLabelProvider;
        this.diagramStyleProvider = diagramStyleProvider;
        this.substationLayoutFactory = substationLayoutFactory;
        this.voltageLevelLayoutFactory = voltageLevelLayoutFactory;
    }

    public SvgParameters getSvgParameters() {
        return svgParameters;
    }

    public LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }

    public ComponentLibrary getComponentLibrary() {
        return componentLibrary;
    }

    public DiagramLabelProvider getDiagramLabelProvider() {
        return diagramLabelProvider;
    }

    public DiagramStyleProvider getDiagramStyleProvider() {
        return diagramStyleProvider;
    }

    public SubstationLayoutFactory getSubstationLayoutFactory() {
        return substationLayoutFactory;
    }

    public VoltageLevelLayoutFactory getVoltageLevelLayoutFactory() {
        return voltageLevelLayoutFactory;
    }
}
