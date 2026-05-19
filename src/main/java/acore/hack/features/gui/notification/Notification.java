package acore.hack.features.gui.notification;

import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class Notification {
    private final String message;
    private final String title;
    private final Type type;
    private final int lifeTime;
    private long createdTime;
    private float animationProgress = 0f;
    private boolean direction = false;
    
    public Notification(String title, String message, Type type, int lifeTime) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.lifeTime = lifeTime;
        this.createdTime = System.currentTimeMillis();
    }
    
    public void render(MatrixStack matrices, float x, float y, float width, float height) {
        int alpha = getAlpha();
        Color bg = new Color(20, 20, 25, alpha);
        
        matrices.push();
        matrices.translate(x, y, 0);
        // Draw notification background
        matrices.pop();
    }
    
    public void onUpdate() {
        boolean finished = System.currentTimeMillis() - createdTime >= lifeTime;
        if (finished != direction) {
            direction = finished;
        }
    }
    
    public boolean shouldDelete() {
        return direction && animationProgress >= 0.95f;
    }
    
    private int getAlpha() {
        return (int)(255 * (1f - animationProgress));
    }
    
    public float getHeight() { return 25f; }
    
    public enum Type {
        INFO, WARNING, ERROR, SUCCESS, ENABLED, DISABLED
    }
      }
