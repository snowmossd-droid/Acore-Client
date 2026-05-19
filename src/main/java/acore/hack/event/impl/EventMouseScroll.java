package acore.hack.event.impl;

import acore.hack.event.Event;

public class EventMouseScroll extends Event {
    private final double horizontal;
    private final double vertical;

    public EventMouseScroll(double horizontal, double vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    public double getHorizontal() {
        return horizontal;
    }

    public double getVertical() {
        return vertical;
    }
}
