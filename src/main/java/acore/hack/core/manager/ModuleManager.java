package acore.hack.core.manager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;
import acore.hack.AcoreHack;
import acore.hack.core.manager.ConfigManager;
import acore.hack.core.manager.IManager;
import acore.hack.features.hud.HudElement;
import acore.hack.features.modules.Module;
import acore.hack.features.modules.combat.Aura;
import acore.hack.features.modules.combat.AntiBot;
import acore.hack.features.modules.combat.AutoBuff;
import acore.hack.features.modules.combat.AutoCart;
import acore.hack.features.modules.combat.AutoCrystal;
import acore.hack.features.modules.combat.AutoTotem;
import acore.hack.features.modules.combat.AutoTrap;
import acore.hack.features.modules.combat.AutoWeb;
import acore.hack.features.modules.combat.BowSpam;
import acore.hack.features.modules.combat.Criticals;
import acore.hack.features.modules.combat.ElytraTarget;
import acore.hack.features.modules.combat.HitBox;
import acore.hack.features.modules.combat.TargetStrafe;
import acore.hack.features.modules.combat.TriggerBot;
import acore.hack.features.modules.combat.WallsBypass;
import acore.hack.features.modules.combat.EMaceHelper;
import acore.hack.features.modules.combat.CrystalOptimizer;
import acore.hack.features.modules.combat.MaceSwap;
import acore.hack.features.modules.misc.ClientSettings;
import acore.hack.features.modules.misc.ClientSound;
import acore.hack.features.modules.misc.ClientSpoof;
import acore.hack.features.modules.misc.ClickGui;
import acore.hack.features.modules.misc.FakePlayer;
import acore.hack.features.modules.misc.FixHP;
import acore.hack.features.modules.misc.NameProtect;
import acore.hack.features.modules.misc.Notifications;
import acore.hack.features.modules.misc.RPC;
import acore.hack.features.modules.misc.TotemPopCounter;
import acore.hack.features.modules.misc.UnHook;
import acore.hack.features.modules.movement.AntiWeb;
import acore.hack.features.modules.movement.AutoSprint;
import acore.hack.features.modules.movement.Blink;
import acore.hack.features.modules.movement.ElytraPlus;
import acore.hack.features.modules.movement.Flight;
import acore.hack.features.modules.movement.GuiMove;
import acore.hack.features.modules.movement.MoveFix;
import acore.hack.features.modules.movement.NoFall;
import acore.hack.features.modules.movement.NoSlow;
import acore.hack.features.modules.movement.Phase;
import acore.hack.features.modules.movement.Speed;
import acore.hack.features.modules.movement.Velocity;
import acore.hack.features.modules.player.AutoRespawn;
import acore.hack.features.modules.player.AutoTool;
import acore.hack.features.modules.player.FreeCam;
import acore.hack.features.modules.player.ItemScroller;
import acore.hack.features.modules.player.PearlChaser;
import acore.hack.features.modules.player.ElytraSwap;
import acore.hack.features.modules.render.Animations;
import acore.hack.features.modules.render.BlockESP;
import acore.hack.features.modules.render.ESP;
import acore.hack.features.modules.render.Fullbright;
import acore.hack.features.modules.render.Hat;
import acore.hack.features.modules.render.HitParticles;
import acore.hack.features.modules.render.HudEditor;
import acore.hack.features.modules.render.ItemESP;
import acore.hack.features.modules.render.JumpCircle;
import acore.hack.features.modules.render.NoRender;
import acore.hack.features.modules.render.Particles;
import acore.hack.features.modules.render.PopChams;
import acore.hack.features.modules.render.StorageEsp;
import acore.hack.features.modules.render.TargetESP;
import acore.hack.features.modules.render.ViewModel;
import acore.hack.features.gui.clickui.ClickGUI;

