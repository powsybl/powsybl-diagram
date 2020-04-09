/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.color;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class BaseVoltageColor {

    private static final String CONFIG_FILE = "base-voltages.yml";

    private BaseVoltagesConfig config;

    public BaseVoltageColor(Path configFile) {
        Objects.requireNonNull(configFile);
        Yaml yaml = new Yaml(new Constructor(BaseVoltagesConfig.class));
        if (Files.exists(configFile)) {
            try (InputStream configInputStream = Files.newInputStream(configFile)) {
                config = yaml.load(configInputStream);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            InputStream configInputStream = BaseVoltageColor.class.getResourceAsStream("/" + CONFIG_FILE);
            if (configInputStream != null) {
                config = yaml.load(configInputStream);
            } else {
                throw new PowsyblException("No base voltages configuration found");
            }
        }
    }

    public BaseVoltageColor() {
        this(PlatformConfig.defaultConfig().getConfigDir().resolve(CONFIG_FILE));
    }

    public List<String> getProfiles() {
        return config.getBaseVoltages()
                     .stream()
                     .map(BaseVoltageConfig::getProfile)
                     .distinct()
                     .collect(Collectors.toList());
    }

    public String getDefaultProfile() {
        return config.getDefaultProfile();
    }

    public List<String> getBaseVoltageNames(String profile) {
        Objects.requireNonNull(profile);
        return config.getBaseVoltages()
                     .stream()
                     .filter(baseVoltage -> baseVoltage.getProfile().equals(profile))
                     .map(BaseVoltageConfig::getName)
                     .collect(Collectors.toList());
    }

    public Optional<String> getBaseVoltageName(double baseVoltage, String profile) {
        Objects.requireNonNull(profile);
        return config.getBaseVoltages()
                     .stream()
                     .filter(v -> v.getProfile().equals(profile)
                                            && v.getMinValue() <= baseVoltage
                                            && v.getMaxValue() > baseVoltage)
                     .map(BaseVoltageConfig::getName)
                     .findFirst();
    }

    public Optional<String> getColor(String baseVoltageName, String profile) {
        Objects.requireNonNull(baseVoltageName);
        Objects.requireNonNull(profile);
        return config.getBaseVoltages()
                     .stream()
                     .filter(v -> v.getProfile().equals(profile)
                                            && v.getName().equals(baseVoltageName))
                     .map(BaseVoltageConfig::getColor)
                     .findFirst();
    }

    public Optional<String> getColor(double baseVoltage, String profile) {
        Objects.requireNonNull(profile);
        return config.getBaseVoltages()
                     .stream()
                     .filter(v -> v.getProfile().equals(profile)
                                            && v.getMinValue() <= baseVoltage
                                            && v.getMaxValue() > baseVoltage)
                     .map(BaseVoltageConfig::getColor)
                     .findFirst();
    }

}
