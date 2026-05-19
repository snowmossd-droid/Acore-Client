package acore.hack.features.modules;

import acore.hack.core.AcoreHack;
import acore.hack.core.manager.ConfigManager;
import acore.hack.core.manager.ModuleManager;
import acore.hack.core.sound.SoundManager;
import acore.hack.features.gui.notification.Notification;
import acore.hack.setting.Setting;
import acore.hack.setting.impl.Bind;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.*;

public abstract class Module {
    private final Setting<Bind> bind = new Setting<>("Keybind", new Bind(-1, false, false));
    private final Setting<Boolean> enabled = new Setting<>("Enabled", false);
    private final String description;
    private final Category category;
    private final String displayName;
    private static boolean silentToggle = false;
    private final List<String> ignoreSoundList = Arrays.asList("ClickGUI", "HUD", "ClickGuiModule");
    public static final MinecraftClient mc = MinecraftClient.getInstance();

    public Module(@NotNull String name, @NotNull Category category) {
        this(name, "No description provided.", category);
    }

    public Module(@NotNull String name, @NotNull String description, @NotNull Category category) {
        this.displayName = name;
        this.description = description;
        this.category = category;
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onLogin() {}
    public void onLogout() {}
    public void onUpdate() {}
    public void onRender2D(DrawContext context) {}
    public void onRender3D(MatrixStack matrices) {}
    public void onUnload() {}

    public boolean isToggleable() {
        return true;
    }

    protected void sendPacket(Packet<?> packet) {
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(packet);
        }
    }

    protected void sendPacketSilent(Packet<?> packet) {
        if (mc.getNetworkHandler() != null) {
            AcoreHack.silentPackets.add(packet);
            mc.getNetworkHandler().sendPacket(packet);
        }
    }

    protected void sendSequencedPacket(SequencedPacketCreator packetCreator) {
        if (mc.getNetworkHandler() != null && mc.world != null) {
            PendingUpdateManager pendingUpdateManager = mc.world.getPendingUpdateManager().incrementSequence();
            try {
                int i = pendingUpdateManager.getSequence();
                mc.getNetworkHandler().sendPacket(packetCreator.predict(i));
            } finally {
                pendingUpdateManager.close();
            }
        }
    }

    public String getDisplayInfo() {
        return null;
    }

    public boolean isOn() {
        return enabled.getValue();
    }

    public boolean isOff() {
        return !enabled.getValue();
    }

    public void setEnabled(boolean enabled) {
        this.enabled.setValue(enabled);
    }

    public void onThread() {}

    public void enable() {
        enabled.setValue(true);
        if (!fullNullCheck()) {
            onEnable();
        }
        if (isOn()) {
            AcoreHack.EVENT_BUS.subscribe(this);
        }
        if (!fullNullCheck()) {
            if (!silentToggle && !ignoreSoundList.contains(getDisplayName())) {
                AcoreHack.NOTIFICATION.publicity(getDisplayName(), "Was Enabled!", 2, Notification.Type.ENABLED);
                SoundManager.playEnable();
            }
        }
    }

    public void disable(String reason) {
        sendMessage(reason);
        disable();
    }

    public void disable() {
        try {
            AcoreHack.EVENT_BUS.unsubscribe(this);
        } catch (Exception ignored) {}
        enabled.setValue(false);
        if (!fullNullCheck()) {
            onDisable();
            if (!silentToggle && !ignoreSoundList.contains(getDisplayName())) {
                AcoreHack.NOTIFICATION.publicity(getDisplayName(), "Was Disabled!", 2, Notification.Type.DISABLED);
                SoundManager.playDisable();
            }
        }
    }

    public void enableSilently() {
        boolean prev = silentToggle;
        silentToggle = true;
        try {
            enable();
        } finally {
            silentToggle = prev;
        }
    }

    public void disableSilently() {
        boolean prev = silentToggle;
        silentToggle = true;
        try {
            disable();
        } finally {
            silentToggle = prev;
        }
    }

    public void toggle() {
        if (enabled.getValue()) {
            disable();
        } else {
            enable();
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public Bind getBind() {
        return bind.getValue();
    }

    public void setBind(int key, boolean mouse, boolean hold) {
        setBind(new Bind(key, mouse, hold));
    }

    public void setBind(Bind b) {
        bind.setValue(b);
        ConfigManager.getInstance().saveCurrentConfig();
    }

    public boolean listening() {
        return isOn();
    }

    public static boolean fullNullCheck() {
        return mc.player == null || mc.world == null;
    }

    public String getName() {
        return getDisplayName();
    }

    public List<Setting<?>> getSettings() {
        List<Setting<?>> settingList = new ArrayList<>();
        Map<Setting<?>, Integer> settingDepth = new IdentityHashMap<>();
        Class<?> currentSuperclass = getClass();

        for (int depth = 0; currentSuperclass != null; depth++) {
            for (Field field : currentSuperclass.getDeclaredFields()) {
                if (Setting.class.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);
                        Setting<?> setting = (Setting<?>) field.get(this);
                        if (setting != null) {
                            settingList.add(setting);
                            settingDepth.put(setting, depth);
                        }
                    } catch (IllegalAccessException error) {
                        AcoreHack.LOGGER.warn(error.getMessage());
                    }
                }
            }
            currentSuperclass = currentSuperclass.getSuperclass();
        }

        settingList.sort(Comparator.<Setting<?>>comparingInt(setting -> settingDepth.getOrDefault(setting, Integer.MAX_VALUE))
                .thenComparingLong(Setting::getCreationOrder));
        settingList.forEach(s -> s.setModule(this));
        return settingList;
    }

