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

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;

@Mixin(BlockTile.class)
public class MixinBlockTile {

    private World storedWorld;

    @Inject(method = "getSelectedBoundingBoxFromPool", remap = false, at = @At(value = "HEAD"))
    private void getSelectedBoundingBoxFromPool(World world, int x, int y, int z,
        CallbackInfoReturnable<AxisAlignedBB> cir) {
        storeWorld(world);
    }

    @Inject(method = "getPickBlock", remap = false, at = @At(value = "HEAD"))
    private void getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player,
        CallbackInfoReturnable<ItemStack> cir) {
        storeWorld(world);
    }

    @Inject(method = "addHitEffects", remap = false, at = @At(value = "HEAD"))
    public void addHitEffects(World worldObj, MovingObjectPosition target, EffectRenderer effectRenderer,
        CallbackInfoReturnable<Boolean> cir) {
        storeWorld(worldObj);
    }

    @Inject(method = "addDestroyEffects", remap = false, at = @At(value = "HEAD"))
    public void addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer,
        CallbackInfoReturnable<Boolean> cir) {
        storeWorld(world);
    }

    @Inject(method = "getPlayerRelativeBlockHardness", remap = false, at = @At(value = "HEAD"))
    public void getPlayerRelativeBlockHardness(EntityPlayer player, World world, int x, int y, int z,
        CallbackInfoReturnable<Float> cir) {
        storeWorld(world);
    }

    @Inject(
        method = "removedByPlayer(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;III)Z",
        remap = false,
        at = @At(value = "HEAD"))
    private void removedByPlayer(World world, EntityPlayer player, int x, int y, int z,
        CallbackInfoReturnable<Boolean> cir) {
        storeWorld(world);
    }

    @Inject(method = "onBlockActivated", remap = false, at = @At(value = "HEAD"))
    public void onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float moveX,
        float moveY, float moveZ, CallbackInfoReturnable<Boolean> cir) {
        storeWorld(world);
    }

    private void storeWorld(World world) {
        storedWorld = world;
    }

    @WrapOperation(
        method = { "getSelectedBoundingBoxFromPool", "getPickBlock", "addHitEffects", "addDestroyEffects" },
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lcom/creativemd/littletiles/common/tileentity/TileEntityLittleTiles;updateLoadedTile(Lnet/minecraft/entity/player/EntityPlayer;)Z",
            remap = false))
    private boolean updateLoadedTile(TileEntityLittleTiles instance, EntityPlayer player, Operation<Boolean> original) {
        return original.call(instance, ((IMixinEntity) player).getProxyPlayer(storedWorld));
    }

    @ModifyVariable(
        method = { "removedByPlayer(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;III)Z",
            "getPlayerRelativeBlockHardness", "onBlockActivated" },
        remap = false,
        at = @At(value = "HEAD"),
        argsOnly = true)
    private EntityPlayer modifyPlayer(EntityPlayer player) {
        return ((IMixinEntity) player).getProxyPlayer(storedWorld);
    }

}
