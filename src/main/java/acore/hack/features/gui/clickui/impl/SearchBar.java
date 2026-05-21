package acore.hack.features.gui.clickui.impl;

import acore.hack.features.gui.clickui.ClickGUI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;

public class SearchBar {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static String moduleName = "";
    private static boolean listening = false;
    
    private float x, y, width, height;
    
    public static void resetState() {
        moduleName = "";
        listening = false;
    }
    
    public static boolean hasQuery() {
        return !moduleName.isBlank();
    }
    
    public static boolean matchesQuery(String name) {
        return !hasQuery() || name.toLowerCase().contains(moduleName.toLowerCase());
    }
    
    public void setPosition(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hovered = isHovered(mouseX, mouseY);
        
        Color bg = new Color(25, 25, 28, 200);
        drawRoundedRect(context, x, y, width, height, height / 2f, bg);
        
        String displayText;
        int textColor;
        
        if (!listening && !hasQuery()) {
            displayText = "Search...";
            textColor = new Color(150, 150, 150).getRGB();
        } else {
            String suffix = listening && (System.currentTimeMillis() / 500 % 2 == 0) ? "_" : "";
            displayText = moduleName + suffix;
            textColor = Color.WHITE.getRGB();
        }
        
        float textY = y + (height - mc.textRenderer.fontHeight) / 2f + 3f;
        context.drawTextWithShadow(mc.textRenderer, displayText, (int)(x + 9f), (int)textY, textColor);
        
        if (hovered) {
            ClickGUI.anyHovered = true;
        }
    }
    
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            listening = true;
        } else {
            listening = false;
        }
    }
    
    public void mouseReleased(int mouseX, int mouseY, int button) {}
    
    public void charTyped(char key, int keyCode) {
        if (isValidChar(key) && listening) {
            moduleName += key;
        }
    }
    
    public void keyTyped(int keyCode) {
        if (keyCode == 70 && InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 341)) {
            moduleName = "";
            listening = true;
        }
        
        if (listening) {
            if (keyCode == 32) {
                moduleName += " ";
            } else if (keyCode == 256 || keyCode == 257) {
                listening = false;
            } else if (keyCode == 259 && !moduleName.isEmpty()) {
                moduleName = moduleName.substring(0, moduleName.length() - 1);
            }
        }
    }
    
    public void tick() {}
    
    private boolean isValidChar(char key) {
        return (key >= 'a' && key <= 'z') || (key >= 'A' && key <= 'Z') || (key >= '0' && key <= '9') || key == ' ';
    }
    
    private boolean isHovered(int mx, int my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }
    
    private void drawRoundedRect(DrawContext context, float x, float y, float w, float h, float r, Color color) {
        context.fill((int)x, (int)y, (int)(x + w), (int)(y + h), color.getRGB());
    }
        }
