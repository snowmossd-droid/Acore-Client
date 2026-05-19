package acore.hack.event.impl;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import acore.hack.event.Event;

public class EventDamageBlock extends Event {
    private final BlockPos pos;
    private final Direction direction;

    public EventDamageBlock(BlockPos pos, Direction direction) {
        this.pos = pos;
        this.direction = direction;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Direction getDirection() {
        return direction;
    }
}
