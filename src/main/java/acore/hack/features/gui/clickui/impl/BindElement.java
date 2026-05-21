package acore.hack.features.gui.clickui.impl;

import acore.hack.features.gui.clickui.AbstractElement;
import acore.hack.setting.Setting;
import acore.hack.setting.impl.Bind;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;

public class BindElement extends AbstractElement {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public boolean isListening = false;
    
    public BindElement(Setting setting) {
        super(setting);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hovered = isHovered(mouseX, mouseY);
        
        context.drawTextWithShadow(mc.textRenderer, setting.getName(),
            (int)getSettingNameX(), (int)(y + height / 2f - 3f), Color.WHITE.getRGB());
        
        String bindText = getBindText();
        float tWidth = mc.textRenderer.getWidth(bindText);
        
        context.fill((int)(x + width - tWidth - 11f), (int)(y + 2f),
            (int)(x + width - 7f), (int)(y + 12f), new Color(30, 30, 40).getRGB());
        
        context.drawTextWithShadow(mc.textRenderer, bindText,
            (int)(x + width - tWidth - 9f), (int)(y + height / 2f - 3f), Color.WHITE.getRGB());
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isListening) {
            Bind b = new Bind(button, true, false);
            setting.setValue(b);
            isListening = false;
        } else if (hovered && button == 0) {
            isListening = true;
        }
        super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public void keyTyped(int keyCode) {
        if (isListening) {
            if (keyCode != 256 && keyCode != 261) {
                setting.setValue(new Bind(keyCode, false, false));
            } else {
                setting.setValue(new Bind(-1, false, false));
            }
            isListening = false;
        }
    }
    
    private String getBindText() {
        if (isListening) return "...";
        Bind bind = (Bind)setting.getValue();
        if (bind == null || bind.getKey() == -1) return "None";
        String keyName = GLFW.glfwGetKeyName(bind.getKey(), 0);
        return keyName == null ? "Key" : keyName.toUpperCase();
    }
    
    private boolean isHovered(int mx, int my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }
                       }
