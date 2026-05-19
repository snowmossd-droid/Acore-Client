package acore.hack.features.modules.misc;

import acore.hack.features.modules.Module;
import acore.hack.setting.Setting;

public class RPC extends Module {
    public final Setting<Boolean> showServer = new Setting<>("ShowServer", true);
    public final Setting<Boolean> showPlayers = new Setting<>("ShowPlayers", true);
    public final Setting<String> customDetails = new Setting<>("CustomDetails", "ArisCore on Top");
    public final Setting<String> customState = new Setting<>("CustomState", "Best Client");

    public RPC() {
        super("DiscordRPC", "Discord Rich Presence", Module.Category.MISC);
    }

    @Override
    public void onEnable() {
        // Discord RPC implementation would go here
    }

    @Override
    public void onDisable() {
        // Discord RPC shutdown would go here
    }
}
