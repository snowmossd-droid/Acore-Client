package acore.hack.features.modules.combat;

import acore.hack.features.modules.Module;
import acore.hack.core.manager.ModuleManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.Blocks;

public class TargetStrafe extends Module {
    
    // Settings
    public boolean jump = true;
    public float distance = 1.3f;
    public BoostMode boost = BoostMode.None;
    public float strafeSpeed = 1.0f;
    public boolean smooth = true;
    public float smoothness = 0.85f;
    
    // State variables
    private boolean switchDir = false;
    private int jumpCooldown = 0;
    private int waterTicks = 0;
    private double oldSpeed = 0;
    private float lastYaw = 0;
    private int noClipTicks = 0;
    
    public enum BoostMode {
        None, Elytra, Damage, Speed
    }
    
    public TargetStrafe() {
        super("TargetStrafe", Category.COMBAT);
    }
    
    @Override
    protected void onEnable() {
        switchDir = false;
        oldSpeed = 0;
        jumpCooldown = 0;
        noClipTicks = 0;
    }
    
    @Override
    protected void onDisable() {
        // Reset movement
        if (mc.player != null) {
            mc.player.setVelocity(mc.player.getVelocity().x, mc.player.getVelocity().y, mc.player.getVelocity().z);
        }
    }
    
    private boolean canStrafe() {
        if (mc.player == null) return false;
        if (mc.player.isSneaking()) return false;
        if (mc.player.isInLava()) return false;
        if (mc.player.isTouchingWater() || waterTicks > 0) return false;
        if (mc.player.getAbilities().flying) return false;
        if (mc.player.isFallFlying()) return false;
        if (mc.player.horizontalCollision) return false;
        
        // Check for cobweb
        if (mc.world.getBlockState(mc.player.getBlockPos()).getBlock() == Blocks.COBWEB) return false;
        
        return true;
    }
    
    private boolean isBlockInWay(double x, double z) {
        BlockPos pos = new BlockPos((int) Math.floor(x), (int) mc.player.getY(), (int) Math.floor(z));
        return !mc.world.isAir(pos) && mc.world.getBlockState(pos).getBlock() != Blocks.AIR;
    }
    
    private Vec3d getStrafePosition() {
        Aura aura = (Aura) ModuleManager.getModule("Aura");
        if (aura == null || aura.target == null) return null;
        
        double distToTarget = mc.player.distanceTo(aura.target);
        double radius = Math.min(distance, distToTarget - 0.5);
        if (radius < 0.5) radius = 0.8;
        
        double angle = Math.atan2(mc.player.getZ() - aura.target.getZ(), mc.player.getX() - aura.target.getX());
        
        // Smooth direction switching
        double dirChange = strafeSpeed / Math.max(0.5, distToTarget);
        
        if (shouldSwitchDirection()) {
            switchDir = !switchDir;
            noClipTicks = 5;
        }
        
        angle += (switchDir ? dirChange : -dirChange);
        
        double x = aura.target.getX() + radius * Math.cos(angle);
        double z = aura.target.getZ() + radius * Math.sin(angle);
        
        // Avoid blocks
        if (isBlockInWay(x, z) && noClipTicks <= 0) {
            switchDir = !switchDir;
            angle += (switchDir ? dirChange * 2 : -dirChange * 2);
            x = aura.target.getX() + radius * Math.cos(angle);
            z = aura.target.getZ() + radius * Math.sin(angle);
        }
        
        return new Vec3d(x, mc.player.getY(), z);
    }
    
    private boolean shouldSwitchDirection() {
        if (mc.player.horizontalCollision) return true;
        
        // Check walls in current direction
        Vec3d pos = getStrafePosition();
        if (pos != null && isBlockInWay(pos.x, pos.z)) return true;
        
        return false;
    }
    
    private double getOptimalSpeed() {
        double baseSpeed = mc.player.getMovementSpeed();
        
        // Boost handling
        switch (boost) {
            case Elytra:
                if (mc.player.isFallFlying()) {
                    return Math.min(1.8, baseSpeed * 2.5);
                }
                break;
            case Damage:
                if (mc.player.hurtTime > 0) {
                    return Math.min(1.2, baseSpeed * 1.8);
                }
                break;
            case Speed:
                return baseSpeed * 1.3;
            default:
                break;
        }
        
        // Jump boost
        if (mc.player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.JUMP_BOOST)) {
            baseSpeed *= 1.15;
        }
        
