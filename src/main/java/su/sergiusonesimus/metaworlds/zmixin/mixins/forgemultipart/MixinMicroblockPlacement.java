package su.sergiusonesimus.metaworlds.zmixin.mixins.forgemultipart;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import codechicken.microblock.PlacementProperties;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinMovingObjectPosition;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(targets = "codechicken.microblock.MicroblockPlacement")
public class MixinMicroblockPlacement {

    @Shadow(remap = false)
    private World world;

    private static MovingObjectPosition storedMOP;

    @Inject(method = "<init>", at = @At(value = "HEAD"))
    private static void storeVariables(EntityPlayer player, MovingObjectPosition hit, int size, int material,
        boolean checkMaterial, PlacementProperties pp, CallbackInfo ci) {
        storedMOP = hit;
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/player/EntityPlayer;worldObj:Lnet/minecraft/world/World;"))
    public World getWorld(EntityPlayer player, Operation<World> original) {
        return ((IMixinMovingObjectPosition) storedMOP).getWorld();
    }

    @WrapOperation(
        method = "<init>",
        at = @At(value = "FIELD", target = "Lnet/minecraft/util/MovingObjectPosition;hitVec:Lnet/minecraft/util/Vec3;"))
    public Vec3 getVhit(MovingObjectPosition mop, Operation<Vec3> original) {
        Vec3 result = original.call(mop);
        return this.world.isRemote ? ((IMixinWorld) this.world).transformToLocal(result) : result;
    }

}
