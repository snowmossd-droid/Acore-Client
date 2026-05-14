package acore.hack.features.modules.client;

import acore.hack.features.modules.Module;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;

public class ClientSpoof extends Module {
    
    public boolean spoofLanguage = true;
    public String language = "en_US";
    public boolean spoofViewDistance = true;
    public int viewDistance = 12;
    public boolean spoofChatVisibility = true;
    public String chatVisibility = "FULL";
    
    public ClientSpoof() {
        super("ClientSpoof", Category.CLIENT);
    }
    
    @Override
    protected void onEnable() {
        if (mc.getNetworkHandler() != null && mc.player != null) {
            sendSpoofedSettings();
        }
    }
    
    @Override
    public void onUpdate() {
        if (mc.getNetworkHandler() != null && mc.player != null && isEnabled()) {
            sendSpoofedSettings();
        }
    }
    
    private void sendSpoofedSettings() {
        String lang = spoofLanguage ? language : mc.getLanguageManager().getLanguage();
        int vd = spoofViewDistance ? viewDistance : mc.options.getViewDistance().getValue();
        
        net.minecraft.client.option.ChatVisibility visibility;
        if (chatVisibility.equalsIgnoreCase("FULL")) {
            visibility = net.minecraft.client.option.ChatVisibility.FULL;
        } else if (chatVisibility.equalsIgnoreCase("SYSTEM")) {
            visibility = net.minecraft.client.option.ChatVisibility.SYSTEM;
        } else {
            visibility = net.minecraft.client.option.ChatVisibility.HIDDEN;
        }
        
        mc.getNetworkHandler().sendPacket(new ClientSettingsC2SPacket(
            lang,
            visibility,
            mc.options.getChatColors().getValue(),
            mc.options.getPlayerModelBitMask(),
            mc.player.getMainArm(),
            false,
            vd
        ));
    }
    
    @Override
    protected void onDisable() {
        if (mc.getNetworkHandler() != null && mc.player != null) {
            // Reset to original settings
            mc.getNetworkHandler().sendPacket(new ClientSettingsC2SPacket(
                mc.getLanguageManager().getLanguage(),
                mc.options.getChatVisibility().getValue(),
                mc.options.getChatColors().getValue(),
                mc.options.getPlayerModelBitMask(),
                mc.player.getMainArm(),
                false,
                mc.options.getViewDistance().getValue()
            ));
        }
    }
            }
