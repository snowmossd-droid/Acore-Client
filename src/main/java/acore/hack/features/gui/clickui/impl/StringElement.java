package acore.hack.features.gui.clickui.impl;

import acore.hack.features.gui.clickui.AbstractElement;
import acore.hack.setting.Setting;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

public class StringElement extends AbstractElement {
    public boolean listening = false;
    private String currentString = "";
    
    public StringElement(Setting setting) {
        super(setting);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hovered = isHovered(mouseX, mouseY);
        
        context.fill((int)(x + 5f), (int)(y + 2f), (int)(x + width - 6f), (int)(y + 12f), new Color(30, 30, 40).getRGB());
        
        String displayText = listening 
            ? currentString + ((System.currentTimeMillis() / 500 % 2 == 0) ? "_" : "")
            : (String)setting.getValue();
        
        context.drawTextWithShadow(textRenderer, displayText,
            (int)getSettingNameX(), (int)(y + height / 2f), Color.WHITE.getRGB());
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (hovered && button == 0) {
            listening = !listening;
            if (listening) {
                currentString = (String)setting.getValue();
            }
        }
        super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public void charTyped(char key, int keyCode) {
        if (listening && StringHelper.isValidChar(key)) {
            currentString += key;
        }
    }
    
    @Override
    public void keyTyped(int keyCode) {
        if (listening) {
            if (keyCode == 32) {
                currentString += " ";
            } else if (keyCode == 257) {
                setting.setValue(currentString.isEmpty() ? setting.getDefaultValue() : currentString);
                currentString = "";
                listening = false;
            } else if (keyCode == 259 && !currentString.isEmpty()) {
                currentString = currentString.substring(0, currentString.length() - 1);
            }
        }
    }
    
    private boolean isHovered(int mx, int my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }
}