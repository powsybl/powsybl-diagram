/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class FlowTransfers {

    private FlowTransfer active = new FlowTransfer();
    private FlowTransfer reactive = new FlowTransfer();

    public FlowTransfer getActive() {
        return active;
    }

    public FlowTransfer getReactive() {
        return reactive;
    }

    public FlowTransfers setActive(double value, String customText) {
        this.active = new FlowTransfer(value, customText);
        return this;
    }

    public FlowTransfers setReactive(double value, String customText) {
        this.reactive = new FlowTransfer(value, customText);
        return this;
    }
}
