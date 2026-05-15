package acore.hack.features.modules.combat;

import acore.hack.features.modules.Module;
import acore.hack.core.manager.FriendManager;
import acore.hack.core.manager.ModuleManager;
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
import net.minecraft.item.Items;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Aura extends Module {
    
    // ==================== ENUMS ====================
    public enum AimMode { HEAD, BODY, LEGS }
    public enum RotationMode { NONE, LEGIT, NORMAL }
    public enum SortMode { LowestDistance, HighestDistance, LowestHealth, HighestHealth, FOV }
    public enum WallsBypass { Off, V1, V2 }
    public enum RayTrace { OFF, OnlyTarget, AllEntities }
    public enum ESPMode { Off, Box, BoxHealth, Full, Tracer }
    
    // ==================== MAIN SETTINGS ====================
    public boolean enabled = true;
    public float range = 4.2f;
    public float wallRange = 3.1f;
    public boolean throughWalls = false;
    public int aps = 20;
    public SortMode sort = SortMode.LowestDistance;
    public boolean lockTarget = true;
    
    // ==================== TARGET SETTINGS ====================
    public boolean players = true;
    public boolean monsters = false;
    public boolean animals = false;
    public boolean onlyESP = false;
    public boolean ignoreInvisible = false;
    public boolean ignoreNamed = false;
    public boolean ignoreTeam = false;
    public boolean ignoreCreative = true;
    
    // ==================== ROTATION SETTINGS ====================
    public RotationMode rotationMode = RotationMode.NORMAL;
    public AimMode aimMode = AimMode.HEAD;
    public int fov = 180;
    public boolean clientLook = false;
    public boolean smoothRotation = true;
    public float smoothSpeed = 0.85f;
    
    // ==================== WEAPON SETTINGS ====================
    public boolean autoWeapon = true;
    public boolean onlyWeapon = false;
    public boolean shieldBreaker = true;
    public boolean autoCrit = true;
    public boolean autoJump = false;
    
    // ==================== ANTI-CHEAT SETTINGS ====================
    public boolean pauseInCobweb = true;
    public boolean hitWhenBehindWall = false;
    public boolean hitWhenInCobweb = false;
    public boolean pauseWhileEating = false;
    public boolean pauseInInventory = true;
    public boolean randomHitDelay = true;
    public int minDelay = 1;
    public int maxDelay = 3;
    
    // ==================== ANTI-BAN (Matrix/Grim/Vulcan/Spartan) ====================
    public boolean antiBan = true;
    public boolean limitRotations = true;
    public boolean randomizePackets = true;
    public boolean fakeLag = false;
    public int fakeLagTicks = 2;
    public boolean autoReset = true;
    
    // ==================== ESP SETTINGS (giống ThunderHack) ====================
    public ESPMode espMode = ESPMode.Full;
    public boolean espBox = true;
    public boolean espHealth = true;
    public boolean espTracer = true;
    public boolean espName = true;
    public Color espColor = new Color(0xFF9200);
    public Color espFriendColor = new Color(0x30FF00);
    public Color espEnemyColor = new Color(0xFF0000);
    
    // ==================== STATE ====================
    public static Entity target;
    public float rotationYaw = 0;
    public float rotationPitch = 0;
    private int hitCooldown = 0;
    private int hitTicks = 0;
    private boolean wasSprinting = false;
    private int randomDelay = 0;
    private int fakeLagCounter = 0;
    
    public Aura() {
        super("Aura", Category.COMBAT);
    }
    
    private float getCurrentRange() {
        return range;
    }
    
    private float getCurrentWallRange() {
        return wallRange;
    }
    
    @Override
    protected void onEnable() {
        target = null;
        hitCooldown = 0;
        hitTicks = 0;
        rotationYaw = mc.player.getYaw();
        rotationPitch = mc.player.getPitch();
        randomDelay = getRandomDelay();
    }
    
    @Override
    protected void onDisable() {
        target = null;
        if (wasSprinting) {
            mc.player.setSprinting(true);
            wasSprinting = false;
        }
    }
    
    private int getRandomDelay() {
        if (!randomHitDelay) return 0;
        return (int) (Math.random() * (maxDelay - minDelay + 1) + minDelay);
    }
    
    private boolean shouldSkipTick() {
        if (!antiBan) return false;
        // Randomize packet timing to avoid anti-cheat detection
        if (randomizePackets && hitCooldown > 0) {
            return Math.random() < 0.1;
        }
        return false;
    }
    
    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;
        
        // Anti-ban: fake lag
        if (antiBan && fakeLag) {
            fakeLagCounter++;
            if (fakeLagCounter < fakeLagTicks) return;
            fakeLagCounter = 0;
        }
        
        // Check pause conditions
        if (pauseWhileEating && mc.player.isUsingItem()) return;
        if (pauseInInventory && mc.currentScreen != null) return;
        
        // Check cobweb
        boolean inCobweb = mc.world.getBlockState(mc.player.getBlockPos()).getBlock() == Blocks.COBWEB;
        if (inCobweb && pauseInCobweb) {
            target = null;
            return;
        }
        
        // Update target
        updateTarget();
        
        // Check target validity
        if (target == null) return;
        
        // Check friend
        if (target instanceof PlayerEntity && FriendManager.isFriend(((PlayerEntity) target).getName().getString())) {
            target = null;
            return;
        }
        
        // Check wall
        boolean targetBehindWall = !mc.player.canSee(target);
        if (!throughWalls && targetBehindWall && !hitWhenBehindWall) {
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
        if (onlyESP && !isESPEnabled(target)) {
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
            if (antiBan) {
                mc.player.setSprinting(false);
            }
        }
        
        // Auto jump
        if (autoJump && mc.player.isOnGround() && !inCobweb) {
            mc.player.jump();
        }
        
        // Auto crit
        if (autoCrit && mc.player.isOnGround() && !inCobweb && hitCooldown <= 0) {
            mc.player.jump();
        }
        
        // Calculate rotations
        boolean readyForAttack = canHit() && (rotationMode != RotationMode.NONE);
        updateRotations(readyForAttack);
        
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
        if (readyForAttack && hitCooldown <= 0 && target != null && !shouldSkipTick()) {
            attack();
            hitCooldown = getRandomDelay() + (20 / Math.max(aps, 1));
            hitTicks = 5;
            randomDelay = getRandomDelay();
        }
        
        if (hitCooldown > 0) hitCooldown--;
        if (hitTicks > 0) hitTicks--;
        
        // Auto reset (fix anti-cheat flags)
        if (antiBan && autoReset && hitCooldown == 0 && target == null) {
            rotationYaw = mc.player.getYaw();
            rotationPitch = mc.player.getPitch();
        }
        
        // Restore sprint
        if (wasSprinting && hitCooldown <= 0) {
            mc.player.setSprinting(true);
            wasSprinting = false;
        }
    }
    
    private boolean isESPEnabled(Entity entity) {
        return espMode != ESPMode.Off;
    }
    
    private boolean hasWeapon() {
        ItemStack mainHand = mc.player.getMainHandStack();
        return mainHand.getItem() instanceof SwordItem || 
               mainHand.getItem() instanceof AxeItem;
    }
    
    private void updateTarget() {
        List<LivingEntity> targets = new CopyOnWriteArrayList<>();
        
        for (Entity entity : mc.world.getEntities()) {
            if (shouldSkipEntity(entity)) continue;
            if (!(entity instanceof LivingEntity)) continue;
            targets.add((LivingEntity) entity);
        }
        
        if (targets.isEmpty()) {
            if (!lockTarget) target = null;
            return;
        }
        
        Entity newTarget = null;
        switch (sort) {
            case LowestDistance:
                newTarget = targets.stream().min(Comparator.comparingDouble(e -> mc.player.distanceTo(e))).orElse(null);
                break;
            case HighestDistance:
                newTarget = targets.stream().max(Comparator.comparingDouble(e -> mc.player.distanceTo(e))).orElse(null);
                break;
            case LowestHealth:
                newTarget = targets.stream().min(Comparator.comparingDouble(e -> e.getHealth() + e.getAbsorptionAmount())).orElse(null);
                break;
            case HighestHealth:
                newTarget = targets.stream().max(Comparator.comparingDouble(e -> e.getHealth() + e.getAbsorptionAmount())).orElse(null);
                break;
            case FOV:
                newTarget = targets.stream().min(Comparator.comparingDouble(this::getFOVAngle)).orElse(null);
                break;
        }
        
        if (target == null || !lockTarget || sort == SortMode.FOV) {
            target = (LivingEntity) newTarget;
        }
    }
    
    private double getFOVAngle(Entity entity) {
        Vec3d vec = entity.getPos().subtract(mc.player.getPos()).normalize();
        Vec3d look = mc.player.getRotationVec(1.0f);
        return Math.acos(Math.min(1.0, Math.max(-1.0, vec.dotProduct(look)))) * 180 / Math.PI;
    }
    
    private boolean isInFOV(Entity entity) {
        return getFOVAngle(entity) <= fov;
    }
    
    private boolean shouldSkipEntity(Entity entity) {
        if (!(entity instanceof LivingEntity ent)) return true;
        if (ent.isDead() || !entity.isAlive()) return true;
        if (entity == mc.player) return true;
        
        // Distance check
        float currentRange = getCurrentRange();
        if (mc.player.distanceTo(entity) > currentRange) return true;
        
        // FOV check
        if (fov < 180 && !isInFOV(entity)) return true;
        
        // Wall check
        if (!throughWalls && !mc.player.canSee(entity) && !hitWhenBehindWall) return true;
        
        // Cobweb check
        boolean inCobweb = mc.world.getBlockState(entity.getBlockPos()).getBlock() == Blocks.COBWEB;
        if (inCobweb && !hitWhenInCobweb) return true;
        
        // ESP check
        if (onlyESP && espMode == ESPMode.Off) return true;
        
        // Type check
        if (entity instanceof PlayerEntity player) {
            if (!players) return true;
            if (FriendManager.isFriend(player.getName().getString())) return true;
            if (ignoreCreative && player.isCreative()) return true;
            if (ignoreInvisible && player.isInvisible()) return true;
            if (ignoreNamed && player.hasCustomName()) return true;
            if (ignoreTeam && mc.player.getTeam() != null && player.getTeam() != null &&
                mc.player.getTeam().isEqual(player.getTeam())) return true;
        } else {
            return true;
        }
        
        return false;
    }
    
    private boolean canHit() {
        if (target == null) return false;
        
        float currentRange = getCurrentRange();
        if (mc.player.distanceTo(target) > currentRange) return false;
        
        if (!throughWalls && !mc.player.canSee(target) && !hitWhenBehindWall) return false;
        
        boolean targetInCobweb = mc.world.getBlockState(target.getBlockPos()).getBlock() == Blocks.COBWEB;
        if (targetInCobweb && !hitWhenInCobweb) return false;
        
        return true;
    }
    
    private void updateRotations(boolean ready) {
        if (target == null) return;
        
        Vec3d aimPos = getAimPosition();
        if (aimPos == null) return;
        
        double diffX = aimPos.x - mc.player.getX();
        double diffY = aimPos.y - (mc.player.getY() + mc.player.getStandingEyeHeight());
        double diffZ = aimPos.z - mc.player.getZ();
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        
        float targetYaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));
        
        // Anti-ban: limit rotation changes
        if (antiBan && limitRotations) {
            float deltaYaw = MathHelper.wrapDegrees(targetYaw - rotationYaw);
            float deltaPitch = targetPitch - rotationPitch;
            
            deltaYaw = MathHelper.clamp(deltaYaw, -30, 30);
            deltaPitch = MathHelper.clamp(deltaPitch, -15, 15);
            
            targetYaw = rotationYaw + deltaYaw;
            targetPitch = rotationPitch + deltaPitch;
        }
        
        targetPitch = MathHelper.clamp(targetPitch, -90, 90);
        
        if (ready) {
            rotationYaw = targetYaw;
            rotationPitch = targetPitch;
        }
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
        
        // Shield breaker
        if (shieldBreaker && target instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) target;
            if (player.isUsingItem() && (player.getOffHandStack().getItem() == Items.SHIELD ||
                player.getMainHandStack().getItem() == Items.SHIELD)) {
                int axeSlot = getAxeSlot();
                if (axeSlot != -1) {
                    int prevSlot = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = axeSlot;
                    mc.interactionManager.attackEntity(mc.player, target);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    mc.player.getInventory().selectedSlot = prevSlot;
                    return;
                }
            }
        }
        
        // Normal attack
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
    
    private int getAxeSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof AxeItem) return i;
        }
        return -1;
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
    
    // ==================== ESP RENDER (giống ThunderHack) ====================
    
    public void onRender3D(MatrixStack stack) {
        if (target == null) return;
        if (espMode == ESPMode.Off) return;
        
        drawESP(stack, target);
    }
    
    private void drawESP(MatrixStack stack, Entity entity) {
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
        
        // Get color
        Color color = getESPColor(entity);
        
        // Draw box
        if (espBox) {
            drawBoxOutline(stack, minX, minY, minZ, maxX, maxY, maxZ, color);
        }
        
        // Draw health bar
        if (espHealth && entity instanceof LivingEntity living) {
            float health = living.getHealth();
            float maxHealth = living.getMaxHealth();
            float percent = health / maxHealth;
            
            double width = (maxX - minX);
            double xPos = (minX + maxX) / 2 - width / 2;
            double yPos = maxY + 0.2;
            
            Color healthColor = new Color((int) (255 * (1 - percent)), (int) (255 * percent), 0);
            drawHealthBar(stack, xPos, yPos, width, 0.05, percent, healthColor);
        }
        
        // Draw tracer
        if (espTracer) {
            drawTracer(stack, entity, color);
        }
    }
    
    private Color getESPColor(Entity entity) {
        if (entity instanceof PlayerEntity) {
            if (FriendManager.isFriend(((PlayerEntity) entity).getName().getString())) {
                return espFriendColor;
            }
            return espColor;
        }
        return espEnemyColor;
    }
    
    private void drawBoxOutline(MatrixStack stack, double minX, double minY, double minZ, 
                                 double maxX, double maxY, double maxZ, Color color) {
        Vec3d camera = mc.gameRenderer.getCamera().getPos();
        
        stack.push();
        stack.translate(minX - camera.x, minY - camera.y, minZ - camera.z);
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        // Bottom face
        buffer.vertex(0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(maxX - minX, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(maxX - minX, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(maxX - minX, 0, maxZ - minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(maxX - minX, 0, maxZ - minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(0, 0, maxZ - minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(0, 0, maxZ - minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        
        // Top face
        buffer.vertex(0, maxY - minY, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(maxX - minX, maxY - minY, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(maxX - minX, maxY - minY, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(maxX - minX, maxY - minY, maxZ - minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(maxX - minX, maxY - minY, maxZ - minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(0, maxY - minY, maxZ - minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(0, maxY - minY, maxZ - minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(0, maxY - minY, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        
        // Vertical edges
        buffer.vertex(0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(0, maxY - minY, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(maxX - minX, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(maxX - minX, maxY - minY, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(maxX - minX, 0, maxZ - minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(maxX - minX, maxY - minY, maxZ - minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(0, 0, maxZ - minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(0, maxY - minY, maxZ - minZ).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        
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
    
    private void drawTracer(MatrixStack stack, Entity entity, Color color) {
        Vec3d camera = mc.gameRenderer.getCamera().getPos();
        
        double x = entity.getX() - camera.x;
        double y = entity.getY() + entity.getHeight() / 2 - camera.y;
        double z = entity.getZ() - camera.z;
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        buffer.vertex(0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        buffer.vertex(x, y, z).color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder.BuildResult result = buffer.end();
        BufferDrawer drawer = tessellator.draw(result);
        drawer.draw();
    }
    
    // Client look for render
    public void onRender() {
        if (clientLook && rotationMode != RotationMode.NONE && target != null) {
            mc.player.setYaw(rotationYaw);
            mc.player.setPitch(rotationPitch);
        }
    }
}