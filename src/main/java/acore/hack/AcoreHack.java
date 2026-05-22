package acore.hack;

import acore.hack.core.Managers;
import acore.hack.core.Core;
import acore.hack.core.hooks.ManagerShutdownHook;
import acore.hack.core.hooks.ModuleShutdownHook;
import acore.hack.core.manager.ConfigManager;
import acore.hack.core.manager.ModuleManager;
import acore.hack.core.manager.SoundManager;
import acore.hack.event.EventBus;
import acore.hack.features.gui.clickui.ClickGUI;
import acore.hack.core.manager.NotificationManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.lwjgl.glfw.GLFW;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

public class AcoreHack implements ClientModInitializer {
    public static AcoreHack INSTANCE;
    public static final EventBus EVENT_BUS = new EventBus();
    public static final NotificationManager NOTIFICATION = new NotificationManager();
    public static final Logger LOGGER = LoggerFactory.getLogger("AcoreHack");
    public static final List<Packet<?>> silentPackets = new ArrayList<>();
    public static float TICK_TIMER = 1.0F;
    public static Core core = new Core();
    public static BlockPos gps_position = null;
    public static long initTime = System.currentTimeMillis();

    public static ClickGUI clickGUI;
    public static KeyBinding openGuiKey;
    public static KeyBinding reloadConfigKey;

    private SoundManager soundManager;
    private ModuleManager moduleManager;

    static {
        Runtime.getRuntime().addShutdownHook(new ManagerShutdownHook());
        Runtime.getRuntime().addShutdownHook(new ModuleShutdownHook());
    }

    @Override
    public void onInitializeClient() {
        INSTANCE = this;

        LOGGER.info("[AcoreHack] Initializing...");

        EVENT_BUS.registerLambdaFactory("acore.hack", (lookupInMethod, klass) -> {
            return (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup());
        });

        EVENT_BUS.subscribe(core);

        Managers.init();
        Managers.subscribe();

        soundManager = new SoundManager();
        moduleManager = new ModuleManager();

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
                soundManager.playClickSound();
            }

            if (reloadConfigKey.wasPressed()) {
                ConfigManager.getInstance().reloadConfig();
                soundManager.playClickSound();
                LOGGER.info("[AcoreHack] Config reloaded!");
            }

            if (client.currentScreen == null) {
                moduleManager.onUpdate();
            }

            silentPackets.clear();
        });

        LOGGER.info("[AcoreHack] Initialized! Press P to open GUI");
        LOGGER.info("[AcoreHack] Config folder: .minecraft/acorehack/configs/");
    }
    }
