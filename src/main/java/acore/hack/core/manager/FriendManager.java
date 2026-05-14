package acore.hack.core.manager;

import java.util.ArrayList;
import java.util.List;

public class FriendManager {
    private static List<String> friends = new ArrayList<>();
    
    public static void init() {
        friends = new ArrayList<>();
    }
    
    public static void addFriend(String name) {
        if (!friends.contains(name)) {
            friends.add(name);
            saveFriends();
        }
    }
    
    public static void removeFriend(String name) {
        friends.remove(name);
        saveFriends();
    }
    
    public static boolean isFriend(String name) {
        return friends.contains(name);
    }
    
    public static List<String> getFriends() {
        return new ArrayList<>(friends);
    }
    
    private static void saveFriends() {
        ConfigManager.saveFriends(friends);
    }
    
    public static void loadFriends(List<String> loaded) {
        friends = new ArrayList<>(loaded);
    }
}