public class ModuleManager implements IManager {
   public ArrayList<Module> modules = new ArrayList<>();
   public List<Integer> activeMouseKeys = new ArrayList<>();
   public static TotemPopCounter totemPopCounter = new TotemPopCounter();
   public static ClientSettings clientSettings = new ClientSettings();
   public static Notifications notifications = new Notifications();
   public static ItemScroller itemScroller = new ItemScroller();
   public static HitParticles hitParticles = new HitParticles();
   public static NameProtect nameProtect = new NameProtect();
   public static AutoRespawn autoRespawn = new AutoRespawn();
   public static ClientSpoof clientSpoof = new ClientSpoof();
   public static TriggerBot triggerBot = new TriggerBot();
   public static StorageEsp storageEsp = new StorageEsp();
   public static JumpCircle jumpCircle = new JumpCircle();
   public static Fullbright fullbright = new Fullbright();
   public static FakePlayer fakePlayer = new FakePlayer();
   public static AutoSprint autoSprint = new AutoSprint();
   public static Animations animations = new Animations();
   public static ElytraPlus elytraPlus = new ElytraPlus();
   public static ElytraTarget elytraTarget = new ElytraTarget();
   public static Particles particles = new Particles();
   public static TargetESP targetESP = new TargetESP();
   public static HudEditor hudEditor = new HudEditor();
   public static Criticals criticals = new Criticals();
   public static AutoTotem autoTotem = new AutoTotem();
   public static Velocity velocity = new Velocity();
   public static PopChams popChams = new PopChams();
   public static NoRender noRender = new NoRender();
   public static ClickGui clickGui = new ClickGui();
   public static EMaceHelper eMaceHelper = new EMaceHelper();
   public static CrystalOptimizer crystalOptimizer = new CrystalOptimizer();
   public static AutoTrap autoTrap = new AutoTrap();
   public static BlockESP blockESP = new BlockESP();
   public static MaceSwap maceSwap = new MaceSwap();
   public static ElytraSwap elytraSwap = new ElytraSwap();
   public static WallsBypass wallsBypass = new WallsBypass();
   public static AutoTool autoTool = new AutoTool();
   public static AutoBuff autoBuff = new AutoBuff();
   public static MoveFix moveFix = new MoveFix();
   public static FreeCam freeCam = new FreeCam();
   public static BowSpam bowSpam = new BowSpam();
   public static ItemESP itemESP = new ItemESP();
   public static GuiMove guiMove = new GuiMove();
   public static AutoWeb autoWeb = new AutoWeb();
   public static AntiWeb antiWeb = new AntiWeb();
   public static AntiBot antiBot = new AntiBot();
   public static NoSlow noSlow = new NoSlow();
   public static NoFall noFall = new NoFall();
   public static HitBox hitBox = new HitBox();
   public static Flight flight = new Flight();
   public static FixHP fixHP = new FixHP();
   public static Speed speed = new Speed();
   public static Blink blink = new Blink();
   public static Phase phase = new Phase();
   public static AutoCart autoCart = new AutoCart();
   public static AutoCrystal autoCrystal = new AutoCrystal();
   public static Aura aura = new Aura();
   public static ESP esp = new ESP();
   public static Hat hat = new Hat();
   public static RPC rpc = new RPC();
   public static ViewModel viewModel = new ViewModel();
   public static PearlChaser pearlChaser = new PearlChaser();
   public static ClientSound ClientSound = new ClientSound();
   public static TargetStrafe targetStrafe = new TargetStrafe();
   public static UnHook unHook = new UnHook();

   public ModuleManager() {
      for (Field field : this.getClass().getDeclaredFields()) {
         if (Module.class.isAssignableFrom(field.getType())) {
            field.setAccessible(true);
            try {
               this.modules.add((Module)field.get(this));
            } catch (IllegalAccessException e) {
               AcoreHack.LOGGER.error("Error initializing modules", e);
            }
         }
      }
      hudEditor.enableSilently();
   }

   public Module get(String name) {
      for (Module module : this.modules) {
         if (module.getName().equalsIgnoreCase(name)) {
            return module;
         }
      }
      return null;
   }

   public ArrayList<Module> getEnabledModules() {
      ArrayList<Module> enabledModules = new ArrayList<>();
      for (Module module : this.modules) {
         if (module.isEnabled()) {
            enabledModules.add(module);
         }
      }
      return enabledModules;
   }

   public ArrayList<Module> getModulesByCategory(Module.Category category) {
      ArrayList<Module> modulesCategory = new ArrayList<>();
      this.modules.forEach(module -> {
         if (module.getCategory() == category) {
            modulesCategory.add(module);
         }
      });
      return modulesCategory;
   }

   public List<Module.Category> getCategories() {
      return new ArrayList<>(Module.Category.values());
   }

