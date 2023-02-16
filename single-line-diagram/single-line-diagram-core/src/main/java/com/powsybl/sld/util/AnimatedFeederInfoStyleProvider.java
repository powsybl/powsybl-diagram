/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.svg.DirectionalFeederInfo;
import com.powsybl.sld.svg.FeederInfo;

import java.util.*;

import static com.powsybl.sld.svg.DiagramStyles.STYLE_PREFIX;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class AnimatedFeederInfoStyleProvider extends TopologicalStyleProvider {

    private static final String ARROW_ANIMATION = STYLE_PREFIX + "arrow-animation";

    private static final String ARROW_SPEED = "speed";

    private static final String ARROW_ANIMATION_LOW_SPEED = ARROW_ANIMATION + "-low-" + ARROW_SPEED;

    private static final String ARROW_ANIMATION_NOMINAL_SPEED = ARROW_ANIMATION + "-nominal-" + ARROW_SPEED;

    private static final String ARROW_ANIMATION_HIGH_SPEED = ARROW_ANIMATION + "-high-" + ARROW_SPEED;

    public AnimatedFeederInfoStyleProvider(Network network) {
        super(network);
    }

    @Override
    public List<String> getFeederInfoStyles(FeederInfo info) {
        List<String> styles = new ArrayList<>(super.getFeederInfoStyles(info));
        if (info instanceof DirectionalFeederInfo) {
            DirectionalFeederInfo feederInfo = (DirectionalFeederInfo) info;
            feederInfo.getRightLabel().ifPresent(label -> {
                double power = feederInfo.getValue();
                if (!Double.isNaN(power) && Math.abs(power) > 0) {
                    if (Math.abs(power) > 1000) {
                        styles.add(ARROW_ANIMATION_HIGH_SPEED);
                    } else if (Math.abs(power) > 500.0) {
                        styles.add(ARROW_ANIMATION_NOMINAL_SPEED);
                    } else {
                        styles.add(ARROW_ANIMATION_LOW_SPEED);
                    }
                }
            });
        }
        return styles;
    }

    @Override
    public List<String> getCssFilenames() {
        List<String> styles = new ArrayList<>(super.getCssFilenames());
        styles.add("animations.css");
        return styles;
    }
}
