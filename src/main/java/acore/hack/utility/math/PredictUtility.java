package acore.hack.utility.math;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import acore.hack.features.modules.Module;

public final class PredictUtility {
    public static Box predictBox(Entity entity, int ticks) {
        if (entity == null || Module.mc.player == null) {
            return new Box(0, 0, 0, 0, 0, 0);
        }

        Vec3d currentPos = entity.getPos();
        Vec3d velocity = entity.getVelocity();
        Vec3d predictedPos = currentPos.add(velocity.multiply(ticks));

        Box currentBox = entity.getBoundingBox();
        double width = currentBox.getLengthX();
        double height = currentBox.getLengthY();
        double depth = currentBox.getLengthZ();

        return new Box(
            predictedPos.x - width / 2,
            predictedPos.y,
            predictedPos.z - depth / 2,
            predictedPos.x + width / 2,
            predictedPos.y + height,
            predictedPos.z + depth / 2
        );
    }

    public static Vec3d predictPosition(Entity entity, int ticks) {
        if (entity == null) {
            return Vec3d.ZERO;
        }
        return entity.getPos().add(entity.getVelocity().multiply(ticks));
    }
}
