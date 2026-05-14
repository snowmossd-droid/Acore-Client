package acore.hack.core.manager;

import acore.hack.features.modules.Module;
import acore.hack.features.modules.client.Rotations;
import acore.hack.features.modules.client.ClientSpoof;
import acore.hack.features.modules.combat.Aura;
import acore.hack.features.modules.combat.BowSpam;
import acore.hack.features.modules.combat.TargetStrafe;
import acore.hack.features.modules.combat.HitBox;
import acore.hack.features.modules.combat.AutoTNTcart;
import acore.hack.features.modules.render.ESP;
import acore.hack.features.modules.render.BlockESP;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private static List<Module> modules = new ArrayList<>();
    
    public static void init() {
        modules.clear();
        modules.add(new Rotations());
        modules.add(new ClientSpoof());
        modules.add(new Aura());
        modules.add(new BowSpam());
        modules.add(new TargetStrafe());
        modules.add(new HitBox());
        modules.add(new AutoTNTcart());
        modules.add(new ESP());
        modules.add(new BlockESP());
    }
    
    public static List<Module> getModules() {
        return modules;
    }
    
    public static Module getModule(String name) {
        for (Module m : modules) {
            if (m.getName().equalsIgnoreCase(name)) return m;
        }
        return null;
    }
    
    public static void onUpdate() {
        for (Module m : modules) {
            if (m.isEnabled()) m.onUpdate();
        }
    }
    
    public static void saveModules() {
        // Save module settings (will implement later)
    }
}
