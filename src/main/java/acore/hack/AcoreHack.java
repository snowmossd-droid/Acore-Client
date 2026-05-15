package acore.hack;

import acore.hack.core.manager.ModuleManager;
import acore.hack.core.manager.FriendManager;
import acore.hack.core.manager.ConfigManager;
import acore.hack.core.sound.SoundManager;
import acore.hack.features.gui.ClickGUI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
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
        
        System.out.println("[AcoreHack] Initializing for Minecraft 1.21...");
        
        // Khởi tạo managers
        ModuleManager.init();
        FriendManager.init();
        ConfigManager.init();
        SoundManager.init();
        
        // Đăng ký keybinding cho GUI (phím P)
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.acorehack.openGui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "category.acorehack"
        ));
        
        // Đăng ký keybinding reload config (phím R)
        reloadConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.acorehack.reloadConfig",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "category.acorehack"
        ));
        
        // Event tick để xử lý keybinding
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.currentScreen == null) {
                // Xử lý mở/đóng GUI khi nhấn P
                if (openGuiKey.wasPressed()) {
                    if (client.currentScreen instanceof ClickGUI) {
                        // Đang mở AcoreHack -> đóng
                        client.setScreen(null);
                        SoundManager.playClickSound();
                    } else {
                        // Mở AcoreHack GUI, đè lên mọi menu
                        if (clickGUI == null) clickGUI = new ClickGUI();
                        client.setScreen(clickGUI);
                        SoundManager.playClickSound();
                    }
                }
                
                // Xử lý reload config khi nhấn R + Ctrl
                if (reloadConfigKey.wasPressed() && hasControlDown()) {
                    ConfigManager.reloadConfig();
                    SoundManager.playClickSound();
                    System.out.println("[AcoreHack] Config reloaded!");
                }
            }
            
            // Module updates (chỉ khi không trong GUI)
            if (client.currentScreen == null || !(client.currentScreen instanceof ClickGUI)) {
                ModuleManager.onUpdate();
            }
        });
        
        System.out.println("[AcoreHack] Initialized successfully!");
        System.out.println("[AcoreHack] Press P to open GUI | Ctrl+R to reload config");
    }
    
    private boolean hasControlDown() {
        return MinecraftClient.getInstance().currentScreen != null 
            && MinecraftClient.getInstance().currentScreen.hasControlDown();
    }
    }
