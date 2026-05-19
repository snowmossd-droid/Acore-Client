package acore.hack.event.impl;

import net.minecraft.entity.Entity;
import net.minecraft.client.util.math.MatrixStack;
import acore.hack.event.Event;

public class EventEntityRender extends Event {
    private final Entity entity;
    private final MatrixStack matrices;
    private final float tickDelta;

    public EventEntityRender(Entity entity, MatrixStack matrices, float tickDelta) {
        this.entity = entity;
        this.matrices = matrices;
        this.tickDelta = tickDelta;
    }

    public Entity getEntity() {
        return entity;
    }

    public MatrixStack getMatrices() {
        return matrices;
    }

    public float getTickDelta() {
        return tickDelta;
    }
}
