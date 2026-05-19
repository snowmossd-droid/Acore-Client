package acore.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import acore.hack.events.impl.PacketEvent;
import acore.hack.events.impl.PlayerUpdateEvent;
import acore.hack.features.modules.Module;
import acore.hack.setting.Setting;

public class NoFall extends Module {
    public final Setting<Boolean> packet = new Setting<>("Packet", true);
    public final Setting<Boolean> grim = new Setting<>("Grim", false);

    public NoFall() {
        super("NoFall", "Prevents fall damage", Module.Category.MOVEMENT);
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player == null || mc.world == null) return;
        
        if (mc.player.fallDistance > 3.0F) {
            if (packet.getValue()) {
                sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
            }
            mc.player.fallDistance = 0.0F;
        }
    }
}
