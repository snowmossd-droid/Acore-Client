package acore.hack.features.modules.render;

import acore.hack.features.modules.Module;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.List;

public class BlockESP extends Module {
    
    private List<BlockPos> trackedBlocks = new ArrayList<>();
    
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
    public void onRender() {
        // Block ESP rendering will be implemented later
    }
}
