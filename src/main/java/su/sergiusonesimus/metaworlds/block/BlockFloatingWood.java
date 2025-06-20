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

    public void onBlockAdded(World par1World, int par2, int par3, int par4) {
        if (!((IMixinWorld) par1World).isSubWorld()) {
            World newWorld = ((IMixinWorld) par1World).createSubWorld();
            SubWorld newSubWorld = (SubWorld) newWorld;
            newSubWorld.setSubWorldType(SubWorldTypeManager.SUBWORLD_TYPE_BOAT);
            newSubWorld.setTranslation((double) par2, newSubWorld.getTranslationY(), (double) par4);
            newWorld.setBlock(0, par3, 0, this);
            par1World.setBlockToAir(par2, par3, par4);
            newSubWorld.setRotationYaw(45.0D);
        }
    }
}
