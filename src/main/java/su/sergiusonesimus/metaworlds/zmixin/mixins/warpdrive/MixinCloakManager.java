package su.sergiusonesimus.metaworlds.zmixin.mixins.warpdrive;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import cr0s.warpdrive.data.CloakManager;
import su.sergiusonesimus.metaworlds.integrations.warpdrive.SubworldChunkHandler;
import su.sergiusonesimus.metaworlds.zmixin.MixinPriorities;

@Mixin(value = CloakManager.class, priority = MixinPriorities.WARPDRIVE)
public class MixinCloakManager {

    @WrapOperation(
        method = "onBlockChange",
        remap = false,
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;theWorld:Lnet/minecraft/client/multiplayer/WorldClient;",
            remap = true))
    private static WorldClient getCurentWorld(Minecraft instance, Operation<WorldClient> original) {
        return (WorldClient) SubworldChunkHandler.currentWorld;
    }

}
