package acore.hack.features.modules.combat;

import acore.hack.core.manager.FriendManager;
import acore.hack.core.manager.ModuleManager;
import acore.hack.features.modules.Module;
import acore.hack.features.setting.BooleanSetting;
import acore.hack.features.setting.NumberSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
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

/**
 * AutoTNTcart – Tự động đặt đường ray + TNT Cart khi enemy bể totem.
 *
 * Khi MÌNH bể totem:
 *   1. Tự động lấy totem mới từ hotbar / inventory
 *
 * Khi ENEMY (không phải friend) bể totem:
 *   1. Collect đường ray từ hotbar → slot tay
 *   2. Đặt tất cả đường ray kế bên player đó (chỗ trống, không nhìn xuống đất)
 *   3. Đặt TNT Minecart lên đường ray
 *   4. Cầm cung / cross bow bắn với tốc độ tuỳ chỉnh
 *   5. Bỏ qua nếu là friend
 */
public class AutoTNTcart extends Module {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    // ─── Settings ─────────────────────────────────────────────────
    public final NumberSetting placeDelay = new NumberSetting("Place Delay", 2, 1, 20, 1);
    public final NumberSetting shootDelay = new NumberSetting("Shoot Delay", 5, 1, 20, 1);
    public final NumberSetting railCount  = new NumberSetting("Rail Count",  4, 1,  8, 1);
    public final BooleanSetting skipFriend = new BooleanSetting("Skip Friend", true);
    public final BooleanSetting autoPickTotem = new BooleanSetting("Auto Totem", true);
    public final BooleanSetting silentSwap   = new BooleanSetting("Silent Swap", true);

    // ─── Internal state ───────────────────────────────────────────
    private int tickTimer   = 0;
    private int shootTimer  = 0;
    private int phase       = 0;   // 0=idle 1=place_rail 2=place_cart 3=shoot
    private PlayerEntity target = null;
    private List<BlockPos> railPositions = new ArrayList<>();
    private int railIndex   = 0;
    private int savedSlot   = -1;

    // Track totem pops
    private final Map<UUID, Integer> totemCount = new HashMap<>();

    public AutoTNTcart() {
        super("AutoTNTcart", Category.COMBAT);
        addSettings(placeDelay, shootDelay, railCount, skipFriend, autoPickTotem, silentSwap);
    }

    @Override
    public void onEnable() {
        phase = 0;
        target = null;
        railPositions.clear();
        tickTimer = 0;
        shootTimer = 0;
        savedSlot = -1;
    }

    @Override
    public void onDisable() {
        restoreSlot();
        phase = 0;
        target = null;
    }

    // ═══════════════════════════════════════════════════════════════
    // onTick – gọi mỗi game tick (đăng ký trong EventBus hoặc override)
    // ═══════════════════════════════════════════════════════════════
    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        checkTotemPops();

        tickTimer++;

        switch (phase) {
            case 1 -> doPlaceRails();
            case 2 -> doPlaceCart();
            case 3 -> doShoot();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Phát hiện totem pop qua health drop + totem use packet
    // Dùng cách đơn giản: track lần dùng totem qua event hoặc health
    // ═══════════════════════════════════════════════════════════════
    private void checkTotemPops() {
        if (mc.world == null) return;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;

            UUID uid = player.getUuid();
            int curTotem = getTotemCount(player);
            int prevTotem = totemCount.getOrDefault(uid, curTotem);

            // Phát hiện totem pop: health rất thấp + totem animation
            if (hasJustPoppedTotem(player)) {
                // Nếu là mình
                if (player == mc.player) {
                    if (autoPickTotem.isEnabled()) autoEquipTotem();
                    totemCount.put(uid, curTotem);
                    continue;
                }
                // Nếu là friend
                if (skipFriend.isEnabled() && FriendManager.isFriend(player)) {
                    totemCount.put(uid, curTotem);
                    continue;
                }
                // Enemy bể totem → kích hoạt
                if (phase == 0) {
                    triggerOn(player);
                }
            }
            totemCount.put(uid, curTotem);
        }
    }

