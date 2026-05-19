package acore.hack.features.gui.clickui.impl;

import acore.hack.features.gui.clickui.AbstractElement;
import acore.hack.setting.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

import java.awt.Color;

public class ModeElement extends AbstractElement {
    private boolean open = false;
    private float animation = 0f;
    private String prevMode;
    
    public ModeElement(Setting setting) {
        super(setting);
        this.prevMode = setting.currentEnumName();
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hovered = isHovered(mouseX, mouseY);
        animation += (open ? 0f : 1f) - animation * 0.1f;
        
        MatrixStack matrices = context.getMatrices();
        
        float tx = x + width - 11f;
        float ty = y + 7.5f;
        matrices.push();
        matrices.translate(tx, ty, 0);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-180f * animation));
        matrices.translate(-tx, -ty, 0);
        matrices.pop();
        
        context.drawTextWithShadow(textRenderer, setting.getName(),
            (int)getSettingNameX(), (int)(y + height / 2f - 3f), Color.WHITE.getRGB());
        
        String value = setting.currentEnumName();
        context.drawTextWithShadow(textRenderer, value,
            (int)(x + width - 18f - textRenderer.getStringWidth(value)), (int)(y + height / 2f - 3f), Color.WHITE.getRGB());
        
        if (open) {
            float startX = x + 6f;
            float startY = y + height + 3f;
            String[] modes = setting.getModes();
            
            for (int i = 0; i < modes.length; i++) {
                float chipWidth = textRenderer.getStringWidth(modes[i]) + 8f;
                float chipX = startX;
                float chipY = startY + i * 14f;
                
                context.fill((int)chipX, (int)chipY, (int)(chipX + chipWidth), (int)(chipY + 12f), 
                    new Color(40, 40, 50).getRGB());
                
                context.drawTextWithShadow(textRenderer, modes[i],
                    (int)(chipX + 4f), (int)(chipY + 2f),
                    setting.currentEnumName().equals(modes[i]) ? Color.CYAN.getRGB() : Color.WHITE.getRGB());
            }
        }
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (hovered) {
            if (button == 0) {
                setting.increaseEnum();
            } else if (button == 1) {
                open = !open;
            }
        }
        
        if (open && button == 0) {
            float startX = x + 6f;
            float startY = y + height + 3f;
            String[] modes = setting.getModes();
            
            for (int i = 0; i < modes.length; i++) {
                float chipWidth = textRenderer.getStringWidth(modes[i]) + 8f;
                if (mouseX >= startX && mouseX <= startX + chipWidth &&
                    mouseY >= startY + i * 14f && mouseY <= startY + i * 14f + 12f) {
                    setting.setEnumByNumber(i);
                    open = false;
                    break;
                }
            }
        }
        
        super.mouseClicked(mouseX, mouseY, button);
    }
    
    public float getExpandedHeight() {
        return open ? height + setting.getModes().length * 14f : height;
    }
    
    private boolean isHovered(int mx, int my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }
}