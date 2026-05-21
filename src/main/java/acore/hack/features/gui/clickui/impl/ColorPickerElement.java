package acore.hack.features.gui.clickui.impl;

import acore.hack.features.gui.clickui.AbstractElement;
import acore.hack.setting.Setting;
import acore.hack.setting.impl.ColorSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

public class ColorPickerElement extends AbstractElement {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private boolean extended = false;
    private final Setting<?> colorSetting;
    
    public ColorPickerElement(Setting<?> setting) {
        super(setting);
        this.colorSetting = setting;
    }
    
    public ColorSetting getColorSetting() {
        return (ColorSetting) colorSetting.getValue();
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hovered = isHovered(mouseX, mouseY);
        
        context.drawTextWithShadow(mc.textRenderer, setting.getName(),
            (int)getSettingNameX(), (int)(y + 5f), Color.WHITE.getRGB());
        
        float swatchX = x + width - 22f;
        float swatchY = y + 4f;
        
        context.fill((int)swatchX, (int)swatchY, (int)(swatchX + 14), (int)(swatchY + 8), getColorSetting().getColorObject().getRGB());
        
        if (extended) {
            float pickerX = x + 6f;
            float pickerY = y + 16f;
            float pickerW = width - 31f;
            float pickerH = height - 30f;
            
            context.fill((int)pickerX, (int)pickerY, (int)(pickerX + pickerW), (int)(pickerY + pickerH), new Color(40, 40, 50).getRGB());
            
            // Hue bar
            for (int i = 0; i < (int)pickerH; i++) {
                float hue = 1f - (i / pickerH);
                context.fill((int)(pickerX + pickerW + 4), (int)(pickerY + i), 
                    (int)(pickerX + pickerW + 12), (int)(pickerY + i + 1), Color.getHSBColor(hue, 1f, 1f).getRGB());
            }
        }
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        float swatchX = x + width - 22f;
        float swatchY = y + 4f;
        
        if (mouseX >= swatchX && mouseX <= swatchX + 14 && mouseY >= swatchY && mouseY <= swatchY + 8) {
            extended = !extended;
        }
        super.mouseClicked(mouseX, mouseY, button);
    }
    
    public float getHeight() {
        return extended ? 66f : 15f;
    }
    
    private boolean isHovered(int mx, int my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }
        }
