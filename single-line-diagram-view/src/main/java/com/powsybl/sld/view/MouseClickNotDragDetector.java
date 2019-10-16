/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.view;

import java.util.Objects;
import java.util.function.Consumer;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public final class MouseClickNotDragDetector {

    private Consumer<MouseEvent> onClickedNotDragged;
    private boolean wasDragged;
    private long timePressed;
    private long timeReleased;
    private long pressedDurationTreshold = 200;

    private MouseClickNotDragDetector(Node node) {

        node.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> this.timePressed = System.currentTimeMillis());

        node.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseEvent -> this.wasDragged = true);

        node.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> {
            this.timeReleased = System.currentTimeMillis();
            this.fireEventIfWasClickedNotDragged(mouseEvent);
            this.clear();
        });
    }

    public static MouseClickNotDragDetector clickNotDragDetectingOn(Node node) {
        Objects.requireNonNull(node);
        return new MouseClickNotDragDetector(node);
    }

    public MouseClickNotDragDetector withPressedDurationTreshold(long durationTreshold) {
        this.pressedDurationTreshold = durationTreshold;
        return this;
    }

    public MouseClickNotDragDetector setOnMouseClickedNotDragged(Consumer<MouseEvent> onClickedNotDragged) {
        this.onClickedNotDragged = onClickedNotDragged;
        return this;
    }

    private void clear() {
        this.wasDragged = false;
        this.timePressed = 0;
        this.timeReleased = 0;
    }

    private void fireEventIfWasClickedNotDragged(MouseEvent mouseEvent) {
        if (this.wasDragged) {
            return;
        }
        if (this.mousePressedDuration() > this.pressedDurationTreshold) {
            return;
        }
        this.onClickedNotDragged.accept(mouseEvent);
    }

    private long mousePressedDuration() {
        return this.timeReleased - this.timePressed;
    }
}
