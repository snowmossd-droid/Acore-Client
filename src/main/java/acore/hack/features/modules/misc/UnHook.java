package acore.hack.features.modules.misc;

import acore.hack.core.manager.ModuleManager;
import acore.hack.features.modules.Module;

public class UnHook extends Module {
    private static UnHook instance;

    public UnHook() {
        super("UnHook", "Unhook the client from server", Module.Category.MISC);
        instance = this;
    }

    @Override
    public void onEnable() {
        if (mc.player != null && mc.world != null) {
            ModuleManager.getModules().forEach(module -> {
                if (module.isEnabled() && module != this) {
                    module.disable();
                }
            });
        }
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled();
    }
}
