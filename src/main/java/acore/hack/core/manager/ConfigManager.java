package acore.hack.core.manager;

import acore.hack.core.AcoreHack;
import acore.hack.features.modules.Module;
import acore.hack.setting.Setting;
import acore.hack.setting.impl.*;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class ConfigManager {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    public static final File MAIN_FOLDER = new File(mc.runDirectory, "acore");
    public static final File CONFIGS_FOLDER = new File(MAIN_FOLDER, "configs");
    public static final File MISC_FOLDER = new File(MAIN_FOLDER, "misc");
    
    public File currentConfig = null;
    private static ConfigManager instance;
    
    public ConfigManager() {
        instance = this;
        createDirs(MAIN_FOLDER, CONFIGS_FOLDER, MISC_FOLDER);
        getCurrentConfig();
    }
    
    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
    
    private void createDirs(File... dirs) {
        for (File dir : dirs) {
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
    }
    
    public static String getConfigDate(String name) {
        File file = new File(CONFIGS_FOLDER, name + ".ac");
        if (!file.exists()) return "none";
        return new SimpleDateFormat("dd MMM yyyy HH:mm").format(new Date(file.lastModified()));
    }
    
    public void load(String name) {
        File file = new File(CONFIGS_FOLDER, name + ".ac");
        if (!file.exists()) {
            System.out.println("[AcoreHack] Config " + name + " does not exist!");
            return;
        }
        
        if (currentConfig != null) {
            save(currentConfig);
        }
        
        loadFile(file);
    }
    
    public void load(String name, String category) {
        File file = new File(CONFIGS_FOLDER, name + ".ac");
        if (!file.exists()) {
            System.out.println("[AcoreHack] Config " + name + " does not exist!");
            return;
        }
        
        if (currentConfig != null) {
            save(currentConfig);
        }
        
        ModuleManager.onUnload(category);
        loadFile(file, category);
        ModuleManager.onLoad(category);
    }
    
    private void loadFile(File config) {
        loadFile(config, "none");
    }
    
    private void loadFile(File config, String category) {
        if (!config.exists()) {
            save(config);
        }
        
        try (FileReader reader = new FileReader(config, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray modulesArray = root.getAsJsonArray("modules");
            
            if (modulesArray != null) {
                for (JsonElement element : modulesArray) {
                    try {
                        parseModule(element.getAsJsonObject(), category);
                    } catch (Exception e) {
                        LogUtils.getLogger().warn("Failed to parse module: " + e.getMessage());
                    }
                }
            }
            
            System.out.println("[AcoreHack] Loaded config: " + config.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (category.equals("none")) {
            currentConfig = config;
        }
        
        saveCurrentConfig();
    }
    
    public void loadBinds(String name) {
        File file = new File(CONFIGS_FOLDER, name + ".ac");
        if (!file.exists()) {
            System.out.println("[AcoreHack] Config " + name + " does not exist!");
            return;
        }
        
        if (currentConfig != null) {
            save(currentConfig);
        }
        
        loadBindsFile(file);
    }
    
    private void loadBindsFile(File config) {
        try (FileReader reader = new FileReader(config, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray modulesArray = root.getAsJsonArray("modules");
            
            if (modulesArray != null) {
                for (JsonElement element : modulesArray) {
                    parseBinds(element.getAsJsonObject());
                }
            }
            
            System.out.println("[AcoreHack] Loaded binds from: " + config.getName());
        } catch (IOException e) {
            LogUtils.getLogger().warn(e.getMessage());
        }
        
        saveCurrentConfig();
    }
    
    public void loadModuleOnly(String name, Module module) {
        File file = new File(CONFIGS_FOLDER, name + ".ac");
        if (!file.exists()) {
            System.out.println("[AcoreHack] Config " + name + " does not exist!");
            return;
        }
        
        boolean wasEnabled = module.isEnabled();
        if (wasEnabled) {
            AcoreHack.EVENT_BUS.unsubscribe(module);
            module.setEnabled(false);
        }
        
        loadModuleOnlyFile(file, module);
        
        if (wasEnabled) {
            AcoreHack.EVENT_BUS.subscribe(module);
        }
    }
    
    private void loadModuleOnlyFile(File config, Module targetModule) {
        try (FileReader reader = new FileReader(config, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray modulesArray = root.getAsJsonArray("modules");
            
            if (modulesArray != null) {
                for (JsonElement element : modulesArray) {
                    JsonObject moduleObj = element.getAsJsonObject();
                    String moduleName = moduleObj.get("name").getAsString();
                    
                    if (moduleName.equals(targetModule.getName())) {
                        parseModuleSettings(moduleObj, targetModule);
                        break;
                    }
                }
            }
            
            System.out.println("[AcoreHack] Loaded " + targetModule.getName() + " from " + config.getName());
        } catch (IOException e) {
            LogUtils.getLogger().warn(e.getMessage());
        }
    }
    
    public void save(String name) {
        File file = new File(CONFIGS_FOLDER, name + ".ac");
        if (file.exists()) {
            System.out.println("[AcoreHack] Overwriting " + name + "...");
            file.delete();
        } else {
            System.out.println("[AcoreHack] Config " + name + " saved!");
        }
        save(file);
    }
    
    public void save(File config) {
        try {
            if (!config.exists()) {
                config.createNewFile();
            }
            
            JsonObject root = new JsonObject();
            root.addProperty("version", 1);
            root.add("modules", getModulesArray());
            
            try (FileWriter writer = new FileWriter(config, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException e) {
            LogUtils.getLogger().warn(e.getMessage());
        }
    }
    
    private JsonArray getModulesArray() {
        JsonArray modulesArray = new JsonArray();
        
        for (Module module : ModuleManager.getModules()) {
            modulesArray.add(getModuleObject(module));
        }
        
        return modulesArray;
    }
    
    private JsonObject getModuleObject(Module module) {
        JsonObject moduleObj = new JsonObject();
        moduleObj.addProperty("name", module.getName());
        moduleObj.addProperty("enabled", module.isEnabled());
        
        JsonObject settingsObj = new JsonObject();
        
        for (Setting<?> setting : module.getSettings()) {
            Object value = setting.getValue();
            
            if (value instanceof ColorSetting color) {
                JsonObject colorObj = new JsonObject();
                colorObj.addProperty("color", color.getRawColor());
                colorObj.addProperty("rainbow", color.isRainbow());
                settingsObj.add(setting.getName(), colorObj);
                
            } else if (value instanceof PositionSetting pos) {
                JsonObject posObj = new JsonObject();
                posObj.addProperty("x", pos.getX());
                posObj.addProperty("y", pos.getY());
                settingsObj.add(setting.getName(), posObj);
                
            } else if (value instanceof BooleanSettingGroup bGroup) {
                JsonObject groupObj = new JsonObject();
                groupObj.addProperty("enabled", bGroup.isEnabled());
                groupObj.addProperty("extended", bGroup.isExtended());
                settingsObj.add(setting.getName(), groupObj);
                
            } else if (value instanceof SettingGroup sGroup) {
                JsonObject groupObj = new JsonObject();
                groupObj.addProperty("extended", sGroup.isExtended());
                settingsObj.add(setting.getName(), groupObj);
                
            } else if (value instanceof Bind bind) {
                JsonObject bindObj = new JsonObject();
                bindObj.addProperty("key", bind.getKey());
                bindObj.addProperty("mouse", bind.isMouse());
                bindObj.addProperty("hold", bind.isHold());
                settingsObj.add(setting.getName(), bindObj);
                
            } else if (value instanceof ItemBindSetting itemBind) {
                JsonObject itemBindObj = new JsonObject();
                itemBindObj.addProperty("itemId", itemBind.getItemId());
                itemBindObj.addProperty("key", itemBind.getBind().getKey());
                itemBindObj.addProperty("mouse", itemBind.getBind().isMouse());
                itemBindObj.addProperty("hold", itemBind.getBind().isHold());
                settingsObj.add(setting.getName(), itemBindObj);
                
            } else if (value instanceof ItemSelectSetting itemSelect) {
                JsonArray itemsArray = new JsonArray();
                for (String id : itemSelect.getItemsById()) {
                    itemsArray.add(id);
                }
                settingsObj.add(setting.getName(), itemsArray);
                
            } else if (value instanceof Enum) {
                settingsObj.addProperty(setting.getName(), ((Enum<?>) value).name());
                
            } else if (value instanceof Boolean) {
                settingsObj.addProperty(setting.getName(), (Boolean) value);
                
            } else if (value instanceof Number) {
                settingsObj.addProperty(setting.getName(), (Number) value);
                
            } else if (value instanceof String) {
                String str = (String) value;
                settingsObj.addProperty(setting.getName(), str.replace(" ", "%%").replace("/", "++"));
            }
        }
        
        moduleObj.add("settings", settingsObj);
        return moduleObj;
    }
    
    private void parseModule(JsonObject moduleObj, String category) {
        String moduleName = moduleObj.get("name").getAsString();
        Module module = ModuleManager.getModule(moduleName);
        
        if (module == null) return;
        
        if (category.equals("none") || module.getCategory().getName().equalsIgnoreCase(category)) {
            boolean enabled = moduleObj.get("enabled").getAsBoolean();
            
            if (enabled != module.isEnabled()) {
                if (enabled) {
                    module.enable();
                } else {
                    module.disable();
                }
            }
            
            parseModuleSettings(moduleObj, module);
        }
    }
    
    private void parseModuleSettings(JsonObject moduleObj, Module module) {
        JsonObject settingsObj = moduleObj.getAsJsonObject("settings");
        if (settingsObj == null) return;
        
        for (Setting<?> setting : module.getSettings()) {
            try {
                JsonElement element = settingsObj.get(setting.getName());
                if (element == null) continue;
                
                Object value = setting.getValue();
                
                if (value instanceof ColorSetting color) {
                    JsonObject colorObj = element.getAsJsonObject();
                    if (colorObj.has("color")) {
                        color.setColor(colorObj.get("color").getAsInt());
                    }
                    if (colorObj.has("rainbow")) {
                        color.setRainbow(colorObj.get("rainbow").getAsBoolean());
                    }
                    
                } else if (value instanceof PositionSetting pos) {
                    JsonObject posObj = element.getAsJsonObject();
                    if (posObj.has("x")) pos.setX(posObj.get("x").getAsFloat());
                    if (posObj.has("y")) pos.setY(posObj.get("y").getAsFloat());
                    
                } else if (value instanceof BooleanSettingGroup bGroup) {
                    JsonObject groupObj = element.getAsJsonObject();
                    if (groupObj.has("enabled")) bGroup.setEnabled(groupObj.get("enabled").getAsBoolean());
                    if (groupObj.has("extended")) bGroup.setExtended(groupObj.get("extended").getAsBoolean());
                    
                } else if (value instanceof SettingGroup sGroup) {
                    JsonObject groupObj = element.getAsJsonObject();
                    if (groupObj.has("extended")) sGroup.setExtended(groupObj.get("extended").getAsBoolean());
                    
                } else if (value instanceof Bind bind) {
                    JsonObject bindObj = element.getAsJsonObject();
                    int key = bindObj.get("key").getAsInt();
                    boolean mouse = bindObj.get("mouse").getAsBoolean();
                    boolean hold = bindObj.get("hold").getAsBoolean();
                    setting.setValue(new Bind(key, mouse, hold));
                    
                } else if (value instanceof ItemBindSetting itemBind) {
                    JsonObject itemBindObj = element.getAsJsonObject();
                    String itemId = itemBindObj.get("itemId").getAsString();
                    int key = itemBindObj.get("key").getAsInt();
                    boolean mouse = itemBindObj.get("mouse").getAsBoolean();
                    boolean hold = itemBindObj.get("hold").getAsBoolean();
                    setting.setValue(new ItemBindSetting(itemId, new Bind(key, mouse, hold)));
                    
                } else if (value instanceof ItemSelectSetting itemSelect) {
                    JsonArray itemsArray = element.getAsJsonArray();
                    itemSelect.getItemsById().clear();
                    for (JsonElement item : itemsArray) {
                        itemSelect.getItemsById().add(item.getAsString());
                    }
                    
                } else if (value instanceof Enum) {
                    String enumName = element.getAsString();
                    try {
                        @SuppressWarnings("unchecked")
                        Enum<?> enumValue = Enum.valueOf((Class<Enum>) value.getClass(), enumName);
                        setting.setValue(enumValue);
                    } catch (IllegalArgumentException ignored) {}
                    
                } else if (value instanceof Boolean) {
                    setting.setValue(element.getAsBoolean());
                    
                } else if (value instanceof Float) {
                    setting.setValue(element.getAsFloat());
                    
                } else if (value instanceof Integer) {
                    setting.setValue(element.getAsInt());
                    
                } else if (value instanceof Double) {
                    setting.setValue(element.getAsDouble());
                    
                } else if (value instanceof String) {
                    setting.setValue(element.getAsString().replace("%%", " ").replace("++", "/"));
                }
                
            } catch (Exception e) {
                LogUtils.getLogger().warn("[AcoreHack] Module: " + module.getName() + " Setting: " + setting.getName() + " Error: " + e.getMessage());
            }
        }
    }
    
    private void parseBinds(JsonObject moduleObj) {
        String moduleName = moduleObj.get("name").getAsString();
        Module module = ModuleManager.getModule(moduleName);
        if (module == null) return;
        
        JsonObject settingsObj = moduleObj.getAsJsonObject("settings");
        if (settingsObj == null) return;
        
        for (Setting<?> setting : module.getSettings()) {
            try {
                JsonElement element = settingsObj.get(setting.getName());
                if (element == null) continue;
                
                if (setting.getValue() instanceof Bind) {
                    JsonObject bindObj = element.getAsJsonObject();
                    int key = bindObj.get("key").getAsInt();
                    boolean mouse = bindObj.get("mouse").getAsBoolean();
                    boolean hold = bindObj.get("hold").getAsBoolean();
                    setting.setValue(new Bind(key, mouse, hold));
                }
                
            } catch (Exception e) {
                LogUtils.getLogger().warn("[AcoreHack] Bind parse error: " + e.getMessage());
            }
        }
    }
    
    public void delete(String name) {
        File file = new File(CONFIGS_FOLDER, name + ".ac");
        if (file.exists()) {
            file.delete();
            System.out.println("[AcoreHack] Deleted config: " + name);
        }
    }
    
    public void delete(File file) {
        if (file.exists()) {
            file.delete();
        }
    }
    
    public List<String> getConfigList() {
        List<String> configs = new ArrayList<>();
        if (CONFIGS_FOLDER.exists() && CONFIGS_FOLDER.listFiles() != null) {
            for (File file : CONFIGS_FOLDER.listFiles()) {
                if (file.getName().endsWith(".ac")) {
                    configs.add(file.getName().replace(".ac", ""));
                }
            }
        }
        Collections.sort(configs);
        return configs;
    }
    
    private void saveCurrentConfig() {
        if (currentConfig != null) {
            File file = new File(MISC_FOLDER, "current_config.txt");
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(currentConfig.getName().replace(".ac", ""));
                }
            } catch (Exception e) {
                LogUtils.getLogger().warn(e.getMessage());
            }
        }
    }
    
    public File getCurrentConfig() {
        File file = new File(MISC_FOLDER, "current_config.txt");
        String name = "default";
        
        try {
            if (file.exists()) {
                try (Scanner scanner = new Scanner(file)) {
                    if (scanner.hasNextLine()) {
                        name = scanner.nextLine();
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.getLogger().warn(e.getMessage());
        }
        
        currentConfig = new File(CONFIGS_FOLDER, name + ".ac");
        
        if (!currentConfig.exists()) {
            save("default");
            currentConfig = new File(CONFIGS_FOLDER, "default.ac");
        }
        
        return currentConfig;
    }
    
    public void reloadConfig() {
        if (currentConfig != null) {
            String name = currentConfig.getName().replace(".ac", "");
            load(name);
            System.out.println("[AcoreHack] Reloaded config: " + name);
        }
    }
    
    public boolean isFirstLaunch() {
        return !MAIN_FOLDER.exists();
    }
}