    public boolean isEnabled() {
        return isOn();
    }

    public boolean isDisabled() {
        return !isEnabled();
    }

    public static void clickSlot(int id) {
        if (id != -1 && mc.interactionManager != null && mc.player != null) {
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, id, 0, SlotActionType.PICKUP, mc.player);
        }
    }

    public static void clickSlot(int id, SlotActionType type) {
        if (id != -1 && mc.interactionManager != null && mc.player != null) {
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, id, 0, type, mc.player);
        }
    }

    public static void clickSlot(int id, int button, SlotActionType type) {
        if (id != -1 && mc.interactionManager != null && mc.player != null) {
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, id, button, type, mc.player);
        }
    }

    public void sendMessage(String message) {
        if (!fullNullCheck()) {
            String prefix = Formatting.GRAY + "[" + Formatting.LIGHT_PURPLE + getDisplayName() + Formatting.GRAY + "] ";
            mc.player.sendMessage(Text.of(prefix + message), false);
        }
    }

    public void sendChatMessage(String message) {
        if (!fullNullCheck()) {
            mc.getNetworkHandler().sendChatMessage(message);
        }
    }

    public void sendChatCommand(String command) {
        if (!fullNullCheck()) {
            mc.getNetworkHandler().sendChatCommand(command);
        }
    }

    public void debug(String message) {
        if (!fullNullCheck()) {
            String prefix = Formatting.GRAY + "[" + Formatting.LIGHT_PURPLE + getDisplayName() + Formatting.GRAY + "] [🔧] ";
            mc.player.sendMessage(Text.of(prefix + message), false);
        }
    }

    public boolean isKeyPressed(int key) {
        if (key != -1 && !fullNullCheck()) {
            if (ModuleManager.activeMouseKeys.contains(key)) {
                ModuleManager.activeMouseKeys.clear();
                return true;
            }
            return key >= 10 && InputUtil.isKeyPressed(mc.getWindow().getHandle(), key);
        }
        return false;
    }

    public boolean isKeyPressed(Setting<Bind> bindSetting) {
        return bindSetting.getValue().getKey() != -1 && !fullNullCheck() && isKeyPressed(bindSetting.getValue().getKey());
    }

    public boolean isBindDown(Setting<Bind> bindSetting) {
        if (bindSetting != null && bindSetting.getValue() != null && bindSetting.getValue().getKey() != -1 && !fullNullCheck()) {
            Bind value = bindSetting.getValue();
            if (value.isMouse()) {
                return GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), value.getKey()) == 1;
            }
            return InputUtil.isKeyPressed(mc.getWindow().getHandle(), value.getKey());
        }
        return false;
    }

    @Nullable
    public Setting<?> getSettingByName(String name) {
        for (Setting<?> setting : getSettings()) {
            if (setting.getName().equalsIgnoreCase(name)) {
                return setting;
            }
        }
        return null;
    }

    public static class Category {
        private final String name;
        private static final Map<String, Category> CATEGORIES = new LinkedHashMap<>();
        public static final Category COMBAT = new Category("Combat");
        public static final Category MOVEMENT = new Category("Movement");
        public static final Category VISUAL = new Category("Visual");
        public static final Category PLAYER = new Category("Player");
        public static final Category MISC = new Category("Misc");

        private Category(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Category getCategory(String name) {
            return CATEGORIES.computeIfAbsent(name, Category::new);
        }

        public static Collection<Category> values() {
            return CATEGORIES.values();
        }

        public static boolean isCustomCategory(Category category) {
            Set<String> predefined = Set.of("Combat", "Movement", "Visual", "Player", "Misc");
            return !predefined.contains(category.getName());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Category category = (Category) o;
            return Objects.equals(name, category.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        static {
            CATEGORIES.put("Combat", COMBAT);
            CATEGORIES.put("Movement", MOVEMENT);
            CATEGORIES.put("Visual", VISUAL);
            CATEGORIES.put("Player", PLAYER);
            CATEGORIES.put("Misc", MISC);
        }
    }
}