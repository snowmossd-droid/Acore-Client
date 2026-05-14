package acore.hack.features.gui;

import acore.hack.core.manager.ConfigManager;
import acore.hack.core.sound.SoundManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AcoreGui extends Screen {
    
    private List<ConfigButton> configButtons = new ArrayList<>();
    private String currentConfig = ConfigManager.getCurrentConfig();
    
    public AcoreGui() {
        super(Text.literal("AcoreHack Config Manager"));
        loadConfigs();
    }
    
    private void loadConfigs() {
        configButtons.clear();
        File configDir = new File("acore_configs");
        if (!configDir.exists()) configDir.mkdir();
        
        File[] files = configDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String name = files[i].getName().replace(".json", "");
                boolean isActive = name.equals(currentConfig);
                configButtons.add(new ConfigButton(name, 50, 50 + i * 35, 150, 25, isActive));
            }
        }
        
        // Add new config button
        configButtons.add(new ConfigButton("+ NEW CONFIG", 50, 50 + configButtons.size() * 35, 150, 25, false));
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, new Color(20, 20, 30, 255).getRGB());
        
        // Title
        context.drawText(textRenderer, "AcoreHack Configurations", width / 2 - 100, 20, 0xFFFFFF, true);
        context.drawText(textRenderer, "Current: " + currentConfig, width / 2 - 80, 40, 0xAAAAFF, true);
        
        // Render buttons
        for (ConfigButton btn : configButtons) {
            // Draw button background
            int color = btn.isActive ? new Color(80, 80, 200).getRGB() : new Color(40, 40, 50).getRGB();
            context.fill(btn.x, btn.y, btn.x + btn.width, btn.y + btn.height, color);
            
            // Draw border for active config
            if (btn.isActive) {
                context.drawBorder(btn.x, btn.y, btn.width, btn.height, new Color(255, 255, 100).getRGB());
            }
            
            // Draw button text
            context.drawText(textRenderer, btn.name, btn.x + 5, btn.y + 8, 0xFFFFFF, true);
            
            // Check hover
            if (mouseX >= btn.x && mouseX <= btn.x + btn.width && mouseY >= btn.y && mouseY <= btn.y + btn.height) {
                context.fill(btn.x, btn.y, btn.x + btn.width, btn.y + btn.height, new Color(255, 255, 255, 50).getRGB());
            }
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (ConfigButton btn : configButtons) {
            if (mouseX >= btn.x && mouseX <= btn.x + btn.width && mouseY >= btn.y && mouseY <= btn.y + btn.height) {
                if (btn.name.equals("+ NEW CONFIG")) {
                    // Show popup to enter new config name
                    handleNewConfig();
                } else {
                    ConfigManager.switchConfig(btn.name);
                    currentConfig = btn.name;
                    loadConfigs();
                    SoundManager.playClickSound();
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    private void handleNewConfig() {
        // Simple implementation - you can add a text input popup
        System.out.println("Create new config - implement text input popup");
    }
    
    @Override
    public boolean shouldPause() { return false; }
    
    private static class ConfigButton {
        String name;
        int x, y, width, height;
        boolean isActive;
        
        ConfigButton(String name, int x, int y, int width, int height, boolean isActive) {
            this.name = name;
            this.x = x; this.y = y; this.width = width; this.height = height;
            this.isActive = isActive;
        }
    }
                }
