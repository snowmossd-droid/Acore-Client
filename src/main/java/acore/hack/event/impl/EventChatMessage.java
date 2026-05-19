package acore.hack.event.impl;

import acore.hack.event.Event;

public class EventChatMessage extends Event {
    private String message;

    public EventChatMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
