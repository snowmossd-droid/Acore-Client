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

public class ClickGUI extends Screen {
    
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    
    // Main categories
    private List<CategoryItem> mainCategories = new ArrayList<>();
    private int selectedMainCategory = 0;
    
    // Sub categories
    private List<SubCategoryItem> subCategories = new ArrayList<>();
    private int selectedSubCategory = 0;
    
    // Settings list
    private List<SettingItem> settings = new ArrayList<>();
    
    // Search
    private String searchText = "";
    private boolean isSearching = false;
    
    // Keybind
    private Module bindingModule = null;
    private boolean isBinding = false;
    
    // Colors
    private static final int BG = 0xFF1E1E2E;
    private static final int MAIN_CAT_BG = 0xFF15151F;
    private static final int MAIN_CAT_SELECTED = 0xFF2A2A3E;
    private static final int MAIN_CAT_HOVER = 0xFF252535;
    private static final int SUB_CAT_BG = 0xFF1A1A28;
    private static final int SUB_CAT_SELECTED = 0xFF3A6EA5;
    private static final int SETTINGS_BG = 0xFF1E1E2E;
    private static final int SETTING_HOVER = 0xFF2A2A3E;
    private static final int TOGGLE_ON = 0xFF3A6EA5;
    private static final int TOGGLE_OFF = 0xFF3A3A4A;
    private static final int SLIDER_BG = 0xFF2A2A3E;
    private static final int SLIDER_FILL = 0xFF3A6EA5;
    private static final int BORDER = 0xFF2A2A3E;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int TEXT_DIM = 0xFFAAAAAA;
    private static final int TEXT_HINT = 0xFF666666;
    
    public ClickGUI() {
        super(Text.literal("AcoreHack GUI"));
        initMainCategories();
        initSubCategories();
        initSettings();
    }
    
    private void initMainCategories() {
        mainCategories.clear();
        mainCategories.add(new CategoryItem("Pvp", 0));
        mainCategories.add(new CategoryItem("Movement", 1));
        mainCategories.add(new CategoryItem("Visual", 2));
        mainCategories.add(new CategoryItem("Utility", 3));
    }
    
    private void initSubCategories() {
        subCategories.clear();
        switch (selectedMainCategory) {
            case 0: // Pvp
                subCategories.add(new SubCategoryItem("Attack", 0));
                subCategories.add(new SubCategoryItem("Legit", 1));
                subCategories.add(new SubCategoryItem("Protect", 2));
                subCategories.add(new SubCategoryItem("Basic", 3));
                subCategories.add(new SubCategoryItem("Rage", 4));
                subCategories.add(new SubCategoryItem("Cosmetic", 5));
                subCategories.add(new SubCategoryItem("Esp", 6));
                break;
            case 1: // Movement
                subCategories.add(new SubCategoryItem("Basic", 0));
                subCategories.add(new SubCategoryItem("World", 1));
                subCategories.add(new SubCategoryItem("Equip", 2));
                subCategories.add(new SubCategoryItem("Player", 3));
                subCategories.add(new SubCategoryItem("Misc", 4));
                break;
            case 2: // Visual
                subCategories.add(new SubCategoryItem("Cosmetic", 0));
                subCategories.add(new SubCategoryItem("Rage", 1));
                subCategories.add(new SubCategoryItem("Hud", 2));
                subCategories.add(new SubCategoryItem("World", 3));
                break;
            case 3: // Utility
                subCategories.add(new SubCategoryItem("World", 0));
                subCategories.add(new SubCategoryItem("Equip", 1));
                subCategories.add(new SubCategoryItem("Player", 2));
                subCategories.add(new SubCategoryItem("Misc", 3));
                break;
        }
    }
    
