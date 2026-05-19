package acore.hack.event.impl;

import net.minecraft.client.util.math.MatrixStack;
import acore.hack.event.Event;

public class EventWorldRender extends Event {
    private final MatrixStack matrices;
    private final float tickDelta;

    public EventWorldRender(MatrixStack matrices, float tickDelta) {
        this.matrices = matrices;
        this.tickDelta = tickDelta;
    }

    public MatrixStack getMatrices() {
        return matrices;
    }

    public float getTickDelta() {
        return tickDelta;
    }
}
