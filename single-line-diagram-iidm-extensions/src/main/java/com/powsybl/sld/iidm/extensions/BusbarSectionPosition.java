/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.BusbarSection;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BusbarSectionPosition extends AbstractExtension<BusbarSection> {

    private int busbarIndex;

    private int sectionIndex;

    private static int checkIndex(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("Busbar index has to be greater or equals to zero");
        }
        return index;
    }

    public BusbarSectionPosition(BusbarSection busbarSection, int busbarIndex, int sectionIndex) {
        super(busbarSection);
        this.busbarIndex = checkIndex(busbarIndex);
        this.sectionIndex = checkIndex(sectionIndex);
    }

    @Override
    public String getName() {
        return "busbarSectionPosition";
    }

    public int getBusbarIndex() {
        return busbarIndex;
    }

    public void setBusbarIndex(int busbarIndex) {
        this.busbarIndex = checkIndex(busbarIndex);
    }

    public int getSectionIndex() {
        return sectionIndex;
    }

    public void setSectionIndex(int sectionIndex) {
        this.sectionIndex = checkIndex(sectionIndex);
    }
}
