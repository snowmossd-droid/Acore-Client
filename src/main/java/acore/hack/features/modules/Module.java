package acore.hack.features.modules;

import acore.hack.core.manager.ModuleManager;
import net.minecraft.client.MinecraftClient;
import java.util.ArrayList;
import java.util.List;

public class Module {
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    
    private String name;
    private Category category;
    private String keybind;
    private boolean enabled;
    private int keyCode;
    private String keyPin = "";
    private boolean keyPinEnabled = false;
    
    // 👇 THÊM DÒNG NÀY - lưu danh sách settings
    private final List<Object> settings = new ArrayList<>();
    
    public enum Category {
        COMBAT("Combat"),
        RENDER("Render"),
        CLIENT("Client");
        
        private String displayName;
        Category(String name) { this.displayName = name; }
        public String getDisplayName() { return displayName; }
    }
    
    public Module(String name, Category category) {
        this.name = name;
        this.category = category;
        this.enabled = false;
        this.keyCode = -1;
        this.keybind = "None";
    }
    
    public String getName() { return name; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    public int getKeyCode() { return keyCode; }
    public String getKeybind() { return keybind; }
    public String getKeyPin() { return keyPin; }
    public boolean isKeyPinEnabled() { return keyPinEnabled; }
    
    public void setKeybind(String keybind) { this.keybind = keybind; }
    public void setKeyCode(int code) { this.keyCode = code; }
    public void setKeyPin(String pin) { this.keyPin = pin; }
    public void setKeyPinEnabled(boolean enabled) { this.keyPinEnabled = enabled; }
    
    // 👇 THÊM METHOD NÀY
    protected void addSettings(Object... settingsList) {
        for (Object setting : settingsList) {
            settings.add(setting);
        }
    }
    
    // 👇 THÊM METHOD NÀY (lấy danh sách settings nếu cần)
    public List<Object> getSettings() {
        return settings;
    }
    
    public void enable() {
        enabled = true;
        onEnable();
        ModuleManager.saveModules();
    }
    
    public void disable() {
        enabled = false;
        onDisable();
        ModuleManager.saveModules();
    }
    
    public void toggle() {
        if (enabled) disable();
        else enable();
    }
    
    protected void onEnable() {}
    protected void onDisable() {}
    
    // 👇 THÊM METHOD NÀY (cho onTick)
    public void onTick() {}
    
    public void onUpdate() {}
    public void onRender() {}
    public void onRender3D() {}
    }
