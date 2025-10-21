package su.sergiusonesimus.metaworlds.zmixin.mixins.codechickenlib;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import codechicken.lib.vec.Vector3;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(Vector3.class)
public class MixinVector3 {

    @Overwrite(remap = false)
    public static Vector3 fromEntity(Entity e) {
        return new Vector3(((IMixinWorld) e.worldObj).transformToGlobal(e));
    }

    @Overwrite(remap = false)
    public static Vector3 fromEntityCenter(Entity e) {
        return new Vector3(
            ((IMixinWorld) e.worldObj).transformToGlobal(e.posX, e.posY - e.yOffset + e.height / 2, e.posZ));
    }

    @Overwrite(remap = false)
    public static Vector3 fromTileEntity(TileEntity e) {
        return fromTileEntityCenter(e).add(-0.5D, -0.5D, -0.5D);
    }

    @Overwrite(remap = false)
    public static Vector3 fromTileEntityCenter(TileEntity e) {
        return new Vector3(
            ((IMixinWorld) e.getWorldObj()).transformToGlobal(e.xCoord + 0.5, e.yCoord + 0.5, e.zCoord + 0.5));
    }

}
