package acore.hack.features.modules.combat;

import acore.hack.features.modules.Module;
import acore.hack.core.manager.FriendManager;
import acore.hack.features.modules.render.ESP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.block.Blocks;
import net.minecraft.item.SwordItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Aura extends Module {
    
    public enum AimMode { HEAD, BODY, LEGS }
    public enum RotationMode { NONE, LEGIT, NORMAL }
    
    // Settings
    public float range = 4.2f;
    public float wallRange = 3.0f;
    public boolean throughWalls = true;
    public boolean onlyESP = false;
    public int aps = 20;
    public RotationMode rotationMode = RotationMode.NORMAL;
    public AimMode aimMode = AimMode.HEAD;
    public boolean autoWeapon = true;
    public boolean autoCrit = true;
    public boolean pauseInCobweb = true;
    public boolean hitWhenBehindWall = true;
    public boolean hitWhenInCobweb = true;
    
    // State
    public LivingEntity target = null;
    public float rotationYaw = 0;
    public float rotationPitch = 0;
    private int hitCooldown = 0;
    
    public Aura() {
        super("Aura", Category.COMBAT);
    }
    
    @Override
    protected void onEnable() {
        target = null;
        hitCooldown = 0;
    }
    
    @Override
    protected void onDisable() {
        target = null;
    }
    
    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;
        
        // Check cobweb
        boolean inCobweb = mc.world.getBlockState(mc.player.getBlockPos()).getBlock() == Blocks.COBWEB;
        
        // Pause logic
        if ((inCobweb && pauseInCobweb) || (!hitWhenBehindWall && isBehindWall())) {
            target = null;
            return;
        }
        
        // Update target
        findTarget();
        
        // Check if target is valid
        if (target == null) return;
        
        // Check if target is behind wall
        boolean targetBehindWall = isTargetBehindWall(target);
        boolean validTarget = true;
        
        if (!throughWalls && targetBehindWall) {
            validTarget = false;
        }
        
        if (onlyESP && !ESP.hasESP(target)) {
            validTarget = false;
        }
        
        if (!validTarget) {
            target = null;
            return;
        }
        
        // Auto weapon
        if (autoWeapon) {
            selectBestWeapon();
        }
        
        // Rotations
        if (rotationMode != RotationMode.NONE) {
            updateRotations();
            mc.player.setYaw(rotationYaw);
            mc.player.setPitch(rotationPitch);
        }
        
        // Attack
        if (hitCooldown <= 0 && canHit()) {
            attack();
            hitCooldown = 20 / aps;
        }
        
        if (hitCooldown > 0) hitCooldown--;
        
        // Auto crit
        if (autoCrit && mc.player.isOnGround() && !inCobweb) {
            mc.player.jump();
        }
    }
    
    private void findTarget() {
        List<Entity> targets = new ArrayList<>();
        
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity && entity != mc.player) {
                targets.add(entity);
            }
        }
        
        if (targets.isEmpty()) {
            target = null;
            return;
        }
        
        // Sort by distance
        targets.sort((e1, e2) -> {
            double d1 = mc.player.distanceTo(e1);
            double d2 = mc.player.distanceTo(e2);
            return Double.compare(d1, d2);
        });
        
        // Find first valid target
        for (Entity entity : targets) {
            LivingEntity living = (LivingEntity) entity;
            
            if (living.isDead() || !living.isAlive()) continue;
            if (living instanceof PlayerEntity) {
                if (FriendManager.isFriend(((PlayerEntity) living).getName().getString())) continue;
            }
            if (mc.player.distanceTo(living) > range) continue;
            if (onlyESP && !ESP.hasESP(living)) continue;
            
            target = living;
            return;
        }
        
        target = null;
    }
    
    private boolean isBehindWall() {
        // Simplified check - will be enhanced later
        return false;
    }
    
    private boolean isTargetBehindWall(LivingEntity entity) {
        if (!throughWalls) {
            // Simple distance-based check
            return mc.player.distanceTo(entity) > range * 0.7;
        }
        return false;
    }
    
    private boolean canHit() {
        boolean inCobweb = mc.world.getBlockState(mc.player.getBlockPos()).getBlock() == Blocks.COBWEB;
        
        if (inCobweb && !hitWhenInCobweb) return false;
        if (isTargetBehindWall(target) && !throughWalls && !hitWhenBehindWall) return false;
        
        return true;
    }
    
    private void updateRotations() {
        if (target == null) return;
        
        Vec3d aimPos = getAimPosition();
        
        double diffX = aimPos.x - mc.player.getX();
        double diffY = aimPos.y - (mc.player.getY() + mc.player.getStandingEyeHeight());
        double diffZ = aimPos.z - mc.player.getZ();
        
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        
        float targetYaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));
        
        if (rotationMode == RotationMode.LEGIT) {
            float yawDiff = targetYaw - mc.player.getYaw();
            float pitchDiff = targetPitch - mc.player.getPitch();
            rotationYaw = mc.player.getYaw() + yawDiff / 4;
            rotationPitch = mc.player.getPitch() + pitchDiff / 4;
        } else {
            rotationYaw = targetYaw;
            rotationPitch = targetPitch;
        }
        
        rotationPitch = MathHelper.clamp(rotationPitch, -90, 90);
    }
    
    private Vec3d getAimPosition() {
        if (target == null) return Vec3d.ZERO;
        
        Box box = target.getBoundingBox();
        double height = box.maxY - box.minY;
        
        switch (aimMode) {
            case HEAD:
                return new Vec3d(target.getX(), target.getY() + height * 0.85, target.getZ());
            case BODY:
                return new Vec3d(target.getX(), target.getY() + height / 2, target.getZ());
            case LEGS:
                return new Vec3d(target.getX(), target.getY() + 0.1, target.getZ());
            default:
                return new Vec3d(target.getX(), target.getY() + height / 2, target.getZ());
        }
    }
    
    private void attack() {
        if (target == null) return;
        
        // Apply rotations if needed
        if (rotationMode != RotationMode.NONE) {
            mc.player.setYaw(rotationYaw);
            mc.player.setPitch(rotationPitch);
        }
        
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
    
    private void selectBestWeapon() {
        int bestSlot = -1;
        float bestDamage = 0;
        
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            
            float damage = 0;
            String itemName = stack.getItem().toString().toLowerCase();
            
            // Check for sword
            if (stack.getItem() instanceof SwordItem) {
                if (itemName.contains("netherite")) damage = 8.0f;
                else if (itemName.contains("diamond")) damage = 7.0f;
                else if (itemName.contains("iron")) damage = 6.0f;
                else if (itemName.contains("stone")) damage = 5.0f;
                else if (itemName.contains("wooden") || itemName.contains("golden")) damage = 4.0f;
                else damage = 7.0f;
            }
            // Check for axe
            else if (stack.getItem() instanceof AxeItem) {
                if (itemName.contains("netherite")) damage = 9.0f;
                else if (itemName.contains("diamond")) damage = 8.0f;
                else if (itemName.contains("iron")) damage = 7.0f;
                else if (itemName.contains("stone")) damage = 6.0f;
                else if (itemName.contains("wooden") || itemName.contains("golden")) damage = 5.0f;
                else damage = 8.0f;
            }
            
            if (damage > bestDamage) {
                bestDamage = damage;
                bestSlot = i;
            }
        }
        
        if (bestSlot != -1 && bestSlot != mc.player.getInventory().selectedSlot) {
            mc.player.getInventory().selectedSlot = bestSlot;
        }
    }
    }
