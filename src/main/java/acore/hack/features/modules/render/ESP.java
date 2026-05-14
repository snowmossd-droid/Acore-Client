package acore.hack.features.modules.render;

import acore.hack.features.modules.Module;
import acore.hack.core.manager.FriendManager;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import java.awt.Color;
import java.util.Objects;

public class ESP extends Module {
    
    private static ESP instance;
    
    public ESP() {
        super("ESP", Category.RENDER);
        instance = this;
    }
    
    public static boolean hasESP(Entity entity) {
        return instance != null && instance.isEnabled() && instance.shouldRender(entity);
    }
    
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
    public Color enemyColor = new Color(0xFF0000);
    public Color creatureColor = new Color(0xA0A4A6);
    public Color monsterColor = new Color(0xFF0000);
    
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
    
    public void onRender3D(MatrixStack stack) {
        if (mc.world == null || mc.player == null) return;
        
        for (Entity entity : mc.world.getEntities()) {
            if (shouldRender(entity)) {
                drawEntityESP(stack, entity);
            }
        }
    }
    
    private void drawEntityESP(MatrixStack stack, Entity entity) {
        if (entity == null) return;
        
        double x = entity.prevX + (entity.getX() - entity.prevX) * mc.getTickDelta();
        double y = entity.prevY + (entity.getY() - entity.prevY) * mc.getTickDelta();
        double z = entity.prevZ + (entity.getZ() - entity.prevZ) * mc.getTickDelta();
        
        Box box = entity.getBoundingBox();
        double minX = box.minX - entity.getX() + x - 0.05;
        double minY = box.minY - entity.getY() + y;
        double minZ = box.minZ - entity.getZ() + z - 0.05;
        double maxX = box.maxX - entity.getX() + x + 0.05;
        double maxY = box.maxY - entity.getY() + y + 0.15;
        double maxZ = box.maxZ - entity.getZ() + z + 0.05;
        
        Color color = getEntityColor(entity);
        
        // Draw box outline
        drawBoxOutline(stack, minX, minY, minZ, maxX, maxY, maxZ, color);
        
        // Draw health bar
        if (renderHealth && entity instanceof LivingEntity living) {
            float health = living.getHealth();
            float maxHealth = living.getMaxHealth();
            float healthPercent = health / maxHealth;
            
            double width = (maxX - minX);
            double xPos = (minX + maxX) / 2 - width / 2;
            double yPos = maxY + 0.2;
            
            Color healthColor = new Color((int) (255 * (1 - healthPercent)), (int) (255 * healthPercent), 0);
            drawHealthBar(stack, xPos, yPos, width, 0.05, healthPercent, healthColor);
        }
    }
    
    private void drawBoxOutline(MatrixStack stack, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Color color) {
        Vec3d camera = mc.gameRenderer.getCamera().getPos();
        
        stack.push();
        stack.translate(minX - camera.x, minY - camera.y, minZ - camera.z);
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        // Bottom face
        buffer.vertex(0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(maxX - minX, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(maxX - minX, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(maxX - minX, 0, maxZ - minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(maxX - minX, 0, maxZ - minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(0, 0, maxZ - minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(0, 0, maxZ - minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        
        // Top face
        buffer.vertex(0, maxY - minY, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(maxX - minX, maxY - minY, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(maxX - minX, maxY - minY, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(maxX - minX, maxY - minY, maxZ - minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(maxX - minX, maxY - minY, maxZ - minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(0, maxY - minY, maxZ - minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(0, maxY - minY, maxZ - minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(0, maxY - minY, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        
        // Vertical edges
        buffer.vertex(0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(0, maxY - minY, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(maxX - minX, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(maxX - minX, maxY - minY, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(maxX - minX, 0, maxZ - minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(maxX - minX, maxY - minY, maxZ - minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(0, 0, maxZ - minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(0, maxY - minY, maxZ - minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder.BuildResult result = buffer.end();
        BufferDrawer drawer = tessellator.draw(result);
        drawer.draw();
        
        stack.pop();
    }
    
    private void drawHealthBar(MatrixStack stack, double x, double y, double width, double height, float percent, Color color) {
        Vec3d camera = mc.gameRenderer.getCamera().getPos();
        
        stack.push();
        stack.translate(x - camera.x, y - camera.y, 0);
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        
        // Background
        buffer.vertex(0, 0, 0).color(0, 0, 0, 150);
        buffer.vertex(width, 0, 0).color(0, 0, 0, 150);
        buffer.vertex(width, height, 0).color(0, 0, 0, 150);
        buffer.vertex(0, height, 0).color(0, 0, 0, 150);
        
        // Health fill
        double fillWidth = width * percent;
        buffer.vertex(0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(fillWidth, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(fillWidth, height, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(0, height, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder.BuildResult result = buffer.end();
        BufferDrawer drawer = tessellator.draw(result);
        drawer.draw();
        
        stack.pop();
    }
}
