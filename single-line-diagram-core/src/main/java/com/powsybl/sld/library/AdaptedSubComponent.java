/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class AdaptedSubComponent {

    private String name;

    private String fileName;

    private String styleClass;

    @JsonCreator
    public AdaptedSubComponent(@JsonProperty("name") String name,
                               @JsonProperty("fileName") String fileName,
                               @JsonProperty("styleClass") String styleClass) {
        this.name = Objects.requireNonNull(name);
        this.fileName = Objects.requireNonNull(fileName);
        this.styleClass = Objects.requireNonNull(styleClass);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }
}
