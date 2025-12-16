package su.sergiusonesimus.metaworlds.event;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class MetaworldsEventFactory {

    /**
     * Use this method to fire a {@link BlockDisplacementEvent} whenever a block is displaced from main world to a
     * subworld and vice versa. <br>
     * Used for block rotation on subworld reintegration and for redefining spawn points on bed displacement.
     * 
     * @param x
     * @param y
     * @param z
     * @param sourceWorld - The world from which the block is being removed
     * @param targetWorld - The world in which the block is being placed
     * @return Modified {@link BlockDisplacementEvent} containing metadata and tile entity of the rotated block
     */
    public static BlockDisplacementEvent onBlockDisplacement(int x, int y, int z, World sourceWorld,
        World targetWorld) {
        Block block = sourceWorld.getBlock(x, y, z);
        int meta = sourceWorld.getBlockMetadata(x, y, z);
        TileEntity te = sourceWorld.getTileEntity(x, y, z);

        BlockDisplacementEvent event = new BlockDisplacementEvent(x, y, z, sourceWorld, targetWorld, block, meta, te);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

}
