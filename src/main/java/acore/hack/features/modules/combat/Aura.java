package acore.hack.features.modules.combat;

import acore.hack.features.modules.Module;
import acore.hack.core.manager.FriendManager;
import acore.hack.features.modules.render.ESP;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.Blocks;
import java.util.Comparator;
import java.util.List;

public class Aura extends Module {
    
    public enum Mode { None, Legit, Normal }
    public enum AimMode { Head, Body, Legs }
    
    public Mode rotationMode = Mode.Normal;
    public AimMode aimMode = AimMode.Head;
    public float range = 4.2f;
    public boolean throughWalls = false;
    public boolean onlyESP = false;
    public int aps = 20;
    public float rotationPitch;
    public boolean elytraTarget = true;
    
    public LivingEntity target = null;
    private int hitCooldown = 0;
    
    public Aura() {
        super("Aura", Category.COMBAT);
    }
    
    @Override
    protected void onEnable() {
        target = null;
    }
    
    @Override
    protected void onDisable() {
        target = null;
    }
    
    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;
        
        findTarget();
        
        if (target != null && canHit(target)) {
            if (hitCooldown <= 0) {
                attackTarget();
                hitCooldown = 20 / aps;
            }
        }
        
        if (hitCooldown > 0) hitCooldown--;
    }
    
    private void findTarget() {
        List<PlayerEntity> players = mc.world.getPlayers();
        target = players.stream()
            .filter(p -> p != mc.player)
            .filter(p -> !FriendManager.isFriend(p.getName().getString()))
            .filter(p -> mc.player.distanceTo(p) <= range)
            .filter(p -> !onlyESP || ESP.hasESP(p))
            .filter(p -> !isBehindWall(p))
            .min(Comparator.comparingDouble(p -> mc.player.distanceTo(p)))
            .orElse(null);
    }
    
    private boolean isBehindWall(LivingEntity entity) {
        if (throughWalls) return false;
        Vec3d vec = mc.player.getEyePos();
        Vec3d vec2 = entity.getEyePos();
        var result = mc.world.raycast(new net.minecraft.util.math.RaycastContext(vec, vec2,
            net.minecraft.util.math.RaycastContext.ShapeType.COLLIDER,
            net.minecraft.util.math.RaycastContext.FluidHandling.NONE, mc.player));
        return !result.getPos().equals(vec2);
    }
    
    private boolean canHit(LivingEntity entity) {
        // Check cobweb
        if (mc.world.getBlockState(mc.player.getBlockPos()).getBlock() == Blocks.COBWEB) return false;
        return true;
    }
    
    private void attackTarget() {
        if (target == null) return;
        
        Vec3d aimPos = getAimPosition(target);
        
        double diffX = aimPos.x - mc.player.getX();
        double diffY = aimPos.y - (mc.player.getY() + mc.player.getStandingEyeHeight());
        double diffZ = aimPos.z - mc.player.getZ();
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));
        rotationPitch = pitch;
        
        if (rotationMode == Mode.Legit) {
            float yawDiff = yaw - mc.player.getYaw();
            float pitchDiff = pitch - mc.player.getPitch();
            mc.player.setYaw(mc.player.getYaw() + yawDiff / 4);
            mc.player.setPitch(mc.player.getPitch() + pitchDiff / 4);
        } else {
            mc.player.setYaw(yaw);
            mc.player.setPitch(pitch);
        }
        
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
    
    private Vec3d getAimPosition(LivingEntity entity) {
        Box box = entity.getBoundingBox();
        return switch (aimMode) {
            case Head -> new Vec3d(entity.getX(), entity.getY() + entity.getHeight() * 0.85, entity.getZ());
            case Body -> new Vec3d(entity.getX(), entity.getY() + entity.getHeight() / 2, entity.getZ());
            case Legs -> new Vec3d(entity.getX(), entity.getY() + 0.1, entity.getZ());
        };
    }
    }