    private void initSettings() {
        settings.clear();
        
        // Aura settings (Attack sub-category)
        if (selectedMainCategory == 0 && selectedSubCategory == 0) {
            Aura aura = (Aura) ModuleManager.getModule("Aura");
            if (aura != null) {
                settings.add(new SettingItem("Range", aura.range, 1f, 6f, SettingType.SLIDER));
                settings.add(new SettingItem("Ignore Walls", aura.throughWalls, SettingType.TOGGLE));
                settings.add(new SettingItem("Rotation Mode", aura.rotationMode.name(), new String[]{"NONE", "LEGIT", "NORMAL"}, SettingType.MODE));
                settings.add(new SettingItem("Aim Mode", aura.aimMode.name(), new String[]{"HEAD", "BODY", "LEGS"}, SettingType.MODE));
                settings.add(new SettingItem("Auto Weapon", aura.autoWeapon, SettingType.TOGGLE));
                settings.add(new SettingItem("Auto Crit", aura.autoCrit, SettingType.TOGGLE));
                settings.add(new SettingItem("Only Weapon", aura.onlyWeapon, SettingType.TOGGLE));
                settings.add(new SettingItem("Shield Breaker", aura.shieldBreaker, SettingType.TOGGLE));
                settings.add(new SettingItem("Through Walls", aura.throughWalls, SettingType.TOGGLE));
                settings.add(new SettingItem("APS", aura.aps, 1, 20, SettingType.SLIDER_INT));
                settings.add(new SettingItem("FOV", aura.fov, 1, 180, SettingType.SLIDER_INT));
                
                // Target settings
                settings.add(new SettingItem("Players", aura.players, SettingType.TOGGLE));
                settings.add(new SettingItem("Monsters", aura.monsters, SettingType.TOGGLE));
                settings.add(new SettingItem("Animals", aura.animals, SettingType.TOGGLE));
                settings.add(new SettingItem("Ignore Invisible", aura.ignoreInvisible, SettingType.TOGGLE));
                settings.add(new SettingItem("Ignore Creative", aura.ignoreCreative, SettingType.TOGGLE));
                settings.add(new SettingItem("Ignore Named", aura.ignoreNamed, SettingType.TOGGLE));
                
                // Anti-cheat
                settings.add(new SettingItem("Pause in Cobweb", aura.pauseInCobweb, SettingType.TOGGLE));
                settings.add(new SettingItem("Hit When in Cobweb", aura.hitWhenInCobweb, SettingType.TOGGLE));
                settings.add(new SettingItem("Pause in Inventory", aura.pauseInInventory, SettingType.TOGGLE));
                settings.add(new SettingItem("Random Hit Delay", aura.randomHitDelay, SettingType.TOGGLE));
            }
        }
        
        // Esp settings
        if (selectedMainCategory == 0 && selectedSubCategory == 6) {
            settings.add(new SettingItem("ESP Enabled", true, SettingType.TOGGLE));
            settings.add(new SettingItem("Box ESP", true, SettingType.TOGGLE));
            settings.add(new SettingItem("Health Bar", true, SettingType.TOGGLE));
            settings.add(new SettingItem("Tracers", false, SettingType.TOGGLE));
            settings.add(new SettingItem("Player Color", 0xFF3A6EA5, SettingType.COLOR));
            settings.add(new SettingItem("Enemy Color", 0xFFFF5555, SettingType.COLOR));
            settings.add(new SettingItem("Friend Color", 0xFF55FF55, SettingType.COLOR));
        }
        
        // Movement settings
        if (selectedMainCategory == 1) {
            settings.add(new SettingItem("Sprint", true, SettingType.TOGGLE));
            settings.add(new SettingItem("Speed", 1.0f, 0.1f, 2.0f, SettingType.SLIDER));
            settings.add(new SettingItem("No Fall", false, SettingType.TOGGLE));
            settings.add(new SettingItem("No Push", false, SettingType.TOGGLE));
            settings.add(new SettingItem("No Slow", false, SettingType.TOGGLE));
            settings.add(new SettingItem("Velocity", false, SettingType.TOGGLE));
            settings.add(new SettingItem("Step", 0.0f, 0f, 1.5f, SettingType.SLIDER));
            settings.add(new SettingItem("Scaffold", false, SettingType.TOGGLE));
            settings.add(new SettingItem("Parkour", false, SettingType.TOGGLE));
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Main background
        context.fill(0, 0, width, height, BG);
        
        // Header
        context.fill(0, 0, width, 40, MAIN_CAT_BG);
        context.drawText(textRenderer, "AcoreHack", 15, 14, TEXT, true);
        context.drawText(textRenderer, "v1.0", 85, 16, TEXT_DIM, true);
        
        // Search bar
        int searchX = width - 220;
        context.fill(searchX, 10, searchX + 200, 32, SUB_CAT_BG);
        context.drawBorder(searchX, 10, 200, 22, BORDER);
        context.drawText(textRenderer, isSearching ? searchText + "_" : "Search...", searchX + 8, 16, isSearching ? TEXT : TEXT_HINT, true);
        
        // ==================== LEFT PANEL (Main Categories) ====================
        context.fill(0, 40, 140, height, MAIN_CAT_BG);
        context.fill(140, 40, 141, height, BORDER);
        
        int yOffset = 55;
        for (int i = 0; i < mainCategories.size(); i++) {
            CategoryItem cat = mainCategories.get(i);
            boolean selected = (i == selectedMainCategory);
            boolean hover = mouseX >= 0 && mouseX <= 140 && mouseY >= yOffset - 12 && mouseY <= yOffset + 18;
            
            int bgColor;
            if (selected) bgColor = MAIN_CAT_SELECTED;
            else if (hover) bgColor = MAIN_CAT_HOVER;
            else bgColor = MAIN_CAT_BG;
            
            context.fill(0, yOffset - 12, 140, yOffset + 20, bgColor);
            if (selected) {
                context.fill(0, yOffset - 12, 4, yOffset + 20, SUB_CAT_SELECTED);
            }
            context.drawText(textRenderer, cat.name, 20, yOffset, selected ? SUB_CAT_SELECTED : TEXT, true);
            yOffset += 35;
        }
        
        // ==================== MIDDLE PANEL (Sub Categories) ====================
        context.fill(141, 40, 340, height, SUB_CAT_BG);
        context.fill(340, 40, 341, height, BORDER);
        
        yOffset = 55;
        for (int i = 0; i < subCategories.size(); i++) {
            SubCategoryItem sub = subCategories.get(i);
            boolean selected = (i == selectedSubCategory);
            boolean hover = mouseX >= 141 && mouseX <= 340 && mouseY >= yOffset - 12 && mouseY <= yOffset + 18;
            
            int bgColor;
            if (selected) bgColor = SUB_CAT_SELECTED;
            else if (hover) bgColor = SETTING_HOVER;
            else bgColor = SUB_CAT_BG;
            
            context.fill(141, yOffset - 12, 340, yOffset + 20, bgColor);
            context.drawText(textRenderer, sub.name, 155, yOffset, selected ? TEXT : TEXT_DIM, true);
            yOffset += 28;
        }
        
        // ==================== RIGHT PANEL (Settings) ====================
        context.fill(341, 40, width, height, SETTINGS_BG);
        context.fill(340, 40, 341, height, BORDER);
        
        // Category title
        String title = mainCategories.get(selectedMainCategory).name + " / " + subCategories.get(selectedSubCategory).name;
        context.drawText(textRenderer, title, 360, 55, SUB_CAT_SELECTED, true);
        context.fill(360, 68, width - 20, 69, BORDER);
        
        yOffset = 85;
        for (SettingItem setting : settings) {
            boolean hover = mouseX >= 360 && mouseX <= width - 20 && mouseY >= yOffset && mouseY <= yOffset + 25;
            
            context.fill(360, yOffset, width - 20, yOffset + 25, hover ? SETTING_HOVER : SETTINGS_BG);
            context.drawBorder(360, yOffset, width - 380, 25, BORDER);
            
            // Setting name
            context.drawText(textRenderer, setting.name, 370, yOffset + 9, TEXT, true);
            
            // Setting control
            switch (setting.type) {
                case TOGGLE:
                    int toggleX = width - 70;
                    context.fill(toggleX, yOffset + 5, toggleX + 40, yOffset + 20, (boolean)setting.value ? TOGGLE_ON : TOGGLE_OFF);
                    context.drawBorder(toggleX, yOffset + 5, 40, 15, BORDER);
                    context.drawText(textRenderer, (boolean)setting.value ? "ON" : "OFF", toggleX + 12, yOffset + 10, TEXT, true);
                    break;
                    
                case SLIDER:
                case SLIDER_INT:
                    float percent = ((Number)setting.value).floatValue();
                    float min = setting.min;
                    float max = setting.max;
                    float valuePercent = (percent - min) / (max - min);
                    int sliderX = width - 200;
                    context.fill(sliderX, yOffset + 10, sliderX + 150, yOffset + 15, SLIDER_BG);
                    context.fill(sliderX, yOffset + 10, sliderX + (int)(150 * valuePercent), yOffset + 15, SLIDER_FILL);
                    String valueStr = setting.type == SettingType.SLIDER_INT ? String.valueOf((int)percent) : String.format("%.1f", percent);
                    context.drawText(textRenderer, valueStr, sliderX + 155, yOffset + 9, TEXT, true);
                    break;
                    
                case MODE:
                    String[] modes = (String[]) setting.extra;
                    int modeX = width - 180;
                    context.fill(modeX, yOffset + 5, modeX + 140, yOffset + 20, TOGGLE_OFF);
                    context.drawBorder(modeX, yOffset + 5, 140, 15, BORDER);
                    context.drawText(textRenderer, (String)setting.value, modeX + 10, yOffset + 10, TEXT, true);
                    context.drawText(textRenderer, "▼", modeX + 125, yOffset + 9, TEXT_DIM, true);
                    break;
                    
                case COLOR:
                    int colorX = width - 60;
                    context.fill(colorX, yOffset + 5, colorX + 30, yOffset + 20, (int)setting.value);
                    context.drawBorder(colorX, yOffset + 5, 30, 15, BORDER);
                    break;
            }
            
            yOffset += 30;
        }
        
        // Keybind popup
        if (isBinding && bindingModule != null) {
            renderKeybindPopup(context);
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void renderKeybindPopup(DrawContext context) {
        context.fill(0, 0, width, height, new Color(0, 0, 0, 200).getRGB());
        
        int boxWidth = 280;
        int boxHeight = 100;
        int boxX = width / 2 - boxWidth / 2;
        int boxY = height / 2 - boxHeight / 2;
        
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, SUB_CAT_BG);
        context.drawBorder(boxX, boxY, boxWidth, boxHeight, SUB_CAT_SELECTED);
        
        context.drawText(textRenderer, "Set Keybind", boxX + 15, boxY + 20, SUB_CAT_SELECTED, true);
        context.drawText(textRenderer, "Module: " + bindingModule.getName(), boxX + 15, boxY + 40, TEXT, true);
        context.drawText(textRenderer, "Press any key...", boxX + 15, boxY + 65, TEXT_DIM, true);
        context.drawText(textRenderer, "Press ESC to cancel", boxX + 15, boxY + 80, TEXT_HINT, true);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isBinding) return true;
        
        // Main categories click
        int yOffset = 55;
        for (int i = 0; i < mainCategories.size(); i++) {
            if (mouseX >= 0 && mouseX <= 140 && mouseY >= yOffset - 12 && mouseY <= yOffset + 20) {
                selectedMainCategory = i;
                selectedSubCategory = 0;
                initSubCategories();
                initSettings();
                SoundManager.playClickSound();
                return true;
            }
            yOffset += 35;
        }
        
        // Sub categories click
        yOffset = 55;
        for (int i = 0; i < subCategories.size(); i++) {
            if (mouseX >= 141 && mouseX <= 340 && mouseY >= yOffset - 12 && mouseY <= yOffset + 20) {
                selectedSubCategory = i;
                initSettings();
                SoundManager.playClickSound();
                return true;
            }
            yOffset += 28;
        }
        
        // Settings click
        yOffset = 85;
        Aura aura = (Aura) ModuleManager.getModule("Aura");
        
        for (int i = 0; i < settings.size(); i++) {
            SettingItem setting = settings.get(i);
            
            if (mouseX >= 360 && mouseX <= width - 20 && mouseY >= yOffset && mouseY <= yOffset + 25) {
                switch (setting.type) {
                    case TOGGLE:
                        setting.value = !(boolean)setting.value;
                        applySetting(setting, aura);
                        SoundManager.playClickSound();
                        break;
                    case MODE:
                        String[] modes = (String[]) setting.extra;
                        int currentIdx = -1;
                        for (int j = 0; j < modes.length; j++) {
                            if (modes[j].equals(setting.value)) {
                                currentIdx = j;
                                break;
                            }
                        }
                        int nextIdx = (currentIdx + 1) % modes.length;
                        setting.value = modes[nextIdx];
                        applySetting(setting, aura);
                        SoundManager.playClickSound();
                        break;
                    case SLIDER:
                    case SLIDER_INT:
                        int sliderX = width - 200;
                        if (mouseX >= sliderX && mouseX <= sliderX + 150) {
                            float percent = (float)((mouseX - sliderX) / 150.0);
                            float value = setting.min + percent * (setting.max - setting.min);
                            if (setting.type == SettingType.SLIDER_INT) {
                                setting.value = (int)value;
                            } else {
                                setting.value = value;
                            }
                            applySetting(setting, aura);
                            SoundManager.playClickSound();
                        }
                        break;
                }
            }
            yOffset += 30;
        }
        
        // Search click
        int searchX = width - 220;
        if (mouseX >= searchX && mouseX <= searchX + 200 && mouseY >= 10 && mouseY <= 32) {
            isSearching = true;
            return true;
        }
        
        isSearching = false;
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    private void applySetting(SettingItem setting, Aura aura) {
        if (aura == null) return;
        
        switch (setting.name) {
            case "Range":
                aura.range = (float)setting.value;
                break;
            case "Ignore Walls":
            case "Through Walls":
                aura.throughWalls = (boolean)setting.value;
                break;
            case "Rotation Mode":
                String rotMode = (String)setting.value;
                if (rotMode.equals("NONE")) aura.rotationMode = Aura.RotationMode.NONE;
                else if (rotMode.equals("LEGIT")) aura.rotationMode = Aura.RotationMode.LEGIT;
                else if (rotMode.equals("NORMAL")) aura.rotationMode = Aura.RotationMode.NORMAL;
                break;
            case "Aim Mode":
                String aimMode = (String)setting.value;
                if (aimMode.equals("HEAD")) aura.aimMode = Aura.AimMode.HEAD;
                else if (aimMode.equals("BODY")) aura.aimMode = Aura.AimMode.BODY;
                else if (aimMode.equals("LEGS")) aura.aimMode = Aura.AimMode.LEGS;
                break;
            case "Auto Weapon":
                aura.autoWeapon = (boolean)setting.value;
                break;
            case "Auto Crit":
                aura.autoCrit = (boolean)setting.value;
                break;
            case "Only Weapon":
                aura.onlyWeapon = (boolean)setting.value;
                break;
            case "Shield Breaker":
                aura.shieldBreaker = (boolean)setting.value;
                break;
            case "APS":
                aura.aps = (int)setting.value;
                break;
            case "FOV":
                aura.fov = (int)setting.value;
                break;
            case "Players":
                aura.players = (boolean)setting.value;
                break;
            case "Monsters":
                aura.monsters = (boolean)setting.value;
                break;
            case "Animals":
                aura.animals = (boolean)setting.value;
                break;
            case "Pause in Cobweb":
                aura.pauseInCobweb = (boolean)setting.value;
                break;
            case "Hit When in Cobweb":
                aura.hitWhenInCobweb = (boolean)setting.value;
                break;
            case "Pause in Inventory":
                aura.pauseInInventory = (boolean)setting.value;
                break;
            case "Random Hit Delay":
                aura.randomHitDelay = (boolean)setting.value;
                break;
        }
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
        
        if (isSearching) {
            if (keyCode == 256) {
                isSearching = false;
                searchText = "";
                return true;
            }
            if (keyCode == 257 || keyCode == 335) {
                isSearching = false;
                return true;
            }
            if (keyCode == 259) {
                if (!searchText.isEmpty()) {
                    searchText = searchText.substring(0, searchText.length() - 1);
                }
                return true;
            }
            String key = getKeyName(keyCode);
            if (key.length() == 1 && (Character.isLetterOrDigit(key.charAt(0)) || key.charAt(0) == ' ')) {
                searchText += key;
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
                if (keyCode >= 65 && keyCode <= 90) return String.valueOf((char) keyCode);
                if (keyCode >= 48 && keyCode <= 57) return String.valueOf((char) keyCode);
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
        ConfigManager.saveAllConfigs();
    }
    
    // ==================== INNER CLASSES ====================
    
    private class CategoryItem {
        String name;
        int id;
        CategoryItem(String name, int id) {
            this.name = name;
            this.id = id;
        }
    }
    
    private class SubCategoryItem {
        String name;
        int id;
        SubCategoryItem(String name, int id) {
            this.name = name;
            this.id = id;
        }
    }
    
    private enum SettingType { TOGGLE, SLIDER, SLIDER_INT, MODE, COLOR }
    
    private class SettingItem {
        String name;
        Object value;
        float min, max;
        SettingType type;
        Object extra;
        
        SettingItem(String name, Object value, SettingType type) {
            this.name = name;
            this.value = value;
            this.type = type;
        }
        
        SettingItem(String name, Object value, float min, float max, SettingType type) {
            this.name = name;
            this.value = value;
            this.min = min;
            this.max = max;
            this.type = type;
        }
        
        SettingItem(String name, Object value, int min, int max, SettingType type) {
            this.name = name;
            this.value = value;
            this.min = min;
            this.max = max;
            this.type = type;
        }
        
        SettingItem(String name, Object value, String[] modes, SettingType type) {
            this.name = name;
            this.value = value;
            this.type = type;
            this.extra = modes;
        }
        
        SettingItem(String name, int color, SettingType type) {
            this.name = name;
            this.value = color;
            this.type = type;
        }
    }
}