package su.sergiusonesimus.metaworlds.zmixin.mixins.warpdrive;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cr0s.warpdrive.data.CameraRegistryItem;
import cr0s.warpdrive.data.EnumCameraType;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.warpdrive.IMixinCameraRegistryItem;

@Mixin(CameraRegistryItem.class)
public class MixinCameraRegistryItem implements IMixinCameraRegistryItem {

    private int subworldId = 0;

    @Override
    public int getSubworldId() {
        return subworldId;
    }

    @Override
    public void setSubworldId(int id) {
        subworldId = id;
    }

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    public void storeSubworldId(World world, ChunkPosition position, int videoChannel, EnumCameraType enumCameraType,
        CallbackInfo ci) {
        subworldId = ((IMixinWorld) world).getSubWorldID();
    }

    @Inject(method = "isTileEntity", remap = false, at = @At(value = "RETURN"), cancellable = true)
    public void isTileEntity(TileEntity tileEntity, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(
            cir.getReturnValue() && ((IMixinWorld) tileEntity.getWorldObj()).getSubWorldID() == subworldId);
    }

}
