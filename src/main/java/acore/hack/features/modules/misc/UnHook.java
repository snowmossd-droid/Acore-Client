package acore.hack.features.modules.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.SharedConstants;
import net.minecraft.client.util.Icons;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import acore.hack.core.Managers;
import acore.hack.core.manager.ConfigManager;
import acore.hack.core.manager.ModuleManager;
import acore.hack.features.modules.Module;
import acore.hack.injection.accesors.IMinecraftClient;
import acore.hack.utility.math.MathUtility;

public class UnHook extends Module {
   List<Module> list;
   public int code = 0;
   private static volatile boolean active;

   public UnHook() {
      super("UnHook", "Turns off all modules.", Module.Category.MISC);
   }

   public static boolean isActive() {
      return active || ModuleManager.unHook != null && ModuleManager.unHook.isEnabled();
   }

   @Override
   public void onEnable() {
      active = true;
      this.code = (int)MathUtility.random(10.0F, 99.0F);

      for (int i = 0; i < 20; i++) {
         this.sendMessage(Formatting.RED + "It's all close now, write to the chat " + Formatting.WHITE + this.code + Formatting.RED + " to return everything!");
      }

      this.list = Managers.MODULE.getEnabledModules();
      mc.setScreen(null);
      this.refreshWindowTitle();
      Managers.ASYNC.run(() -> mc.execute(() -> {
         for (Module module : this.list) {
            if (!module.equals(this)) {
               module.disable();
            }
         }

         this.restoreVanillaWindowAppearance();
         mc.inGameHud.getChatHud().clear(true);
         this.setEnabled(true);

         try {
            File file = new File(mc.runDirectory + File.separator + "logs" + File.separator + "latest.log");
            LoggerContext ctx = (LoggerContext)LogManager.getContext(false);

            try {
               ctx.stop();
            } catch (Exception var15) {
            }

            try (
               FileInputStream fis = new FileInputStream(file);
               BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
            ) {
               ArrayList<String> lines = new ArrayList<>();

               String line;
               while ((line = reader.readLine()) != null) {
                  if (!shouldStripLogLine(line)) {
                     lines.add(line);
                  }
               }

               try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                  raf.setLength(0L);
                  String nl = System.lineSeparator();

                  for (String s : lines) {
                     raf.write((s + nl).getBytes(StandardCharsets.UTF_8));
                  }

                  raf.getChannel().force(true);
               }
            }

            try {
               ctx.reconfigure();
               ctx.start();
            } catch (Exception var14) {
            }

            ConfigManager.MAIN_FOLDER.renameTo(new File("Acore_BACKUP092738"));
         } catch (IOException var19) {
         }
      }), 5000L);
   }

   @Override
   public void onDisable() {
      active = false;
      if (this.list == null) {
         this.refreshWindowTitle();
      } else {
         for (Module module : this.list) {
            if (!module.equals(this)) {
               module.enable();
            }
         }

         this.refreshWindowTitle();

         try {
            new File("Acore_BACKUP092738").renameTo(new File("Acore"));
         } catch (Exception var3) {
         }
      }
   }

   private void restoreVanillaWindowAppearance() {
      try {
         mc.getWindow().setIcon(mc.getDefaultResourcePack(), SharedConstants.getGameVersion().isStable() ? Icons.RELEASE : Icons.SNAPSHOT);
      } catch (Exception var2) {
      }

      this.refreshWindowTitle();
   }

   private void refreshWindowTitle() {
      try {
         ((IMinecraftClient)mc).invokeUpdateWindowTitle();
      } catch (Exception var2) {
      }
   }

   private static boolean shouldStripLogLine(String line) {
      if (!line.contains("$$") && !line.contains("\\______/") && !line.contains("⚡") && !line.contains("By VennLMAO")) {
         String lower = line.toLowerCase(Locale.ROOT);
         return !lower.contains("acore") ? false : !isAcoreModListEntry(lower.trim());
      } else {
         return true;
      }
   }

   private static boolean isAcoreModListEntry(String trimmedLowerLine) {
      return trimmedLowerLine.matches("^(?:[-+*|`\\\\]+\\s+)?acore\\b.*");
   }
               }
