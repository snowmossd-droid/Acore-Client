package acore.hack.event.impl;

import net.minecraft.entity.LivingEntity;
import acore.hack.event.Event;

public class EventLivingUpdate extends Event {
    private final LivingEntity entity;

    public EventLivingUpdate(LivingEntity entity) {
        this.entity = entity;
    }

    public LivingEntity getEntity() {
        return entity;
    }
}
