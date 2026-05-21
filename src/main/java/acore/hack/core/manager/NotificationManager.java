package acore.hack.core.manager;

import com.google.common.collect.Lists;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import org.apache.commons.lang3.SystemUtils;
import acore.hack.AcoreHack;
import acore.hack.core.manager.IManager;
import acore.hack.features.modules.misc.Notifications;
import acore.hack.gui.notification.Notification;

public class NotificationManager implements IManager {
   private final List<Notification> notifications = new ArrayList<>();
   private TrayIcon trayIcon;

   public void publicity(String title, String content, int second, Notification.Type type) {
      if (!mc.isWindowFocused()) {
         this.nativeNotification(content, title);
      }
      this.notifications.add(new Notification(title, content, type, second * 1000));
   }

   public void onRender2D(DrawContext context) {
      boolean notificationsEnabled = ModuleManager.notifications.isEnabled();
      float startY = isDefault() ? mc.getWindow().getScaledHeight() - 36.0F : mc.getWindow().getScaledHeight() / 2.0F + 25.0F;
      if (this.notifications.size() > 8) {
         this.notifications.removeFirst();
      }
      this.notifications.removeIf(Notification::shouldDelete);
      Iterator var4 = Lists.newArrayList(this.notifications).iterator();
      while (true) {
         Notification n;
         while (true) {
            if (!var4.hasNext()) {
               return;
            }
            n = (Notification)var4.next();
            if (notificationsEnabled) {
               break;
            }
            if (n != null) {
               String title;
               try {
                  Field f = n.getClass().getDeclaredField("title");
                  f.setAccessible(true);
                  title = (String)f.get(n);
               } catch (Exception e) {
                  continue;
               }
               if (title != null && title.equals("TotemPopCounter")) {
                  break;
               }
            }
         }
         startY = (float)(startY - n.getHeight() - 3.0);
         n.renderShaders(context.getMatrices(), startY + (isDefault() ? 0 : this.notifications.size() * 16));
         n.render(context.getMatrices(), startY + (isDefault() ? 0 : this.notifications.size() * 16));
      }
   }

   public void onUpdate() {
      boolean notificationsEnabled = ModuleManager.notifications.isEnabled();
      this.notifications.removeIf(Notification::shouldDelete);
      Iterator var2 = Lists.newArrayList(this.notifications).iterator();
      while (true) {
         Notification n;
         while (true) {
            if (!var2.hasNext()) {
               return;
            }
            n = (Notification)var2.next();
            if (notificationsEnabled) {
               break;
            }
            if (n != null) {
               String title;
               try {
                  Field f = n.getClass().getDeclaredField("title");
                  f.setAccessible(true);
                  title = (String)f.get(n);
               } catch (Exception e) {
                  continue;
               }
               if (title != null && title.equals("TotemPopCounter")) {
                  break;
               }
            }
         }
         n.onUpdate();
      }
   }

   public static boolean isDefault() {
      return ModuleManager.notifications.mode.getValue() == Notifications.Mode.Default;
   }

   private void nativeNotification(String message, String title) {
      if (SystemUtils.IS_OS_WINDOWS) {
         this.windows(message, title);
      } else if (SystemUtils.IS_OS_LINUX) {
         this.linux(message);
      } else if (SystemUtils.IS_OS_MAC) {
         this.mac(message);
      } else {
         AcoreHack.LOGGER.error("Unsupported OS: {}", SystemUtils.OS_NAME);
      }
   }

   private void windows(String message, String title) {
      if (SystemTray.isSupported()) {
         try {
            if (this.trayIcon == null) {
               SystemTray tray = SystemTray.getSystemTray();
               Image image = Toolkit.getDefaultToolkit().createImage("resources/icon.png");
               this.trayIcon = new TrayIcon(image, "AcoreHack");
               this.trayIcon.setImageAutoSize(true);
               this.trayIcon.setToolTip("AcoreHack");
               tray.add(this.trayIcon);
            }
            this.trayIcon.displayMessage(title, message, MessageType.INFO);
         } catch (Exception e) {
            AcoreHack.LOGGER.error(e.getMessage());
         }
      } else {
         AcoreHack.LOGGER.error("SystemTray is not supported");
      }
   }

   private void mac(String message) {
      ProcessBuilder processBuilder = new ProcessBuilder();
      processBuilder.command("osascript", "-e", "display notification \"" + message + "\" with title \"AcoreHack\"");
      try {
         processBuilder.start();
      } catch (IOException e) {
         AcoreHack.LOGGER.error(e.getMessage());
      }
   }

   private void linux(String message) {
      ProcessBuilder processBuilder = new ProcessBuilder();
      processBuilder.command("notify-send", "-a", "AcoreHack", message);
      try {
         processBuilder.start();
      } catch (IOException e) {
         AcoreHack.LOGGER.error(e.getMessage());
      }
   }
                     }
