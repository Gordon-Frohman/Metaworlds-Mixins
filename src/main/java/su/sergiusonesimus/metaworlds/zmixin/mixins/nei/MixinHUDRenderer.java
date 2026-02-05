package su.sergiusonesimus.metaworlds.zmixin.mixins.nei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import codechicken.nei.HUDRenderer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinMovingObjectPosition;

@Mixin(HUDRenderer.class)
public class MixinHUDRenderer {

    @WrapOperation(
        method = "renderOverlay",
        remap = false,
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;theWorld:Lnet/minecraft/client/multiplayer/WorldClient;",
            remap = true))
    private static WorldClient getSubworld(Minecraft instance, Operation<WorldClient> original) {
        return (WorldClient) ((IMixinMovingObjectPosition) Minecraft.getMinecraft().objectMouseOver).getWorld();
    }

}
