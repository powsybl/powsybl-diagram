/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Connectable;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ConnectablePosition<C extends Connectable<C>> extends AbstractExtension<C> {

    static final String NAME = "position";

    public enum Direction {
        TOP,
        BOTTOM,
        UNDEFINED
    }

    public static class Feeder {

        private String name;

        private int order;

        private Direction direction;

        public Feeder(String name, int order, Direction direction) {
            this.name = Objects.requireNonNull(name);
            this.order = order;
            this.direction = Objects.requireNonNull(direction);
        }

        public String getName() {
            return name;
        }

        public Feeder setName(String name) {
            this.name = Objects.requireNonNull(name);
            return this;
        }

        public int getOrder() {
            return order;
        }

        public Feeder setOrder(int order) {
            this.order = order;
            return this;
        }

        public Direction getDirection() {
            return direction;
        }

        public Feeder setDirection(Direction direction) {
            this.direction = Objects.requireNonNull(direction);
            return this;
        }
    }

    private Feeder feeder;
    private Feeder feeder1;
    private Feeder feeder2;
    private Feeder feeder3;

    private static void check(Feeder feeder, Feeder feeder1, Feeder feeder2, Feeder feeder3) {
        if (feeder == null && feeder1 == null && feeder2 == null && feeder3 == null) {
            throw new IllegalArgumentException("invalid feeder");
        }
        if (feeder != null && (feeder1 != null || feeder2 != null || feeder3 != null)) {
            throw new IllegalArgumentException("feeder and feeder 1|2|3 are exclusives");
        }
        boolean error = false;
        if (feeder1 != null) {
            if (feeder2 == null && feeder3 != null) {
                error = true;
            }
        } else {
            if (feeder2 != null || feeder3 != null) {
                error = true;
            }
        }
        if (error) {
            throw new IllegalArgumentException("feeder 1|2|3 have to be set in the right order");
        }
    }

    public ConnectablePosition(C connectable, Feeder feeder, Feeder feeder1, Feeder feeder2, Feeder feeder3) {
        super(connectable);
        check(feeder, feeder1, feeder2, feeder3);
        this.feeder = feeder;
        this.feeder1 = feeder1;
        this.feeder2 = feeder2;
        this.feeder3 = feeder3;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public Feeder getFeeder() {
        return feeder;
    }

    public Feeder getFeeder1() {
        return feeder1;
    }

    public Feeder getFeeder2() {
        return feeder2;
    }

    public Feeder getFeeder3() {
        return feeder3;
    }

    public ConnectablePosition setFeeders(Feeder feeder, Feeder feeder1, Feeder feeder2, Feeder feeder3) {
        check(feeder, feeder1, feeder2, feeder3);
        this.feeder = feeder;
        this.feeder1 = feeder1;
        this.feeder2 = feeder2;
        this.feeder3 = feeder3;
        return this;
    }
}
