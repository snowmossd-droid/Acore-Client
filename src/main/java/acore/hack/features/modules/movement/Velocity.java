package acore.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import acore.hack.events.impl.EventFixVelocity;
import acore.hack.features.modules.Module;
import acore.hack.setting.Setting;

public class Velocity extends Module {
    public final Setting<Boolean> horizontal = new Setting<>("Horizontal", true);
    public final Setting<Boolean> vertical = new Setting<>("Vertical", true);
    public final Setting<Integer> strength = new Setting<>("Strength", 100, 0, 100);

    public Velocity() {
        super("Velocity", "Reduce knockback", Module.Category.MOVEMENT);
    }

    @EventHandler
    public void onVelocity(EventFixVelocity event) {
        if (mc.player == null || mc.world == null) return;
        
        double multiplier = strength.getValue() / 100.0;
        
        if (horizontal.getValue()) {
            event.x = event.x * multiplier;
            event.z = event.z * multiplier;
        }
        if (vertical.getValue()) {
            event.y = event.y * multiplier;
        }
    }
}
