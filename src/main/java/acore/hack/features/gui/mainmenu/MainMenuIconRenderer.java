package acore.hack.features.gui.mainmenu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import java.awt.Color;

public final class MainMenuIconRenderer {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    
    private MainMenuIconRenderer() {}
    
    public static void render(DrawContext context, MainMenuButton.IconType iconType, float centerX, float centerY, float size, boolean hovered) {
        Color color = hovered ? Color.CYAN : Color.WHITE;
        String icon = getIconChar(iconType);
        context.drawCenteredTextWithShadow(mc.textRenderer, icon, (int)centerX, (int)(centerY - size / 2f), color.getRGB());
    }
    
    private static String getIconChar(MainMenuButton.IconType type) {
        return switch (type) {
            case SINGLEPLAYER -> "👤";
            case MULTIPLAYER -> "🌐";
            case ALT -> "👥";
            case SETTING -> "⚙";
            case LEAVE -> "🚪";
        };
    }
        }
