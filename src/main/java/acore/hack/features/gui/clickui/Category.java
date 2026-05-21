package acore.hack.features.gui.clickui;

import acore.hack.features.gui.clickui.impl.SearchBar;
import acore.hack.features.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Category extends AbstractCategory {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final int CAT_HEIGHT = 280;
    private final List<AbstractButton> buttons;
    private float scrollOffset = 0f;
    private float scrollTarget = 0f;
    
    public Category(Module.Category category, ArrayList<Module> modules, float x, float y, float width, float height) {
        super(category.getName(), x, y, width, height);
        this.buttons = new ArrayList<>();
        modules.forEach(module -> this.buttons.add(new ModuleButton(module)));
        setOpen(true);
    }
    
    @Override
    public void init() {
        buttons.forEach(AbstractButton::init);
    }
    
    @Override
    public void setModuleOffset(float offset, int mouseX, int mouseY) {
        if (isHovered(mouseX, mouseY, getX(), getY(), width, CAT_HEIGHT)) {
            scrollTarget -= offset;
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hovered = isHovered(mouseX, mouseY, x, y, width, 25f);
        
        Color bg = new Color(20, 20, 25, (int)(180 * getAlpha()));
        drawRoundedRect(context.getMatrices(), x, y, width, isOpen() ? CAT_HEIGHT : 25f, 12f, bg);
        
        context.drawCenteredTextWithShadow(mc.textRenderer, getName(), (int)(x + width / 2f), (int)(y + 7f), Color.WHITE.getRGB());
        
        if (isOpen()) {
            float maxScroll = (float)Math.max(0, getButtonsHeight() - 250);
            scrollTarget = MathHelper.clamp(scrollTarget, 0f, maxScroll);
            scrollOffset += (scrollTarget - scrollOffset) * 0.1f;
            
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            
            float baseY = y + 25f - scrollOffset;
            for (AbstractButton button : buttons) {
                if (button instanceof ModuleButton mb && !SearchBar.matchesQuery(mb.module.getName())) continue;
                
                button.setX(x + 4f);
                button.setWidth(width - 8f);
                button.setHeight(20f);
                button.setY(baseY);
                
                if (button instanceof ModuleButton mb) {
                    mb.setContentArea(x, y + 25f, width, 250f);
                }
                
                button.render(context, mouseX, mouseY, delta);
                baseY += button.getHeight();
                if (button instanceof ModuleButton mb && mb.isOpen()) {
                    baseY += mb.getElementsHeight();
                }
            }
            
            matrices.pop();
        }
        
        updatePositions();
    }
    
    private void drawRoundedRect(MatrixStack matrices, float x, float y, float w, float h, float r, Color color) {
        matrices.push();
        matrices.translate(x, y, 0);
        matrices.pop();
    }
    
    private boolean isHovered(int mx, int my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (button == 1 && hovered) {
            setOpen(!isOpen());
        }
        
        if (isOpen() && isHovered(mouseX, mouseY, x, y + 25f, width, 250f)) {
            buttons.forEach(b -> b.mouseClicked(mouseX, mouseY, button));
        }
    }
    
    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (isOpen()) {
            buttons.forEach(b -> b.mouseReleased(mouseX, mouseY, button));
        }
    }
    
    @Override
    public boolean keyTyped(int keyCode) {
        if (isOpen()) {
            buttons.forEach(b -> b.keyTyped(keyCode));
        }
        return false;
    }
    
    @Override
    public void charTyped(char key, int keyCode) {
        if (isOpen()) {
            buttons.forEach(b -> b.charTyped(key, keyCode));
        }
    }
    
    @Override
    public void onClose() {
        buttons.forEach(AbstractButton::onGuiClosed);
    }
    
    @Override
    public void tick() {
        buttons.forEach(AbstractButton::tick);
    }
    
    private void updatePositions() {
        float offsetY = 0f;
        for (AbstractButton button : buttons) {
            if (button instanceof ModuleButton mb && !SearchBar.matchesQuery(mb.module.getName())) continue;
            
            button.setTargetOffset(offsetY);
            if (button instanceof ModuleButton mb && mb.isOpen()) {
                offsetY += mb.getTargetElementsHeight();
            }
            offsetY += button.getHeight();
        }
    }
    
    private double getButtonsHeight() {
        double height = 8.0;
        for (AbstractButton button : buttons) {
            if (button instanceof ModuleButton mb && !SearchBar.matchesQuery(mb.module.getName())) continue;
            
            if (button instanceof ModuleButton mb) {
                height += mb.getElementsHeight();
            }
            height += button.getHeight();
        }
        return height;
    }
        }
