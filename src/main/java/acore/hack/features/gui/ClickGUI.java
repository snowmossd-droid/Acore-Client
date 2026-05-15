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
import java.util.stream.Collectors;

public class ClickGUI extends Screen {
    
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    
    // Components
    private List<CategoryButton> categoryButtons = new ArrayList<>();
    private List<ModuleButton> moduleButtons = new ArrayList<>();
    private List<FriendEntry> friendEntries = new ArrayList<>();
    
    // State
    private Module.Category selectedCategory = Module.Category.COMBAT;
    private Module bindingModule = null;
    private boolean isBinding = false;
    private String searchText = "";
    private boolean isSearching = false;
    private String friendInput = "";
    private boolean isAddingFriend = false;
    
    // Animation
    private float alpha = 0;
    private float hoverAnim = 0;
    
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
        // Animate alpha
        alpha += (0.95f - alpha) * 0.1f;
        
        // Main background
        context.fill(0, 0, width, height, new Color(20, 20, 30, (int)(255 * alpha)).getRGB());
        
        // ==================== HEADER ====================
        context.fill(0, 0, width, 50, new Color(35, 35, 45, 255).getRGB());
        context.fill(0, 50, width, 51, BORDER);
        
        // Logo
        context.drawText(textRenderer, "§lAcoreHack §7v1.0", 15, 18, TEXT, true);
        
        // Close instruction
        context.drawText(textRenderer, "§7Press §eP §7to close", width - 120, 18, TEXT_DIM, true);
        
        // Search bar
        int searchX = width - 250;
        context.fill(searchX, 12, searchX + 130, 38, new Color(50, 50, 60, 255).getRGB());
        context.drawBorder(searchX, 12, 130, 26, BORDER);
        context.drawText(textRenderer, isSearching ? searchText + (System.currentTimeMillis() / 500 % 2 == 0 ? "_" : "") : "§7Search...", 
            searchX + 5, 20, isSearching ? TEXT : TEXT_DIM, true);
        
        // ==================== LEFT PANEL (Categories) ====================
        context.fill(0, 51, 130, height, PANEL_BG);
        context.fill(130, 51, 131, height, BORDER);
        
        // Panel title
        context.drawText(textRenderer, "§lMODULES", 35, 55, ACCENT, true);
        context.fill(10, 70, 120, 71, BORDER);
        
        for (CategoryButton btn : categoryButtons) {
            btn.render(context, mouseX, mouseY);
        }
        
        // ==================== MIDDLE PANEL (Modules) ====================
        context.fill(131, 51, width - 160, height, BACKGROUND);
        
        // Category title
        context.drawText(textRenderer, "§l" + selectedCategory.name().toUpperCase(), 150, 60, ACCENT, true);
        context.fill(140, 78, width - 170, 79, BORDER);
        
        for (ModuleButton btn : moduleButtons) {
            btn.render(context, mouseX, mouseY);
        }
        
        // No modules message
        if (moduleButtons.isEmpty()) {
            context.drawText(textRenderer, "§7No modules found", width / 2 - 100, height / 2, TEXT_DIM, true);
        }
        
        // ==================== RIGHT PANEL (Friend Manager) ====================
        int rightX = width - 155;
        context.fill(rightX, 51, width, height, PANEL_BG);
        context.fill(rightX - 1, 51, rightX, height, BORDER);
        
        // Friend panel title
        context.drawText(textRenderer, "§lFRIENDS", rightX + 25, 55, ACCENT, true);
        context.fill(rightX + 10, 70, width - 10, 71, BORDER);
        
        // Friend list
        for (FriendEntry entry : friendEntries) {
            entry.render(context, mouseX, mouseY);
        }
        
        // Add friend input
        int inputY = 95 + (friendEntries.size() * 26);
        context.fill(rightX + 10, inputY, width - 10, inputY + 22, new Color(50, 50, 60, 255).getRGB());
        context.drawBorder(rightX + 10, inputY, 115, 22, BORDER);
        
