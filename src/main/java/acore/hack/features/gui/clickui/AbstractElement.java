package acore.hack.features.gui.clickui;

import acore.hack.features.modules.misc.ClickGui;
import acore.hack.setting.Setting;
import acore.hack.setting.impl.ColorSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;

public abstract class AbstractElement {
    protected Setting setting;
    protected float x;
    protected float y;
    protected float width;
    protected float height;
    protected float offsetY;
    protected boolean hovered;
    
    public AbstractElement(Setting setting) {
        this.setting = setting;
    }
    
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {}
    public void init() {}
    
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 261) && button == 2 && hovered) {
            if (setting.getValue() instanceof ColorSetting cs) {
                cs.setDefault();
            } else {
                setting.setValue(setting.getDefaultValue());
            }
        }
    }
    
    public void mouseReleased(int mouseX, int mouseY, int button) {}
    public void keyTyped(int keyCode) {}
    public void onClose() {}
    public void charTyped(char key, int keyCode) {}
    
    public Setting getSetting() { return setting; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y + offsetY; }
    public void setWidth(float width) { this.width = width; }
    public void setHeight(float height) { this.height = height; }
    public void setOffsetY(float offsetY) { this.offsetY = offsetY; }
    public boolean isVisible() { return setting.isVisible(); }
    
    protected float getSettingNameX() {
        return x + 6f;
    }
}