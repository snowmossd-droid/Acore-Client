package acore.hack.core.manager;

import acore.hack.features.modules.Module;

public class KeybindManager {
    public static void init() {
        // Initialize keybinds
    }
    
    public static void setKeybind(Module module, int keyCode) {
        module.setKeyCode(keyCode);
    }
}
