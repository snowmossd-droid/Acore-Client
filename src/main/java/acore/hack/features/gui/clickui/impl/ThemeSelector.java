package acore.hack.features.gui.clickui.impl;

import acore.hack.features.gui.clickui.ClickGUI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class ThemeSelector {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private float x, y, size;
    private boolean paletteOpen = false;
    private float themeAlphaAnim = 0f;
    
    public void setLayout(float x, float y, float size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }
    
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hovered = isHovered(mouseX, mouseY);
        Color bg = new Color(25, 25, 28, 200);
        drawRoundedRect(context, x, y, size, size, size / 4f, bg);
        
        context.drawTextWithShadow(mc.textRenderer, "🎨", (int)(x + size / 2f - 5f), (int)(y + size / 2f - 5f), Color.WHITE.getRGB());
        
        if (hovered) {
            ClickGUI.anyHovered = true;
        }
    }
    
    public boolean shouldRenderPalette() {
        return paletteOpen || themeAlphaAnim > 0.01f;
    }
    
    public void renderPalette(DrawContext context, int mouseX, int mouseY, float delta, float px, float pw, float py, float ph) {
        float target = paletteOpen ? 1f : 0f;
        themeAlphaAnim += (target - themeAlphaAnim) * 0.15f;
        
        if (themeAlphaAnim < 0.01f && !paletteOpen) return;
        
        Color bg = new Color(20, 20, 22, (int)(200 * themeAlphaAnim));
        drawRoundedRect(context, px, py, pw, ph, ph / 2f, bg);
        
        Color[] themes = {
            new Color(100, 100, 255),
            new Color(255, 100, 100),
            new Color(100, 255, 100),
            new Color(255, 200, 100),
            new Color(200, 100, 255)
        };
        
        float swatchSize = Math.max(10.5f, ph - 5.5f);
        float spacing = 5f;
        float totalWidth = themes.length * (swatchSize + spacing) - spacing;
        float startX = px + (pw - totalWidth) / 2f;
        float swatchY = py + (ph - swatchSize) / 2f;
        
        for (int i = 0; i < themes.length; i++) {
            float sx = startX + i * (swatchSize + spacing);
            boolean hovered = isHovered(mouseX, mouseY, sx, swatchY, swatchSize, swatchSize);
            
            if (hovered) {
                ClickGUI.anyHovered = true;
            }
            
            drawRoundedRect(context, sx, swatchY, swatchSize, swatchSize, swatchSize / 2f, themes[i]);
        }
    }
    
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            paletteOpen = !paletteOpen;
        }
    }
    
    public void mouseReleased(int mouseX, int mouseY, int button) {}
    public void tick() {}
    
    private boolean isHovered(int mx, int my) {
        return mx >= x && mx <= x + size && my >= y && my <= y + size;
    }
    
    private boolean isHovered(int mx, int my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
    
    private void drawRoundedRect(DrawContext context, float x, float y, float w, float h, float r, Color color) {
        context.fill((int)x, (int)y, (int)(x + w), (int)(y + h), color.getRGB());
    }
            }
