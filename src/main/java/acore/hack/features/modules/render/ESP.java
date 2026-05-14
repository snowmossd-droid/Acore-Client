package acore.hack.features.modules.render;

import acore.hack.features.modules.Module;
import net.minecraft.entity.LivingEntity;

public class ESP extends Module {
    
    public ESP() {
        super("ESP", Category.RENDER);
    }
    
    @Override
    protected void onEnable() {
        super.onEnable();
    }
    
    @Override
    protected void onDisable() {
        super.onDisable();
    }
    
    public static boolean hasESP(LivingEntity entity) {
        // Check if entity has ESP
        return false;
    }
}
