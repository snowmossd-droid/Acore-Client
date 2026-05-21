package acore.hack.features.gui.clickui.impl;

import acore.hack.features.gui.clickui.AbstractElement;
import acore.hack.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

public class BooleanElement extends AbstractElement {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private float animation = 0f;
    
    public BooleanElement(Setting setting) {
        super(setting);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hovered = isHovered(mouseX, mouseY);
        
        animation += (((Boolean)setting.getValue() ? 1f : 0f) - animation) * 0.125f;
        
        float checkboxX = x + width - 17f;
        float checkboxY = y + height / 2f - 5f;
        
        drawCheckbox(context, checkboxX, checkboxY, animation);
        
        context.drawTextWithShadow(mc.textRenderer, setting.getName(), 
            (int)getSettingNameX(), (int)(y + height / 2f - 3f), Color.WHITE.getRGB());
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (hovered && button == 0) {
            setting.setValue(!(Boolean)setting.getValue());
        }
        super.mouseClicked(mouseX, mouseY, button);
    }
    
    private void drawCheckbox(DrawContext context, float x, float y, float anim) {
        Color bg = new Color(40, 40, 50, 200);
        context.fill((int)x, (int)y, (int)(x + 10), (int)(y + 10), bg.getRGB());
        
        if (anim > 0.05f) {
            Color fill = new Color(100, 100, 255, (int)(255 * anim));
            context.fill((int)x + 2, (int)y + 2, (int)(x + 8), (int)(y + 8), fill.getRGB());
        }
    }
    
    private boolean isHovered(int mx, int my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }
             }
