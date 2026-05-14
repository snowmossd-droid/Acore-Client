package acore.hack.features.gui;

import acore.hack.core.manager.ModuleManager;
import acore.hack.features.modules.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ClickGUI extends Screen {
    
    private List<CategoryPanel> panels = new ArrayList<>();
    
    public ClickGUI() {
        super(Text.literal("AcoreHack GUI"));
        initPanels();
    }
    
    private void initPanels() {
        int x = 100;
        int y = 50;
        for (Module.Category category : Module.Category.values()) {
            panels.add(new CategoryPanel(category, x, y));
            y += 120;
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, new Color(0, 0, 0, 180).getRGB());
        for (CategoryPanel panel : panels) {
            panel.render(context, mouseX, mouseY);
        }
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (CategoryPanel panel : panels) {
            panel.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean shouldPause() { return false; }
    
    private static class CategoryPanel {
        Module.Category category;
        int x, y;
        boolean expanded = true;
        
        CategoryPanel(Module.Category category, int x, int y) {
            this.category = category;
            this.x = x;
            this.y = y;
        }
        
        void render(DrawContext context, int mouseX, int mouseY) {
            context.fill(x, y, x + 100, y + 20, new Color(30, 30, 40).getRGB());
            context.drawText(mc.textRenderer, category.getDisplayName(), x + 5, y + 6, 0xFFFFFF, true);
        }
        
        void mouseClicked(double mouseX, double mouseY, int button) {
            // Handle click
        }
    }
    }
