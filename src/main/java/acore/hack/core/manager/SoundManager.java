package acore.hack.core.manager;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import acore.hack.core.manager.IManager;

public class SoundManager implements IManager {
   public final Identifier KEYRELEASE_SOUND = Identifier.of("ariscore", "keyrelease");
   public SoundEvent KEYRELEASE_SOUNDEVENT = SoundEvent.of(this.KEYRELEASE_SOUND);
   public final Identifier NURSULTAN_ON_SOUND = Identifier.of("ariscore", "nursultan_on");
   public SoundEvent NURSULTAN_ON_SOUNDEVENT = SoundEvent.of(this.NURSULTAN_ON_SOUND);
   public final Identifier NURSULTAN_OFF_SOUND = Identifier.of("ariscore", "nursultan_off");
   public SoundEvent NURSULTAN_OFF_SOUNDEVENT = SoundEvent.of(this.NURSULTAN_OFF_SOUND);
   public final Identifier NEWCODE_ON_SOUND = Identifier.of("ariscore", "newcode_on");
   public SoundEvent NEWCODE_ON_SOUNDEVENT = SoundEvent.of(this.NEWCODE_ON_SOUND);
   public final Identifier NEWCODE_OFF_SOUND = Identifier.of("ariscore", "newcode_off");
   public SoundEvent NEWCODE_OFF_SOUNDEVENT = SoundEvent.of(this.NEWCODE_OFF_SOUND);
   public final Identifier CATLAVAN_ON_SOUND = Identifier.of("ariscore", "catlavan_on");
   public SoundEvent CATLAVAN_ON_SOUNDEVENT = SoundEvent.of(this.CATLAVAN_ON_SOUND);
   public final Identifier CATLAVAN_OFF_SOUND = Identifier.of("ariscore", "catlavan_off");
   public SoundEvent CATLAVAN_OFF_SOUNDEVENT = SoundEvent.of(this.CATLAVAN_OFF_SOUND);
   public final Identifier CELESTIAL_ON_SOUND = Identifier.of("ariscore", "celestial_on");
   public SoundEvent CELESTIAL_ON_SOUNDEVENT = SoundEvent.of(this.CELESTIAL_ON_SOUND);
   public final Identifier CELESTIAL_OFF_SOUND = Identifier.of("ariscore", "celestial_off");
   public SoundEvent CELESTIAL_OFF_SOUNDEVENT = SoundEvent.of(this.CELESTIAL_OFF_SOUND);
   public final Identifier SWIPEIN_SOUND = Identifier.of("ariscore", "swipein");
   public SoundEvent SWIPEIN_SOUNDEVENT = SoundEvent.of(this.SWIPEIN_SOUND);
   public final Identifier SWIPEOUT_SOUND = Identifier.of("ariscore", "swipeout");
   public SoundEvent SWIPEOUT_SOUNDEVENT = SoundEvent.of(this.SWIPEOUT_SOUND);
   public final Identifier CLICK_SOUND = Identifier.of("ariscore", "click");
   public SoundEvent CLICK_SOUNDEVENT = SoundEvent.of(this.CLICK_SOUND);

   public void registerSounds() {
      Registry.register(Registries.SOUND_EVENT, this.KEYRELEASE_SOUND, this.KEYRELEASE_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.SWIPEIN_SOUND, this.SWIPEIN_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.SWIPEOUT_SOUND, this.SWIPEOUT_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.NEWCODE_ON_SOUND, this.NEWCODE_ON_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.NEWCODE_OFF_SOUND, this.NEWCODE_OFF_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.CATLAVAN_ON_SOUND, this.CATLAVAN_ON_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.CATLAVAN_OFF_SOUND, this.CATLAVAN_OFF_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.CELESTIAL_ON_SOUND, this.CELESTIAL_ON_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.CELESTIAL_OFF_SOUND, this.CELESTIAL_OFF_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.NURSULTAN_ON_SOUND, this.NURSULTAN_ON_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.NURSULTAN_OFF_SOUND, this.NURSULTAN_OFF_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.CLICK_SOUND, this.CLICK_SOUNDEVENT);
   }

   public void playEnable() {
      if (!ModuleManager.ClientSound.isDisabled()) {
         switch (ModuleManager.ClientSound.onOffSound.getValue().name()) {
            case "Newcode":
               this.playSound(this.NEWCODE_ON_SOUNDEVENT);
               break;
            case "Catlavan":
               this.playSound(this.CATLAVAN_ON_SOUNDEVENT);
               break;
            case "Celestial":
               this.playSound(this.CELESTIAL_ON_SOUNDEVENT);
               break;
            case "Nursultan":
               this.playSound(this.NURSULTAN_ON_SOUNDEVENT);
         }
      }
   }

   public void playDisable() {
      if (!ModuleManager.ClientSound.isDisabled()) {
         switch (ModuleManager.ClientSound.onOffSound.getValue().name()) {
            case "Newcode":
               this.playSound(this.NEWCODE_OFF_SOUNDEVENT);
               break;
            case "Catlavan":
               this.playSound(this.CATLAVAN_OFF_SOUNDEVENT);
               break;
            case "Celestial":
               this.playSound(this.CELESTIAL_OFF_SOUNDEVENT);
               break;
            case "Nursultan":
               this.playSound(this.NURSULTAN_OFF_SOUNDEVENT);
         }
      }
   }

   public void playClickSound() {
      this.playSound(this.CLICK_SOUNDEVENT);
   }

   public void playSound(SoundEvent sound) {
      if (!ModuleManager.ClientSound.isDisabled()) {
         if (mc.player != null && mc.world != null) {
            mc.world.playSound(mc.player, mc.player.getBlockPos(), sound, SoundCategory.BLOCKS, ModuleManager.ClientSound.volume.getValue().intValue() / 100.0F, 1.0F);
         }
      }
   }

   public void playBoolean() {
      this.playSound(this.KEYRELEASE_SOUNDEVENT);
   }

   public void playSwipeIn() {
      this.playSound(this.SWIPEIN_SOUNDEVENT);
   }

   public void playSwipeOut() {
      this.playSound(this.SWIPEOUT_SOUNDEVENT);
   }
  }
