package acore.hack.event.impl;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import acore.hack.event.Event;

public class EventBlockRender extends Event {
    private final BlockState state;
    private final BlockPos pos;
    private final MatrixStack matrices;
    private final VertexConsumer vertexConsumer;

    public EventBlockRender(BlockState state, BlockPos pos, MatrixStack matrices, VertexConsumer vertexConsumer) {
        this.state = state;
        this.pos = pos;
        this.matrices = matrices;
        this.vertexConsumer = vertexConsumer;
    }

    public BlockState getState() {
        return state;
    }

    public BlockPos getPos() {
        return pos;
    }

    public MatrixStack getMatrices() {
        return matrices;
    }

    public VertexConsumer getVertexConsumer() {
        return vertexConsumer;
    }
}
