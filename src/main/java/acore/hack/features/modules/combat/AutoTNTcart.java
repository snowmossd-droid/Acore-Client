package acore.hack.features.modules.combat;

import acore.hack.core.manager.FriendManager;
import acore.hack.features.modules.Module;
import acore.hack.features.setting.BooleanSetting;
import acore.hack.features.setting.NumberSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

public class AutoTNTcart extends Module {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    // ─── Settings ─────────────────────────────────────────────────
    public final NumberSetting placeDelay = new NumberSetting("Place Delay", 2, 1, 10, 1);
    public final NumberSetting shootDelay = new NumberSetting("Shoot Delay", 3, 1, 10, 1);
    public final NumberSetting railCount  = new NumberSetting("Rail Count",  4, 1,  8, 1);
    public final NumberSetting placeRange = new NumberSetting("Place Range", 3, 1, 5, 0.5);
    public final BooleanSetting skipFriend = new BooleanSetting("Skip Friend", true);
    public final BooleanSetting silentSwap   = new BooleanSetting("Silent Swap", true);

    // ─── Internal state ───────────────────────────────────────────
    private int placeTimer   = 0;
    private int shootTimer   = 0;
    private int phase        = 0;   // 0=idle 1=place_rail 2=place_cart 3=shoot
    private PlayerEntity target = null;
    private List<BlockPos> railPositions = new ArrayList<>();
    private int railIndex   = 0;
    private int savedSlot   = -1;
    private boolean isShooting = false;

    // Track totem counts in offhand
    private final Map<UUID, Integer> offhandTotemCount = new HashMap<>();

    public AutoTNTcart() {
        super("AutoTNTcart", Category.COMBAT);
        addSettings(placeDelay, shootDelay, railCount, placeRange, skipFriend, silentSwap);
    }

    @Override
    public void onEnable() {
        phase = 0;
        target = null;
        railPositions.clear();
        placeTimer = 0;
        shootTimer = 0;
        savedSlot = -1;
        isShooting = false;
        offhandTotemCount.clear();
        
        if (mc.player != null) {
            mc.options.useKey.setPressed(false);
        }
    }