        String inputText = isAddingFriend ? friendInput + (System.currentTimeMillis() / 500 % 2 == 0 ? "_" : "") : "§7Enter name...";
        context.drawText(textRenderer, inputText, rightX + 15, inputY + 7, isAddingFriend ? TEXT : TEXT_DIM, true);
        
        // Add button
        context.fill(width - 35, inputY, width - 10, inputY + 22, BUTTON_ADD);
        context.drawBorder(width - 35, inputY, 25, 22, BORDER);
        context.drawText(textRenderer, "+", width - 28, inputY + 6, TEXT, true);
        
        // ==================== KEYBIND POPUP ====================
        if (isBinding && bindingModule != null) {
            renderKeybindPopup(context);
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void renderKeybindPopup(DrawContext context) {
        // Overlay
        context.fill(0, 0, width, height, new Color(0, 0, 0, 180).getRGB());
        
        // Popup box
        int boxWidth = 280;
        int boxHeight = 120;
        int boxX = width / 2 - boxWidth / 2;
        int boxY = height / 2 - boxHeight / 2;
        
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, PANEL_BG);
        context.drawBorder(boxX, boxY, boxWidth, boxHeight, ACCENT);
        
        context.drawText(textRenderer, "§lSet Keybind", boxX + 15, boxY + 20, ACCENT, true);
        context.drawText(textRenderer, "Module: §e" + bindingModule.getName(), boxX + 15, boxY + 40, TEXT, true);
        context.drawText(textRenderer, "§7Press any key...", boxX + 15, boxY + 65, TEXT_DIM, true);
        context.drawText(textRenderer, "§7Press §eESC §7to cancel", boxX + 15, boxY + 90, TEXT_DIM, true);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isBinding) return true;
        
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
        if (mouseX >= rightX + 10 && mouseX <= width - 35 && mouseY >= inputY && mouseY <= inputY + 22) {
            isAddingFriend = true;
            return true;
        }
        
        isSearching = false;
        isAddingFriend = false;
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
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
        
        // Handle search input
        if (isSearching) {
            if (keyCode == 256) { // ESC
                isSearching = false;
                searchText = "";
                updateModules();
                return true;
            }
            if (keyCode == 257 || keyCode == 335) { // ENTER
                isSearching = false;
                return true;
            }
            if (keyCode == 259) { // BACKSPACE
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
        
        // Handle friend input
        if (isAddingFriend) {
            if (keyCode == 256) { // ESC
                isAddingFriend = false;
                friendInput = "";
                return true;
            }
            if (keyCode == 257 || keyCode == 335) { // ENTER
                if (!friendInput.isEmpty()) {
                    FriendManager.addFriend(friendInput);
                    friendInput = "";
                    updateFriends();
                }
                isAddingFriend = false;
                SoundManager.playClickSound();
                return true;
            }
            if (keyCode == 259) { // BACKSPACE
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
            case 262: return "RIGHT";
            case 263: return "LEFT";
            case 264: return "DOWN";
            case 265: return "UP";
            case 266: return "PAGE_UP";
            case 267: return "PAGE_DOWN";
            case 268: return "HOME";
            case 269: return "END";
            case 290: return "CAPS";
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
        FriendManager.saveFriends();
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
            
            // Keybind
            String keybind = module.getKeybind();
            if (keybind != null && !keybind.equals("None") && !keybind.equals("-1")) {
                String displayKey = keybind.length() > 5 ? keybind.substring(0, 4) : keybind;
                context.drawText(textRenderer, "§7[" + displayKey + "]", x + width - 50, y + 7, TEXT_DIM, true);
            }
            
            context.drawBorder(x, y, width, height, BORDER);
        }
        
        boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
                if (button == 0) {
                    module.toggle();
                } else if (button == 1) {
                    bindingModule = module;
                    isBinding = true;
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
            
            // Remove button
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
}