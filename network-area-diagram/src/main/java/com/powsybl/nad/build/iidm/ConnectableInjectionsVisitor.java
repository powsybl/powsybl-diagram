/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.build.iidm;

import com.powsybl.iidm.network.*;
import com.powsybl.nad.model.Injection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ConnectableInjectionsVisitor implements TopologyVisitor {
    private final IdProvider idProvider;
    private final List<Injection> connectableInjections = new ArrayList<>();

    public ConnectableInjectionsVisitor(IdProvider idProvider) {
        this.idProvider = idProvider;
    }

    public List<Injection> getConnectableInjections() {
        return connectableInjections;
    }

    private Injection createInjectionFromIidm(com.powsybl.iidm.network.Injection<?> inj) {
        String diagramId = idProvider.createId(inj);
        Injection.Type injectionType = getInjectionType(inj);
        return new Injection(diagramId, inj.getId(), inj.getNameOrId(), injectionType);
    }

    private static Injection.Type getInjectionType(com.powsybl.iidm.network.Injection<?> inj) {
        return switch (inj.getType()) {
            case GENERATOR -> Injection.Type.GENERATOR;
            case BATTERY -> Injection.Type.BATTERY;
            case LOAD -> Injection.Type.LOAD;
            case SHUNT_COMPENSATOR -> Injection.Type.SHUNT_COMPENSATOR;
            case DANGLING_LINE -> Injection.Type.DANGLING_LINE;
            case STATIC_VAR_COMPENSATOR -> Injection.Type.STATIC_VAR_COMPENSATOR;
            case HVDC_CONVERTER_STATION -> Injection.Type.HVDC_CONVERTER_STATION;
            case GROUND -> Injection.Type.GROUND;
            default -> throw new AssertionError("Unexpected injection type: " + inj.getType());
        };
    }

    @Override
    public void visitGenerator(Generator generator) {
        addInjection(generator);
    }

    @Override
    public void visitBattery(Battery battery) {
        addInjection(battery);
    }

    @Override
    public void visitLoad(Load load) {
        addInjection(load);
    }

    @Override
    public void visitShuntCompensator(ShuntCompensator shuntCompensator) {
        addInjection(shuntCompensator);
    }

    @Override
    public void visitStaticVarCompensator(StaticVarCompensator staticVarCompensator) {
        addInjection(staticVarCompensator);
    }

    private void addInjection(com.powsybl.iidm.network.Injection<?> inj) {
        connectableInjections.add(createInjectionFromIidm(inj));
    }

    @Override
    public void visitBusbarSection(BusbarSection busbarSection) {
        // Not displayed: nothing to do
    }

    @Override
    public void visitLine(Line line, TwoSides twoSides) {
        // Not an injection: nothing to do
    }

    @Override
    public void visitTwoWindingsTransformer(TwoWindingsTransformer twoWindingsTransformer, TwoSides twoSides) {
        // Not an injection: nothing to do
    }

    @Override
    public void visitThreeWindingsTransformer(ThreeWindingsTransformer threeWindingsTransformer, ThreeSides threeSides) {
        // Not an injection: nothing to do
    }

    @Override
    public void visitHvdcConverterStation(HvdcConverterStation<?> converterStation) {
        // Seen as a branch in NAD: nothing to do
    }

    @Override
    public void visitDanglingLine(DanglingLine danglingLine) {
        // Displayed as a voltage level node: nothing to do
    }

    @Override
    public void visitGround(Ground ground) {
        // Not displayed: nothing to do
    }
}
