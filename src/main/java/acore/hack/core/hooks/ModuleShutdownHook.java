package acore.hack.core.hooks;

import acore.hack.core.manager.client.ModuleManager;
import acore.hack.features.modules.misc.UnHook;

public class ModuleShutdownHook extends Thread {
   @Override
   public void run() {
      if (UnHook.isActive()) {
         ModuleManager.unHook.disable();
      }
   }
}
