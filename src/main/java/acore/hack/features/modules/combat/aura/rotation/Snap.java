package acore.hack.features.modules.combat.aura.rotation;

import acore.hack.features.modules.combat.Aura;

public class Snap implements RotationModeHandler {
   @Override
   public void rotate(Aura aura, boolean ready) {
      Track.rotateClassic(aura, ready);
   }
}