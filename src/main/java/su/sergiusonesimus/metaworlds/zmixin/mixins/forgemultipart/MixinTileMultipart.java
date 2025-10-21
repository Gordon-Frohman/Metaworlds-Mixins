package su.sergiusonesimus.metaworlds.zmixin.mixins.forgemultipart;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.tileentity.MixinTileEntity;

@Mixin(targets = "codechicken.multipart.TileMultipart")
public class MixinTileMultipart extends MixinTileEntity {

    @Inject(method = { "notifyTileChange", "markRender" }, remap = false, at = @At(value = "HEAD"), cancellable = true)
    public void notifyTileChange(CallbackInfo ci) {
        if (this.getWorldObj() == null) ci.cancel();
    }

}
