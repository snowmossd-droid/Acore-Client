package acore.hack.features.modules.combat;

import io.netty.buffer.Unpooled;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.jetbrains.annotations.NotNull;
import acore.hack.AcoreHack;
import acore.hack.events.impl.PacketEvent;
import acore.hack.features.modules.Module;
import acore.hack.setting.Setting;

public final class CrystalOptimizer extends Module {
   private static final long MIN_BLATANT_TIME_MS = 150L;
   private static final double CHAIN_CRYSTAL_RADIUS_SQ = 144.0;
   private final Setting<Boolean> blatant = new Setting<>("Blatant", true);
   private final Map<Integer, Long> inhibitedCrystals = new ConcurrentHashMap<>();

   public CrystalOptimizer() {
      super("CrystalOptimizer", "Removes attacked crystals client-side faster.", Module.Category.COMBAT);
   }

   @Override
   public void onDisable() {
      this.inhibitedCrystals.clear();
   }

   @Override
   public void onLogout() {
      this.inhibitedCrystals.clear();
   }

   @Override
   public void onUpdate() {
      if (fullNullCheck()) {
         this.inhibitedCrystals.clear();
      } else {
         if (this.blatant.getValue()) {
            this.pruneInhibitedCrystals();
         } else {
            this.inhibitedCrystals.clear();
         }
      }
   }

   @EventHandler
   private void onPacketSend(PacketEvent.@NotNull Send event) {
      if (!fullNullCheck() && event.getPacket() instanceof PlayerInteractEntityC2SPacket packet) {
         Entity entity = this.getEntityFromPacket(packet);
         if (entity instanceof EndCrystalEntity crystal) {
            if (this.blatant.getValue()) {
               this.pruneInhibitedCrystals();
               if (this.shouldBlatantInhibit(this.getEntityId(packet))) {
                  event.cancel();
                  this.removeCrystal(crystal);
               } else if (!this.cantBreakCrystal()) {
                  this.markInhibited(crystal.getId());
                  this.markNearbyCrystalsDead(crystal.getX(), crystal.getY(), crystal.getZ());
                  this.removeCrystal(crystal);
               }
            } else if (!this.cantBreakCrystal()) {
               this.removeCrystal(crystal);
            }
         }
      }
   }

   @EventHandler
   private void onPacketReceive(PacketEvent.@NotNull Receive event) {
      if (!fullNullCheck() && this.blatant.getValue()) {
         this.pruneInhibitedCrystals();
         if (event.getPacket() instanceof ExplosionS2CPacket explosion) {
            this.markNearbyCrystalsDead(explosion.getX(), explosion.getY(), explosion.getZ());
         }
      }
   }

   private void removeCrystal(@NotNull Entity entity) {
      entity.remove(RemovalReason.KILLED);
      entity.onRemoved();
   }

   public boolean isAttackInhibited(Entity entity) {
      if (!fullNullCheck() && !this.isOff() && this.blatant.getValue() && entity instanceof EndCrystalEntity crystal) {
         this.pruneInhibitedCrystals();
         if (!this.isInhibited(crystal.getId())) {
            return false;
         }
         this.removeCrystal(crystal);
         return true;
      } else {
         return false;
      }
   }

   public boolean shouldIgnoreForPlacement(Entity entity) {
      if (!fullNullCheck() && !this.isOff() && this.blatant.getValue() && entity instanceof EndCrystalEntity crystal) {
         this.pruneInhibitedCrystals();
         return this.isInhibited(crystal.getId());
      } else {
         return false;
      }
   }

   private void markNearbyCrystalsDead(double x, double y, double z) {
      long time = System.currentTimeMillis();
      if (mc.world != null) {
         for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity crystal) {
               double dx = crystal.getX() - x;
               double dy = crystal.getY() - y;
               double dz = crystal.getZ() - z;
               double distSq = dx * dx + dy * dy + dz * dz;
               if (distSq <= CHAIN_CRYSTAL_RADIUS_SQ && !this.isInhibited(crystal.getId())) {
                  this.inhibitedCrystals.put(crystal.getId(), time);
               }
            }
         }
      }
   }

   private void markInhibited(int entityId) {
      this.inhibitedCrystals.put(entityId, System.currentTimeMillis());
   }

   private void pruneInhibitedCrystals() {
      long now = System.currentTimeMillis();
      this.inhibitedCrystals.entrySet().removeIf(entry -> now - entry.getValue() > this.getBlatantTimeMs());
   }

   private boolean shouldBlatantInhibit(int entityId) {
      return this.blatant.getValue() && this.isInhibited(entityId);
   }

   private boolean isInhibited(int entityId) {
      return this.inhibitedCrystals.containsKey(entityId);
   }

   private long getBlatantTimeMs() {
      return Math.max(MIN_BLATANT_TIME_MS, 150L);
   }

   private int getEntityId(@NotNull PlayerInteractEntityC2SPacket packet) {
      try {
         Method getEntityId = PlayerInteractEntityC2SPacket.class.getDeclaredMethod("getEntityId");
         getEntityId.setAccessible(true);
         return (int) getEntityId.invoke(packet);
      } catch (Exception e) {
         PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.buffer());
         try {
            Method write = PlayerInteractEntityC2SPacket.class.getDeclaredMethod("write", PacketByteBuf.class);
            write.setAccessible(true);
            write.invoke(packet, packetBuf);
            return packetBuf.readVarInt();
         } catch (Exception ex) {
            return -1;
         }
      }
   }

   private Entity getEntityFromPacket(@NotNull PlayerInteractEntityC2SPacket packet) {
      int entityId = this.getEntityId(packet);
      if (mc.world != null && entityId != -1) {
         return mc.world.getEntityById(entityId);
      }
      return null;
   }

   private boolean cantBreakCrystal() {
      if (mc.player == null) return true;
      StatusEffectInstance weakness = mc.player.getStatusEffect(StatusEffects.WEAKNESS);
      StatusEffectInstance strength = mc.player.getStatusEffect(StatusEffects.STRENGTH);
      boolean hasWeakness = weakness != null;
      boolean hasStrongerStrength = strength != null && (hasWeakness ? strength.getAmplifier() > weakness.getAmplifier() : true);
      return hasWeakness && !hasStrongerStrength && !this.isTool(mc.player.getMainHandStack());
   }

   private boolean isTool(@NotNull ItemStack stack) {
      return stack.getItem() instanceof SwordItem
         || stack.getItem() instanceof PickaxeItem
         || stack.getItem() instanceof AxeItem
         || stack.getItem() instanceof ShovelItem;
   }
}
