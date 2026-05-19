package acore.hack.event.impl;

import net.minecraft.util.Hand;
import acore.hack.event.Event;

public class EventSwingHand extends Event {
    private final Hand hand;

    public EventSwingHand(Hand hand) {
        this.hand = hand;
    }

    public Hand getHand() {
        return hand;
    }
}
