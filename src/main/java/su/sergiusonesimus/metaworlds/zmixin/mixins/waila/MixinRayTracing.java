package su.sergiusonesimus.metaworlds.zmixin.mixins.waila;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.MovingObjectPosition;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import mcp.mobius.waila.overlay.RayTracing;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinMovingObjectPosition;

@Mixin(RayTracing.class)
public class MixinRayTracing {

    @Shadow(remap = false)
    private MovingObjectPosition target;

    @WrapOperation(
        method = "getIdentifierItems",
        remap = false,
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;theWorld:Lnet/minecraft/client/multiplayer/WorldClient;",
            remap = true))
    public WorldClient getSubworld(Minecraft instance, Operation<WorldClient> original) {
        return (WorldClient) ((IMixinMovingObjectPosition) target).getWorld();
    }

}
