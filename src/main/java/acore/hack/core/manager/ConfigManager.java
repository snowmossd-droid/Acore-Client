package acore.hack.core.manager;

import acore.hack.features.modules.Module;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private static String currentConfig = "default";
    private static File configDir = new File("acore_configs");
    
    public static void init() {
        if (!configDir.exists()) configDir.mkdir();
    }
    
    public static String getCurrentConfig() {
        return currentConfig;
    }
    
    public static void saveModules(List<Module> modules) {
        // Save module settings
    }
    
    public static void saveFriends(List<String> friends) {
        File file = new File(configDir, currentConfig + "_friends.json");
        // Save friends
    }
    
    public static void loadAllConfigs() {
        // Load all configs
    }
    
    public static void switchConfig(String name) {
        currentConfig = name;
    }
    
    public static void saveConfig(String name) {
        // Save current config
    }
    
    public static void loadConfig(String name) {
        currentConfig = name;
        // Load config
    }
}
