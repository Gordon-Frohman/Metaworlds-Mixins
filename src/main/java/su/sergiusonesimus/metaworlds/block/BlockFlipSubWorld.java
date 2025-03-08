package su.sergiusonesimus.metaworlds.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class BlockFlipSubWorld extends Block {

    public BlockFlipSubWorld() {
        super(Material.rock);
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    public void onBlockAdded(World par1World, int par2, int par3, int par4) {
        if (((IMixinWorld) par1World).isSubWorld()) {
            SubWorld subWorldPar = (SubWorld) par1World;
            if (subWorldPar.getRotationPitch() == 0.0D) {
                subWorldPar.setRotationPitch(180.0D);
            } else {
                subWorldPar.setRotationPitch(0.0D);
            }
        }
    }
}
