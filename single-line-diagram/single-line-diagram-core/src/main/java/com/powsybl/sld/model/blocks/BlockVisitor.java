/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model.blocks;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

public interface BlockVisitor {

    void visit(BodyPrimaryBlock block);

    void visit(LegPrimaryBlock block);

    void visit(FeederPrimaryBlock block);

    void visit(UndefinedBlock block);

    void visit(BodyParallelBlock block);

    void visit(SerialBlock block);

    void visit(LegParallelBlock block);
}
