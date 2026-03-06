package su.sergiusonesimus.metaworlds.zmixin.mixins.warpdrive;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import cr0s.warpdrive.block.detection.BlockMonitor;
import cr0s.warpdrive.data.CameraRegistryItem;
import cr0s.warpdrive.data.EnumCameraType;
import su.sergiusonesimus.metaworlds.integrations.warpdrive.SubworldClientCameraHandler;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.warpdrive.IMixinCameraRegistryItem;

@Mixin(BlockMonitor.class)
public class MixinBlockMonitor {

    private World monitorWorld;
    private World cameraWorld;

    @Inject(
        method = "onBlockActivated",
        at = @At(
            value = "INVOKE",
            target = "Lcr0s/warpdrive/render/ClientCameraHandler;setupViewpoint(Lcr0s/warpdrive/data/EnumCameraType;Lnet/minecraft/entity/player/EntityPlayer;FFIIILnet/minecraft/block/Block;IIILnet/minecraft/block/Block;)V",
            remap = false,
            shift = Shift.BEFORE))
    public void onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX,
        float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir,
        @Local(name = "camera") CameraRegistryItem camera) {
        monitorWorld = world;
        cameraWorld = ((IMixinWorld) ((IMixinWorld) world).getParentWorld())
            .getSubWorld(((IMixinCameraRegistryItem) camera).getSubworldId());
    }

    @WrapOperation(
        method = "onBlockActivated",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlock(III)Lnet/minecraft/block/Block;"))
    public Block getCameraBlock(World world, int x, int y, int z, Operation<Block> original) {
        return cameraWorld != null ? original.call(cameraWorld, x, y, z) : original.call(world, x, y, z);
    }

    @WrapOperation(
        method = "onBlockActivated",
        at = @At(
            value = "INVOKE",
            target = "Lcr0s/warpdrive/render/ClientCameraHandler;setupViewpoint(Lcr0s/warpdrive/data/EnumCameraType;Lnet/minecraft/entity/player/EntityPlayer;FFIIILnet/minecraft/block/Block;IIILnet/minecraft/block/Block;)V",
            remap = false))
    public void setupViewpoint(EnumCameraType enumCameraType, EntityPlayer entityPlayer, float initialYaw,
        float initialPitch, int monitor_x, int monitor_y, int monitor_z, Block blockMonitor, int camera_x, int camera_y,
        int camera_z, Block blockCamera, Operation<Void> original) {
        SubworldClientCameraHandler.setupViewpoint(
            enumCameraType,
            entityPlayer,
            initialYaw,
            initialPitch,
            monitor_x,
            monitor_y,
            monitor_z,
            blockMonitor,
            monitorWorld,
            camera_x,
            camera_y,
            camera_z,
            blockCamera,
            cameraWorld);
    }

}
