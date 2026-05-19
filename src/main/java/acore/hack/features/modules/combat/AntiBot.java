package acore.hack.features.modules.combat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import acore.hack.events.impl.EventEntityRemoved;
import acore.hack.events.impl.EventEntitySpawn;
import acore.hack.events.impl.EventSync;
import acore.hack.features.modules.Module;
import acore.hack.features.modules.misc.FakePlayer;
import acore.hack.setting.Setting;

public final class AntiBot extends Module {
   public final Setting<AntiBot.Mode> mode = new Setting<>("Mode", AntiBot.Mode.Matrix);
   public final Setting<Boolean> enabledRemove = new Setting<>("Remove", true);
   public final Setting<Boolean> onlyAura = new Setting<>("OnlyAura", false);
   public static final List<PlayerEntity> bots = new CopyOnWriteArrayList<>();
   private static final Set<UUID> botIds = ConcurrentHashMap.newKeySet();
   private static final Map<UUID, Integer> pendingChecks = new ConcurrentHashMap<>();
   private static final int MAX_SPAWN_CHECKS = 6;

   public AntiBot() {
      super("AntiBot", "Removes matrix bots.", Module.Category.COMBAT);
   }

   @Override
   public void onEnable() {
      super.onEnable();
      if (mc.world != null) {
         for (PlayerEntity p : mc.world.getPlayers()) {
            this.detect(p);
         }
      }
   }

   @EventHandler
   public void onSync(EventSync e) {
      if (this.mode.getValue() == AntiBot.Mode.Matrix) {
         if (mc.world != null && !pendingChecks.isEmpty()) {
            for (PlayerEntity p : mc.world.getPlayers()) {
               try {
                  Integer rem = pendingChecks.get(p.getUuid());
                  if (rem != null) {
                     this.detect(p);
                     if (!isBot(p) && rem - 1 > 0) {
                        pendingChecks.put(p.getUuid(), rem - 1);
                     } else {
                        pendingChecks.remove(p.getUuid());
                     }
                  }
               } catch (Exception var6) {
               }
            }
         }

         if (!this.onlyAura.getValue()) {
            for (PlayerEntity p : mc.world.getPlayers()) {
               this.detect(p);
            }
         } else if (Aura.target instanceof PlayerEntity ent) {
            this.detect((PlayerEntity)Aura.target);
         }

         if (this.enabledRemove.getValue()) {
            for (PlayerEntity p : bots) {
               try {
                  if (FakePlayer.fakePlayer == null || p != FakePlayer.fakePlayer) {
                     mc.world.removeEntity(p.getId(), RemovalReason.KILLED);
                  }
               } catch (Exception var5) {
               }
            }
         }
      }
   }

   @EventHandler
   public void onEntitySpawn(EventEntitySpawn e) {
      if (this.mode.getValue() == AntiBot.Mode.Matrix) {
         if (e != null && e.getEntity() != null) {
            if (e.getEntity() instanceof PlayerEntity p) {
               this.detect(p);

               try {
                  if (!isBot(p)) {
                     pendingChecks.put(p.getUuid(), 6);
                  }
               } catch (Exception var4) {
               }
            }
         }
      }
   }

   @EventHandler
   public void onEntityRemoved(EventEntityRemoved e) {
      if (e != null && e.getEntity() != null) {
         if (e.getEntity() instanceof PlayerEntity p) {
            try {
               bots.removeIf(b -> b == null || b.getUuid().equals(p.getUuid()));
               botIds.remove(p.getUuid());
               pendingChecks.remove(p.getUuid());
            } catch (Exception var4) {
            }
         }
      }
   }

   private void detect(PlayerEntity ent) {
      if (ent != null && ent != mc.player) {
         if (FakePlayer.fakePlayer == null || ent != FakePlayer.fakePlayer) {
            if (ent instanceof OtherClientPlayerEntity) {
               if (!isBot(ent)) {
                  boolean visualArmor = false;

                  try {
                     ItemStack head = ent.getEquippedStack(EquipmentSlot.HEAD);
                     ItemStack chest = ent.getEquippedStack(EquipmentSlot.CHEST);
                     ItemStack legs = ent.getEquippedStack(EquipmentSlot.LEGS);
                     ItemStack feet = ent.getEquippedStack(EquipmentSlot.FEET);
                     if (head != null && !head.isEmpty()
                        || chest != null && !chest.isEmpty()
                        || legs != null && !legs.isEmpty()
                        || feet != null && !feet.isEmpty()) {
                        visualArmor = true;
                     }
                  } catch (Exception var9) {
                  }

                  boolean invArmor = false;

                  try {
                     for (ItemStack s : ent.getInventory().armor) {
                        if (s != null && !s.isEmpty()) {
                           invArmor = true;
                           break;
                        }
                     }
                  } catch (Exception var8) {
                  }

                  try {
                     if (visualArmor && (!invArmor || ent.getXpToLevelUp() == 0)) {
                        this.addBot(ent);
                     }
                  } catch (Exception ignored) {
                     if (visualArmor && !invArmor) {
                        this.addBot(ent);
                     }
                  }
               }
            }
         }
      }
   }

   private void addBot(PlayerEntity p) {
      if (p != null) {
         if (FakePlayer.fakePlayer == null || p != FakePlayer.fakePlayer) {
            try {
               bots.add(p);
               botIds.add(p.getUuid());
               pendingChecks.remove(p.getUuid());
               this.sendMessage(p.getName().getString() + " is a bot (Matrix)!");
            } catch (Exception var3) {
            }
         }
      }
   }

   public static boolean isBot(Entity e) {
      if (e == null) {
         return false;
      }

      try {
         if (FakePlayer.fakePlayer != null && e == FakePlayer.fakePlayer) {
            return false;
         }
      } catch (Exception var3) {
      }

      try {
         if (e instanceof PlayerEntity pl) {
            if (bots.contains(pl)) {
               return true;
            }

            return botIds.contains(pl.getUuid());
         }
      } catch (Exception var2) {
      }

      return false;
   }

   @Override
   public String getDisplayInfo() {
      return String.valueOf(bots.size());
   }

   public enum Mode {
      Matrix;
   }
}