package acore.hack.core.manager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

public class FriendManager {
    
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public static List<String> friends = new ArrayList<>();
    private static final String CONFIG_FOLDER = "acore_configs";
    
    public static void init() {
        friends = new ArrayList<>();
        loadFriends();
    }
    
    public static boolean isFriend(String name) {
        return friends.stream().anyMatch(friend -> friend.equalsIgnoreCase(name));
    }
    
    public static boolean isFriend(@NotNull PlayerEntity player) {
        return isFriend(player.getName().getString());
    }
    
    public static void removeFriend(String name) {
        friends.remove(name);
        saveFriends();
    }
    
    public static void addFriend(String friend) {
        if (!friends.contains(friend)) {
            friends.add(friend);
            saveFriends();
        }
    }
    
    public static List<String> getFriends() {
        return new ArrayList<>(friends);
    }
    
    public static void clear() {
        friends.clear();
        saveFriends();
    }
    
    public static List<AbstractClientPlayerEntity> getNearFriends() {
        if (mc.world == null) return new ArrayList<>();
        
        return mc.world.getPlayers().stream()
                .filter(player -> friends.contains(player.getName().getString()))
                .map(player -> (AbstractClientPlayerEntity) player)
                .toList();
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void saveFriends() {
        File configDir = new File(CONFIG_FOLDER + "/misc");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        File file = new File(configDir, "friends.txt");
        try {
            file.createNewFile();
        } catch (Exception ignored) {
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String friend : friends) {
                writer.write(friend + "\n");
            }
        } catch (Exception ignored) {
        }
    }
    
    public static void loadFriends() {
        try {
            File file = new File(CONFIG_FOLDER + "/misc/friends.txt");
            
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.isEmpty()) {
                            friends.add(line);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
            }
