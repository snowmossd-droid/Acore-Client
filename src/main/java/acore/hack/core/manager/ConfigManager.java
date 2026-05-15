package acore.hack.core.manager;

import acore.hack.features.modules.Module;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private static String currentConfig = "default";
    private static File configDir = new File("acore_configs");
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public static void init() {
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        loadConfig(currentConfig);
        loadFriends();
        loadKeybinds();
    }
    
    public static void reloadConfig() {
        System.out.println("[AcoreHack] Reloading config...");
        saveConfig(currentConfig);
        loadConfig(currentConfig);
        loadFriends();
        loadKeybinds();
        System.out.println("[AcoreHack] Config reloaded successfully!");
    }
    
    public static String getCurrentConfig() {
        return currentConfig;
    }
    
    public static void setCurrentConfig(String name) {
        currentConfig = name;
    }
    
    public static void switchConfig(String name) {
        saveConfig(currentConfig);
        currentConfig = name;
        loadConfig(name);
        loadFriends();
        loadKeybinds();
    }
    
    public static void saveConfig(String name) {
        File file = new File(configDir, name + ".json");
        try (Writer writer = new FileWriter(file)) {
            List<ModuleConfig> configs = new ArrayList<>();
            for (Module module : ModuleManager.getModules()) {
                ModuleConfig mc = new ModuleConfig();
                mc.name = module.getName();
                mc.enabled = module.isEnabled();
                mc.keybind = module.getKeybind();
                mc.keyCode = module.getKeyCode();
                configs.add(mc);
            }
            gson.toJson(configs, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void loadConfig(String name) {
        File file = new File(configDir, name + ".json");
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                ModuleConfig[] configs = gson.fromJson(reader, ModuleConfig[].class);
                if (configs != null) {
                    for (ModuleConfig mc : configs) {
                        Module module = ModuleManager.getModule(mc.name);
                        if (module != null) {
                            if (mc.enabled) module.enable();
                            else module.disable();
                            module.setKeybind(mc.keybind);
                            module.setKeyCode(mc.keyCode);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void saveModules(List<Module> modules) {
        saveConfig(currentConfig);
    }
    
    public static void saveFriends() {
        File file = new File(configDir, currentConfig + "_friends.json");
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(FriendManager.getFriends(), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void loadFriends() {
        File file = new File(configDir, currentConfig + "_friends.json");
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                String[] friends = gson.fromJson(reader, String[].class);
                if (friends != null) {
                    FriendManager.clear();
                    for (String friend : friends) {
                        FriendManager.addFriend(friend);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void saveKeybinds() {
        File file = new File(configDir, currentConfig + "_keybinds.json");
        try (Writer writer = new FileWriter(file)) {
            List<KeybindConfig> keybinds = new ArrayList<>();
            for (Module module : ModuleManager.getModules()) {
                KeybindConfig kc = new KeybindConfig();
                kc.name = module.getName();
                kc.keybind = module.getKeybind();
                kc.keyCode = module.getKeyCode();
                keybinds.add(kc);
            }
            gson.toJson(keybinds, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void loadKeybinds() {
        File file = new File(configDir, currentConfig + "_keybinds.json");
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                KeybindConfig[] keybinds = gson.fromJson(reader, KeybindConfig[].class);
                if (keybinds != null) {
                    for (KeybindConfig kc : keybinds) {
                        Module module = ModuleManager.getModule(kc.name);
                        if (module != null) {
                            module.setKeybind(kc.keybind);
                            module.setKeyCode(kc.keyCode);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void loadAllConfigs() {
        loadConfig(currentConfig);
        loadFriends();
        loadKeybinds();
    }
    
    public static void saveAllConfigs() {
        saveConfig(currentConfig);
        saveFriends();
        saveKeybinds();
    }
    
    // Inner classes for JSON serialization
    private static class ModuleConfig {
        String name;
        boolean enabled;
        String keybind;
        int keyCode;
    }
    
    private static class KeybindConfig {
        String name;
        String keybind;
        int keyCode;
    }
    }
