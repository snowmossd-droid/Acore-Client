package acore.hack.injection.accessors;

import net.minecraft.client.network.OtherClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OtherClientPlayerEntity.class)
public interface IOtherClientPlayerEntity {
   @Accessor("lastX")
   double getLastX();

   @Accessor("lastY")
   double getLastY();

   @Accessor("lastZ")
   double getLastZ();
}
