package acore.hack.features.modules.render;

import acore.hack.features.modules.Module;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class BlockESP extends Module {
    
    private List<BlockPos> trackedBlocks = new ArrayList<>();
    public Color blockColor = new Color(0xFF0000, true);
    
    public BlockESP() {
        super("BlockESP", Category.RENDER);
    }
    
    public void addBlock(BlockPos pos) {
        if (!trackedBlocks.contains(pos)) {
            trackedBlocks.add(pos);
        }
    }
    
    public void removeBlock(BlockPos pos) {
        trackedBlocks.remove(pos);
    }
    
    public void clearBlocks() {
        trackedBlocks.clear();
    }
    
    public List<BlockPos> getTrackedBlocks() {
        return new ArrayList<>(trackedBlocks);
    }
    
    @Override
    public void onRender3D() {
        if (mc.world == null) return;
        
        Vec3d camera = mc.gameRenderer.getCamera().getPos();
        MatrixStack stack = new MatrixStack();
        
        for (BlockPos pos : trackedBlocks) {
            drawBlockESP(stack, pos, camera);
        }
    }
    
    private void drawBlockESP(MatrixStack stack, BlockPos pos, Vec3d camera) {
        double x = pos.getX() - camera.x;
        double y = pos.getY() - camera.y;
        double z = pos.getZ() - camera.z;
        
        stack.push();
        stack.translate(x, y, z);
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        // Draw box outline
        buffer.vertex(0, 0, 0).color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 255);
        buffer.vertex(1, 0, 0).color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 255);
        buffer.vertex(1, 0, 0).color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 255);
        buffer.vertex(1, 0, 1).color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 255);
        buffer.vertex(1, 0, 1).color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 255);
        buffer.vertex(0, 0, 1).color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 255);
        buffer.vertex(0, 0, 1).color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 255);
        buffer.vertex(0, 0, 0).color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 255);
        
        buffer.vertex(0, 1, 0).color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 255);
        buffer.vertex(1, 1, 0).color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 255);
        buffer.vertex(1, 1, 0).color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 255);
        buffer.vertex(1, 1, 1).color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 255);
        buffer.vertex(1, 1, 1).color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 255);
        buffer.vertex(0, 1, 1).color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 255);
        buffer.vertex(0, 1, 1).color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 255);
        buffer.vertex(0, 1, 0).color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 255);
        
        buffer.vertex(0, 0, 0).color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 255);
        buffer.vertex(0, 1, 0).color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 255);
        buffer.vertex(1, 0, 0).color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 255);
        buffer.vertex(1, 1, 0).color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 255);
        buffer.vertex(1, 0, 1).color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 255);
        buffer.vertex(1, 1, 1).color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 255);
        buffer.vertex(0, 0, 1).color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 255);
        buffer.vertex(0, 1, 1).color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 255);
        
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder.BuildResult result = buffer.end();
        BufferDrawer drawer = tessellator.draw(result);
        drawer.draw();
        
        stack.pop();
    }
            }
