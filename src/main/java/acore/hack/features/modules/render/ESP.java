package acore.hack.features.modules.render;

import acore.hack.features.modules.Module;
import acore.hack.core.manager.FriendManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RotationAxis;
import java.awt.Color;

public class ESP extends Module {
    
    private static ESP instance;
    
    // Settings
    public boolean players = true;
    public boolean friends = true;
    public boolean creatures = false;
    public boolean monsters = false;
    public boolean outline = true;
    public boolean renderHealth = true;
    
    // Colors
    public Color playerColor = new Color(0xFF9200);
    public Color friendColor = new Color(0x30FF00);
    public Color monsterColor = new Color(0xFF0000);
    public Color creatureColor = new Color(0xA0A4A6);
    
    public ESP() {
        super("ESP", Category.RENDER);
        instance = this;
    }
    
    public static boolean hasESP(Entity entity) {
        return instance != null && instance.isEnabled();
    }
    
    public boolean shouldRender(Entity entity) {
        if (entity == null || mc.player == null) return false;
        if (entity == mc.player) return false;
        
        if (entity instanceof PlayerEntity) {
            if (FriendManager.isFriend(((PlayerEntity) entity).getName().getString()))
                return friends;
            return players;
        }
        
        if (entity instanceof net.minecraft.entity.mob.HostileEntity)
            return monsters;
        
        if (entity instanceof net.minecraft.entity.passive.PassiveEntity)
            return creatures;
        
        return false;
    }
    
    public Color getEntityColor(Entity entity) {
        if (entity instanceof PlayerEntity) {
            if (FriendManager.isFriend(((PlayerEntity) entity).getName().getString()))
                return friendColor;
            return playerColor;
        }
        if (entity instanceof net.minecraft.entity.mob.HostileEntity)
            return monsterColor;
        if (entity instanceof net.minecraft.entity.passive.PassiveEntity)
            return creatureColor;
        return Color.WHITE;
    }
    
    @Override
    public void onRender() {
        if (mc.world == null || mc.player == null) return;
        
        for (Entity entity : mc.world.getEntities()) {
            if (shouldRender(entity)) {
                renderEntityESP(entity);
            }
        }
    }
    
    private void renderEntityESP(Entity entity) {
        if (entity == null) return;
        
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        
        Box box = entity.getBoundingBox();
        double minX = box.minX;
        double minY = box.minY;
        double minZ = box.minZ;
        double maxX = box.maxX;
        double maxY = box.maxY;
        double maxZ = box.maxZ;
        
        Color color = getEntityColor(entity);
        
        // Draw box outline using world rendering
        drawBoundingBox(minX, minY, minZ, maxX, maxY, maxZ, color);
        
        // Draw health bar
        if (renderHealth && entity instanceof LivingEntity living) {
            float health = living.getHealth();
            float maxHealth = living.getMaxHealth();
            if (maxHealth > 0) {
                float percent = health / maxHealth;
                double width = maxX - minX;
                double xPos = minX;
                double yPos = maxY + 0.2;
                Color healthColor = new Color((int)(255 * (1 - percent)), (int)(255 * percent), 0);
                drawHealthBar(xPos, yPos, width, 0.05, percent, healthColor);
            }
        }
    }
    
    private void drawBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Color color) {
        // This will be implemented later with proper rendering
    }
    
    private void drawHealthBar(double x, double y, double width, double height, float percent, Color color) {
        // This will be implemented later with proper rendering
    }
                }
