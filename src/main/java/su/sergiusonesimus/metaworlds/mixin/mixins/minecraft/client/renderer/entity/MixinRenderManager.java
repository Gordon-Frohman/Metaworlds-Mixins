package su.sergiusonesimus.metaworlds.mixin.mixins.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import su.sergiusonesimus.metaworlds.mixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(RenderManager.class)
public class MixinRenderManager {

    // TODO

    @Shadow(remap = true)
    public boolean func_147939_a(Entity p_147939_1_, double p_147939_2_, double p_147939_4_, double p_147939_6_,
        float p_147939_8_, float p_147939_9_, boolean p_147939_10_) {
        return false;
    }

    @Overwrite
    public boolean renderEntityStatic(Entity entity, float p_147936_2_, boolean p_147936_3_) {
        if (entity.ticksExisted == 0) {
            entity.lastTickPosX = entity.posX;
            entity.lastTickPosY = entity.posY;
            entity.lastTickPosZ = entity.posZ;
        }

        World world = entity.worldObj;
        Vec3 lastTickPos = ((IMixinWorld) world)
            .transformToGlobal(Vec3.createVectorHelper(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ));
        Vec3 pos = ((IMixinWorld) world)
            .transformToGlobal(Vec3.createVectorHelper(entity.posX, entity.posY, entity.posZ));

        double d0 = lastTickPos.xCoord + (pos.xCoord - lastTickPos.xCoord) * (double) p_147936_2_;
        double d1 = lastTickPos.yCoord + (pos.yCoord - lastTickPos.yCoord) * (double) p_147936_2_;
        double d2 = lastTickPos.zCoord + (pos.zCoord - lastTickPos.zCoord) * (double) p_147936_2_;
        float f1 = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * p_147936_2_
            + (float) ((IMixinWorld) entity.worldObj).getRotationYaw() % 360;
        int i = entity.getBrightnessForRender(p_147936_2_);

        if (entity.isBurning()) {
            i = 15728880;
        }

        int j = i % 65536;
        int k = i / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j / 1.0F, (float) k / 1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        return this.func_147939_a(
            entity,
            d0 - RenderManager.renderPosX,
            d1 - RenderManager.renderPosY,
            d2 - RenderManager.renderPosZ,
            f1,
            p_147936_2_,
            p_147936_3_);
    }

}
