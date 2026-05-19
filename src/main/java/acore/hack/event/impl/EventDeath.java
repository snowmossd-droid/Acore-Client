package acore.hack.event.impl;

import net.minecraft.entity.player.PlayerEntity;
import acore.hack.event.Event;

public class EventDeath extends Event {
   private final PlayerEntity player;

   public EventDeath(PlayerEntity player) {
      this.player = player;
   }

   public PlayerEntity getPlayer() {
      return this.player;
   }
}
