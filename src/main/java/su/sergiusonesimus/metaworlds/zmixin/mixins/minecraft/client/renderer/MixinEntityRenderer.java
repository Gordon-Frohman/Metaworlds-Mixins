package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.renderer;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.util.IMixinAxisAlignedBB;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Shadow(remap = true)
    private Minecraft mc;

    @Shadow(remap = true)
    private Entity pointedEntity;

    @Shadow(remap = true)
    private boolean lightmapUpdateNeeded;

    @Shadow(remap = true)
    public static int anaglyphField;

    @Shadow(remap = true)
    public int debugViewDirection;

    @Shadow(remap = true)
    private double cameraZoom;

    // TODO

    @Shadow(remap = true)
    protected abstract void renderHand(float p_78476_1_, int p_78476_2_);

    @Shadow(remap = true)
    protected abstract void renderRainSnow(float p_78474_1_);

    @Shadow(remap = true)
    public abstract void enableLightmap(double p_78463_1_);

    @Shadow(remap = true)
    public abstract void disableLightmap(double p_78483_1_);

    @Shadow(remap = true)
    protected abstract void renderCloudsCheck(RenderGlobal p_82829_1_, float p_82829_2_);

    @Shadow(remap = true)
    protected abstract void setupFog(int p_78468_1_, float p_78468_2_);

    @Shadow(remap = true)
    protected abstract void updateFogColor(float p_78466_1_);

    @Shadow(remap = true)
    protected abstract void updateLightmap(float p_78472_1_);

    @Shadow(remap = true)
    protected abstract void setupCameraTransform(float p_78479_1_, int p_78479_2_);

    /**
     * Finds what block or object the mouse is over at the specified partial tick time. Args: partialTickTime
     */
    @Overwrite
    public void getMouseOver(float p_78473_1_) {
        if (this.mc.renderViewEntity != null) {
            if (this.mc.theWorld != null) {
                this.mc.pointedEntity = null;
                double d0 = (double) this.mc.playerController.getBlockReachDistance();
                this.mc.objectMouseOver = this.mc.renderViewEntity.rayTrace(d0, p_78473_1_);
                double d1 = d0;
                Vec3 vec3 = this.mc.renderViewEntity.getPosition(p_78473_1_);

                if (this.mc.playerController.extendedReach()) {
                    d0 = 6.0D;
                    d1 = 6.0D;
                } else {
                    if (d0 > 3.0D) {
                        d1 = 3.0D;
                    }

                    d0 = d1;
                }

                if (this.mc.objectMouseOver != null) {
                    d1 = this.mc.objectMouseOver.hitVec.distanceTo(vec3);
                }

                Vec3 vec31 = this.mc.renderViewEntity.getLook(p_78473_1_);
                Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
                this.pointedEntity = null;
                Vec3 vec33 = null;
                float f1 = 1.0F;
                List list = this.mc.theWorld.getEntitiesWithinAABBExcludingEntity(
                    this.mc.renderViewEntity,
                    this.mc.renderViewEntity.boundingBox
                        .addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0)
                        .expand((double) f1, (double) f1, (double) f1));
                double d2 = d1;

                for (int i = 0; i < list.size(); ++i) {
                    Entity entity = (Entity) list.get(i);

                    if (entity.canBeCollidedWith()) {
                        float f2 = entity.getCollisionBorderSize();
                        AxisAlignedBB axisalignedbb = ((IMixinAxisAlignedBB) entity.boundingBox
                            .expand((double) f2, (double) f2, (double) f2))
                                .getTransformedToGlobalBoundingBox(entity.worldObj);
                        MovingObjectPosition movingobjectposition = ((IMixinAxisAlignedBB) axisalignedbb)
                            .calculateIntercept(vec3, vec32, entity.worldObj);

                        if (axisalignedbb.isVecInside(vec3)) {
                            if (0.0D < d2 || d2 == 0.0D) {
                                this.pointedEntity = entity;
                                vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                                d2 = 0.0D;
                            }
                        } else if (movingobjectposition != null) {
                            double d3 = vec3.distanceTo(movingobjectposition.hitVec);

                            if (d3 < d2 || d2 == 0.0D) {
                                if (entity == this.mc.renderViewEntity.ridingEntity && !entity.canRiderInteract()) {
                                    if (d2 == 0.0D) {
                                        this.pointedEntity = entity;
                                        vec33 = movingobjectposition.hitVec;
                                    }
                                } else {
                                    this.pointedEntity = entity;
                                    vec33 = movingobjectposition.hitVec;
                                    d2 = d3;
                                }
                            }
                        }
                    }
                }

                if (this.pointedEntity != null && (d2 < d1 || this.mc.objectMouseOver == null)) {
                    this.mc.objectMouseOver = new MovingObjectPosition(this.pointedEntity, vec33);

                    if (this.pointedEntity instanceof EntityLivingBase
                        || this.pointedEntity instanceof EntityItemFrame) {
                        this.mc.pointedEntity = this.pointedEntity;
                    }
                }
            }
        }
    }

}
