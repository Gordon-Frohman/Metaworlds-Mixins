package su.sergiusonesimus.metaworlds.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.item.MetaworldsItems;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class BlockSubWorldAligner extends Block {

    public BlockSubWorldAligner() {
        super(Material.wood);
        this.setCreativeTab(MetaworldsItems.creativeTab);
    }

    public void onBlockAdded(World par1World, int par2, int par3, int par4) {
        if (((IMixinWorld) par1World).isSubWorld()) {
            SubWorld subWorldPar = (SubWorld) par1World;
            subWorldPar.setRotationYaw((double) Math.round(subWorldPar.getRotationYaw() / 90.0D) * 90.0D);
            subWorldPar.setRotationPitch((double) Math.round(subWorldPar.getRotationPitch() / 90.0D) * 90.0D);
            subWorldPar.setRotationRoll((double) Math.round(subWorldPar.getRotationRoll() / 90.0D) * 90.0D);
            subWorldPar.setTranslation(
                (double) Math.round(subWorldPar.getTranslationX()),
                (double) Math.round(subWorldPar.getTranslationY()),
                (double) Math.round(subWorldPar.getTranslationZ()));
            subWorldPar.setMotion(0.0D, 0.0D, 0.0D);
            subWorldPar.setRotationYawSpeed(0.0D);
            subWorldPar.setRotationPitchSpeed(0.0D);
            subWorldPar.setRotationRollSpeed(0.0D);
            subWorldPar.setScaleChangeRate(0.0D);
        }
    }
}
