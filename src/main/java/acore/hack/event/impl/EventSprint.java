package acore.hack.event.impl;

import acore.hack.event.Event;

public class EventSprint extends Event {
   private boolean sprintState;

   public EventSprint(boolean sprintState) {
      this.sprintState = sprintState;
   }

   public boolean getSprintState() {
      return this.sprintState;
   }

   public void setSprintState(boolean sprintState) {
      this.sprintState = sprintState;
   }
}
