/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.powsybl.iidm.network.*;
import net.java.truecommons.services.Function;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TopologyVisitorId implements TopologyVisitor {

    private final Function<String> idFunction;

    public TopologyVisitorId(Function<String> idFunction) {
        this.idFunction = idFunction;
    }

    @Override
    public void visitBusbarSection(BusbarSection e) {
        idFunction.apply(e.getId());
    }

    @Override
    public void visitDanglingLine(DanglingLine e) {
        idFunction.apply(e.getId());
    }

    @Override
    public void visitGenerator(Generator e) {
        idFunction.apply(e.getId());
    }

    @Override
    public void visitLine(Line e, Branch.Side s) {
        idFunction.apply(e.getId());
    }

    @Override
    public void visitLoad(Load e) {
        idFunction.apply(e.getId());
    }

    @Override
    public void visitShuntCompensator(ShuntCompensator e) {
        idFunction.apply(e.getId());
    }

    @Override
    public void visitStaticVarCompensator(StaticVarCompensator e) {
        idFunction.apply(e.getId());
    }

    @Override
    public void visitThreeWindingsTransformer(ThreeWindingsTransformer e,
                                              ThreeWindingsTransformer.Side s) {
        idFunction.apply(e.getId());
    }

    @Override
    public void visitTwoWindingsTransformer(TwoWindingsTransformer e, Branch.Side s) {
        idFunction.apply(e.getId());
    }
}
