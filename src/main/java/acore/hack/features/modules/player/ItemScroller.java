package acore.hack.features.modules.player;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import acore.hack.events.impl.EventClickSlot;
import acore.hack.features.modules.Module;
import acore.hack.setting.Setting;

public class ItemScroller extends Module {
   public Setting<Integer> delay = new Setting<>("Delay", 50, 0, 500);
   private boolean pauseListening = false;

   public ItemScroller() {
      super("ItemScroller", "Fast item swap in chests.", Module.Category.PLAYER);
   }

   @EventHandler
   public void onClick(EventClickSlot e) {
      if ((this.isKeyPressed(340) || this.isKeyPressed(344)) && (this.isKeyPressed(341) || this.isKeyPressed(345)) && e.getSlotActionType() == SlotActionType.THROW && !this.pauseListening) {
         Item copy = ((Slot)mc.player.playerScreenHandler.slots.get(e.getSlot())).getStack().getItem();
         this.pauseListening = true;
         for (int i2 = 0; i2 < mc.player.playerScreenHandler.slots.size(); i2++) {
            if (((Slot)mc.player.playerScreenHandler.slots.get(i2)).getStack().getItem() == copy) {
               mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, i2, 1, SlotActionType.THROW, mc.player);
            }
         }
         this.pauseListening = false;
      }
   }
}