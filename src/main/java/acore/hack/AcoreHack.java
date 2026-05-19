package acore.hack;

import acore.hack.core.manager.ModuleManager;
import acore.hack.core.manager.ConfigManager;
import acore.hack.core.sound.SoundManager;
import acore.hack.features.gui.clickui.ClickGUI;
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
    public static KeyBinding reloadConfigKey;
    
    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        
        System.out.println("[AcoreHack] Initializing...");
        
        ModuleManager.init();
        ConfigManager.init();
        SoundManager.init();
        
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.acorehack.openGui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "category.acorehack"
        ));
        
        reloadConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.acorehack.reloadConfig",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_PAGE_UP,
            "category.acorehack"
        ));
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openGuiKey.wasPressed()) {
                if (client.currentScreen instanceof ClickGUI) {
                    client.setScreen(null);
                } else {
                    if (clickGUI == null) clickGUI = new ClickGUI();
                    client.setScreen(clickGUI);
                }
                SoundManager.playClickSound();
            }
            
            if (reloadConfigKey.wasPressed()) {
                ConfigManager.reloadConfig();
                SoundManager.playClickSound();
                System.out.println("[AcoreHack] Config reloaded!");
            }
            
            if (client.currentScreen == null) {
                ModuleManager.onUpdate();
            }
        });
        
        System.out.println("[AcoreHack] Initialized! Press P to open GUI");
    }
            }
