package acore.hack.features.modules.combat;

import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;
import acore.hack.core.Managers;
import acore.hack.core.manager.player.CombatManager;
import acore.hack.features.modules.Module;
import acore.hack.features.modules.base.TrapModule;
import acore.hack.setting.Setting;

public final class AutoTrap extends TrapModule {
   private final Setting<CombatManager.TargetBy> targetBy = new Setting<>("Target By", CombatManager.TargetBy.Distance);
   private final Setting<Boolean> targetMovingPlayers = new Setting<>("MovingPlayers", false);

   public AutoTrap() {
      super("AutoTrap", "Auto traps players.", Module.Category.COMBAT);
   }

   @Override
   protected boolean needNewTarget() {
      return this.target == null || this.target.distanceTo(mc.player) > this.range.getValue() || this.target.getHealth() + this.target.getAbsorptionAmount() <= 0.0F || this.target.isDead();
   }

   @Nullable
   @Override
   protected PlayerEntity getTarget() {
      return Managers.COMBAT.getTarget(this.range.getValue(), this.targetBy.getValue(), p -> p.getVelocity().lengthSquared() < 0.08 || this.targetMovingPlayers.getValue());
   }
}