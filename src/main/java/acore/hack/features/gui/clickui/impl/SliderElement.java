package acore.hack.features.gui.clickui.impl;

import acore.hack.features.gui.clickui.AbstractElement;
import acore.hack.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;

public class SliderElement extends AbstractElement {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private final float min;
    private final float max;
    private float animation = 0f;
    private boolean dragging = false;
    
    public SliderElement(Setting setting) {
        super(setting);
        this.min = ((Number)setting.getMin()).floatValue();
        this.max = ((Number)setting.getMax()).floatValue();
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hovered = isHovered(mouseX, mouseY);
        
        float current = ((Number)setting.getValue()).floatValue();
        animation += ((current - min) / (max - min) - animation) * 0.1f;
        
        context.drawTextWithShadow(mc.textRenderer, setting.getName(),
            (int)getSettingNameX(), (int)(y + 4f), Color.WHITE.getRGB());
        
        String valueStr = setting.getDisplayValue();
        context.drawTextWithShadow(mc.textRenderer, valueStr,
            (int)(x + width - 6f - mc.textRenderer.getWidth(valueStr)), (int)(y + 4f), Color.WHITE.getRGB());
        
        float trackX = x + 6f;
        float trackY = y + height - 6f;
        float trackWidth = width - 12f;
        
        context.fill((int)trackX, (int)trackY, (int)(trackX + trackWidth), (int)(trackY + 2f), new Color(60, 60, 70).getRGB());
        context.fill((int)trackX, (int)trackY, (int)(trackX + trackWidth * animation), (int)(trackY + 2f), new Color(100, 100, 255).getRGB());
        
        float thumbX = trackX + trackWidth * animation - 3f;
        float thumbY = trackY - 2f;
        context.fill((int)thumbX, (int)thumbY, (int)(thumbX + 6f), (int)(thumbY + 6f), Color.WHITE.getRGB());
        
        if (dragging) {
            setValue(mouseX);
        }
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0 && isSliderHovered(mouseX, mouseY)) {
            dragging = true;
            setValue(mouseX);
        }
        super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        dragging = false;
    }
    
    private void setValue(int mouseX) {
        float trackX = x + 6f;
        float trackWidth = width - 12f;
        float value = MathHelper.clamp((mouseX - trackX) / trackWidth, 0f, 1f);
        float result = min + (max - min) * value;
        
        if (setting.getValue() instanceof Float) {
            setting.setValue(result);
        } else if (setting.getValue() instanceof Integer) {
            setting.setValue((int)result);
        }
    }
    
    private boolean isSliderHovered(int mx, int my) {
        float trackX = x + 6f;
        float trackY = y + height - 6f;
        float trackWidth = width - 12f;
        return mx >= trackX && mx <= trackX + trackWidth && my >= trackY - 4f && my <= trackY + 6f;
    }
    
    private boolean isHovered(int mx, int my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }
            }
