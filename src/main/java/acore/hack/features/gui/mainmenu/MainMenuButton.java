package acore.hack.features.gui.mainmenu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

public class MainMenuButton {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private final float x, y, width, height, radius;
    private final String label;
    private final IconType iconType;
    private final Runnable action;
    
    public MainMenuButton(float x, float y, float width, float height, float radius, String label, IconType iconType, Runnable action) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.radius = radius;
        this.label = label;
        this.iconType = iconType;
        this.action = action;
    }
    
    public void render(DrawContext context, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY);
        Color bg = hovered ? new Color(60, 60, 70) : new Color(40, 40, 50);
        context.fill((int)x, (int)y, (int)(x + width), (int)(y + height), bg.getRGB());
        
        String icon = getIcon();
        context.drawCenteredTextWithShadow(mc.textRenderer, icon, (int)(x + width / 2f), (int)(y + height / 2f - 4f), 
            hovered ? Color.CYAN.getRGB() : Color.WHITE.getRGB());
    }
    
    public boolean onClick(int mouseX, int mouseY) {
        if (isHovered(mouseX, mouseY)) {
            action.run();
            return true;
        }
        return false;
    }
    
    public boolean isHovered(int mx, int my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }
    
    private String getIcon() {
        switch (iconType) {
            case SINGLEPLAYER: return "👤";
            case MULTIPLAYER: return "🌐";
            case ALT: return "👥";
            case SETTING: return "⚙";
            case LEAVE: return "🚪";
            default: return "•";
        }
    }
    
    public enum IconType {
        SINGLEPLAYER, MULTIPLAYER, ALT, SETTING, LEAVE
    }
}
