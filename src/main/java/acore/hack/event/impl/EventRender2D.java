package acore.hack.event.impl;

import net.minecraft.client.gui.DrawContext;
import acore.hack.event.Event;

public class EventRender2D extends Event {
    private final DrawContext context;
    private final float tickDelta;

    public EventRender2D(DrawContext context, float tickDelta) {
        this.context = context;
        this.tickDelta = tickDelta;
    }

    public DrawContext getContext() {
        return context;
    }

    public float getTickDelta() {
        return tickDelta;
    }
}