        // Speed effect
        if (mc.player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.SPEED)) {
            baseSpeed *= 1.2;
        }
        
        return Math.min(0.42, baseSpeed);
    }
    
    private float getStrafeYaw(Vec3d targetPos) {
        double diffX = targetPos.x - mc.player.getX();
        double diffZ = targetPos.z - mc.player.getZ();
        return (float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90);
    }
    
    private void updateJump() {
        if (!jump) return;
        if (jumpCooldown > 0) {
            jumpCooldown--;
            return;
        }
        
        Aura aura = (Aura) ModuleManager.getModule("Aura");
        if (aura == null || aura.target == null) return;
        
        // Jump when close to target or on ground
        double dist = mc.player.distanceTo(aura.target);
        if (mc.player.isOnGround() && dist < 2.5) {
            mc.player.jump();
            jumpCooldown = 15;
        }
    }
    
    public void onUpdate() {
        if (mc.player == null) return;
        
        // Update water ticks
        if (mc.player.isTouchingWater()) {
            waterTicks = 5;
        } else if (waterTicks > 0) {
            waterTicks--;
        }
        
        // Update old speed (anti-lag)
        oldSpeed = Math.hypot(mc.player.getX() - mc.player.prevX, mc.player.getZ() - mc.player.prevZ);
        
        // Update no clip ticks
        if (noClipTicks > 0) noClipTicks--;
        
        updateJump();
    }
    
    public void onMove() {
        if (!canStrafe()) return;
        
        Aura aura = (Aura) ModuleManager.getModule("Aura");
        if (aura == null || !aura.isEnabled() || aura.target == null) return;
        
        Vec3d strafePos = getStrafePosition();
        if (strafePos == null) return;
        
        double speed = getOptimalSpeed();
        float yaw = getStrafeYaw(strafePos);
        
        // Smooth rotation
        if (smooth) {
            float yawDiff = yaw - lastYaw;
            yaw = lastYaw + yawDiff * smoothness;
        }
        lastYaw = yaw;
        
        // Calculate motion
        float rad = (float) Math.toRadians(yaw);
        double motionX = -Math.sin(rad) * speed;
        double motionZ = Math.cos(rad) * speed;
        
        // Apply motion
        mc.player.setVelocity(motionX, mc.player.getVelocity().y, motionZ);
        
        // Update player rotation for smoother movement
        if (smooth) {
            mc.player.setYaw(yaw);
        }
    }
    
    public void onPreMotion() {
        if (!canStrafe()) return;
        
        Aura aura = (Aura) ModuleManager.getModule("Aura");
        if (aura == null || !aura.isEnabled() || aura.target == null) return;
        
        // Fix for Grim/Matrix anti-cheat - don't send too many movement packets
        if (mc.player.age % 2 == 0) {
            Vec3d strafePos = getStrafePosition();
            if (strafePos != null) {
                float yaw = getStrafeYaw(strafePos);
                mc.player.setYaw(yaw);
            }
        }
    }
    
    public boolean shouldModifyVelocity() {
        return canStrafe() && ModuleManager.getModule("Aura") != null && 
               ((Aura) ModuleManager.getModule("Aura")).isEnabled() &&
               ((Aura) ModuleManager.getModule("Aura")).target != null;
    }
    
    public Vec3d getModifiedVelocity(Vec3d original) {
        if (!shouldModifyVelocity()) return original;
        
        Aura aura = (Aura) ModuleManager.getModule("Aura");
        if (aura == null || aura.target == null) return original;
        
        double speed = getOptimalSpeed();
        Vec3d toTarget = new Vec3d(aura.target.getX() - mc.player.getX(), 0, aura.target.getZ() - mc.player.getZ()).normalize();
        
        // Perpendicular vector for strafing
        Vec3d strafeVec = new Vec3d(-toTarget.z, 0, toTarget.x);
        strafeVec = strafeVec.multiply(switchDir ? 1 : -1);
        
        // Combine forward and strafe
        Vec3d result = toTarget.multiply(0.3).add(strafeVec.multiply(speed));
        
        return new Vec3d(result.x, original.y, result.z);
    }
          }
