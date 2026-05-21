package acore.hack.core.manager;

import acore.hack.features.modules.Module;

public class KeybindManager {
    public static void init() {
    }
    
    public static void setKeybind(Module module, int keyCode) {
        if (module != null) {
            module.setBind(keyCode, false, false);
        }
    }
}
