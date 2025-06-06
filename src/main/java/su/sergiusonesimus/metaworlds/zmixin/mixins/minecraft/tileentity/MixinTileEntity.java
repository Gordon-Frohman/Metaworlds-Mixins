package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;

import org.spongepowered.asm.mixin.Mixin;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.tileentity.IMixinTileEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(TileEntity.class)
public abstract class MixinTileEntity implements IMixinTileEntity {

    public double getDistanceFromGlobal(double par1, double par3, double par5) {
        // This may show up an error in an IDE, but in reality we do indeed make it extend the right thing so there is
        // no error here
        TileEntity tThis = (TileEntity) (Object) this;
        if (tThis.hasWorldObj() && tThis.getWorldObj() instanceof SubWorld) {
            Vec3 transformedPos = ((IMixinWorld) tThis.getWorldObj()).transformToGlobal(
                (double) tThis.xCoord + 0.5D,
                (double) tThis.yCoord + 0.5D,
                (double) tThis.zCoord + 0.5D);
            return transformedPos.squareDistanceTo(par1, par3, par5);
        } else {
            return tThis.getDistanceFrom(par1, par3, par5);
        }
    }

}
