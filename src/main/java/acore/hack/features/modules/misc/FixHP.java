package acore.hack.features.modules.misc;

import net.minecraft.entity.player.PlayerEntity;
import acore.hack.features.modules.Module;

public class FixHP extends Module {
    public FixHP() {
        super("FixHP", "Fixes health display", Module.Category.MISC);
    }

    public static float getHealth(PlayerEntity player) {
        return player.getHealth() + player.getAbsorptionAmount();
    }
}
