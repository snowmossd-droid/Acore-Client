package acore.hack.features.modules.combat;

import acore.hack.features.modules.Module;
import acore.hack.core.manager.FriendManager;
import acore.hack.features.modules.render.ESP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.block.Blocks;
import net.minecraft.item.SwordItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
    private boolean wasInCobweb = false;
    private boolean wasBehindWall = false;
    
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
        
        // Check if behind wall
        boolean behindWall = isBehindWall();
        
        // Pause logic
        if ((inCobweb && pauseInCobweb) || (!hitWhenBehindWall && behindWall)) {
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
        
        wasInCobweb = inCobweb;
        wasBehindWall = behindWall;
    }
    
    private void findTarget() {
        List<Entity> targets = mc.world.getEntities().stream()
            .filter(e -> e instanceof LivingEntity)
            .filter(e -> e != mc.player)
            .filter(e -> !FriendManager.isFriend(e.getName().getString()))
            .filter(e -> !onlyESP || ESP.hasESP(e))
            .filter(e -> mc.player.distanceTo(e) <= range)
            .sorted(Comparator.comparingDouble(e -> mc.player.distanceTo(e)))
            .collect(Collectors.toList());
        
        if (!targets.isEmpty()) {
            target = (LivingEntity) targets.get(0);
        } else {
            target = null;
        }
    }
    
    private boolean isBehindWall() {
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d targetPos = eyePos.add(mc.player.getRotationVector().multiply(5));
        return !mc.world.raycast(new net.minecraft.util.math.RaycastContext(
            eyePos, targetPos,
            net.minecraft.util.math.RaycastContext.ShapeType.COLLIDER,
            net.minecraft.util.math.RaycastContext.FluidHandling.NONE,
            mc.player
        )).getPos().equals(targetPos);
    }
    
    private boolean isTargetBehindWall(LivingEntity entity) {
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d targetEyePos = entity.getEyePos();
        var result = mc.world.raycast(new net.minecraft.util.math.RaycastContext(
            eyePos, targetEyePos,
            net.minecraft.util.math.RaycastContext.ShapeType.COLLIDER,
            net.minecraft.util.math.RaycastContext.FluidHandling.NONE,
            mc.player
        ));
        return !result.getPos().equals(targetEyePos);
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
        
        return switch (aimMode) {
            case HEAD -> new Vec3d(target.getX(), target.getY() + target.getHeight() * 0.85, target.getZ());
            case BODY -> new Vec3d(target.getX(), target.getY() + target.getHeight() / 2, target.getZ());
            case LEGS -> new Vec3d(target.getX(), target.getY() + 0.1, target.getZ());
        };
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
            if (stack.getItem() instanceof SwordItem) {
                float damage = ((SwordItem) stack.getItem()).getAttackDamage();
                if (damage > bestDamage) {
                    bestDamage = damage;
                    bestSlot = i;
                }
            } else if (stack.getItem() instanceof AxeItem) {
                float damage = ((AxeItem) stack.getItem()).getAttackDamage();
                if (damage > bestDamage) {
                    bestDamage = damage;
                    bestSlot = i;
                }
            }
        }
        
        if (bestSlot != -1 && bestSlot != mc.player.getInventory().selectedSlot) {
            mc.player.getInventory().selectedSlot = bestSlot;
        }
    }
    
    public boolean shouldRotate() {
        return rotationMode != RotationMode.NONE && target != null;
    }
    }
