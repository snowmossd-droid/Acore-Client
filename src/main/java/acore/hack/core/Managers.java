package acore.hack.core;

import acore.hack.AcoreHack;
import acore.hack.core.manager.AsyncManager;
import acore.hack.core.manager.CommandManager;
import acore.hack.core.manager.ConfigManager;
import acore.hack.core.manager.MacroManager;
import acore.hack.core.manager.ModuleManager;
import acore.hack.core.manager.NotificationManager;
import acore.hack.core.manager.ServerManager;
import acore.hack.core.manager.SoundManager;
import acore.hack.core.manager.player.CombatManager;
import acore.hack.core.manager.player.FriendManager;
import acore.hack.core.manager.player.PlayerManager;

public class Managers {
   public static final CombatManager COMBAT = new CombatManager();
   public static final FriendManager FRIEND = new FriendManager();
   public static final PlayerManager PLAYER = new PlayerManager();
   public static final AsyncManager ASYNC = new AsyncManager();
   public static final ModuleManager MODULE = new ModuleManager();
   public static final ConfigManager CONFIG = ConfigManager.getInstance();
   public static final MacroManager MACRO = new MacroManager();
   public static final NotificationManager NOTIFICATION = new NotificationManager();
   public static final ServerManager SERVER = new ServerManager();
   public static final SoundManager SOUND = new SoundManager();
   public static final CommandManager COMMAND = new CommandManager();

   public static void init() {
      CONFIG.load(CONFIG.getCurrentConfig());
      MODULE.onLoad("none");
      FRIEND.loadFriends();
      MACRO.onLoad();
      SOUND.registerSounds();
   }

   public static void subscribe() {
      AcoreHack.EVENT_BUS.subscribe(NOTIFICATION);
      AcoreHack.EVENT_BUS.subscribe(SERVER);
      AcoreHack.EVENT_BUS.subscribe(PLAYER);
      AcoreHack.EVENT_BUS.subscribe(COMBAT);
      AcoreHack.EVENT_BUS.subscribe(ASYNC);
   }
   }
