package su.sergiusonesimus.metaworlds.zmixin.mixins.forgemultipart;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import codechicken.microblock.CommonMicroClass;
import su.sergiusonesimus.metaworlds.integrations.ForgeMultipartIntegration;

@Mixin(codechicken.microblock.MicroblockRender$.class)
public class MixinMicroblockRender {

    @Inject(method = "renderHighlight", remap = false, at = { @At(value = "HEAD") })
    public void storeMOP(final EntityPlayer player, final MovingObjectPosition hit, final CommonMicroClass mcrClass,
        final int size, final int material, CallbackInfo ci) {
        ForgeMultipartIntegration.currentMOP = hit;
    }

}
