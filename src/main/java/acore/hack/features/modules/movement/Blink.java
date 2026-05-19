package acore.hack.features.modules.movement;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import acore.hack.events.impl.PacketEvent;
import acore.hack.events.impl.PlayerUpdateEvent;
import acore.hack.features.modules.Module;
import acore.hack.setting.Setting;

public class Blink extends Module {
    public final Setting<Integer> limit = new Setting<>("Limit", 100, 0, 1000);
    public static Vec3d lastPos = Vec3d.ZERO;
    private final List<Packet<?>> storedPackets = new ArrayList<>();
    private int packetsStored = 0;

    public Blink() {
        super("Blink", "Delay outgoing packets", Module.Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        storedPackets.clear();
        packetsStored = 0;
        if (mc.player != null) {
            lastPos = mc.player.getPos();
        }
    }

    @Override
    public void onDisable() {
        storedPackets.forEach(p -> sendPacket(p));
        storedPackets.clear();
        packetsStored = 0;
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            if (packetsStored < limit.getValue()) {
                event.cancel();
                storedPackets.add(event.getPacket());
                packetsStored++;
            }
        }
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null) {
            lastPos = mc.player.getPos();
        }
    }
}
