/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sld.svg.styles;

import com.powsybl.sld.svg.DirectionalFeederInfo;
import com.powsybl.sld.svg.FeederInfo;

import java.util.ArrayList;
import java.util.List;

import static com.powsybl.sld.svg.styles.StyleClassConstants.STYLE_PREFIX;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class AnimatedFeederInfoStyleProvider extends EmptyStyleProvider {

    private static final String ARROW_ANIMATION = STYLE_PREFIX + "arrow-animation";
    private static final String ARROW_SPEED = "speed";
    private static final String ARROW_ANIMATION_NO_SPEED = ARROW_ANIMATION + "-no-" + ARROW_SPEED;
    private static final String ARROW_ANIMATION_LOW_SPEED = ARROW_ANIMATION + "-low-" + ARROW_SPEED;
    private static final String ARROW_ANIMATION_AVERAGE_SPEED = ARROW_ANIMATION + "-average-" + ARROW_SPEED;
    private static final String ARROW_ANIMATION_HIGH_SPEED = ARROW_ANIMATION + "-high-" + ARROW_SPEED;

    private final double threshold1;
    private final double threshold2;

    public AnimatedFeederInfoStyleProvider(double threshold1, double threshold2) {
        this.threshold1 = threshold1;
        this.threshold2 = threshold2;
    }

    @Override
    public List<String> getFeederInfoStyles(FeederInfo info) {
        List<String> styles = new ArrayList<>(super.getFeederInfoStyles(info));
        if (info instanceof DirectionalFeederInfo) {
            DirectionalFeederInfo feederInfo = (DirectionalFeederInfo) info;
            feederInfo.getRightLabel().ifPresent(label -> {
                double value = Math.abs(feederInfo.getValue());
                if (!Double.isNaN(value) && value > 0) {
                    if (value > threshold2) {
                        styles.add(ARROW_ANIMATION_HIGH_SPEED);
                    } else if (value > threshold1) {
                        styles.add(ARROW_ANIMATION_AVERAGE_SPEED);
                    } else {
                        styles.add(ARROW_ANIMATION_LOW_SPEED);
                    }
                } else {
                    styles.add(ARROW_ANIMATION_NO_SPEED);
                }
            });
        } else {
            styles.add(ARROW_ANIMATION_NO_SPEED);
        }
        return styles;
    }

    @Override
    public List<String> getCssFilenames() {
        return List.of("animations.css");
    }
}
