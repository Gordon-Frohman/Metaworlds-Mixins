package su.sergiusonesimus.metaworlds.event;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;

/**
 * An event fired whenever a block is displaced from main world to a subworld and vice versa. <br>
 * Used for block rotation on subworld reintegration and for redefining spawn points on bed displacement. <br>
 * Use {@link MetaworldsEventFactory#onBlockDisplacement()} to fire this event if you are implementing a custom
 * subworld creation/reintegration technique.
 */
public class BlockDisplacementEvent extends BlockEvent {

    /** The world in which the block is being placed */
    public final World targetWorld;
    /** The tile entity on specified coordinates. Can be null if there's none */
    public TileEntity tileEntity;

    /**
     * An event fired whenever a block is displaced from main world to a subworld and vice versa. <br>
     * Used for block rotation on subworld reintegration and for redefining spawn points on bed displacement. <br>
     * Use {@link MetaworldsEventFactory#onBlockDisplacement()} to fire this event if you are implementing a custom
     * subworld creation/reintegration technique.
     * 
     * @param x
     * @param y
     * @param z
     * @param sourceWorld   - The world from which the block is being removed
     * @param targetWorld   - The world in which the block is being placed
     * @param block
     * @param blockMetadata
     * @param tileEntity    - The tile entity on specified coordinates. Can be null if there's none
     */
    BlockDisplacementEvent(int x, int y, int z, World sourceWorld, World targetWorld, Block block, int blockMetadata,
        TileEntity tileEntity) {
        super(x, y, z, sourceWorld, block, blockMetadata);
        this.targetWorld = targetWorld;
        this.tileEntity = tileEntity;
    }

}
