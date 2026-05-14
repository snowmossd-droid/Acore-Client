package acore.hack.features.modules.client;

import acore.hack.features.modules.Module;
import net.minecraft.network.packet.c2s.common.ServerMetadataC2SPacket;

public class ClientSpoof extends Module {

    public enum Mode {
        Vanilla, Lunar1_20_4, Lunar1_20_1, Custom, Null
    }
    
    public Mode mode = Mode.Vanilla;
    public String customClient = "feather";
    
    public ClientSpoof() {
        super("ClientSpoof", Category.CLIENT);
    }
    
    @Override
    protected void onEnable() {
        sendSpoofedPacket();
    }
    
    @Override
    public void onUpdate() {
        if (mc.getNetworkHandler() != null) {
            sendSpoofedPacket();
        }
    }
    
    private void sendSpoofedPacket() {
        String brand = getClientName();
        if (brand != null && mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new ServerMetadataC2SPacket(brand));
        }
    }
    
    public String getClientName() {
        switch (mode) {
            case Vanilla:
                return "vanilla";
            case Lunar1_20_4:
                return "lunarclient:1.20.4";
            case Lunar1_20_1:
                return "lunarclient:1.20.1";
            case Custom:
                return customClient;
            case Null:
                return null;
            default:
                return "vanilla";
        }
    }
    
    @Override
    protected void onDisable() {
        // Reset to vanilla
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new ServerMetadataC2SPacket("vanilla"));
        }
    }
}