   public void onLoad(String category) {
      try {
         AcoreHack.EVENT_BUS.unsubscribe(unHook);
      } catch (Exception var3) {
      }
      unHook.setEnabled(false);
      this.modules.sort(Comparator.comparing(Module::getName));
      this.modules.forEach(m -> {
         boolean shouldEnable = m.isEnabled() && (m.getCategory().getName().equalsIgnoreCase(category) || category.equals("none"));
         if (shouldEnable) {
            m.enableSilently();
         }
      });
      if (ConfigManager.firstLaunch) {
         notifications.enable();
         rpc.enable();
         ClientSound.enable();
      }
   }

   public static void saveModules() {
      if (ConfigManager.getInstance() != null) {
         if (ConfigManager.getInstance().getCurrentConfig() != null) {
            ConfigManager.getInstance().save(ConfigManager.getInstance().getCurrentConfig());
         } else {
            ConfigManager.getInstance().save("default");
         }
      }
   }

   public void onUpdate() {
      if (!Module.fullNullCheck()) {
         this.modules.stream().filter(Module::isEnabled).forEach(Module::onUpdate);
      }
   }

   public void onRender2D(DrawContext context) {
      if (!mc.getDebugHud().shouldShowDebugHud() && !mc.options.hudHidden) {
         HudElement.anyHovered = false;
         this.modules.stream().filter(Module::isEnabled).forEach(module -> module.onRender2D(context));
         if (!HudElement.anyHovered && !ClickGUI.anyHovered && GLFW.glfwGetPlatform() != 393219) {
            GLFW.glfwSetCursor(mc.getWindow().getHandle(), GLFW.glfwCreateStandardCursor(221185));
         }
      }
   }

   public void onRender3D(MatrixStack stack) {
      this.modules.stream().filter(Module::isEnabled).forEach(module -> module.onRender3D(stack));
   }

   public void onLogout() {
      this.modules.forEach(Module::onLogout);
   }

   public void onLogin() {
      this.modules.forEach(Module::onLogin);
   }

   public void onUnload(String category) {
      this.modules.forEach(module -> {
         if (module.isEnabled() && (module.getCategory().getName().equalsIgnoreCase(category) || category.equals("none"))) {
            AcoreHack.EVENT_BUS.unsubscribe(module);
            module.setEnabled(false);
         }
      });
      this.modules.forEach(Module::onUnload);
   }

   public void onKeyPressed(int eventKey) {
      if (eventKey != -1 && eventKey != 0 && !(mc.currentScreen instanceof ClickGUI)) {
         this.modules.forEach(module -> {
            if (module.getBind().getKey() == eventKey) {
               module.toggle();
            }
         });
      }
   }

   public void onKeyReleased(int eventKey) {
      if (eventKey != -1 && eventKey != 0 && !(mc.currentScreen instanceof ClickGUI)) {
         this.modules.forEach(module -> {
            if (module.getBind().getKey() == eventKey && module.getBind().isHold()) {
               module.disable();
            }
         });
      }
   }

   public void onMouseKeyPressed(int eventKey) {
      if (eventKey != -1 && !(mc.currentScreen instanceof ClickGUI)) {
         this.modules.forEach(module -> {
            if (Objects.equals(module.getBind().getBind(), "M" + eventKey)) {
               module.toggle();
            }
         });
      }
   }

   public void onMouseKeyReleased(int eventKey) {
      if (eventKey != -1 && !(mc.currentScreen instanceof ClickGUI)) {
         this.activeMouseKeys.add(eventKey);
         this.modules.forEach(module -> {
            if (Objects.equals(module.getBind().getBind(), "M" + eventKey) && module.getBind().isHold()) {
               module.disable();
            }
         });
      }
   }

   public ArrayList<Module> getModulesSearch(String string) {
      ArrayList<Module> modulesCategory = new ArrayList<>();
      this.modules.forEach(module -> {
         if (module.getName().toLowerCase().contains(string.toLowerCase())) {
            modulesCategory.add(module);
         }
      });
      return modulesCategory;
   }

   public void registerModule(Module module) {
      if (module != null) {
         this.modules.add(module);
         if (module.isEnabled()) {
            AcoreHack.EVENT_BUS.subscribe(module);
         }
      }
   }
}
