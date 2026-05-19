package acore.hack.event.impl;

import acore.hack.event.Event;
import acore.hack.setting.Setting;

public class EventSetting extends Event {
   final Setting<?> setting;

   public EventSetting(Setting<?> setting) {
      this.setting = setting;
   }

   public Setting<?> getSetting() {
      return this.setting;
   }
}
