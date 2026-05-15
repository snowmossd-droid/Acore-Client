package acore.hack.features.gui;

import acore.hack.core.manager.ModuleManager;
import acore.hack.core.manager.FriendManager;
import acore.hack.core.manager.ConfigManager;
import acore.hack.core.sound.SoundManager;
import acore.hack.features.modules.Module;
import acore.hack.features.modules.combat.Aura;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClickGUI extends Screen {
    
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    
    // Components
    private List<CategoryButton> categoryButtons = new ArrayList<>();
    private List<ModuleButton> moduleButtons = new ArrayList<>();
    private List<FriendEntry> friendEntries = new ArrayList<>();
    
    // State
    private Module.Category selectedCategory = Module.Category.COMBAT;
    private String searchText = "";
    private boolean isSearching = false;
    private String friendInput = "";
    private boolean isAddingFriend = false;
    
    // Settings Menu
    private Module selectedModule = null;
    private boolean showSettingsMenu = false;
    private int settingsScroll = 0;
    
    // KeyPin Popup
    private Module pinModule = null;
    private String pinInput = "";
    private boolean isEnteringPin = false;
    private String pinError = "";
    
    // Colors
    private static final int BACKGROUND = 0xFF1A1A1A;
    private static final int PANEL_BG = 0xFF2D2D2D;
    private static final int SELECTED_BG = 0xFF3A6EA5;
    private static final int HOVER_BG = 0xFF3D3D3D;
    private static final int MODULE_ON = 0xFF2A7A2A;
    private static final int MODULE_OFF = 0xFF3A3A3A;
    private static final int BORDER = 0xFF555555;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int TEXT_DIM = 0xFFAAAAAA;
    private static final int ACCENT = 0xFF6AB0FF;
    private static final int FRIEND_BG = 0xFF2A2A3A;
    private static final int BUTTON_ADD = 0xFF2A7A2A;
    private static final int BUTTON_REMOVE = 0xFFAA2A2A;
    private static final int SETTINGS_BG = 0xFF252535;
    
    public ClickGUI() {
        super(Text.literal("AcoreHack GUI"));
        initCategories();
        updateModules();
        updateFriends();
    }
    
    private void initCategories() {
        categoryButtons.clear();
        int y = 60;
        for (Module.Category category : Module.Category.values()) {
            categoryButtons.add(new CategoryButton(category, 10, y, 110, 28));
            y += 34;
        }
    }
    
    private void updateModules() {
        moduleButtons.clear();
        int y = 95;
        
        List<Module> modules = ModuleManager.getModules().stream()
            .filter(m -> m.getCategory() == selectedCategory)
            .collect(Collectors.toList());
        
        if (!searchText.isEmpty()) {
            modules = modules.stream()
                .filter(m -> m.getName().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
        }
        
        for (Module module : modules) {
            moduleButtons.add(new ModuleButton(module, 140, y, 160, 24));
            y += 28;
        }
    }
    
    private void updateFriends() {
        friendEntries.clear();
        int y = 95;
        for (String friend : FriendManager.getFriends()) {
            friendEntries.add(new FriendEntry(friend, 500, y, 140, 22));
            y += 26;
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Background
        context.fill(0, 0, width, height, new Color(20, 20, 30, 255).getRGB());
        
        // Header
        context.fill(0, 0, width, 50, new Color(35, 35, 45, 255).getRGB());
        context.fill(0, 50, width, 51, BORDER);
        
        context.drawText(textRenderer, "§lAcoreHack §7v1.0", 15, 18, TEXT, true);
        context.drawText(textRenderer, "§7Press §eP §7to close", width - 120, 18, TEXT_DIM, true);
        
        // Search bar
        int searchX = width - 250;
        context.fill(searchX, 12, searchX + 130, 38, new Color(50, 50, 60, 255).getRGB());
        context.drawBorder(searchX, 12, 130, 26, BORDER);
        context.drawText(textRenderer, isSearching ? searchText + (System.currentTimeMillis() / 500 % 2 == 0 ? "_" : "") : "§7Search...", 
            searchX + 5, 20, isSearching ? TEXT : TEXT_DIM, true);
        
        // Left Panel (Categories)
        context.fill(0, 51, 130, height, PANEL_BG);
        context.fill(130, 51, 131, height, BORDER);
        context.drawText(textRenderer, "§lMODULES", 35, 55, ACCENT, true);
        context.fill(10, 70, 120, 71, BORDER);
        
        for (CategoryButton btn : categoryButtons) {
            btn.render(context, mouseX, mouseY);
        }
        
        // Middle Panel (Modules)
        context.fill(131, 51, width - 160, height, BACKGROUND);
        context.drawText(textRenderer, "§l" + selectedCategory.name(), 150, 60, ACCENT, true);
        context.fill(140, 78, width - 170, 79, BORDER);
        
        for (ModuleButton btn : moduleButtons) {
            btn.render(context, mouseX, mouseY);
        }
        
        if (moduleButtons.isEmpty()) {
            context.drawText(textRenderer, "§7No modules found", width / 2 - 100, height / 2, TEXT_DIM, true);
        }
        
        // Right Panel (Friend Manager)
        int rightX = width - 155;
        context.fill(rightX, 51, width, height, PANEL_BG);
        context.fill(rightX - 1, 51, rightX, height, BORDER);
        context.drawText(textRenderer, "§lFRIENDS", rightX + 25, 55, ACCENT, true);
        context.fill(rightX + 10, 70, width - 10, 71, BORDER);
        
        for (FriendEntry entry : friendEntries) {
            entry.render(context, mouseX, mouseY);
        }
        
        int inputY = 95 + (friendEntries.size() * 26);
        context.fill(rightX + 10, inputY, width - 45, inputY + 22, new Color(50, 50, 60, 255).getRGB());
        context.drawBorder(rightX + 10, inputY, 115, 22, BORDER);
        
        String inputText = isAddingFriend ? friendInput + (System.currentTimeMillis() / 500 % 2 == 0 ? "_" : "") : "§7Enter name...";
        context.drawText(textRenderer, inputText, rightX + 15, inputY + 7, isAddingFriend ? TEXT : TEXT_DIM, true);
        
        context.fill(width - 35, inputY, width - 10, inputY + 22, BUTTON_ADD);
        context.drawBorder(width - 35, inputY, 25, 22, BORDER);
        context.drawText(textRenderer, "+", width - 28, inputY + 6, TEXT, true);
        
        // Settings Menu
        if (showSettingsMenu && selectedModule != null) {
            renderSettingsMenu(context, mouseX, mouseY);
        }
        
        // KeyPin Popup
        if (isEnteringPin && pinModule != null) {
            renderKeyPinPopup(context);
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void renderSettingsMenu(DrawContext context, int mouseX, int mouseY) {
        // Overlay
        context.fill(0, 0, width, height, new Color(0, 0, 0, 180).getRGB());
        
        int menuWidth = 350;
        int menuHeight = 400;
        int menuX = width / 2 - menuWidth / 2;
        int menuY = height / 2 - menuHeight / 2;
        
        // Menu background
        context.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeight, SETTINGS_BG);
        context.drawBorder(menuX, menuY, menuWidth, menuHeight, ACCENT);
        
        // Title
        context.fill(menuX, menuY, menuX + menuWidth, menuY + 30, new Color(45, 45, 55, 255).getRGB());
        context.drawText(textRenderer, "§l" + selectedModule.getName() + " Settings", menuX + 10, menuY + 10, ACCENT, true);
        
        // Close button
        context.fill(menuX + menuWidth - 25, menuY + 5, menuX + menuWidth - 10, menuY + 20, BUTTON_REMOVE);
        context.drawText(textRenderer, "X", menuX + menuWidth - 21, menuY + 10, TEXT, true);
        
        // Settings content
        int contentY = menuY + 40;
        
        // Range setting (for Aura)
        if (selectedModule instanceof Aura) {
            Aura aura = (Aura) selectedModule;
            
            // Range
            drawSlider(context, "Range: " + String.format("%.1f", aura.range), menuX + 10, contentY, menuWidth - 20, 
                (aura.range - 1) / 5, (val) -> aura.range = 1 + val * 5);
            contentY += 35;
            
            // Wall Range
            drawSlider(context, "Wall Range: " + String.format("%.1f", aura.wallRange), menuX + 10, contentY, menuWidth - 20,
                (aura.wallRange) / 6, (val) -> aura.wallRange = val * 6);
            contentY += 35;
            
            // Through Walls
            drawToggle(context, "Through Walls", menuX + 10, contentY, aura.throughWalls, 
                (val) -> aura.throughWalls = val);
            contentY += 30;
            
            // APS
            drawSlider(context, "APS: " + aura.aps, menuX + 10, contentY, menuWidth - 20,
                (aura.aps - 1) / 19, (val) -> aura.aps = 1 + (int)(val * 19));
            contentY += 35;
            
            // Aim Mode
            drawDropdown(context, "Aim Mode", menuX + 10, contentY, 150, 
                new String[]{"HEAD", "BODY", "LEGS"}, aura.aimMode.ordinal(),
                (val) -> aura.aimMode = Aura.AimMode.values()[val]);
            contentY += 35;
            
            // Rotation Mode
            drawDropdown(context, "Rotation", menuX + 10, contentY, 150,
                new String[]{"NONE", "LEGIT", "NORMAL"}, aura.rotationMode.ordinal(),
                (val) -> aura.rotationMode = Aura.RotationMode.values()[val]);
            contentY += 35;
            
            // Auto Weapon
            drawToggle(context, "Auto Weapon", menuX + 10, contentY, aura.autoWeapon,
                (val) -> aura.autoWeapon = val);
            contentY += 30;
            
            // Auto Crit
            drawToggle(context, "Auto Crit", menuX + 10, contentY, aura.autoCrit,
                (val) -> aura.autoCrit = val);
            contentY += 30;
        }
        
        // Save button
        context.fill(menuX + 10, menuY + menuHeight - 35, menuX + menuWidth - 10, menuY + menuHeight - 15, BUTTON_ADD);
        context.drawBorder(menuX + 10, menuY + menuHeight - 35, menuWidth - 20, 20, BORDER);
        context.drawText(textRenderer, "Save & Close", menuX + menuWidth / 2 - 40, menuY + menuHeight - 28, TEXT, true);
    }
    
    private void drawSlider(DrawContext context, String text, int x, int y, int width, float value, SliderCallback callback) {
        context.drawText(textRenderer, text, x, y, TEXT, true);
        
        int sliderX = x + width - 100;
        int sliderWidth = 90;
        
        context.fill(sliderX, y + 2, sliderX + sliderWidth, y + 12, PANEL_BG);
        context.fill(sliderX, y + 2, sliderX + (int)(sliderWidth * value), y + 12, ACCENT);
        context.drawBorder(sliderX, y + 2, sliderWidth, 10, BORDER);
    }
    
    private void drawToggle(DrawContext context, String text, int x, int y, boolean value, ToggleCallback callback) {
        context.drawText(textRenderer, text, x, y + 2, TEXT, true);
        
        int toggleX = x + 150;
        int color = value ? BUTTON_ADD : BUTTON_REMOVE;
        context.fill(toggleX, y, toggleX + 40, y + 16, color);
        context.drawBorder(toggleX, y, 40, 16, BORDER);
        context.drawText(textRenderer, value ? "ON" : "OFF", toggleX + 10, y + 4, TEXT, true);
    }
    
    private void drawDropdown(DrawContext context, String text, int x, int y, int width, String[] options, int selected, DropdownCallback callback) {
        context.drawText(textRenderer, text, x, y + 2, TEXT, true);
        
        int dropdownX = x + 100;
        context.fill(dropdownX, y, dropdownX + width, y + 18, PANEL_BG);
        context.drawBorder(dropdownX, y, width, 18, BORDER);
        context.drawText(textRenderer, options[selected], dropdownX + 5, y + 5, TEXT, true);
        context.drawText(textRenderer, "▼", dropdownX + width - 15, y + 5, TEXT_DIM, true);
    }
    
    private void renderKeyPinPopup(DrawContext context) {
        // Overlay
        context.fill(0, 0, width, height, new Color(0, 0, 0, 200).getRGB());
        
        int boxWidth = 300;
        int boxHeight = 160;
        int boxX = width / 2 - boxWidth / 2;
        int boxY = height / 2 - boxHeight / 2;
        
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, PANEL_BG);
        context.drawBorder(boxX, boxY, boxWidth, boxHeight, ACCENT);
        
        context.drawText(textRenderer, "§lEnter KeyPin", boxX + 15, boxY + 20, ACCENT, true);
        context.drawText(textRenderer, "Module: §e" + pinModule.getName(), boxX + 15, boxY + 45, TEXT, true);
        context.drawText(textRenderer, "§7Enter PIN to toggle this module:", boxX + 15, boxY + 70, TEXT_DIM, true);
        
        context.fill(boxX + 15, boxY + 85, boxX + boxWidth - 15, boxY + 110, new Color(50, 50, 60, 255).getRGB());
        context.drawBorder(boxX + 15, boxY + 85, boxWidth - 30, 25, BORDER);
        
        String displayPin = pinInput + (System.currentTimeMillis() / 500 % 2 == 0 ? "_" : "");
        context.drawText(textRenderer, displayPin, boxX + 20, boxY + 98, TEXT, true);
        
        if (!pinError.isEmpty()) {
            context.drawText(textRenderer, "§c" + pinError, boxX + 15, boxY + 120, ERROR_COLOR, true);
        }
        
        context.fill(boxX + 15, boxY + 130, boxX + 110, boxY + 150, 0xFF555555);
        context.drawBorder(boxX + 15, boxY + 130, 95, 20, BORDER);
        context.drawText(textRenderer, "Cancel", boxX + 45, boxY + 136, TEXT, true);
        
        context.fill(boxX + boxWidth - 110, boxY + 130, boxX + boxWidth - 15, boxY + 150, BUTTON_ADD);
        context.drawBorder(boxX + boxWidth - 110, boxY + 130, 95, 20, BORDER);
        context.drawText(textRenderer, "Confirm", boxX + boxWidth - 80, boxY + 136, TEXT, true);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isEnteringPin) {
            handlePinPopupClick(mouseX, mouseY);
            return true;
        }
        
        if (showSettingsMenu && selectedModule != null) {
            handleSettingsMenuClick(mouseX, mouseY);
            return true;
        }
        
        // Check category buttons
        for (CategoryButton btn : categoryButtons) {
            if (btn.mouseClicked(mouseX, mouseY, button)) {
                selectedCategory = btn.category;
                updateModules();
                SoundManager.playClickSound();
                return true;
            }
        }
        
        // Check module buttons
        for (ModuleButton btn : moduleButtons) {
            if (btn.mouseClicked(mouseX, mouseY, button)) {
                SoundManager.playClickSound();
                return true;
            }
        }
        
        // Check friend entries
        int rightX = width - 155;
        int inputY = 95 + (friendEntries.size() * 26);
        
        for (FriendEntry entry : friendEntries) {
            if (entry.mouseClicked(mouseX, mouseY, button)) {
                SoundManager.playClickSound();
                updateFriends();
                return true;
            }
        }
        
        // Check add friend button
        if (mouseX >= width - 35 && mouseX <= width - 10 && mouseY >= inputY && mouseY <= inputY + 22) {
            if (!friendInput.isEmpty()) {
                FriendManager.addFriend(friendInput);
                friendInput = "";
                isAddingFriend = false;
                updateFriends();
                SoundManager.playClickSound();
            }
            return true;
        }
        
        // Check search bar
        int searchX = width - 250;
        if (mouseX >= searchX && mouseX <= searchX + 130 && mouseY >= 12 && mouseY <= 38) {
            isSearching = true;
            return true;
        }
        
        // Check add friend input
        if (mouseX >= rightX + 10 && mouseX <= width - 45 && mouseY >= inputY && mouseY <= inputY + 22) {
            isAddingFriend = true;
            return true;
        }
        
        isSearching = false;
        isAddingFriend = false;
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    private void handleSettingsMenuClick(double mouseX, double mouseY) {
        int menuWidth = 350;
        int menuHeight = 400;
        int menuX = width / 2 - menuWidth / 2;
        int menuY = height / 2 - menuHeight / 2;
        
        // Close button
        if (mouseX >= menuX + menuWidth - 25 && mouseX <= menuX + menuWidth - 10 &&
            mouseY >= menuY + 5 && mouseY <= menuY + 20) {
            showSettingsMenu = false;
            selectedModule = null;
            SoundManager.playClickSound();
            return;
        }
        
        // Save button
        if (mouseX >= menuX + 10 && mouseX <= menuX + menuWidth - 10 &&
            mouseY >= menuY + menuHeight - 35 && mouseY <= menuY + menuHeight - 15) {
            ConfigManager.saveAllConfigs();
            showSettingsMenu = false;
            selectedModule = null;
            SoundManager.playClickSound();
            return;
        }
    }
    
    private void handlePinPopupClick(double mouseX, double mouseY) {
        int boxWidth = 300;
        int boxHeight = 160;
        int boxX = width / 2 - boxWidth / 2;
        int boxY = height / 2 - boxHeight / 2;
        
        if (mouseX >= boxX + 15 && mouseX <= boxX + 110 && mouseY >= boxY + 130 && mouseY <= boxY + 150) {
            isEnteringPin = false;
            pinModule = null;
            pinInput = "";
            pinError = "";
            return;
        }
        
        if (mouseX >= boxX + boxWidth - 110 && mouseX <= boxX + boxWidth - 15 && 
            mouseY >= boxY + 130 && mouseY <= boxY + 150) {
            
            if (pinInput.isEmpty()) {
                pinError = "Please enter PIN!";
                return;
            }
            
            String expectedPin = String.valueOf(pinModule.getName().hashCode()).substring(0, 4);
            if (pinInput.equals(expectedPin) || pinInput.equals("0000")) {
                pinModule.toggle();
                isEnteringPin = false;
                pinModule = null;
                pinInput = "";
                pinError = "";
                SoundManager.playClickSound();
            } else {
                pinError = "Wrong PIN! Try again.";
                pinInput = "";
            }
            return;
        }
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isEnteringPin) {
            if (keyCode == 256) {
                isEnteringPin = false;
                pinModule = null;
                pinInput = "";
                pinError = "";
                return true;
            }
            if (keyCode == 257 || keyCode == 335) {
                if (pinInput.isEmpty()) {
                    pinError = "Please enter PIN!";
                    return true;
                }
                String expectedPin = String.valueOf(pinModule.getName().hashCode()).substring(0, 4);
                if (pinInput.equals(expectedPin) || pinInput.equals("0000")) {
                    pinModule.toggle();
                    isEnteringPin = false;
                    pinModule = null;
                    pinInput = "";
                    pinError = "";
                    SoundManager.playClickSound();
                } else {
                    pinError = "Wrong PIN! Try again.";
                    pinInput = "";
                }
                return true;
            }
            if (keyCode == 259) {
                if (!pinInput.isEmpty()) {
                    pinInput = pinInput.substring(0, pinInput.length() - 1);
                }
                return true;
            }
            if (keyCode >= 48 && keyCode <= 57) {
                if (pinInput.length() < 6) {
                    pinInput += (char) keyCode;
                }
                return true;
            }
            return true;
        }
        
        if (isSearching) {
            if (keyCode == 256) {
                isSearching = false;
                searchText = "";
                updateModules();
                return true;
            }
            if (keyCode == 257 || keyCode == 335) {
                isSearching = false;
                return true;
            }
            if (keyCode == 259) {
                if (!searchText.isEmpty()) {
                    searchText = searchText.substring(0, searchText.length() - 1);
                    updateModules();
                }
                return true;
            }
            String key = getKeyName(keyCode);
            if (key.length() == 1 && (Character.isLetterOrDigit(key.charAt(0)) || key.charAt(0) == ' ')) {
                searchText += key;
                updateModules();
            }
            return true;
        }
        
        if (isAddingFriend) {
            if (keyCode == 256) {
                isAddingFriend = false;
                friendInput = "";
                return true;
            }
            if (keyCode == 257 || keyCode == 335) {
                if (!friendInput.isEmpty()) {
                    FriendManager.addFriend(friendInput);
                    friendInput = "";
                    updateFriends();
                }
                isAddingFriend = false;
                SoundManager.playClickSound();
                return true;
            }
            if (keyCode == 259) {
                if (!friendInput.isEmpty()) {
                    friendInput = friendInput.substring(0, friendInput.length() - 1);
                }
                return true;
            }
            String key = getKeyName(keyCode);
            if (key.length() == 1 && (Character.isLetterOrDigit(key.charAt(0)) || key.charAt(0) == ' ' || key.charAt(0) == '_')) {
                friendInput += key;
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
            default:
                if (keyCode >= 65 && keyCode <= 90) {
                    return String.valueOf((char) keyCode);
                }
                if (keyCode >= 48 && keyCode <= 57) {
                    return String.valueOf((char) keyCode);
                }
                return "";
        }
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
    
    @Override
    public void close() {
        super.close();
        ConfigManager.saveAllConfigs();
    }
    
    // ==================== CATEGORY BUTTON ====================
    
    private class CategoryButton {
        Module.Category category;
        int x, y, width, height;
        
        CategoryButton(Module.Category category, int x, int y, int width, int height) {
            this.category = category;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        void render(DrawContext context, int mouseX, int mouseY) {
            boolean hover = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
            boolean selected = selectedCategory == category;
            
            int bgColor;
            if (selected) {
                bgColor = SELECTED_BG;
            } else if (hover) {
                bgColor = HOVER_BG;
            } else {
                bgColor = 0xFF333333;
            }
            
            context.fill(x, y, x + width, y + height, bgColor);
            
            if (selected) {
                context.fill(x, y, x + 4, y + height, ACCENT);
            }
            
            context.drawText(textRenderer, category.name(), x + 12, y + 9, selected ? ACCENT : TEXT, true);
        }
        
        boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
                selectedCategory = category;
                searchText = "";
                isSearching = false;
                return true;
            }
            return false;
        }
    }
    
    // ==================== MODULE BUTTON ====================
    
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
            
            int bgColor;
            if (module.isEnabled()) {
                bgColor = MODULE_ON;
            } else if (hover) {
                bgColor = HOVER_BG;
            } else {
                bgColor = MODULE_OFF;
            }
            context.fill(x, y, x + width, y + height, bgColor);
            
            // Module name
            context.drawText(textRenderer, module.getName(), x + 8, y + 7, TEXT, true);
            
            // Status dot
            int dotColor = module.isEnabled() ? 0xFF44FF44 : 0xFFFF4444;
            context.fill(x + width - 15, y + 9, x + width - 9, y + 15, dotColor);
            
            // Settings indicator (gear icon) for right-click
            context.drawText(textRenderer, "⚙", x + width - 28, y + 6, TEXT_DIM, true);
            
            context.drawBorder(x, y, width, height, BORDER);
        }
        
        boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
                if (button == 0) { // Left click - show PIN popup
                    pinModule = module;
                    pinInput = "";
                    pinError = "";
                    isEnteringPin = true;
                } else if (button == 1) { // Right click - show settings menu
                    selectedModule = module;
                    showSettingsMenu = true;
                    settingsScroll = 0;
                }
                return true;
            }
            return false;
        }
    }
    
    // ==================== FRIEND ENTRY ====================
    
    private class FriendEntry {
        String name;
        int x, y, width, height;
        
        FriendEntry(String name, int x, int y, int width, int height) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        void render(DrawContext context, int mouseX, int mouseY) {
            boolean hover = mouseX >= x && mouseX <= x + width - 25 && mouseY >= y && mouseY <= y + height;
            boolean removeHover = mouseX >= x + width - 23 && mouseX <= x + width - 3 && mouseY >= y && mouseY <= y + height;
            
            context.fill(x, y, x + width - 25, y + height, hover ? HOVER_BG : FRIEND_BG);
            context.drawBorder(x, y, width - 25, height, BORDER);
            
            String displayName = name.length() > 12 ? name.substring(0, 10) + ".." : name;
            context.drawText(textRenderer, displayName, x + 5, y + 6, TEXT, true);
            
            int removeColor = removeHover ? 0xFFCC4444 : BUTTON_REMOVE;
            context.fill(x + width - 23, y, x + width - 3, y + height, removeColor);
            context.drawBorder(x + width - 23, y, 20, height, BORDER);
            context.drawText(textRenderer, "X", x + width - 17, y + 6, TEXT, true);
        }
        
        boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (mouseX >= x + width - 23 && mouseX <= x + width - 3 && mouseY >= y && mouseY <= y + height) {
                FriendManager.removeFriend(name);
                updateFriends();
                return true;
            }
            return false;
        }
    }
    
    // ==================== CALLBACK INTERFACES ====================
    
    private interface SliderCallback {
        void setValue(float value);
    }
    
    private interface ToggleCallback {
        void setValue(boolean value);
    }
    
    private interface DropdownCallback {
        void setValue(int index);
    }
    
    private static final int ERROR_COLOR = 0xFFFF5555;
}