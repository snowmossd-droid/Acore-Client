package acore.hack.features.modules.player;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.AirBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import acore.hack.features.modules.Module;
import acore.hack.setting.Setting;

public class AutoTool extends Module {
   public static Setting<Boolean> swapBack = new Setting<>("SwapBack", true);
   public static Setting<Boolean> saveItem = new Setting<>("SaveItem", true);
   public static Setting<Boolean> silent = new Setting<>("Silent", false);
   public static Setting<Boolean> echestSilk = new Setting<>("EchestSilk", true);
   public static int itemIndex;
   private boolean swap;
   private long swapDelay;
   private final List<Integer> lastItem = new ArrayList<>();

   public AutoTool() {
      super("AutoTool", "Auto switches to best tool.", Module.Category.PLAYER);
   }

   @Override
   public void onUpdate() {
      if (mc.crosshairTarget instanceof BlockHitResult result) {
         BlockPos pos = result.getBlockPos();
         if (!mc.world.getBlockState(pos).isAir()) {
            if (getTool(pos) != -1 && mc.options.attackKey.isPressed()) {
               this.lastItem.add(mc.player.getInventory().selectedSlot);
               if (silent.getValue()) {
                  mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(getTool(pos)));
               } else {
                  mc.player.getInventory().selectedSlot = getTool(pos);
               }
               itemIndex = getTool(pos);
               this.swap = true;
               this.swapDelay = System.currentTimeMillis();
            } else if (this.swap && !this.lastItem.isEmpty() && System.currentTimeMillis() >= this.swapDelay + 300L && swapBack.getValue()) {
               if (silent.getValue()) {
                  mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(this.lastItem.get(0)));
               } else {
                  mc.player.getInventory().selectedSlot = this.lastItem.get(0);
               }
               itemIndex = this.lastItem.get(0);
               this.lastItem.clear();
               this.swap = false;
            }
         }
      }
   }

   public static int getTool(BlockPos pos) {
      int index = -1;
      float CurrentFastest = 1.0F;

      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc.player.getInventory().getStack(i);
         if (stack != ItemStack.EMPTY && (mc.player.getInventory().getStack(i).getMaxDamage() - mc.player.getInventory().getStack(i).getDamage() > 10 || !saveItem.getValue())) {
            int efficiencyLevel = getEnchantmentLevel(Enchantments.EFFICIENCY, stack);
            float destroySpeed = stack.getMiningSpeedMultiplier(mc.world.getBlockState(pos));
            if (mc.world.getBlockState(pos).getBlock() instanceof AirBlock) {
               return -1;
            }
            if (mc.world.getBlockState(pos).getBlock() instanceof EnderChestBlock && echestSilk.getValue()) {
               int silkLevel = getEnchantmentLevel(Enchantments.SILK_TOUCH, stack);
               if (silkLevel > 0 && efficiencyLevel + destroySpeed > CurrentFastest) {
                  CurrentFastest = efficiencyLevel + destroySpeed;
                  index = i;
               }
            } else if (efficiencyLevel + destroySpeed > CurrentFastest) {
               CurrentFastest = efficiencyLevel + destroySpeed;
               index = i;
            }
         }
      }
      return index;
   }

   private static int getEnchantmentLevel(RegistryEntry<Enchantment> enchantment, ItemStack stack) {
      if (mc.world == null) return 0;
      RegistryEntry<Enchantment> targetEnchantment = mc.world.getRegistryManager()
          .get(RegistryKeys.ENCHANTMENT)
          .entryOf(enchantment.getKey().orElseThrow());
      return EnchantmentHelper.getLevel(targetEnchantment, stack);
   }
   }
