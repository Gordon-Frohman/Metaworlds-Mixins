package su.sergiusonesimus.metaworlds.zmixin.mixins.littletiles;

import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;

@Mixin(BlockTile.class)
public class MixinBlockTile {

    @Inject(method = "getSelectedBoundingBoxFromPool", remap = false, at = @At(value = "HEAD"))
    private void getSelectedBoundingBoxFromPool$shareWorld(World world, int x, int y, int z,
        CallbackInfoReturnable<AxisAlignedBB> cir, @Share("world") LocalRef<World> sharedWorld) {
        shareWorld(world, sharedWorld);
    }

    @Inject(method = "getPickBlock", remap = false, at = @At(value = "HEAD"))
    private void getPickBlock$shareWorld(MovingObjectPosition target, World world, int x, int y, int z,
        EntityPlayer player, CallbackInfoReturnable<ItemStack> cir, @Share("world") LocalRef<World> sharedWorld) {
        shareWorld(world, sharedWorld);
    }

    @Inject(method = "addHitEffects", remap = false, at = @At(value = "HEAD"))
    public void addHitEffects$shareWorld(World worldObj, MovingObjectPosition target, EffectRenderer effectRenderer,
        CallbackInfoReturnable<Boolean> cir, @Share("world") LocalRef<World> sharedWorld) {
        shareWorld(worldObj, sharedWorld);
    }

    @Inject(method = "addDestroyEffects", remap = false, at = @At(value = "HEAD"))
    public void addDestroyEffects$shareWorld(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer,
        CallbackInfoReturnable<Boolean> cir, @Share("world") LocalRef<World> sharedWorld) {
        shareWorld(world, sharedWorld);
    }

    @Inject(method = "getPlayerRelativeBlockHardness", remap = false, at = @At(value = "HEAD"))
    public void getPlayerRelativeBlockHardness$shareWorld(EntityPlayer player, World world, int x, int y, int z,
        CallbackInfoReturnable<Float> cir, @Share("world") LocalRef<World> sharedWorld) {
        shareWorld(world, sharedWorld);
    }

    @Inject(
        method = "removedByPlayer(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;III)Z",
        remap = false,
        at = @At(value = "HEAD"))
    private void removedByPlayer$shareWorld(World world, EntityPlayer player, int x, int y, int z,
        CallbackInfoReturnable<Boolean> cir, @Share("world") LocalRef<World> sharedWorld) {
        shareWorld(world, sharedWorld);
    }

    @Inject(method = "onBlockActivated", remap = false, at = @At(value = "HEAD"))
    public void onBlockActivated$shareWorld(World world, int x, int y, int z, EntityPlayer player, int side,
        float moveX, float moveY, float moveZ, CallbackInfoReturnable<Boolean> cir,
        @Share("world") LocalRef<World> sharedWorld) {
        shareWorld(world, sharedWorld);
    }

    private void shareWorld(World world, LocalRef<World> sharedWorld) {
        sharedWorld.set(world);
    }

    @WrapOperation(
        method = { "getSelectedBoundingBoxFromPool", "getPickBlock", "addHitEffects", "addDestroyEffects" },
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lcom/creativemd/littletiles/common/tileentity/TileEntityLittleTiles;updateLoadedTile(Lnet/minecraft/entity/player/EntityPlayer;)Z",
            remap = false))
    private boolean updateLoadedTile(TileEntityLittleTiles instance, EntityPlayer player, Operation<Boolean> original,
        @Share("world") LocalRef<World> world) {
        return original.call(instance, ((IMixinEntity) player).getProxyPlayer(world.get()));
    }

    @ModifyVariable(
        method = { "removedByPlayer(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;III)Z",
            "getPlayerRelativeBlockHardness", "onBlockActivated" },
        remap = false,
        at = @At(value = "HEAD"),
        argsOnly = true)
    private EntityPlayer modifyPlayer(EntityPlayer player, @Share("world") LocalRef<World> world) {
        return ((IMixinEntity) player).getProxyPlayer(world.get());
    }

}
