package acore.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import acore.hack.events.impl.EventFixVelocity;
import acore.hack.features.modules.Module;
import acore.hack.setting.Setting;
import net.minecraft.util.math.Vec3d;

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
        
        Vec3d velocity = event.getVelocity();
        double x = velocity.x;
        double y = velocity.y;
        double z = velocity.z;
        
        if (horizontal.getValue()) {
            x = x * multiplier;
            z = z * multiplier;
        }
        if (vertical.getValue()) {
            y = y * multiplier;
        }
        
        event.setVelocity(new Vec3d(x, y, z));
    }
                }
