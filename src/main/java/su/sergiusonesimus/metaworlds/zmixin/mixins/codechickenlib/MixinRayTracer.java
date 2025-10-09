package su.sergiusonesimus.metaworlds.zmixin.mixins.codechickenlib;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import codechicken.lib.raytracer.RayTracer;
import su.sergiusonesimus.metaworlds.entity.player.EntityPlayerProxy;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinMovingObjectPosition;

@Mixin(RayTracer.class)
public class MixinRayTracer {

    private static EntityPlayer playerProxy;

    @Inject(method = "retraceBlock", remap = false, at = @At(value = "HEAD"))
    private static void getPlayerProxy(World world, EntityPlayer player, int x, int y, int z,
        CallbackInfoReturnable<MovingObjectPosition> cir) {
        getPlayerProxy(world, player);
    }

    @Inject(
        method = "reTrace(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;D)Lnet/minecraft/util/MovingObjectPosition;",
        remap = false,
        at = @At(value = "HEAD"))
    private static void getPlayerProxy(World world, EntityPlayer player, double reach,
        CallbackInfoReturnable<MovingObjectPosition> cir) {
        getPlayerProxy(world, player);
    }

    private static void getPlayerProxy(World world, EntityPlayer player) {
        playerProxy = player instanceof EntityPlayerProxy ? player : ((IMixinEntity) player).getProxyPlayer(world);
    }

    @WrapOperation(
        method = { "retraceBlock",
            "reTrace(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;D)Lnet/minecraft/util/MovingObjectPosition;" },
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lcodechicken/lib/raytracer/RayTracer;getCorrectedHeadVec(Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/util/Vec3;"))
    private static Vec3 getCorrectedHeadVec(EntityPlayer player, Operation<Vec3> original) {
        return original.call(playerProxy);
    }

    @WrapOperation(
        method = { "retraceBlock",
            "reTrace(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;D)Lnet/minecraft/util/MovingObjectPosition;" },
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;getLook(F)Lnet/minecraft/util/Vec3;"))
    private static Vec3 getLook(EntityPlayer player, float distance, Operation<Vec3> original) {
        return original.call(playerProxy, distance);
    }

    @Inject(method = "retraceBlock", remap = false, at = @At(value = "RETURN"))
    private static void setWorldObj(World world, EntityPlayer player, int x, int y, int z,
        CallbackInfoReturnable<MovingObjectPosition> cir) {
        setWorldObj(world, cir);
    }

    @Inject(
        method = "reTrace(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;D)Lnet/minecraft/util/MovingObjectPosition;",
        remap = false,
        at = @At(value = "RETURN"))
    private static void setWorldObj(World world, EntityPlayer player, double reach,
        CallbackInfoReturnable<MovingObjectPosition> cir) {
        setWorldObj(world, cir);
    }

    private static void setWorldObj(World world, CallbackInfoReturnable<MovingObjectPosition> cir) {
        MovingObjectPosition result = cir.getReturnValue();
        if (result != null) {
            ((IMixinMovingObjectPosition) result).setWorld(world);
        }
    }

}
