/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.color;

import com.powsybl.commons.config.PlatformConfig;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class BaseVoltageColor {

    private static final String CONFIG_FILE = "base-voltages.yml";

    private BaseVoltagesConfig config;

    public BaseVoltageColor(Path configFile) throws IOException {
        Objects.requireNonNull(configFile);
        Yaml yaml = new Yaml(new Constructor(BaseVoltagesConfig.class));
        if (Files.exists(configFile)) {
            try (InputStream configInputStream = Files.newInputStream(configFile)) {
                config = yaml.load(configInputStream);
            }
        } else {
            InputStream configInputStream = BaseVoltageColor.class.getResourceAsStream("/" + CONFIG_FILE);
            if (configInputStream != null) {
                config = yaml.load(configInputStream);
            } else {
                throw new IOException("No base voltages configuration found");
            }
        }
    }

    public BaseVoltageColor() throws IOException {
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

    public String getBaseVoltageName(double voltage, String profile) {
        Objects.requireNonNull(profile);
        return config.getBaseVoltages()
                     .stream()
                     .filter(baseVoltage -> baseVoltage.getProfile().equals(profile)
                                            && baseVoltage.getMinValue() <= voltage
                                            && baseVoltage.getMaxValue() > voltage)
                     .map(BaseVoltageConfig::getName)
                     .findFirst()
                     .orElse(null);
    }

    public String getColor(String baseVoltageName, String profile) {
        Objects.requireNonNull(baseVoltageName);
        Objects.requireNonNull(profile);
        return config.getBaseVoltages()
                     .stream()
                     .filter(baseVoltage -> baseVoltage.getProfile().equals(profile)
                                            && baseVoltage.getName().equals(baseVoltageName))
                     .map(BaseVoltageConfig::getColor)
                     .findFirst()
                     .orElse(null);
    }

    public String getColor(double voltage, String profile) {
        Objects.requireNonNull(profile);
        return config.getBaseVoltages()
                     .stream()
                     .filter(baseVoltage -> baseVoltage.getProfile().equals(profile)
                                            && baseVoltage.getMinValue() <= voltage
                                            && baseVoltage.getMaxValue() > voltage)
                     .map(BaseVoltageConfig::getColor)
                     .findFirst()
                     .orElse(null);
    }

}
