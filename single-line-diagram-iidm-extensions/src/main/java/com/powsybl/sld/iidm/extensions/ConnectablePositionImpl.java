/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Connectable;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ConnectablePositionImpl<C extends Connectable<C>> extends AbstractExtension<C>
        implements ConnectablePosition<C> {

    public static class InfoImpl implements Info {

        private String name;

        private Integer order;

        private Direction direction;

        public InfoImpl(String name) {
            this(name, null, null);
        }

        public InfoImpl(String name, int order) {
            this(name, order, null);
        }

        public InfoImpl(String name, Direction direction) {
            this(name, null, direction);
        }

        public InfoImpl(String name, Integer order, Direction direction) {
            this.name = Objects.requireNonNull(name);
            this.order = order;
            this.direction = Objects.requireNonNullElse(direction, Direction.UNDEFINED);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Info setName(String name) {
            this.name = Objects.requireNonNull(name);
            return this;
        }

        @Override
        public Optional<Integer> getOrder() {
            return Optional.ofNullable(order);
        }

        @Override
        public Info setOrder(int order) {
            this.order = order;
            return this;
        }

        @Override
        public Info removeOrder() {
            this.order = null;
            return this;
        }

        @Override
        public Direction getDirection() {
            return direction;
        }

        @Override
        public Info setDirection(Direction direction) {
            this.direction = Objects.requireNonNull(direction);
            return this;
        }
    }

    private final InfoImpl info;
    private final InfoImpl info1;
    private final InfoImpl info2;
    private final InfoImpl info3;

    public ConnectablePositionImpl(C connectable, InfoImpl info, InfoImpl info1, InfoImpl info2, InfoImpl info3) {
        super(connectable);
        ConnectablePosition.check(info, info1, info2, info3);
        this.info = info;
        this.info1 = info1;
        this.info2 = info2;
        this.info3 = info3;
    }

    @Override
    public InfoImpl getInfo() {
        return info;
    }

    @Override
    public InfoImpl getInfo1() {
        return info1;
    }

    @Override
    public InfoImpl getInfo2() {
        return info2;
    }

    @Override
    public InfoImpl getInfo3() {
        return info3;
    }
}
