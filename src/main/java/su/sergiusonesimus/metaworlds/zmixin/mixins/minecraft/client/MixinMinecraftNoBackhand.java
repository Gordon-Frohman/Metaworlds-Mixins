package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.MovingObjectPosition;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinMovingObjectPosition;

@Mixin(Minecraft.class)
public class MixinMinecraftNoBackhand {

    @Shadow(remap = true)
    public MovingObjectPosition objectMouseOver;

    // This mixin should only be applied if Backhand mod is not installed
    // Otherwise we use a mod-specific mixin

    @WrapOperation(
        method = "func_147121_ag",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/Minecraft;theWorld:Lnet/minecraft/client/multiplayer/WorldClient;",
            ordinal = 2))
    private WorldClient redirectTheWorld3(Minecraft instance, Operation<WorldClient> original) {
        return getCorrectWorld();
    }

    @WrapOperation(
        method = "func_147121_ag",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/Minecraft;theWorld:Lnet/minecraft/client/multiplayer/WorldClient;",
            ordinal = 3))
    private WorldClient redirectTheWorld4(Minecraft instance, Operation<WorldClient> original) {
        return getCorrectWorld();
    }

    private WorldClient getCorrectWorld() {
        return (WorldClient) ((IMixinMovingObjectPosition) this.objectMouseOver).getWorld();
    }
}
