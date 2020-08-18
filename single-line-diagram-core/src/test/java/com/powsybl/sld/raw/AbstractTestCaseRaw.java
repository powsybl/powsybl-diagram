/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.AbstractTestCase;
import com.powsybl.sld.RawGraphBuilder;
import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.FeederNode;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.svg.InitialValue;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public abstract class AbstractTestCaseRaw extends AbstractTestCase {

    protected RawGraphBuilder rawGraphBuilder = new RawGraphBuilder();


}
