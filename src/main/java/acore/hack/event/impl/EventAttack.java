package acore.hack.event.impl;

import net.minecraft.entity.Entity;
import acore.hack.event.Event;

public class EventAttack extends Event {
   private Entity entity;
   boolean pre;

   public EventAttack(Entity entity, boolean pre) {
      this.entity = entity;
      this.pre = pre;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public boolean isPre() {
      return this.pre;
   }
}
