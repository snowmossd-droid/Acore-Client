package acore.hack.core.manager;

import acore.hack.features.modules.Module;
import acore.hack.features.modules.client.Rotations;
// import acore.hack.features.modules.client.ClientSpoof; // Tạm comment
import acore.hack.features.modules.combat.Aura;
import acore.hack.features.modules.combat.BowSpam;
import acore.hack.features.modules.combat.TargetStrafe;
import acore.hack.features.modules.combat.HitBox;
import acore.hack.features.modules.combat.AutoTNTcart;
import acore.hack.features.modules.render.ESP;
import acore.hack.features.modules.render.BlockESP;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {
    private static List<Module> modules = new ArrayList<>();
    
    public static void init() {
        modules.clear();
        
        // Client modules
        modules.add(new Rotations());
        // modules.add(new ClientSpoof()); // Tạm comment
        
        // Combat modules
        modules.add(new Aura());
        modules.add(new BowSpam());
        modules.add(new TargetStrafe());
        modules.add(new HitBox());
        modules.add(new AutoTNTcart());
        
        // Render modules
        modules.add(new ESP());
        modules.add(new BlockESP());
    }
    
    public static List<Module> getModules() {
        return modules;
    }
    
    public static List<Module> getModulesInCategory(Module.Category category) {
        return modules.stream()
            .filter(m -> m.getCategory() == category)
            .collect(Collectors.toList());
    }
    
    public static Module getModule(String name) {
        return modules.stream()
            .filter(m -> m.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }
    
    public static void onUpdate() {
        modules.stream().filter(Module::isEnabled).forEach(Module::onUpdate);
    }
    
    public static void onRender() {
        modules.stream().filter(Module::isEnabled).forEach(Module::onRender);
    }
    
    public static void onRender3D() {
        modules.stream().filter(Module::isEnabled).forEach(Module::onRender3D);
    }
    
    public static void saveModules() {
        ConfigManager.saveModules(modules);
    }
}
