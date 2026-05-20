package acore.hack.event.impl;

import acore.hack.event.Event;

public class PostPlayerUpdateEvent extends Event {
   private int iterations;

   public int getIterations() {
      return this.iterations;
   }

   public void setIterations(int in) {
      this.iterations = in;
   }
}
