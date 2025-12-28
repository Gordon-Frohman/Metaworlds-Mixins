package su.sergiusonesimus.metaworlds.zmixin.mixins.angelica;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import su.sergiusonesimus.metaworlds.zmixin.MixinPriorities;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinMovingObjectPosition;

@Pseudo
@Mixin(targets = "com.gtnewhorizons.angelica.proxy.ClientProxy", remap = false, priority = MixinPriorities.ANGELICA)
public class MixinClientProxy {

    @WrapOperation(
        method = "onRenderOverlay(Lnet/minecraftforge/client/event/RenderGameOverlayEvent$Text;)V",
        remap = false,
        at = {
            @At(
                value = "FIELD",
                target = "Lnet/minecraft/client/Minecraft;theWorld:Lnet/minecraft/client/multiplayer/WorldClient;",
                ordinal = 0),
            @At(
                value = "FIELD",
                target = "Lnet/minecraft/client/Minecraft;theWorld:Lnet/minecraft/client/multiplayer/WorldClient;",
                ordinal = 1) })
    private WorldClient wrapGetTheWorld(Minecraft instance, Operation<WorldClient> original) {
        return (WorldClient) ((IMixinMovingObjectPosition) instance.objectMouseOver).getWorld();
    }

}
