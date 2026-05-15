package acore.hack.features.modules.combat;

import acore.hack.features.modules.Module;
import acore.hack.core.manager.FriendManager;
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
import java.util.Comparator;
import java.util.List;

public class Aura extends Module {
    
    // ==================== ENUMS ====================
    public enum AimMode { HEAD, BODY, LEGS }
    public enum RotationMode { NONE, LEGIT, NORMAL }
    public enum SortMode { LowestDistance, HighestDistance, LowestHealth, HighestHealth }
    
    // ==================== SETTINGS ====================
    // Range Settings
    public float range = 4.2f;
    public boolean throughWalls = false;
    public int aps = 20;
    
    // Rotation Settings
    public RotationMode rotationMode = RotationMode.NORMAL;
    public AimMode aimMode = AimMode.HEAD;
    public boolean clientLook = false;
    public boolean smoothRotation = true;
    public float smoothSpeed = 0.85f;
    
    // Target Settings
    public SortMode sort = SortMode.LowestDistance;
    public boolean lockTarget = true;
    public boolean players = true;
    public boolean onlyESP = false;
    public boolean ignoreInvisible = false;
    public boolean ignoreCreative = true;
    
    // Anti-Cheat Settings
    public boolean pauseInCobweb = true;
    public boolean hitWhenInCobweb = false;
    public boolean pauseWhileEating = false;
    public boolean randomHitDelay = true;
    
    // Combat Settings
    public boolean autoWeapon = true;
    public boolean onlyWeapon = false;
    public boolean autoCrit = true;
    public boolean autoJump = false;
    
    // ESP Settings
    public boolean espEnabled = true;
    public boolean espBox = true;
    public boolean espHealth = true;
    
    // ==================== STATE ====================
    public LivingEntity target = null;
    public float rotationYaw = 0;
    public float rotationPitch = 0;
    private int hitCooldown = 0;
    private boolean wasSprinting = false;
    
    public Aura() {
        super("Aura", Category.COMBAT);
    }
    
    @Override
    protected void onEnable() {
        target = null;
        hitCooldown = 0;
        rotationYaw = mc.player.getYaw();
        rotationPitch = mc.player.getPitch();
    }
    
    @Override
    protected void onDisable() {
        target = null;
        if (wasSprinting) {
            mc.player.setSprinting(true);
            wasSprinting = false;
        }
    }
    
    private int getHitDelay() {
        if (randomHitDelay) {
            return (int) (Math.random() * 3) + 1;
        }
        return 20 / Math.max(aps, 1);
    }
    
    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;
        
        // Check pause conditions
        if (pauseWhileEating && mc.player.isUsingItem()) return;
        
        // Check cobweb
        boolean inCobweb = mc.world.getBlockState(mc.player.getBlockPos()).getBlock() == Blocks.COBWEB;
        if (inCobweb && pauseInCobweb) {
            target = null;
            return;
        }
        
        // Update target
        updateTarget();
        
        // Check if target is valid
        if (target == null) return;
        
        // Check friend
        if (target instanceof PlayerEntity && FriendManager.isFriend(((PlayerEntity) target).getName().getString())) {
            target = null;
            return;
        }
        
        // Check wall
        if (!throughWalls && !mc.player.canSee(target)) {
            target = null;
            return;
        }
        
        // Check cobweb target
        boolean targetInCobweb = mc.world.getBlockState(target.getBlockPos()).getBlock() == Blocks.COBWEB;
        if (targetInCobweb && !hitWhenInCobweb) {
            target = null;
            return;
        }
        
        // Check ESP only
        if (onlyESP && !espEnabled) {
            target = null;
            return;
        }
        
        // Auto weapon
        if (autoWeapon) {
            selectBestWeapon();
        }
        
        // Check weapon
        if (onlyWeapon && !hasWeapon()) return;
        
        // Handle sprint
        if (mc.player.isSprinting()) {
            wasSprinting = true;
            mc.player.setSprinting(false);
        }
        
        // Auto jump
        if (autoJump && mc.player.isOnGround() && !inCobweb) {
            mc.player.jump();
        }
        
        // Auto crit
        if (autoCrit && mc.player.isOnGround() && !inCobweb && hitCooldown <= 0) {
            mc.player.jump();
        }
        
        // Update rotations
        updateRotations();
        
        // Apply rotations
        if (rotationMode != RotationMode.NONE && target != null) {
            if (smoothRotation && rotationMode == RotationMode.LEGIT) {
                float yawDiff = rotationYaw - mc.player.getYaw();
                float pitchDiff = rotationPitch - mc.player.getPitch();
                mc.player.setYaw(mc.player.getYaw() + yawDiff * smoothSpeed);
                mc.player.setPitch(mc.player.getPitch() + pitchDiff * smoothSpeed);
            } else if (rotationMode == RotationMode.NORMAL) {
                mc.player.setYaw(rotationYaw);
                mc.player.setPitch(rotationPitch);
            }
        }
        
