package acore.hack.features.modules.misc;

import acore.hack.features.modules.Module;
import acore.hack.setting.Setting;

public final class Notifications extends Module {
   public final Setting<Notifications.Mode> mode = new Setting<>("Mode", Notifications.Mode.Default);

   public Notifications() {
      super("Notifications", "Client notifications.", Module.Category.MISC);
   }

   public enum Mode {
      Default,
      CrossHair;
   }
}