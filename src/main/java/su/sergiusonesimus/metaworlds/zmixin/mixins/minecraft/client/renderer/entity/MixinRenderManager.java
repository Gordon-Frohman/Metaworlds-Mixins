package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(RenderManager.class)
public class MixinRenderManager {

    // TODO

    @Shadow(remap = true)
    public boolean func_147939_a(Entity p_147939_1_, double p_147939_2_, double p_147939_4_, double p_147939_6_,
        float p_147939_8_, float p_147939_9_, boolean p_147939_10_) {
        return false;
    }

    private Vec3 lastTickPos;
    private Vec3 pos;

    @Inject(method = "renderEntityStatic", at = { @At("HEAD") })
    public void injectRenderEntityStatic(Entity entity, float p_147936_2_, boolean p_147936_3_,
        CallbackInfoReturnable<Boolean> ci) {
        World world = entity.worldObj;
        lastTickPos = ((IMixinWorld) world)
            .transformToGlobal(Vec3.createVectorHelper(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ));
        pos = ((IMixinWorld) world).transformToGlobal(Vec3.createVectorHelper(entity.posX, entity.posY, entity.posZ));
    }

    @ModifyVariable(method = "renderEntityStatic", at = @At("STORE"), ordinal = 0)
    private double modifyD0(double d0, Entity entity, float p_147936_2_, boolean p_147936_3_) {
        return lastTickPos.xCoord + (pos.xCoord - lastTickPos.xCoord) * (double) p_147936_2_;
    }

    @ModifyVariable(method = "renderEntityStatic", at = @At("STORE"), ordinal = 1)
    private double modifyD1(double d1, Entity entity, float p_147936_2_, boolean p_147936_3_) {
        return lastTickPos.yCoord + (pos.yCoord - lastTickPos.yCoord) * (double) p_147936_2_;
    }

    @ModifyVariable(method = "renderEntityStatic", at = @At("STORE"), ordinal = 2)
    private double modifyD2(double d2, Entity entity, float p_147936_2_, boolean p_147936_3_) {
        return lastTickPos.zCoord + (pos.zCoord - lastTickPos.zCoord) * (double) p_147936_2_;
    }

}
