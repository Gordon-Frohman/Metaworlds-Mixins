package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.renderer;

import net.minecraft.client.renderer.EntitySorter;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.Vec3;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(EntitySorter.class)
public class MixinEntitySorter {

    @Shadow(remap = true)
    private double entityPosX;

    @Shadow(remap = true)
    private double entityPosY;

    @Shadow(remap = true)
    private double entityPosZ;

    // compare

    private Vec3 transformedPos1;
    private Vec3 transformedPos2;

    @Inject(
        method = "compare(Lnet/minecraft/client/renderer/WorldRenderer;Lnet/minecraft/client/renderer/WorldRenderer;)I",
        at = @At("HEAD"))
    private void getTransformedPos(WorldRenderer p_compare_1_, WorldRenderer p_compare_2_,
        CallbackInfoReturnable<Integer> ci) {
        transformedPos1 = ((IMixinWorld) p_compare_1_.worldObj)
            .transformToLocal(-this.entityPosX, -this.entityPosY, -this.entityPosZ);
        transformedPos2 = ((IMixinWorld) p_compare_2_.worldObj)
            .transformToLocal(-this.entityPosX, -this.entityPosY, -this.entityPosZ);
    }

    @WrapOperation(
        method = "compare(Lnet/minecraft/client/renderer/WorldRenderer;Lnet/minecraft/client/renderer/WorldRenderer;)I",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/renderer/EntitySorter;entityPosX:D",
            ordinal = 0))
    private double redirectEntityPosX1(EntitySorter entitySorter, Operation<Double> original) {
        return transformedPos1.xCoord;
    }

    @WrapOperation(
        method = "compare(Lnet/minecraft/client/renderer/WorldRenderer;Lnet/minecraft/client/renderer/WorldRenderer;)I",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/renderer/EntitySorter;entityPosY:D",
            ordinal = 0))
    private double redirectEntityPosY1(EntitySorter entitySorter, Operation<Double> original) {
        return transformedPos1.yCoord;
    }

    @WrapOperation(
        method = "compare(Lnet/minecraft/client/renderer/WorldRenderer;Lnet/minecraft/client/renderer/WorldRenderer;)I",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/renderer/EntitySorter;entityPosZ:D",
            ordinal = 0))
    private double redirectEntityPosZ1(EntitySorter entitySorter, Operation<Double> original) {
        return transformedPos1.zCoord;
    }

    @WrapOperation(
        method = "compare(Lnet/minecraft/client/renderer/WorldRenderer;Lnet/minecraft/client/renderer/WorldRenderer;)I",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/renderer/EntitySorter;entityPosX:D",
            ordinal = 1))
    private double redirectEntityPosX2(EntitySorter entitySorter, Operation<Double> original) {
        return transformedPos2.xCoord;
    }

    @WrapOperation(
        method = "compare(Lnet/minecraft/client/renderer/WorldRenderer;Lnet/minecraft/client/renderer/WorldRenderer;)I",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/renderer/EntitySorter;entityPosY:D",
            ordinal = 1))
    private double redirectEntityPosY2(EntitySorter entitySorter, Operation<Double> original) {
        return transformedPos2.yCoord;
    }

    @WrapOperation(
        method = "compare(Lnet/minecraft/client/renderer/WorldRenderer;Lnet/minecraft/client/renderer/WorldRenderer;)I",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/renderer/EntitySorter;entityPosZ:D",
            ordinal = 1))
    private double redirectEntityPosZ2(EntitySorter entitySorter, Operation<Double> original) {
        return transformedPos2.zCoord;
    }

}
