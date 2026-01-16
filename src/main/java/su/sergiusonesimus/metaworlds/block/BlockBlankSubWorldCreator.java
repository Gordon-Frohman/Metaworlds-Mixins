package su.sergiusonesimus.metaworlds.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.item.MetaworldsItems;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class BlockBlankSubWorldCreator extends Block {

    public BlockBlankSubWorldCreator() {
        super(Material.rock);
        this.setCreativeTab(MetaworldsItems.creativeTab);
    }

    public void onBlockAdded(World world, int x, int y, int z) {
        if (!((IMixinWorld) world).isSubWorld()) {
            World newWorld = ((IMixinWorld) world)
                .createSubWorld(x + 0.5D, y + 0.5D, z + 0.5D, 0, 0, 0, 0, 45.0D, 0, 1.0D);
            newWorld.setBlock(x, y, z, this);
            world.setBlockToAir(x, y, z);
        }
    }
}
