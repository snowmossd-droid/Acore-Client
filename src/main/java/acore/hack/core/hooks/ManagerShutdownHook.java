package acore.hack.core.hooks;

import acore.hack.core.Managers;

public class ManagerShutdownHook extends Thread {
   @Override
   public void run() {
      Managers.FRIEND.saveFriends();
      Managers.CONFIG.save(Managers.CONFIG.getCurrentConfig());
      Managers.MACRO.saveMacro();
   }
}