        // Attack
        if (hitCooldown <= 0 && target != null) {
            attack();
            hitCooldown = getHitDelay();
        }
        
        if (hitCooldown > 0) hitCooldown--;
        
        // Restore sprint
        if (wasSprinting && hitCooldown <= 0) {
            mc.player.setSprinting(true);
            wasSprinting = false;
        }
    }
    
    private boolean hasWeapon() {
        ItemStack mainHand = mc.player.getMainHandStack();
        return mainHand.getItem() instanceof SwordItem || 
               mainHand.getItem() instanceof AxeItem;
    }
    
    private void updateTarget() {
        List<Entity> targets = new ArrayList<>();
        
        for (Entity entity : mc.world.getEntities()) {
            if (shouldSkipEntity(entity)) continue;
            if (!(entity instanceof LivingEntity)) continue;
            targets.add(entity);
        }
        
        if (targets.isEmpty()) {
            if (!lockTarget) target = null;
            return;
        }
        
        // Sort targets
        switch (sort) {
            case LowestDistance:
                targets.sort(Comparator.comparingDouble(e -> mc.player.distanceTo(e)));
                break;
            case HighestDistance:
                targets.sort(Comparator.comparingDouble(e -> -mc.player.distanceTo(e)));
                break;
            case LowestHealth:
                targets.sort(Comparator.comparingDouble(e -> ((LivingEntity) e).getHealth()));
                break;
            case HighestHealth:
                targets.sort(Comparator.comparingDouble(e -> -((LivingEntity) e).getHealth()));
                break;
        }
        
        Entity newTarget = targets.get(0);
        if (target == null || !lockTarget) {
            target = (LivingEntity) newTarget;
        }
    }
    
    private boolean shouldSkipEntity(Entity entity) {
        if (!(entity instanceof LivingEntity ent)) return true;
        if (ent.isDead() || !entity.isAlive()) return true;
        if (entity == mc.player) return true;
        
        // Distance check
        if (mc.player.distanceTo(entity) > range) return true;
        
        // Wall check
        if (!throughWalls && !mc.player.canSee(entity)) return true;
        
        // Cobweb check
        boolean inCobweb = mc.world.getBlockState(entity.getBlockPos()).getBlock() == Blocks.COBWEB;
        if (inCobweb && !hitWhenInCobweb) return true;
        
        // ESP check
        if (onlyESP && !espEnabled) return true;
        
        // Type check
        if (entity instanceof PlayerEntity player) {
            if (!players) return true;
            if (FriendManager.isFriend(player.getName().getString())) return true;
            if (ignoreCreative && player.isCreative()) return true;
            if (ignoreInvisible && player.isInvisible()) return true;
        } else {
            return true;
        }
        
        return false;
    }
    
    private void updateRotations() {
        if (target == null) return;
        
        Vec3d aimPos = getAimPosition();
        if (aimPos == null) return;
        
        double diffX = aimPos.x - mc.player.getX();
        double diffY = aimPos.y - (mc.player.getY() + mc.player.getStandingEyeHeight());
        double diffZ = aimPos.z - mc.player.getZ();
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        
        float targetYaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));
        targetPitch = MathHelper.clamp(targetPitch, -90, 90);
        
        rotationYaw = targetYaw;
        rotationPitch = targetPitch;
    }
    
    private Vec3d getAimPosition() {
        if (target == null) return null;
        
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
                return target.getEyePos();
        }
    }
    
    private void attack() {
        if (target == null) return;
        
        // Apply rotations for normal mode
        if (rotationMode == RotationMode.NORMAL) {
            mc.player.setYaw(rotationYaw);
            mc.player.setPitch(rotationPitch);
        }
        
        // Attack
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
            
            if (stack.getItem() instanceof SwordItem) {
                if (itemName.contains("netherite")) damage = 8.0f;
                else if (itemName.contains("diamond")) damage = 7.0f;
                else if (itemName.contains("iron")) damage = 6.0f;
                else if (itemName.contains("stone")) damage = 5.0f;
                else if (itemName.contains("wooden") || itemName.contains("golden")) damage = 4.0f;
                else damage = 7.0f;
            } else if (stack.getItem() instanceof AxeItem) {
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
    
    // Client look for render
    public void onRender() {
        if (clientLook && rotationMode != RotationMode.NONE && target != null) {
            mc.player.setYaw(rotationYaw);
            mc.player.setPitch(rotationPitch);
        }
    }
}