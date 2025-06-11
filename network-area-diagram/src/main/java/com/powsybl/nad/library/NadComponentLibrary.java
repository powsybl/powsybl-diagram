/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nad.library;

import com.powsybl.diagram.components.ComponentLibrary;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public interface NadComponentLibrary extends ComponentLibrary {
    Transformer getSvgTransformer() throws TransformerConfigurationException;
}
