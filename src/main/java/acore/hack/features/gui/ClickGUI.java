package acore.hack.features.gui;

import acore.hack.core.manager.ModuleManager;
import acore.hack.core.sound.SoundManager;
import acore.hack.features.modules.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ClickGUI extends Screen {
    
    private List<ModuleButton> buttons = new ArrayList<>();
    private int x = 100, y = 50;
    
    public ClickGUI() {
        super(Text.literal("AcoreHack GUI"));
        initButtons();
    }
    
    private void initButtons() {
        int offsetY = 0;
        for (Module.Category category : Module.Category.values()) {
            CategoryPanel panel = new CategoryPanel(category, x, y + offsetY);
            offsetY += 150;
            // Add modules to panel
            for (Module module : ModuleManager.getModulesInCategory(category)) {
                panel.addModule(module);
            }
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, new Color(0, 0, 0, 150).getRGB());
        
        for (CategoryPanel panel : CategoryPanel.panels) {
            panel.render(context, mouseX, mouseY);
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (CategoryPanel panel : CategoryPanel.panels) {
            panel.mouseClicked(mouseX, mouseY, button);
        }
        SoundManager.playClickSound();
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean shouldPause() { return false; }
}
