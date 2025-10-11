package su.sergiusonesimus.metaworlds.zmixin.mixins.forgemultipart;

import net.minecraft.entity.player.EntityPlayerMP;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import su.sergiusonesimus.metaworlds.integrations.ForgeMultipartIntegration;

@Mixin(
    targets = { "codechicken.multipart.handler.MultipartSPH$$anonfun$onTickEnd$1",
        "codechicken.multipart.handler.MultipartSPH$$anonfun$onTickEnd$4",
        "codechicken.multipart.handler.MultipartSPH$$anonfun$onTickEnd$5$$anonfun$apply$4" })
public class MixinMultipartSPH$$anonfun$onTickEnd {

    @WrapOperation(
        method = { "apply" },
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;getEntityId()I"),
        remap = false)
    public int getSubworldSpecificEntityId(EntityPlayerMP player, Operation<Integer> original) {
        return ForgeMultipartIntegration.getSubworldSpecificEntityId(player);
    }

}
