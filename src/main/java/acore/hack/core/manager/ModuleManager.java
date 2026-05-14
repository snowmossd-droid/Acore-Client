package acore.hack.features.gui;

import acore.hack.core.manager.ModuleManager;
import acore.hack.core.manager.KeybindManager;
import acore.hack.core.sound.SoundManager;
import acore.hack.features.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ClickGUI extends Screen {
    
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private List<CategoryPanel> panels = new ArrayList<>();
    private boolean binding = false;
    private Module bindingModule = null;
    private int mouseX, mouseY;
    
    public ClickGUI() {
        super(Text.literal("AcoreHack GUI"));
        initPanels();
    }
    
    private void initPanels() {
        panels.clear();
        int x = 10;
        int y = 50;
        
        for (Module.Category category : Module.Category.values()) {
            panels.add(new CategoryPanel(category, x, y));
            x += 110;
            if (x + 110 > mc.getWindow().getScaledWidth()) {
                x = 10;
                y += 200;
            }
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        
        // Background
        context.fill(0, 0, width, height, new Color(0, 0, 0, 180).getRGB());
        
        // Title
        context.drawText(textRenderer, "AcoreHack v1.0", 10, 10, 0xFFFFFF, true);
        context.drawText(textRenderer, "Press P to close", 10, 25, 0xAAAAAA, true);
        
        // Render panels
        for (CategoryPanel panel : panels) {
            panel.render(context, mouseX, mouseY);
        }
        
        // Render keybind popup
        if (binding && bindingModule != null) {
            context.fill(width / 2 - 100, height / 2 - 30, width / 2 + 100, height / 2 + 30, new Color(30, 30, 40, 240).getRGB());
            context.drawText(textRenderer, "Press a key for " + bindingModule.getName(), width / 2 - 90, height / 2 - 20, 0xFFFFFF, true);
            context.drawText(textRenderer, "Press ESC to cancel", width / 2 - 90, height / 2 - 5, 0xAAAAAA, true);
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (binding) {
            return true;
        }
        
        for (CategoryPanel panel : panels) {
            if (panel.mouseClicked(mouseX, mouseY, button)) {
                SoundManager.playClickSound();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding && bindingModule != null) {
            if (keyCode == 256) { // ESC
                binding = false;
                bindingModule = null;
            } else {
                String keyName = getKeyName(keyCode);
                bindingModule.setKeybind(keyName);
                bindingModule.setKeyCode(keyCode);
                binding = false;
                SoundManager.playClickSound();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    private String getKeyName(int keyCode) {
        switch (keyCode) {
            case 32: return "SPACE";
            case 256: return "ESC";
            case 257: return "ENTER";
            case 258: return "TAB";
            case 259: return "BACKSPACE";
            case 262: return "RIGHT";
            case 263: return "LEFT";
            case 264: return "DOWN";
            case 265: return "UP";
            default:
                if (keyCode >= 65 && keyCode <= 90) {
                    return String.valueOf((char) keyCode);
                }
                if (keyCode >= 48 && keyCode <= 57) {
                    return String.valueOf((char) keyCode);
                }
                return "KEY_" + keyCode;
        }
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
    
    @Override
    public void close() {
        super.close();
        ModuleManager.saveModules();
    }
    
    // Inner class for category panel
    private class CategoryPanel {
        Module.Category category;
        int x, y;
        boolean expanded = true;
        int width = 100;
        int headerHeight = 20;
        List<ModuleButton> buttons = new ArrayList<>();
        
        CategoryPanel(Module.Category category, int x, int y) {
            this.category = category;
            this.x = x;
            this.y = y;
            loadModules();
        }
        
        private void loadModules() {
            buttons.clear();
            int offsetY = headerHeight + 2;
            for (Module module : ModuleManager.getModulesInCategory(category)) {
                buttons.add(new ModuleButton(module, x + 2, y + offsetY, width - 4, 18));
                offsetY += 20;
            }
        }
        
        void render(DrawContext context, int mouseX, int mouseY) {
            // Header
            context.fill(x, y, x + width, y + headerHeight, new Color(40, 40, 50, 255).getRGB());
            context.drawText(mc.textRenderer, category.name(), x + 5, y + 6, 0xFFFFFF, true);
            
            // Expand/collapse arrow
            String arrow = expanded ? "▼" : "▶";
            context.drawText(mc.textRenderer, arrow, x + width - 15, y + 6, 0xAAAAAA, true);
            
            // Border
            context.drawBorder(x, y, width, headerHeight, 0x555555);
            
            if (expanded) {
                // Background for modules
                int contentHeight = buttons.size() * 20 + 4;
                context.fill(x, y + headerHeight, x + width, y + headerHeight + contentHeight, new Color(25, 25, 35, 255).getRGB());
                context.drawBorder(x, y + headerHeight, width, contentHeight, 0x444444);
                
                // Render buttons
                for (ModuleButton button : buttons) {
                    button.render(context, mouseX, mouseY);
                }
            }
        }
        
        boolean mouseClicked(double mouseX, double mouseY, int button) {
            // Header click (expand/collapse)
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + headerHeight) {
                expanded = !expanded;
                return true;
            }
            
            // Button clicks
            if (expanded) {
                for (ModuleButton moduleButton : buttons) {
                    if (moduleButton.mouseClicked(mouseX, mouseY, button)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
    
    // Inner class for module button
    private class ModuleButton {
        Module module;
        int x, y, width, height;
        
        ModuleButton(Module module, int x, int y, int width, int height) {
            this.module = module;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        void render(DrawContext context, int mouseX, int mouseY) {
            // Background
            Color bgColor;
            if (module.isEnabled()) {
                bgColor = new Color(0, 100, 0, 200);
            } else {
                bgColor = new Color(50, 50, 60, 200);
            }
            context.fill(x, y, x + width, y + height, bgColor.getRGB());
            
            // Hover effect
            if (isHovered(mouseX, mouseY)) {
                context.fill(x, y, x + width, y + height, new Color(255, 255, 255, 30).getRGB());
            }
            
            // Module name
            context.drawText(mc.textRenderer, module.getName(), x + 3, y + 5, 0xFFFFFF, true);
            
            // Keybind
            String keybind = module.getKeybind();
            if (keybind != null && !keybind.equals("None") && !keybind.equals("KEY_-1")) {
                String displayKey = keybind.length() > 6 ? keybind.substring(0, 6) : keybind;
                context.drawText(mc.textRenderer, "[" + displayKey + "]", x + width - 40, y + 5, 0xAAAAAA, true);
            } else {
                context.drawText(mc.textRenderer, "[NONE]", x + width - 40, y + 5, 0x666666, true);
            }
            
            // Border
            context.drawBorder(x, y, width, height, 0x666666);
        }
        
        boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!isHovered(mouseX, mouseY)) return false;
            
            if (button == 0) { // Left click - toggle
                module.toggle();
                SoundManager.playClickSound();
                return true;
            } else if (button == 1) { // Right click - set keybind
                binding = true;
                bindingModule = module;
                SoundManager.playClickSound();
                return true;
            }
            return false;
        }
        
        boolean isHovered(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }
        }
