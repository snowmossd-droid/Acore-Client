package acore.hack.features.gui.clickui.impl;

import acore.hack.features.gui.clickui.AbstractElement;
import acore.hack.setting.Setting;
import acore.hack.setting.impl.SettingGroup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

public class ParentElement extends AbstractElement {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private final Setting<SettingGroup> parentSetting;
    private float animation = 0f;
    
    public ParentElement(Setting<?> setting) {
        super(setting);
        this.parentSetting = (Setting<SettingGroup>) setting;
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hovered = isHovered(mouseX, mouseY);
        
        animation += ((parentSetting.getValue().isExtended() ? 0f : 1f) - animation) * 0.1f;
        
        context.drawTextWithShadow(mc.textRenderer, setting.getName(),
            (int)getSettingNameX(), (int)(y + height / 2f - 3f), Color.WHITE.getRGB());
        
        String arrow = parentSetting.getValue().isExtended() ? "▼" : "▶";
        context.drawTextWithShadow(mc.textRenderer, arrow,
            (int)(x + width - 14f), (int)(y + height / 2f - 3f), Color.GRAY.getRGB());
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (hovered) {
            parentSetting.getValue().setExtended(!parentSetting.getValue().isExtended());
        }
        super.mouseClicked(mouseX, mouseY, button);
    }
    
    private boolean isHovered(int mx, int my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }
                                          }
