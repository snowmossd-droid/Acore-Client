package acore.hack.features.gui;

import acore.hack.core.manager.ModuleManager;
import acore.hack.core.manager.FriendManager;
import acore.hack.core.sound.SoundManager;
import acore.hack.features.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ClickGUI extends Screen {
    
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private List<CategoryWindow> windows = new ArrayList<>();
    private Module bindingModule = null;
    private boolean isBinding = false;
    
    // Animation
    private float alpha = 0;
    
    // Colors
    private static final int BACKGROUND = 0xFF1A1A1A;
    private static final int PANEL_BG = 0xFF2D2D2D;
    private static final int PANEL_HEADER = 0xFF3A3A3A;
    private static final int MODULE_OFF = 0xFF2A2A2A;
    private static final int MODULE_ON = 0xFF00AA00;
    private static final int MODULE_HOVER = 0xFF3D3D3D;
    private static final int BORDER = 0xFF555555;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int TEXT_DIM = 0xFFAAAAAA;
    
    public ClickGUI() {
        super(Text.literal("AcoreHack GUI"));
        initWindows();
    }
    
    private void initWindows() {
        int x = 10;
        int y = 40;
        
        for (Module.Category category : Module.Category.values()) {
            List<Module> modules = new ArrayList<>();
            for (Module m : ModuleManager.getModules()) {
                if (m.getCategory() == category) {
                    modules.add(m);
                }
            }
            windows.add(new CategoryWindow(category.name(), x, y, 110, 20, modules));
            x += 120;
            
            // Wrap to next row if needed
            if (x + 120 > mc.getWindow().getScaledWidth()) {
                x = 10;
                y += 250;
            }
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Animate alpha
        alpha += (0.9f - alpha) * 0.1f;
        
        // Background
        context.fill(0, 0, width, height, new Color(0, 0, 0, (int)(180 * alpha)).getRGB());
        
        // Title
        context.drawText(textRenderer, "§lAcoreHack §7v1.0", 10, 10, TEXT, true);
        context.drawText(textRenderer, "§7Press §eP §7to close | §7Click §aON §7- toggle module | §7Right click §e§lKEY §7- set keybind", 
            10, 25, TEXT_DIM, true);
        
        // Render all windows
        for (CategoryWindow window : windows) {
            window.render(context, mouseX, mouseY);
        }
        
        // Keybind popup
        if (isBinding && bindingModule != null) {
            // Background overlay
            context.fill(0, 0, width, height, new Color(0, 0, 0, 180).getRGB());
            
            // Popup box
            int boxWidth = 250;
            int boxHeight = 100;
            int boxX = width / 2 - boxWidth / 2;
            int boxY = height / 2 - boxHeight / 2;
            
            context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, PANEL_HEADER);
            context.drawBorder(boxX, boxY, boxWidth, boxHeight, BORDER);
            
            context.drawText(textRenderer, "§lSet Keybind for §e" + bindingModule.getName(), 
                boxX + 10, boxY + 20, TEXT, true);
            context.drawText(textRenderer, "§7Press any key...", 
                boxX + 10, boxY + 40, TEXT_DIM, true);
            context.drawText(textRenderer, "§7Press §eESC §7to cancel", 
                boxX + 10, boxY + 70, TEXT_DIM, true);
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isBinding) return true;
        
        for (CategoryWindow window : windows) {
            if (window.mouseClicked(mouseX, mouseY, button)) {
                SoundManager.playClickSound();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isBinding && bindingModule != null) {
            if (keyCode == 256) { // ESC
                isBinding = false;
                bindingModule = null;
            } else {
                String keyName = getKeyName(keyCode);
                bindingModule.setKeybind(keyName);
                bindingModule.setKeyCode(keyCode);
                isBinding = false;
                bindingModule = null;
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
            case 266: return "PAGE_UP";
            case 267: return "PAGE_DOWN";
            case 268: return "HOME";
            case 269: return "END";
            case 290: return "CAPS";
            case 340: return "LCONTROL";
            case 341: return "LSHIFT";
            case 342: return "LALT";
            case 344: return "RCONTROL";
            case 345: return "RSHIFT";
            case 346: return "RALT";
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
    
    // ==================== INNER CLASS ====================
    
    private class CategoryWindow {
        String name;
        int x, y, width, height;
        boolean expanded = true;
        List<ModuleButton> buttons;
        
        CategoryWindow(String name, int x, int y, int width, int height, List<Module> modules) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.buttons = new ArrayList<>();
            
            int btnY = y + height + 2;
            for (Module module : modules) {
                buttons.add(new ModuleButton(module, x + 2, btnY, width - 4, 18));
                btnY += 20;
            }
        }
        
        void render(DrawContext context, int mouseX, int mouseY) {
            // Header
            boolean hover = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
            int headerColor = hover ? PANEL_HEADER + 0x111111 : PANEL_HEADER;
            context.fill(x, y, x + width, y + height, headerColor);
            context.drawBorder(x, y, width, height, BORDER);
            
            // Title
            context.drawText(textRenderer, name, x + 5, y + 6, TEXT, true);
            
            // Expand/collapse arrow
            String arrow = expanded ? "▼" : "▶";
            context.drawText(textRenderer, arrow, x + width - 12, y + 6, TEXT_DIM, true);
            
            if (expanded) {
                // Draw modules background
                int contentHeight = buttons.size() * 20;
                if (contentHeight > 0) {
                    context.fill(x, y + height, x + width, y + height + contentHeight + 2, PANEL_BG);
                    context.drawBorder(x, y + height, width, contentHeight + 2, BORDER);
                }
                
                // Draw modules
                for (ModuleButton button : buttons) {
                    button.render(context, mouseX, mouseY);
                }
            }
        }
        
        boolean mouseClicked(double mouseX, double mouseY, int button) {
            // Header click
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
                expanded = !expanded;
                return true;
            }
            
            // Module clicks
            if (expanded) {
                for (ModuleButton moduleBtn : buttons) {
                    if (moduleBtn.mouseClicked(mouseX, mouseY, button)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
    
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
            boolean hover = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
            
            // Background based on state
            int bgColor;
            if (module.isEnabled()) {
                bgColor = MODULE_ON;
            } else if (hover) {
                bgColor = MODULE_HOVER;
            } else {
                bgColor = MODULE_OFF;
            }
            context.fill(x, y, x + width, y + height, bgColor);
            
            // Module name
            context.drawText(textRenderer, module.getName(), x + 3, y + 5, TEXT, true);
            
            // Keybind display
            String keybind = module.getKeybind();
            if (keybind != null && !keybind.equals("None") && !keybind.equals("-1")) {
                String displayKey = keybind.length() > 8 ? keybind.substring(0, 6) + ".." : keybind;
                context.drawText(textRenderer, "§7[" + displayKey + "]", x + width - 45, y + 5, TEXT_DIM, true);
            }
            
            // Border
            context.drawBorder(x, y, width, height, BORDER);
        }
        
        boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
                if (button == 0) { // Left click - toggle
                    module.toggle();
                    SoundManager.playClickSound();
                } else if (button == 1) { // Right click - set keybind
                    bindingModule = module;
                    isBinding = true;
                    SoundManager.playClickSound();
                }
                return true;
            }
            return false;
        }
    }
                           }
