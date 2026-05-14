package acore.hack.core.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SoundManager {
    
    private static MinecraftClient mc = MinecraftClient.getInstance();
    
    public static void init() {
        // Initialize sounds
    }
    
    public static void playClickSound() {
        // Play UI click sound
        if (mc.player != null) {
            mc.player.playSound(SoundEvent.of(Identifier.of("ui.button.click")), 0.5f, 1.0f);
        }
    }
    
    public static void playToggleSound() {
        if (mc.player != null) {
            mc.player.playSound(SoundEvent.of(Identifier.of("block.note_block.pling")), 0.5f, 1.0f);
        }
    }
}
