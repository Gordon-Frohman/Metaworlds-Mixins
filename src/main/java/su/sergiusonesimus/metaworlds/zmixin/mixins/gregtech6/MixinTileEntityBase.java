package su.sergiusonesimus.metaworlds.zmixin.mixins.gregtech6;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregapi.network.IPacket;
import gregapi.network.packets.PacketCoordinates;
import gregapi.tileentity.base.TileEntityBase04MultiTileEntities;
import gregapi.tileentity.base.TileEntityBase06Covers;
import gregapi.tileentity.notick.TileEntityBase03MultiTileEntities;
import gregapi.tileentity.notick.TileEntityBase04Covers;
import su.sergiusonesimus.metaworlds.MetaworldsMod;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.gregtech6.IMixinPacketCoordinates;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.tileentity.MixinTileEntity;

@Mixin(
    value = { TileEntityBase04MultiTileEntities.class, TileEntityBase06Covers.class,
        TileEntityBase03MultiTileEntities.class, TileEntityBase04Covers.class })
public class MixinTileEntityBase extends MixinTileEntity {

    @Inject(method = "getClientDataPacket", remap = false, at = @At(value = "RETURN"), cancellable = true)
    public void getClientDataPacket(boolean aSendAll, CallbackInfoReturnable<IPacket> cir) {
        modifyResult(cir);
    }

    @Inject(method = "getClientDataPacketByte", remap = false, at = @At(value = "RETURN"), cancellable = true)
    public void getClientDataPacketByte(boolean aSendAll, byte aByte, CallbackInfoReturnable<IPacket> cir) {
        modifyResult(cir);
    }

    @Inject(method = "getClientDataPacketShort", remap = false, at = @At(value = "RETURN"), cancellable = true)
    public void getClientDataPacketShort(boolean aSendAll, short aShort, CallbackInfoReturnable<IPacket> cir) {
        modifyResult(cir);
    }

    @Inject(method = "getClientDataPacketInteger", remap = false, at = @At(value = "RETURN"), cancellable = true)
    public void getClientDataPacketInteger(boolean aSendAll, int aInteger, CallbackInfoReturnable<IPacket> cir) {
        modifyResult(cir);
    }

    @Inject(method = "getClientDataPacketLong", remap = false, at = @At(value = "RETURN"), cancellable = true)
    public void getClientDataPacketLong(boolean aSendAll, long aLong, CallbackInfoReturnable<IPacket> cir) {
        modifyResult(cir);
    }

    @Inject(method = "getClientDataPacketByteArray", remap = false, at = @At(value = "RETURN"), cancellable = true)
    public void getClientDataPacketByteArray(boolean aSendAll, byte[] aByteArray, CallbackInfoReturnable<IPacket> cir) {
        modifyResult(cir);
    }

    private void modifyResult(CallbackInfoReturnable<IPacket> cir) {
        IPacket result = cir.getReturnValue();
        if (result instanceof PacketCoordinates cPacket) cir.setReturnValue(
            ((IMixinPacketCoordinates) cPacket).setSubworldId(((IMixinWorld) this.getWorldObj()).getSubWorldID()));
    }

}
