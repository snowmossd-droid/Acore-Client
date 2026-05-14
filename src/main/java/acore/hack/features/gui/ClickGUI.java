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
    
    // Friend Manager variables
    private String newFriendName = "";
    private boolean isTypingFriend = false;
    private String currentInput = "";
    
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
    private static final int BUTTON_ADD = 0xFF00AA00;
    private static final int BUTTON_REMOVE = 0xFFAA0000;
    private static final int INPUT_BG = 0xFF1A1A1A;
    
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
            if (x + 120 > mc.getWindow().getScaledWidth() - 160) {
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
        
        // Render Friend Manager Panel
        renderFriendManager(context, mouseX, mouseY);
        
        // Keybind popup
        if (isBinding && bindingModule != null) {
            renderKeybindPopup(context);
        }
        
        // Friend input popup (if typing)
        if (isTypingFriend) {
            renderFriendInputPopup(context);
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    // ==================== FRIEND MANAGER PANEL ====================
    
    private void renderFriendManager(DrawContext context, int mouseX, int mouseY) {
        int panelX = width - 160;
        int panelY = 40;
        int panelWidth = 150;
        
        // Panel Header
        context.fill(panelX, panelY, panelX + panelWidth, panelY + 20, PANEL_HEADER);
        context.drawBorder(panelX, panelY, panelWidth, 20, BORDER);
        context.drawText(textRenderer, "§lFriend Manager", panelX + 5, panelY + 6, TEXT, true);
        
        int yOffset = panelY + 22;
        
        // Friend list
        List<String> friends = FriendManager.getFriends();
        
        if (friends.isEmpty()) {
            context.drawText(textRenderer, "§7No friends added", panelX + 5, yOffset + 5, TEXT_DIM, true);
            yOffset += 20;
        } else {
            for (String friend : friends) {
                // Friend entry background
                boolean hover = mouseX >= panelX && mouseX <= panelX + panelWidth - 25 && 
                               mouseY >= yOffset && mouseY <= yOffset + 16;
                int bgColor = hover ? MODULE_HOVER : PANEL_BG;
                context.fill(panelX, yOffset, panelX + panelWidth - 25, yOffset + 16, bgColor);
                
                // Friend name
                String displayName = friend.length() > 12 ? friend.substring(0, 10) + ".." : friend;
                context.drawText(textRenderer, displayName, panelX + 3, yOffset + 4, TEXT, true);
                
                // Remove button
                boolean removeHover = mouseX >= panelX + panelWidth - 23 && mouseX <= panelX + panelWidth - 3 &&
                                     mouseY >= yOffset && mouseY <= yOffset + 16;
                int removeColor = removeHover ? 0xFFCC0000 : BUTTON_REMOVE;
                context.fill(panelX + panelWidth - 23, yOffset, panelX + panelWidth - 3, yOffset + 16, removeColor);
                context.drawBorder(panelX + panelWidth - 23, yOffset, 20, 16, BORDER);
                context.drawText(textRenderer, "X", panelX + panelWidth - 17, yOffset + 4, TEXT, true);
                
                yOffset += 18;
            }
        }
        
        // Add friend input field
        context.fill(panelX, yOffset, panelX + panelWidth - 55, yOffset + 18, INPUT_BG);
        context.drawBorder(panelX, yOffset, panelWidth - 55, 18, BORDER);
        
        // Show input text or placeholder
        if (newFriendName.isEmpty()) {
            context.drawText(textRenderer, "§7Enter name...", panelX + 3, yOffset + 5, TEXT_DIM, true);
        } else {
            String displayInput = newFriendName.length() > 12 ? newFriendName.substring(0, 10) + ".." : newFriendName;
            context.drawText(textRenderer, displayInput, panelX + 3, yOffset + 5, TEXT, true);
        }
        
        // Add button
        boolean addHover = mouseX >= panelX + panelWidth - 53 && mouseX <= panelX + panelWidth - 3 &&
                          mouseY >= yOffset && mouseY <= yOffset + 18;
        int addColor = addHover ? 0xFF00CC00 : BUTTON_ADD;
        context.fill(panelX + panelWidth - 53, yOffset, panelX + panelWidth - 3, yOffset + 18, addColor);
        context.drawBorder(panelX + panelWidth - 53, yOffset, 50, 18, BORDER);
        context.drawText(textRenderer, "Add", panelX + panelWidth - 43, yOffset + 5, TEXT, true);
    }
    
    // ==================== FRIEND INPUT POPUP ====================
    
    private void renderFriendInputPopup(DrawContext context) {
        // Background overlay
        context.fill(0, 0, width, height, new Color(0, 0, 0, 180).getRGB());
        
        // Popup box
        int boxWidth = 250;
        int boxHeight = 120;
        int boxX = width / 2 - boxWidth / 2;
        int boxY = height / 2 - boxHeight / 2;
        
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, PANEL_HEADER);
        context.drawBorder(boxX, boxY, boxWidth, boxHeight, BORDER);
        
        context.drawText(textRenderer, "§lAdd Friend", boxX + 10, boxY + 15, TEXT, true);
        context.drawText(textRenderer, "§7Enter player name:", boxX + 10, boxY + 35, TEXT_DIM, true);
        
        // Input field
        context.fill(boxX + 10, boxY + 50, boxX + boxWidth - 10, boxY + 70, INPUT_BG);
        context.drawBorder(boxX + 10, boxY + 50, boxWidth - 20, 20, BORDER);
        
        String displayInput = currentInput.isEmpty() ? "§7Type name here..." : currentInput;
        context.drawText(textRenderer, displayInput, boxX + 15, boxY + 58, 
            currentInput.isEmpty() ? TEXT_DIM : TEXT, true);
        
        // Blinking cursor
        if ((System.currentTimeMillis() / 500) % 2 == 0 && !currentInput.isEmpty()) {
            int cursorX = boxX + 15 + textRenderer.getWidth(currentInput);
            context.fill(cursorX, boxY + 53, cursorX + 2, boxY + 68, TEXT);
        }
        
        // Cancel button
        context.fill(boxX + 10, boxY + 80, boxX + 110, boxY + 100, 0xFF555555);
        context.drawBorder(boxX + 10, boxY + 80, 100, 20, BORDER);
        context.drawText(textRenderer, "Cancel", boxX + 45, boxY + 86, TEXT, true);
        
        // Confirm button
        context.fill(boxX + 130, boxY + 80, boxX + boxWidth - 10, boxY + 100, BUTTON_ADD);
        context.drawBorder(boxX + 130, boxY + 80, boxWidth - 140, 20, BORDER);
        context.drawText(textRenderer, "Add", boxX + 170, boxY + 86, TEXT, true);
    }
    
    // ==================== KEYBIND POPUP ====================
    
    private void renderKeybindPopup(DrawContext context) {
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
    
    // ==================== MOUSE CLICK HANDLER ====================
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isBinding) return true;
        if (isTypingFriend) {
            handleFriendPopupClick(mouseX, mouseY, button);
            return true;
        }
        
        // Check Friend Manager panel clicks
        if (handleFriendManagerClick(mouseX, mouseY, button)) {
            SoundManager.playClickSound();
            return true;
        }
        
        // Check module windows
        for (CategoryWindow window : windows) {
            if (window.mouseClicked(mouseX, mouseY, button)) {
                SoundManager.playClickSound();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    private boolean handleFriendManagerClick(double mouseX, double mouseY, int button) {
        int panelX = width - 160;
        int panelY = 40;
        int panelWidth = 150;
        
        // Check remove buttons
        int yOffset = panelY + 22;
        List<String> friends = FriendManager.getFriends();
        
        for (int i = 0; i < friends.size(); i++) {
            if (mouseX >= panelX + panelWidth - 23 && mouseX <= panelX + panelWidth - 3 &&
                mouseY >= yOffset && mouseY <= yOffset + 16) {
                FriendManager.removeFriend(friends.get(i));
                return true;
            }
            yOffset += 18;
        }
        
        // Check add button
        int addY = yOffset;
        if (mouseX >= panelX + panelWidth - 53 && mouseX <= panelX + panelWidth - 3 &&
            mouseY >= addY && mouseY <= addY + 18) {
            if (!newFriendName.isEmpty()) {
                FriendManager.addFriend(newFriendName);
                newFriendName = "";
            }
            return true;
        }
        
        // Check input field - open popup
        if (mouseX >= panelX && mouseX <= panelX + panelWidth - 55 &&
            mouseY >= addY && mouseY <= addY + 18) {
            isTypingFriend = true;
            currentInput = newFriendName;
            return true;
        }
        
        return false;
    }
    
    private void handleFriendPopupClick(double mouseX, double mouseY, int button) {
        int boxWidth = 250;
        int boxHeight = 120;
        int boxX = width / 2 - boxWidth / 2;
        int boxY = height / 2 - boxHeight / 2;
        
        // Cancel button
        if (mouseX >= boxX + 10 && mouseX <= boxX + 110 &&
            mouseY >= boxY + 80 && mouseY <= boxY + 100) {
            isTypingFriend = false;
            currentInput = "";
            SoundManager.playClickSound();
            return;
        }
        
        // Confirm button
        if (mouseX >= boxX + 130 && mouseX <= boxX + boxWidth - 10 &&
            mouseY >= boxY + 80 && mouseY <= boxY + 100) {
            if (!currentInput.isEmpty()) {
                FriendManager.addFriend(currentInput);
                newFriendName = "";
                currentInput = "";
            }
            isTypingFriend = false;
            SoundManager.playClickSound();
        }
    }
    
    // ==================== KEY HANDLER ====================
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Handle keybind setting
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
        
        // Handle friend name input
        if (isTypingFriend) {
            if (keyCode == 256) { // ESC
                isTypingFriend = false;
                currentInput = "";
                return true;
            }
            if (keyCode == 257 || keyCode == 335) { // ENTER
                if (!currentInput.isEmpty()) {
                    FriendManager.addFriend(currentInput);
                    newFriendName = "";
                    currentInput = "";
                }
                isTypingFriend = false;
                SoundManager.playClickSound();
                return true;
            }
            if (keyCode == 259) { // BACKSPACE
                if (!currentInput.isEmpty()) {
                    currentInput = currentInput.substring(0, currentInput.length() - 1);
                }
                return true;
            }
            // Add character
            String key = getKeyName(keyCode);
            if (key.length() == 1 && Character.isLetterOrDigit(key.charAt(0))) {
                if (modifiers == 1) { // Shift for uppercase
                    currentInput += key;
                } else {
                    currentInput += key.toLowerCase();
                }
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
    
    // ==================== INNER CLASS: CATEGORY WINDOW ====================
    
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
    
    // ==================== INNER CLASS: MODULE BUTTON ====================
    
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