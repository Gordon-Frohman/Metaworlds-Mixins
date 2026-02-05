package su.sergiusonesimus.metaworlds.zmixin.mixins.waila;

import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import mcp.mobius.waila.overlay.RayTracing;
import mcp.mobius.waila.overlay.WailaTickHandler;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinMovingObjectPosition;

@Mixin(WailaTickHandler.class)
public class MixinWailaTickHandler {

    @ModifyVariable(
        method = "tickClient",
        remap = false,
        ordinal = 0,
        at = @At(
            value = "INVOKE",
            target = "Lmcp/mobius/waila/overlay/RayTracing;getTarget()Lnet/minecraft/util/MovingObjectPosition;",
            remap = false,
            shift = Shift.AFTER))
    private World setSubworld(World world) {
        MovingObjectPosition target = RayTracing.instance()
            .getTarget();
        return target == null ? world : ((IMixinMovingObjectPosition) target).getWorld();
    }

}
