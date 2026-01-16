package su.sergiusonesimus.metaworlds.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.api.SubWorldTypeManager;
import su.sergiusonesimus.metaworlds.item.MetaworldsItems;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class BlockFloatingWood extends Block {

    public BlockFloatingWood() {
        super(Material.wood);
        this.setCreativeTab(MetaworldsItems.creativeTab);
    }

    public void onBlockAdded(World world, int x, int y, int z) {
        if (!((IMixinWorld) world).isSubWorld()) {
            World newWorld = ((IMixinWorld) world)
                .createSubWorld(x + 0.5D, y + 0.5D, z + 0.5D, 0, 0, 0, 0, 45.0D, 0, 1.0D);
            SubWorld newSubWorld = (SubWorld) newWorld;
            newSubWorld.setSubWorldType(SubWorldTypeManager.SUBWORLD_TYPE_BOAT);
            newWorld.setBlock(x, y, z, this);
            world.setBlockToAir(x, y, z);
        }
    }
}
