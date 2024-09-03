package su.sergiusonesimus.metaworlds.joints;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.api.IMixinWorld;
import su.sergiusonesimus.metaworlds.api.SubWorld;

public class BlockHingeJoint extends Block {

    public BlockHingeJoint() {
        super(Material.wood);
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    public void onBlockAdded(World par1World, int par2, int par3, int par4) {
        if (!(par1World instanceof SubWorld)) {
            World newWorld = ((IMixinWorld) par1World).CreateSubWorld();
            SubWorld newSubWorld = (SubWorld) newWorld;
            newSubWorld.setTranslation((double) par2, newSubWorld.getTranslationY(), (double) par4);
            newWorld.setBlock(0, par3, 0, this);
            par1World.setBlockToAir(par2, par3, par4);
            newSubWorld.setRotationYaw(45.0D);
            newSubWorld.setRotationYawSpeed(1.0D);
            newSubWorld.setCenter(0.5D, (double) par3 + 0.5D, 0.5D);
            newSubWorld.setRotationPitchSpeed(1.0D);
            newSubWorld.setRotationRollSpeed(1.0D);
        }
    }
}
