/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import java.util.Objects;
import java.util.Optional;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.sld.svg.SubstationDiagramInitialValueProvider.Direction;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class InitialValue {

    private final Direction direction1;
    private final Direction direction2;
    private final String label1;
    private final String label2;
    private final String label3;
    private final String label4;

    public InitialValue(Direction dir1, Direction dir2, String text1, String text2, String text3, String text4) {
        direction1 = dir1;
        direction2 = dir2;
        label1 = text1;
        label2 = text2;
        label3 = text3;
        label4 = text4;
    }

    public InitialValue(Branch<?> ln, Side side) {
        Objects.requireNonNull(ln);
        Objects.requireNonNull(side);
        double p = side.equals(Side.ONE) ? ln.getTerminal1().getP() : ln.getTerminal2().getP();
        double q = side.equals(Side.ONE) ? ln.getTerminal1().getQ() : ln.getTerminal2().getQ();
        label1 = String.valueOf(Math.round(p));
        label2 = String.valueOf(Math.round(q));
        label3 = null;
        label4 = null;
        direction1 = p > 0 ? Direction.UP : Direction.DOWN;
        direction2 = q > 0 ? Direction.UP : Direction.DOWN;
    }

    public InitialValue(Load load) {
        Objects.requireNonNull(load);
        double p = load.getP0();
        double q = load.getQ0();
        label1 = String.valueOf(Math.round(p));
        label2 = String.valueOf(Math.round(q));
        label3 = null;
        label4 = null;
        direction1 = p > 0 ? Direction.UP : Direction.DOWN;
        direction2 = q > 0 ? Direction.UP : Direction.DOWN;
    }

    public InitialValue(Injection<?> injection) {
        Objects.requireNonNull(injection);
        double p = injection.getTerminal().getP();
        double q = injection.getTerminal().getQ();
        label1 = String.valueOf(Math.round(p));
        label2 = String.valueOf(Math.round(q));
        label3 = null;
        label4 = null;
        direction1 = p > 0 ? Direction.UP : Direction.DOWN;
        direction2 = q > 0 ? Direction.UP : Direction.DOWN;
    }

    public InitialValue(ThreeWindingsTransformer transformer, ThreeWindingsTransformer.Side side) {
        Objects.requireNonNull(transformer);
        Objects.requireNonNull(side);
        double p = transformer.getTerminal(side).getP();
        double q = transformer.getTerminal(side).getQ();
        label1 = String.valueOf(Math.round(p));
        label2 = String.valueOf(Math.round(q));
        label3 = null;
        label4 = null;
        direction1 = p > 0 ? Direction.UP : Direction.DOWN;
        direction2 = q > 0 ? Direction.UP : Direction.DOWN;
    }

    public Optional<Direction> getArrowDirection1() {
        return Optional.ofNullable(direction1);
    }

    public Optional<Direction> getArrowDirection2() {
        return Optional.ofNullable(direction2);
    }

    public Optional<String> getLabel1() {
        return Optional.ofNullable(label1);
    }

    public Optional<String> getLabel2() {
        return Optional.ofNullable(label2);
    }

    public Optional<String> getLabel3() {
        return Optional.ofNullable(label3);
    }

    public Optional<String> getLabel4() {
        return Optional.ofNullable(label4);
    }

}
