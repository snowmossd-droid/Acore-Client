package acore.hack.features.gui.clickui;

import net.minecraft.client.gui.DrawContext;

public class AbstractCategory {
    private String name;
    protected float x;
    protected float y;
    protected float width;
    protected float height;
    protected boolean hovered;
    public boolean dragging;
    public float moduleOffset;
    private boolean open;
    private float alpha = 1f;
    
    public AbstractCategory(String name, float x, float y, float width, float height) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.open = false;
    }
    
    public void init() {}
    public void setModuleOffset(float offset, int mouseX, int mouseY) {}
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {}
    public void mouseClicked(int mouseX, int mouseY, int button) {}
    public void mouseReleased(int mouseX, int mouseY, int button) {}
    public boolean keyTyped(int keyCode) { return true; }
    public void charTyped(char key, int modifier) {}
    public void onClose() {}
    public void tick() {}
    public void hudClicked(Module module) {}
    
    public void setOpen(boolean open) { this.open = open; }
    public String getName() { return name; }
    public boolean isOpen() { return open; }
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public float getWidth() { return width; }
    public void setWidth(float width) { this.width = width; }
    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }
    public void setAlpha(float alpha) { this.alpha = alpha; }
    public float getAlpha() { return alpha; }
}