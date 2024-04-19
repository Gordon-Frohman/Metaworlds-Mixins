package net.tclproject.metaworlds.controls.flip;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.World;
import net.tclproject.metaworlds.api.IMixinWorld;
import net.tclproject.metaworlds.api.SubWorld;

public class BlockFlipSubWorld extends Block {

    public BlockFlipSubWorld() {
        super(Material.rock);
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    public void onBlockAdded(World par1World, int par2, int par3, int par4) {
        if (((IMixinWorld)par1World).isSubWorld()) {
            SubWorld subWorldPar = (SubWorld) par1World;
            if (subWorldPar.getRotationPitch() == 0.0D) {
                subWorldPar.setRotationPitch(180.0D);
            } else {
                subWorldPar.setRotationPitch(0.0D);
            }
        }
    }
}
