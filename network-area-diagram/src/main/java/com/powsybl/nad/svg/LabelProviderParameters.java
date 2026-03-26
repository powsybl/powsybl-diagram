package com.powsybl.nad.svg;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class LabelProviderParameters {

    private boolean isBusLegend = true;
    private boolean substationDescriptionDisplayed = false;
    private boolean idDisplayed = false;
    private boolean voltageLevelDetails = false;
    private boolean doubleArrowsDisplayed = false;

    public boolean isBusLegend() {
        return isBusLegend;
    }

    public LabelProviderParameters setBusLegend(boolean isBusLegend) {
        this.isBusLegend = isBusLegend;
        return this;
    }

    public boolean isSubstationDescriptionDisplayed() {
        return substationDescriptionDisplayed;
    }

    public LabelProviderParameters setSubstationDescriptionDisplayed(boolean substationDescriptionDisplayed) {
        this.substationDescriptionDisplayed = substationDescriptionDisplayed;
        return this;
    }

    public boolean isIdDisplayed() {
        return idDisplayed;
    }

    public LabelProviderParameters setIdDisplayed(boolean idDisplayed) {
        this.idDisplayed = idDisplayed;
        return this;
    }

    public boolean isVoltageLevelDetails() {
        return voltageLevelDetails;
    }

    public LabelProviderParameters setVoltageLevelDetails(boolean voltageLevelDetails) {
        this.voltageLevelDetails = voltageLevelDetails;
        return this;
    }

    public boolean isDoubleArrowsDisplayed() {
        return doubleArrowsDisplayed;
    }

    public LabelProviderParameters setDoubleArrowsDisplayed(boolean doubleArrowsDisplayed) {
        this.doubleArrowsDisplayed = doubleArrowsDisplayed;
        return this;
    }
}
