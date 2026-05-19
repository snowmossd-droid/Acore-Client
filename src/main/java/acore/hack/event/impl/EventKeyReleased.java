package acore.hack.event.impl;

import acore.hack.event.Event;

public class EventKeyReleased extends Event {
    private final int key;
    private final int scanCode;

    public EventKeyReleased(int key, int scanCode) {
        this.key = key;
        this.scanCode = scanCode;
    }

    public int getKey() {
        return key;
    }

    public int getScanCode() {
        return scanCode;
    }
}