    @Override
    public void onDisable() {
        restoreSlot();
        phase = 0;
        target = null;
        isShooting = false;
        if (mc.player != null) {
            mc.options.useKey.setPressed(false);
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        // 1. Phát hiện enemy bể totem
        checkEnemyTotemPop();

        // 2. Xử lý các phase
        placeTimer++;
        shootTimer++;

        switch (phase) {
            case 1 -> doPlaceRails();
            case 2 -> doPlaceCart();
            case 3 -> doShoot();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // PHÁT HIỆN ENEMY BỂ TOTEM (qua số lượng totem trong offhand giảm)
    // ═══════════════════════════════════════════════════════════════
    private void checkEnemyTotemPop() {
        if (mc.world == null) return;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;

            UUID uid = player.getUuid();
            int currentTotems = getOffhandTotemCount(player);
            int previousTotems = offhandTotemCount.getOrDefault(uid, -1);

            // Nếu trước đó có totem và bây giờ giảm 1 => BỂ TOTEM!
            if (previousTotems > 0 && currentTotems == previousTotems - 1) {
                
                if (skipFriend.isEnabled() && FriendManager.isFriend(player)) {
                    offhandTotemCount.put(uid, currentTotems);
                    continue;
                }
                
                // KÍCH HOẠT NGAY LẬP TỨC!
                if (phase == 0 && target == null) {
                    triggerOn(player);
                    System.out.println("[AutoTNTcart] Enemy popped totem! Attacking: " + player.getName().getString());
                }
            }
            
            offhandTotemCount.put(uid, currentTotems);
        }
    }
    
    private int getOffhandTotemCount(PlayerEntity player) {
        ItemStack offhand = player.getOffHandStack();
        if (offhand.getItem() == Items.TOTEM_OF_UNDYING) {
            return offhand.getCount();
        }
        return 0;
    }

    // ═══════════════════════════════════════════════════════════════
    // KÍCH HOẠT: tìm vị trí đặt rail xung quanh enemy
    // ═══════════════════════════════════════════════════════════════
    private void triggerOn(PlayerEntity enemy) {
        target = enemy;
        railPositions = findValidPositionsAround(enemy, (int) railCount.getValue());
        railIndex = 0;
        savedSlot = mc.player.getInventory().selectedSlot;
        placeTimer = 0;
        shootTimer = 0;
        
        if (railPositions.isEmpty()) {
            System.out.println("[AutoTNTcart] No valid position to place rail!");
            phase = 0;
            target = null;
        } else {
            phase = 1;
            System.out.println("[AutoTNTcart] Found " + railPositions.size() + " positions, starting...");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // PHASE 1: TỰ ĐỘNG ĐẶT ĐƯỜNG RAY
    // ═══════════════════════════════════════════════════════════════
    private void doPlaceRails() {
        if (target == null || railIndex >= railPositions.size()) {
            phase = 2;
            placeTimer = 0;
            return;
        }
        
        if (placeTimer < placeDelay.getValue()) return;
        placeTimer = 0;

        // Tìm đường ray trong inventory
        int railSlot = findRailSlot();
        if (railSlot == -1) {
            System.out.println("[AutoTNTcart] No rail found!");
            phase = 2;
            return;
        }

        // Tự động đổi sang slot có rail
        swapToSlot(railSlot);
        
        // Tự động đặt rail
        BlockPos pos = railPositions.get(railIndex);
        placeBlock(pos);
        railIndex++;
        
        System.out.println("[AutoTNTcart] Placed rail " + railIndex + "/" + railPositions.size());
    }

    // ═══════════════════════════════════════════════════════════════
    // PHASE 2: TỰ ĐỘNG ĐẶT TNT CART LÊN ĐƯỜNG RAY
    // ═══════════════════════════════════════════════════════════════
    private void doPlaceCart() {
        if (placeTimer < placeDelay.getValue()) return;
        placeTimer = 0;

        int cartSlot = findCartSlot();
        if (cartSlot == -1) {
            System.out.println("[AutoTNTcart] No TNT cart found!");
            phase = 3;
            return;
        }

        swapToSlot(cartSlot);

        if (!railPositions.isEmpty()) {
            BlockPos railPos = railPositions.get(0);
            interactBlock(railPos, Direction.UP);
            System.out.println("[AutoTNTcart] Placed TNT cart on rail!");
        }
        
        phase = 3;
        placeTimer = 0;
    }

    // ═══════════════════════════════════════════════════════════════
    // PHASE 3: TỰ ĐỘNG CẦM CUNG BẮN CART
    // ═══════════════════════════════════════════════════════════════
    private void doShoot() {
        // Kiểm tra target còn sống không
        if (target == null || target.isDead() || !target.isAlive()) {
            restoreSlot();
            phase = 0;
            target = null;
            isShooting = false;
            System.out.println("[AutoTNTcart] Target dead, stopping...");
            return;
        }

        if (shootTimer < shootDelay.getValue()) return;
        shootTimer = 0;

        // Tìm cung hoặc nỏ
        int bowSlot = findBowSlot();
        if (bowSlot == -1) {
            System.out.println("[AutoTNTcart] No bow/crossbow found!");
            restoreSlot();
            phase = 0;
            target = null;
            return;
        }

        // Tự động đổi sang cung
        swapToSlot(bowSlot);
        
        // Tự động aim vào target
        aimAt(target);
        
        // Tự động bắn
        shootBow();
        
        System.out.println("[AutoTNTcart] Shot at target!");
    }

    private void shootBow() {
        if (mc.player == null || mc.interactionManager == null) return;
        
        ItemStack held = mc.player.getInventory().getStack(mc.player.getInventory().selectedSlot);
        
        if (held.getItem() instanceof BowItem) {
            // Bắn cung
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.swingHand(Hand.MAIN_HAND);
        } else if (held.getItem() instanceof CrossbowItem) {
            // Bắn nỏ
            if (CrossbowItem.isCharged(held)) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            } else {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            }
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // TÌM VỊ TRÍ ĐẶT RAIL XUNG QUANH ENEMY (chỗ trống, có block dưới)
    // ═══════════════════════════════════════════════════════════════
    private List<BlockPos> findValidPositionsAround(PlayerEntity enemy, int maxCount) {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos center = enemy.getBlockPos();
        int range = (int) placeRange.getValue();

        // Duyệt tất cả vị trí xung quanh
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                for (int y = -1; y <= 1; y++) {
                    if (positions.size() >= maxCount) break;
                    
                    BlockPos pos = center.add(x, y, z);
                    if (isValidPlacePosition(pos) && !positions.contains(pos)) {
                        positions.add(pos);
                    }
                }
            }
        }
        
        // Sắp xếp theo khoảng cách gần enemy nhất
        positions.sort(Comparator.comparingDouble(p -> p.getSquaredDistance(center)));
        return positions;
    }

    private boolean isValidPlacePosition(BlockPos pos) {
        World world = mc.world;
        if (world == null) return false;
        
        // Kiểm tra có block solid bên dưới
        boolean hasGround = world.getBlockState(pos.down()).isOpaque();
        // Kiểm tra vị trí đặt trống
        boolean isEmpty = world.getBlockState(pos).isAir();
        // Không đặt trùng chân enemy
        boolean notInsideEnemy = target == null || !pos.equals(target.getBlockPos());
        // Không đặt quá xa
        boolean inRange = mc.player != null && pos.getSquaredDistance(mc.player.getBlockPos()) <= 36;
        
        return hasGround && isEmpty && notInsideEnemy && inRange;
    }

    // ═══════════════════════════════════════════════════════════════
    // TÌM SLOT ITEM TỰ ĐỘNG
    // ═══════════════════════════════════════════════════════════════
    private int findRailSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem bi) {
                if (bi.getBlock() == Blocks.RAIL ||
                    bi.getBlock() == Blocks.POWERED_RAIL ||
                    bi.getBlock() == Blocks.DETECTOR_RAIL ||
                    bi.getBlock() == Blocks.ACTIVATOR_RAIL) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int findCartSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == Items.TNT_MINECART) {
                return i;
            }
        }
        return -1;
    }

