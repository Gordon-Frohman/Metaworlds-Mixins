package su.sergiusonesimus.metaworlds.zmixin.mixins.forgemultipart;

import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import codechicken.lib.vec.Cuboid6;
import codechicken.multipart.JIconHitEffects;
import codechicken.multipart.TMultiPart;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.integrations.ForgeMultipartIntegration;
import su.sergiusonesimus.metaworlds.util.OrientedBB;

@Mixin(targets = "codechicken.multipart.IconHitEffects$")
public class MixinIconHitEffects {

    @Inject(method = "addHitEffects", remap = false, at = @At(value = "HEAD"))
    private void shareSubworld(JIconHitEffects part, MovingObjectPosition hit, EffectRenderer effectRenderer,
        CallbackInfo ci, @Share("subworld") LocalRef<SubWorld> subworld) {
        shareSubworld(part, subworld);
    }

    @Inject(
        method = "addDestroyEffects(Lcodechicken/multipart/JIconHitEffects;Lnet/minecraft/client/particle/EffectRenderer;Z)V",
        remap = false,
        at = @At(value = "HEAD"))
    private void shareSubworld(JIconHitEffects part, EffectRenderer effectRenderer, boolean scaleDensity,
        CallbackInfo ci, @Share("subworld") LocalRef<SubWorld> subworld) {
        shareSubworld(part, subworld);
    }

    private void shareSubworld(JIconHitEffects part, LocalRef<SubWorld> sharedSubworld) {
        World teWorld = ((TMultiPart) part).tile()
            .getWorldObj();
        if (teWorld instanceof SubWorld subworld) sharedSubworld.set(subworld);
    }

    @WrapOperation(
        method = { "addHitEffects",
            "addDestroyEffects(Lcodechicken/multipart/JIconHitEffects;Lnet/minecraft/client/particle/EffectRenderer;Z)V" },
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lcodechicken/lib/vec/Cuboid6;copy()Lcodechicken/lib/vec/Cuboid6;",
            remap = false))
    public Cuboid6 rotateCuboid(Cuboid6 instance, Operation<Cuboid6> original,
        @Share("subworld") LocalRef<SubWorld> sharedSubworld) {
        Cuboid6 result = original.call(instance);

        SubWorld subworld = sharedSubworld.get();
        if (subworld != null) {
            OrientedBB obb = ForgeMultipartIntegration.createOBB(result);
            obb.rotatePitch(subworld.getRotationPitch() % 360, 0.5D, 0.5D);
            obb.rotateYaw(subworld.getRotationYaw() % 360, 0.5D, 0.5D);
            obb.rotateRoll(subworld.getRotationRoll() % 360, 0.5D, 0.5D);
            result = new Cuboid6(obb.minX, obb.minY, obb.minZ, obb.maxX, obb.maxY, obb.maxZ);
            sharedSubworld.set(null);
        }

        return result;
    }

}
