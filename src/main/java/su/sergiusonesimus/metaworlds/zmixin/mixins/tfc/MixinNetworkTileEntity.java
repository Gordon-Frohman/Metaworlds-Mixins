package su.sergiusonesimus.metaworlds.zmixin.mixins.tfc;

import net.minecraft.nbt.NBTTagCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.bioxx.tfc.Handlers.Network.DataBlockPacket;
import com.bioxx.tfc.TileEntities.NetworkTileEntity;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.tfc.IMixinDataBlockPacket;
import su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.tileentity.MixinTileEntity;

@Mixin(NetworkTileEntity.class)
public abstract class MixinNetworkTileEntity extends MixinTileEntity {

    @Inject(
        method = "createDataPacket(Lnet/minecraft/nbt/NBTTagCompound;)Lcom/bioxx/tfc/Handlers/Network/DataBlockPacket;",
        remap = false,
        at = @At(value = "RETURN"),
        cancellable = true)
    public void createDataPacket(NBTTagCompound nbt, CallbackInfoReturnable<DataBlockPacket> cir) {
        cir.setReturnValue(
            ((IMixinDataBlockPacket) cir.getReturnValue())
                .setSubworldId(((IMixinWorld) this.getWorldObj()).getSubWorldID()));
    }

}