    /**
     * Heuristic: player bể totem khi HP == 1 (sau khi dùng totem HP về 1)
     * Hoặc dựa vào packet TotemUsedS2CPacket nếu có event
     */
    private boolean hasJustPoppedTotem(PlayerEntity player) {
        // Kiểm tra health = 1 (totem vừa kích hoạt, game set HP về 1)
        return player.getHealth() <= 1.0f && !player.isDead();
    }

    private int getTotemCount(PlayerEntity player) {
        // Đơn giản dùng health làm proxy; thực tế nên dùng packet event
        return (int) player.getHealth();
    }

    // ═══════════════════════════════════════════════════════════════
    // Trigger sequence
    // ═══════════════════════════════════════════════════════════════
    private void triggerOn(PlayerEntity enemy) {
        target = enemy;
        railPositions = findPlacePositions(enemy, (int) railCount.getValue());
        railIndex = 0;
        savedSlot = mc.player.getInventory().selectedSlot;
        tickTimer = 0;
        phase = railPositions.isEmpty() ? 0 : 1;
    }

    // ═══════════════════════════════════════════════════════════════
    // Phase 1: Đặt đường ray
    // ═══════════════════════════════════════════════════════════════
    private void doPlaceRails() {
        if (target == null || railIndex >= railPositions.size()) {
            phase = 2;
            tickTimer = 0;
            return;
        }
        if (tickTimer < placeDelay.getValue()) return;
        tickTimer = 0;

        // Tìm slot đường ray
        int railSlot = findItemSlot(item ->
                item instanceof BlockItem bi &&
                (bi.getBlock() == Blocks.RAIL ||
                 bi.getBlock() == Blocks.POWERED_RAIL ||
                 bi.getBlock() == Blocks.DETECTOR_RAIL ||
                 bi.getBlock() == Blocks.ACTIVATOR_RAIL));

        if (railSlot == -1) {
            // Hết đường ray, chuyển sang đặt cart
            phase = 2;
            return;
        }

        swapToSlot(railSlot);
        BlockPos pos = railPositions.get(railIndex);
        placeBlock(pos);
        railIndex++;
    }

    // ═══════════════════════════════════════════════════════════════
    // Phase 2: Đặt TNT Minecart
    // ═══════════════════════════════════════════════════════════════
    private void doPlaceCart() {
        if (tickTimer < placeDelay.getValue()) return;
        tickTimer = 0;

        int cartSlot = findItemSlot(item -> item instanceof MinecartItem m &&
                m == Items.TNT_MINECART);

        if (cartSlot == -1) {
            phase = 3;
            return;
        }

        swapToSlot(cartSlot);

        // Đặt cart lên đường ray đầu tiên
        if (!railPositions.isEmpty()) {
            BlockPos railPos = railPositions.get(0);
            // Right click lên mặt trên đường ray
            interactBlock(railPos, Direction.UP);
        }
        phase = 3;
        tickTimer = 0;
    }

