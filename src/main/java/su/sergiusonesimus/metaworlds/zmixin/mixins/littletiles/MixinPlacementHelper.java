package su.sergiusonesimus.metaworlds.zmixin.mixins.littletiles;

import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.creativemd.littletiles.common.utils.PlacementHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import su.sergiusonesimus.metaworlds.entity.player.EntityPlayerProxy;

@Mixin(PlacementHelper.class)
public class MixinPlacementHelper {

    @WrapOperation(
        method = "getPreviewTiles",
        remap = false,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isSneaking()Z", remap = true))
    private boolean isSneaking(EntityPlayer instance, Operation<Boolean> original) {
        return original.call(instance instanceof EntityPlayerProxy proxy ? proxy.getRealPlayer() : instance);
    }

}
