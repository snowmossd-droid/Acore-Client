package acore.hack.features.modules.movement;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import acore.hack.features.modules.Module;
import acore.hack.setting.Setting;

public class NoSlow extends Module {
   public final Setting<Boolean> sneak = new Setting<>("Sneak", true);
   public final Setting<Boolean> web = new Setting<>("Web", false);
   public final Setting<Boolean> slime = new Setting<>("Slime", false);
   public final Setting<Boolean> berry = new Setting<>("Berry", false);
   public final Setting<Boolean> soulSand = new Setting<>("SoulSand", false);
   public final Setting<Boolean> items = new Setting<>("Items", true);
   private final Setting<Boolean> grim = new Setting<>("Grim", false);

   public NoSlow() {
      super("NoSlow", "Prevents movement slowdown.", Module.Category.MOVEMENT);
   }

   @Override
   public void onUpdate() {
      if (fullNullCheck()) return;
      if (this.sneak.getValue() && mc.player.isSneaking()) {
         mc.player.setSneaking(false);
      }
      if (this.items.getValue() && isUsingItem()) {
         if (!this.grim.getValue()) {
            mc.options.useKey.setPressed(false);
         }
      }
      if (this.web.getValue() && isInWeb()) {
         mc.player.setVelocity(mc.player.getVelocity().x * 1.5, mc.player.getVelocity().y, mc.player.getVelocity().z * 1.5);
      }
   }

   private boolean isUsingItem() {
      return mc.player.isUsingItem() && (mc.player.getActiveItem().getItem() == Items.BOW || mc.player.getActiveItem().getItem() == Items.CROSSBOW || mc.player.getActiveItem().getItem().isFood());
   }

   private boolean isInWeb() {
      return mc.world.getBlockState(mc.player.getBlockPos()).isOf(Items.COBWEB);
   }

   public boolean isSneaking() {
      return this.isEnabled() && this.sneak.getValue();
   }
}