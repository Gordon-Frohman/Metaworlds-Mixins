package su.sergiusonesimus.metaworlds.miniature;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.World;
import su.sergiusonesimus.metaworlds.api.SubWorld;

public class BlockScaleNormalizer extends Block {

    public BlockScaleNormalizer() {
        super(Material.wood);
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    public void onBlockAdded(World par1World, int par2, int par3, int par4) {
        if (par1World instanceof SubWorld) {
            SubWorld subWorldPar = (SubWorld) par1World;
            double oldCenterX = subWorldPar.getCenterX();
            double oldCenterY = subWorldPar.getCenterY();
            double oldCenterZ = subWorldPar.getCenterZ();
            subWorldPar.setCenter((double) par2 + 0.5D, (double) par3 + 0.5D, (double) par4 + 0.5D);
            subWorldPar.setScaling(1.0D);
            subWorldPar.setCenter(oldCenterX, oldCenterY, oldCenterZ);
        }
    }
}
