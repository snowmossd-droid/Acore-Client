package acore.hack.features.modules.misc;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.math.Vec3d;
import acore.hack.AcoreHack;
import acore.hack.core.Managers;
import acore.hack.core.manager.ModuleManager;
import acore.hack.events.impl.EventAttack;
import acore.hack.events.impl.EventSync;
import acore.hack.events.impl.PacketEvent;
import acore.hack.events.impl.TotemPopEvent;
import acore.hack.features.modules.Module;
import acore.hack.setting.Setting;
import acore.hack.utility.player.InventoryUtility;
import acore.hack.utility.world.ExplosionUtility;

public class FakePlayer extends Module {
   public static OtherClientPlayerEntity fakePlayer;
   private final Setting<Boolean> copyInventory = new Setting<>("CopyInventory", true);
   private final Setting<Boolean> autoTotem = new Setting<>("AutoTotem", true);
   private final Setting<String> name = new Setting<>("Name", "Fake Player");
   private int deathTime;

   public FakePlayer() {
      super("FakePlayer", "Fake player for testing PvP modules.", Module.Category.MISC);
   }

   @Override
   public void onEnable() {
      if (mc.player != null && mc.world != null) {
         fakePlayer = new OtherClientPlayerEntity(mc.world, new GameProfile(UUID.fromString("66123666-6666-6666-6666-666666666600"), this.name.getValue()));
         this.copyPlayerState();
         if (this.copyInventory.getValue()) {
            fakePlayer.equipStack(EquipmentSlot.MAINHAND, mc.player.getMainHandStack().copy());
            fakePlayer.equipStack(EquipmentSlot.OFFHAND, mc.player.getOffHandStack().copy());
            fakePlayer.getInventory().setStack(36, mc.player.getInventory().getStack(36).copy());
            fakePlayer.getInventory().setStack(37, mc.player.getInventory().getStack(37).copy());
            fakePlayer.getInventory().setStack(38, mc.player.getInventory().getStack(38).copy());
            fakePlayer.getInventory().setStack(39, mc.player.getInventory().getStack(39).copy());
         }
         mc.world.addEntity(fakePlayer);
         fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 9999, 2));
         fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 9999, 4));
         fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 9999, 1));
      } else {
         this.disable();
      }
   }

   private void copyPlayerState() {
      fakePlayer.copyFrom(mc.player);
      fakePlayer.prevYaw = mc.player.prevYaw;
      fakePlayer.prevPitch = mc.player.prevPitch;
      fakePlayer.setYaw(mc.player.getYaw());
      fakePlayer.setPitch(mc.player.getPitch());
      fakePlayer.setVelocity(mc.player.getVelocity());
      fakePlayer.setSneaking(mc.player.isSneaking());
      fakePlayer.setSprinting(mc.player.isSprinting());
      fakePlayer.setSwimming(mc.player.isSwimming());
      fakePlayer.setOnGround(mc.player.isOnGround());
      fakePlayer.setVelocity(mc.player.getVelocity());
   }

   @EventHandler
   public void onPacketReceive(PacketEvent.Receive e) {
      if (e.getPacket() instanceof ExplosionS2CPacket explosion && fakePlayer != null && fakePlayer.hurtTime == 0) {
         fakePlayer.setHealth(fakePlayer.getHealth() + fakePlayer.getAbsorptionAmount() - ExplosionUtility.getAutoCrystalDamage(new Vec3d(explosion.getX(), explosion.getY(), explosion.getZ()), fakePlayer, 0, false));
         if (fakePlayer.isDead() && fakePlayer.canTakeDamage()) {
            this.handleTotemPop();
         }
      }
   }

   @EventHandler
   public void onSync(EventSync e) {
      if (fakePlayer != null) {
         if (this.autoTotem.getValue() && fakePlayer.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
            fakePlayer.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));
         }
         if (fakePlayer.isDead()) {
            this.deathTime++;
            if (this.deathTime > 10) {
               this.disable();
            }
         }
      }
   }

   @EventHandler
   public void onAttack(EventAttack e) {
      if (fakePlayer != null && e.getEntity() == fakePlayer && fakePlayer.hurtTime == 0 && !e.isPre()) {
         mc.world.playSound(mc.player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(), SoundEvents.ENTITY_PLAYER_HURT, SoundCategory.PLAYERS, 1.0F, 1.0F);
         if (mc.player.fallDistance > 0.0F || ModuleManager.criticals.isEnabled()) {
            mc.world.playSound(mc.player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1.0F, 1.0F);
         }
         fakePlayer.setHealth(fakePlayer.getHealth());
         if (ModuleManager.aura.getAttackCooldown() >= 0.85) {
            fakePlayer.setHealth(fakePlayer.getHealth() + fakePlayer.getAbsorptionAmount() - InventoryUtility.getHitDamage(mc.player.getMainHandStack(), fakePlayer));
         } else {
            fakePlayer.setHealth(fakePlayer.getHealth() + fakePlayer.getAbsorptionAmount() - 1.0F);
         }
         if (fakePlayer.isDead() && fakePlayer.canTakeDamage()) {
            this.handleTotemPop();
         }
      }
   }

   private void handleTotemPop() {
      fakePlayer.setHealth(10.0F);
      this.applyTotemStatus();
      String playerName = fakePlayer.getName().getString();
      int pops = Managers.COMBAT.popList.merge(playerName, 1, Integer::sum);
      AcoreHack.EVENT_BUS.post(new TotemPopEvent(fakePlayer, pops));
   }

   private void applyTotemStatus() {
      if (mc.player != null && mc.player.networkHandler != null) {
         new EntityStatusS2CPacket(fakePlayer, (byte)35).apply(mc.player.networkHandler);
      }
   }

   @Override
   public void onDisable() {
      if (fakePlayer != null) {
         Managers.COMBAT.popList.remove(fakePlayer.getName().getString());
         fakePlayer.remove(RemovalReason.KILLED);
         fakePlayer.discard();
         fakePlayer = null;
         this.deathTime = 0;
      }
   }
   }
