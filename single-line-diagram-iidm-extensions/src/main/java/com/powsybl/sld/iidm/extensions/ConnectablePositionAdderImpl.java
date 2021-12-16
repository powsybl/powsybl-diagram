/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Connectable;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
public class ConnectablePositionAdderImpl<C extends Connectable<C>>
        extends AbstractExtensionAdder<C, ConnectablePosition<C>>
        implements ConnectablePositionAdder<C> {

    private ConnectablePositionImpl.InfoImpl info;
    private ConnectablePositionImpl.InfoImpl info1;
    private ConnectablePositionImpl.InfoImpl info2;
    private ConnectablePositionImpl.InfoImpl info3;

    ConnectablePositionAdderImpl(C connectable) {
        super(connectable);
    }

    private abstract static class AbstractInfoImplAdder<C extends Connectable<C>> implements InfoAdder<C> {
        protected String name;

        protected Integer order;

        protected ConnectablePosition.Direction direction;

        public InfoAdder<C> withName(String name) {
            this.name = name;
            return this;
        }

        public InfoAdder<C> withOrder(int order) {
            this.order = order;
            return this;
        }

        public InfoAdder<C> withDirection(ConnectablePosition.Direction direction) {
            this.direction = direction;
            return this;
        }

    }

    @Override
    public ConnectablePositionImpl<C> createExtension(C extendable) {
        return new ConnectablePositionImpl<>(extendable, info, info1, info2, info3);
    }

    @Override
    public InfoAdder<C> newInfo() {
        return new AbstractInfoImplAdder<C>() {
            @Override
            public ConnectablePositionAdder<C> add() {
                ConnectablePositionAdderImpl.this.info = new ConnectablePositionImpl.InfoImpl(name, order, direction);
                return ConnectablePositionAdderImpl.this;
            }
        };
    }

    @Override
    public InfoAdder<C> newInfo1() {
        return new AbstractInfoImplAdder<C>() {
            @Override
            public ConnectablePositionAdder<C> add() {
                ConnectablePositionAdderImpl.this.info1 = new ConnectablePositionImpl.InfoImpl(name, order, direction);
                return ConnectablePositionAdderImpl.this;
            }
        };
    }

    @Override
    public InfoAdder<C> newInfo2() {
        return new AbstractInfoImplAdder<C>() {
            @Override
            public ConnectablePositionAdder<C> add() {
                ConnectablePositionAdderImpl.this.info2 = new ConnectablePositionImpl.InfoImpl(name, order, direction);
                return ConnectablePositionAdderImpl.this;
            }
        };
    }

    @Override
    public InfoAdder<C> newInfo3() {
        return new AbstractInfoImplAdder<C>() {
            @Override
            public ConnectablePositionAdder<C> add() {
                ConnectablePositionAdderImpl.this.info3 = new ConnectablePositionImpl.InfoImpl(name, order, direction);
                return ConnectablePositionAdderImpl.this;
            }
        };
    }

}
