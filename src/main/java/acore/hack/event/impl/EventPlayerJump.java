package acore.hack.event.impl;

import acore.hack.event.Event;

public class EventPlayerJump extends Event {
   private boolean pre;

   public EventPlayerJump(boolean pre) {
      this.pre = pre;
   }

   public boolean isPre() {
      return this.pre;
   }
}
