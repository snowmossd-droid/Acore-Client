package acore.hack.features.modules.client;

import acore.hack.features.modules.Module;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Rotations extends Module {
    
    public enum MoveFix { Off, Focused, Free }
    
    public MoveFix moveFix = MoveFix.Off;
    public boolean clientLook = false;
    public float fixRotation;
    
    private float prevYaw, prevPitch;
    
    public Rotations() {
        super("Rotations", Category.CLIENT);
    }
    
    public void onJump(boolean pre) {
        if (Float.isNaN(fixRotation) || moveFix == MoveFix.Off || mc.player.hasVehicle())
            return;
        if (pre) {
            prevYaw = mc.player.getYaw();
            mc.player.setYaw(fixRotation);
        } else {
            mc.player.setYaw(prevYaw);
        }
    }
    
    public void onPlayerMove() {
        if (moveFix == MoveFix.Free) {
            if (Float.isNaN(fixRotation) || mc.player.hasVehicle())
                return;
            // Fix movement velocity
        }
    }
    
    public void modifyVelocity(boolean pre) {
        if (moveFix == MoveFix.Focused && !Float.isNaN(fixRotation) && !mc.player.hasVehicle()) {
            if (pre) {
                prevYaw = mc.player.getYaw();
                mc.player.setYaw(fixRotation);
            } else {
                mc.player.setYaw(prevYaw);
            }
        }
    }
    
    public void onKeyInput() {
        if (moveFix == MoveFix.Free) {
            if (Float.isNaN(fixRotation) || mc.player.hasVehicle())
                return;
                
            float mF = mc.player.input.movementForward;
            float mS = mc.player.input.movementSideways;
            float delta = (mc.player.getYaw() - fixRotation) * MathHelper.RADIANS_PER_DEGREE;
            float cos = MathHelper.cos(delta);
            float sin = MathHelper.sin(delta);
            mc.player.input.movementSideways = Math.round(mS * cos - mF * sin);
            mc.player.input.movementForward = Math.round(mF * cos + mS * sin);
        }
    }
    
    @Override
    public boolean isEnabled() {
        return true; // Always enabled
    }
          }
