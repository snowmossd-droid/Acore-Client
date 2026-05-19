package acore.hack.event.impl;

import net.minecraft.entity.Entity;
import acore.hack.event.Event;

public class EventEntitySpawn extends Event {
   private final Entity entity;

   public EventEntitySpawn(Entity entity) {
      this.entity = entity;
   }

   public Entity getEntity() {
      return this.entity;
   }
}