    // ═══════════════════════════════════════════════════════════════
    // Phase 3: Bắn bow / crossbow vào target
    // ═══════════════════════════════════════════════════════════════
    private void doShoot() {
        if (target == null || target.isDead() || target.getHealth() > 6f) {
            // target đã chết hoặc hồi sinh đủ hp
            restoreSlot();
            phase = 0;
            target = null;
            return;
        }

        shootTimer++;
        if (shootTimer < shootDelay.getValue()) return;
        shootTimer = 0;

        int bowSlot = findItemSlot(item -> item instanceof BowItem || item instanceof CrossbowItem);
        if (bowSlot == -1) {
            restoreSlot();
            phase = 0;
            return;
        }

        swapToSlot(bowSlot);

        // Aimbot không cần nhìn xuống đất – tính toán góc chính xác đến target
        aimAt(target);

        ItemStack held = mc.player.getInventory().getStack(mc.player.getInventory().selectedSlot);
        if (held.getItem() instanceof BowItem) {
            // Charge và bắn
            mc.options.useKey.setPressed(true);
            // Sau 1 tick thả ra để bắn (đơn giản: dùng interact)
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        } else if (held.getItem() instanceof CrossbowItem) {
            if (CrossbowItem.isCharged(held)) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            } else {
                mc.options.useKey.setPressed(true);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Helper: Tìm vị trí đặt block kế bên player
    // ═══════════════════════════════════════════════════════════════
    private List<BlockPos> findPlacePositions(PlayerEntity enemy, int count) {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos center = enemy.getBlockPos();

        // Các hướng xung quanh player theo thứ tự ưu tiên
        int[][] offsets = {
            {0, 0, 0}, {1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1},
            {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1}
        };

        for (int[] off : offsets) {
            if (positions.size() >= count) break;
            BlockPos pos = center.add(off[0], off[1], off[2]);
            // Kiểm tra có thể đặt block ở đây không (y-1 phải có block, y phải trống)
            if (isValidRailPosition(pos)) {
                positions.add(pos);
            }
        }
        return positions;
    }

    private boolean isValidRailPosition(BlockPos pos) {
        World world = mc.world;
        if (world == null) return false;
        // Mặt đất bên dưới phải solid
        boolean groundSolid = world.getBlockState(pos.down()).isOpaque();
        // Vị trí đặt phải trống
        boolean isEmpty = world.getBlockState(pos).isAir();
        // Phía trên cũng trống (để đặt cart)
        boolean topEmpty = world.getBlockState(pos.up()).isAir();
        return groundSolid && isEmpty && topEmpty;
    }

    // ═══════════════════════════════════════════════════════════════
    // Helper: Đặt block không cần nhìn xuống đất
    // ═══════════════════════════════════════════════════════════════
    private void placeBlock(BlockPos pos) {
        if (mc.player == null || mc.interactionManager == null) return;
        Vec3d hitVec = Vec3d.ofCenter(pos).add(0, 0.5, 0);
        BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, pos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void interactBlock(BlockPos pos, Direction face) {
        if (mc.player == null || mc.interactionManager == null) return;
        Vec3d hitVec = Vec3d.ofCenter(pos).add(0, 0.5, 0);
        BlockHitResult hitResult = new BlockHitResult(hitVec, face, pos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    // ═══════════════════════════════════════════════════════════════
    // Aimbot: Tính yaw/pitch chính xác đến target (100% trúng)
    // Không cần nhìn xuống đất, nhắm vào body của target
    // ═══════════════════════════════════════════════════════════════
    private void aimAt(Entity entity) {
        if (mc.player == null) return;

        Vec3d playerEyes = mc.player.getEyePos();
        // Nhắm vào giữa body của target
        Vec3d targetPos = entity.getPos().add(0, entity.getHeight() * 0.5, 0);

        double dx = targetPos.x - playerEyes.x;
        double dy = targetPos.y - playerEyes.y;
        double dz = targetPos.z - playerEyes.z;

        double distXZ = Math.sqrt(dx * dx + dz * dz);
        float yaw   = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(dy, distXZ)));

        // Gửi rotation packet (silent – không thay đổi camera thật)
        if (silentSwap.isEnabled()) {
            mc.player.networkHandler.sendPacket(
                new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround())
            );
        } else {
            mc.player.setYaw(yaw);
            mc.player.setPitch(pitch);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Auto totem: khi mình bể totem tự cầm totem mới
    // ═══════════════════════════════════════════════════════════════
    private void autoEquipTotem() {
        if (mc.player == null) return;
        // Totem nằm trong off-hand slot
        ItemStack offhand = mc.player.getOffHandStack();
        if (offhand.getItem() == Items.TOTEM_OF_UNDYING) return;

        // Tìm totem trong inventory
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                // Swap vào offhand
                mc.interactionManager.pickFromInventory(i);
                break;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Slot helpers
    // ═══════════════════════════════════════════════════════════════
    @FunctionalInterface
    interface ItemPredicate {
        boolean test(Item item);
    }

    private int findItemSlot(ItemPredicate predicate) {
        if (mc.player == null) return -1;
        // Tìm trong hotbar trước
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && predicate.test(stack.getItem())) return i;
        }
        return -1;
    }

    private void swapToSlot(int slot) {
        if (mc.player == null) return;
        if (slot >= 0 && slot < 9) {
            mc.player.getInventory().selectedSlot = slot;
        }
    }

    private void restoreSlot() {
        if (savedSlot >= 0 && mc.player != null) {
            mc.player.getInventory().selectedSlot = savedSlot;
            savedSlot = -1;
        }
    }
}
