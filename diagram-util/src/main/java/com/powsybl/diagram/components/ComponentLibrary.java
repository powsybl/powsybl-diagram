/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.components;

import org.w3c.dom.Element;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public interface ComponentLibrary {

    String getName();

    Map<String, List<Element>> getSvgElements(String type);

    ComponentSize getSize(String type);

    Map<String, ComponentSize> getComponentsSize();

    List<String> getCssFilenames();

    List<URL> getCssUrls();

    Optional<String> getComponentStyleClass(String componentType);

    Optional<String> getSubComponentStyleClass(String type, String subComponent);
}
