/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.noIidm;

import com.powsybl.sld.AbstractTestCase;
import com.powsybl.sld.RawGraphBuilder;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public abstract class AbstractTestCaseNoIidm extends AbstractTestCase {

    protected RawGraphBuilder rawGraphBuilder = new RawGraphBuilder();

}