    private int findBowSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && (stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem)) {
                return i;
            }
        }
        return -1;
    }

    private void swapToSlot(int slot) {
        if (mc.player != null && slot >= 0 && slot < 9) {
            mc.player.getInventory().selectedSlot = slot;
        }
    }

    private void restoreSlot() {
        if (savedSlot >= 0 && mc.player != null) {
            mc.player.getInventory().selectedSlot = savedSlot;
            savedSlot = -1;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // ĐẶT BLOCK & BẮN
    // ═══════════════════════════════════════════════════════════════
    private void placeBlock(BlockPos pos) {
        if (mc.player == null || mc.interactionManager == null) return;
        Vec3d hitVec = Vec3d.ofCenter(pos);
        BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, pos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void interactBlock(BlockPos pos, Direction face) {
        if (mc.player == null || mc.interactionManager == null) return;
        Vec3d hitVec = Vec3d.ofCenter(pos);
        BlockHitResult hitResult = new BlockHitResult(hitVec, face, pos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void aimAt(Entity entity) {
        if (mc.player == null) return;

        Vec3d playerEyes = mc.player.getEyePos();
        Vec3d targetPos = entity.getBoundingBox().getCenter();

        double dx = targetPos.x - playerEyes.x;
        double dy = targetPos.y - playerEyes.y;
        double dz = targetPos.z - playerEyes.z;

        double distXZ = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(dy, distXZ)));

        if (silentSwap.isEnabled() && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendPacket(
                new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround())
            );
        } else {
            mc.player.setYaw(yaw);
            mc.player.setPitch(pitch);
        }
    }
                    }
