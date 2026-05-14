package acore.hack;

import acore.hack.core.manager.*;
import acore.hack.core.sound.SoundManager;
import acore.hack.features.gui.ClickGUI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class AcoreHack implements ClientModInitializer {
    public static AcoreHack INSTANCE;
    public static ClickGUI clickGUI;
    public static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        
        System.out.println("[AcoreHack] Initializing for Minecraft 1.21...");
        
        // Khởi tạo managers
        ModuleManager.init();
        FriendManager.init();
        KeybindManager.init();
        SoundManager.init();
        ConfigManager.init();
        
        // Đăng ký keybinding cho GUI (phím P)
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.acorehack.openGui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "category.acorehack"
        ));
        
        // Event tick để mở GUI
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openGuiKey.wasPressed()) {
                if (clickGUI == null) clickGUI = new ClickGUI();
                client.setScreen(clickGUI);
                SoundManager.playClickSound();
            }
            
            // Module updates
            ModuleManager.onUpdate();
        });
        
        // Load config
        ConfigManager.loadAllConfigs();
        
        System.out.println("[AcoreHack] Initialized successfully!");
    }
  }
