/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.view;

import javafx.scene.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Objects;

/**
 * @author Franck Lecuyer <fracck.lecuyer at rte-france.com>
 */
public final class SubstationDiagramView extends AbstractContainerDiagramView {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubstationDiagramView.class);

    private SubstationDiagramView(Group svgImage) {
        super(svgImage);
    }

    public static SubstationDiagramView load(InputStream svgInputStream,
                                             InputStream metadataInputStream,
                                             SwitchPositionChangeListener listener,
                                             DisplayVoltageLevel displayVL) {
        Objects.requireNonNull(svgInputStream);
        Objects.requireNonNull(metadataInputStream);

        Group svgImage = loadSvgAndMetadata(svgInputStream, metadataInputStream, listener, displayVL);

        return new SubstationDiagramView(svgImage);
    }
}
