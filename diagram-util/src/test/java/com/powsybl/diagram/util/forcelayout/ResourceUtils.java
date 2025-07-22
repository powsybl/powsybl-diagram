/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.diagram.util.forcelayout;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
public final class ResourceUtils {

    private ResourceUtils() {
        throw new AssertionError("Instantiating utility class ResourceUtils");
    }

    public static String toString(String resourceName) {
        try {
            InputStream in = Objects.requireNonNull(ResourceUtils.class.getClassLoader().getResourceAsStream(resourceName));
            return new String(ByteStreams.toByteArray(in), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
