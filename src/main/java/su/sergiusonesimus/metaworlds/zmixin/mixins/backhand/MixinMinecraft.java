package su.sergiusonesimus.metaworlds.zmixin.mixins.backhand;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.MovingObjectPosition;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import su.sergiusonesimus.metaworlds.zmixin.MixinPriorities;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinMovingObjectPosition;

@Mixin(value = Minecraft.class, priority = MixinPriorities.BACKHAND)
public class MixinMinecraft {

    @Shadow(remap = true)
    public MovingObjectPosition objectMouseOver;

    @WrapOperation(
        method = "backhand$rightClickBlock",
        remap = false,
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/Minecraft;theWorld:Lnet/minecraft/client/multiplayer/WorldClient;"))
    private WorldClient getCorrectWorld(Minecraft instance, Operation<WorldClient> original) {
        return (WorldClient) ((IMixinMovingObjectPosition) this.objectMouseOver).getWorld();
    }

}
