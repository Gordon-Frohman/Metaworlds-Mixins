package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.renderer;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.client.renderer.IMixinWorldRenderer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.util.IMixinAxisAlignedBB;

@Mixin(value = WorldRenderer.class, priority = 800)
public abstract class MixinWorldRenderer implements IMixinWorldRenderer {

    @Shadow(remap = true)
    public int posX;

    @Shadow(remap = true)
    public int posY;

    @Shadow(remap = true)
    public int posZ;

    @Shadow(remap = true)
    public int posXMinus;

    @Shadow(remap = true)
    public int posYMinus;

    @Shadow(remap = true)
    public int posZMinus;

    @Shadow(remap = true)
    public int posXClip;

    @Shadow(remap = true)
    public int posYClip;

    @Shadow(remap = true)
    public int posZClip;

    @Shadow(remap = true)
    public boolean isInFrustum;

    @Shadow(remap = true)
    public boolean[] skipRenderPass = new boolean[2];

    @Shadow(remap = true)
    public int posXPlus;

    @Shadow(remap = true)
    public int posYPlus;

    @Shadow(remap = true)
    public int posZPlus;

    @Shadow(remap = true)
    public AxisAlignedBB rendererBoundingBox;

    @Shadow(remap = true)
    private int glRenderList;

    @Shadow(remap = true)
    public World worldObj;

    @Shadow(remap = true)
    private boolean isInitialized;

    // TODO

    @Shadow(remap = true)
    public abstract void setDontDraw();

    @Shadow(remap = true)
    public abstract void markDirty();

    /**
     * Sets a new position for the renderer and setting it up so it can be reloaded with the new data for that position
     */
    @Overwrite
    public void setPosition(int p_78913_1_, int p_78913_2_, int p_78913_3_) {
        if (p_78913_1_ != this.posX || p_78913_2_ != this.posY || p_78913_3_ != this.posZ) {
            this.setDontDraw();
            this.posX = p_78913_1_;
            this.posY = p_78913_2_;
            this.posZ = p_78913_3_;
            this.posXPlus = p_78913_1_ + 8;
            this.posYPlus = p_78913_2_ + 8;
            this.posZPlus = p_78913_3_ + 8;
            this.posXClip = p_78913_1_ & 1023;
            this.posYClip = p_78913_2_;
            this.posZClip = p_78913_3_ & 1023;
            this.posXMinus = p_78913_1_ - this.posXClip;
            this.posYMinus = p_78913_2_ - this.posYClip;
            this.posZMinus = p_78913_3_ - this.posZClip;
            float f = 6.0F;
            this.rendererBoundingBox = AxisAlignedBB.getBoundingBox(
                (double) ((float) p_78913_1_ - f),
                (double) ((float) p_78913_2_ - f),
                (double) ((float) p_78913_3_ - f),
                (double) ((float) (p_78913_1_ + 16) + f),
                (double) ((float) (p_78913_2_ + 16) + f),
                (double) ((float) (p_78913_3_ + 16) + f));
            GL11.glNewList(this.glRenderList + 2, GL11.GL_COMPILE);
            RenderItem.renderAABB(
                AxisAlignedBB.getBoundingBox(
                    (double) ((float) this.posXClip - f),
                    (double) ((float) this.posYClip - f),
                    (double) ((float) this.posZClip - f),
                    (double) ((float) (this.posXClip + 16) + f),
                    (double) ((float) (this.posYClip + 16) + f),
                    (double) ((float) (this.posZClip + 16) + f)));
            GL11.glEndList();
            this.markDirty();
        }
    }

    /**
     * Returns the distance of this chunk renderer to the entity without performing the final normalizing square root,
     * for performance reasons.
     */
    @Overwrite
    public float distanceToEntitySquared(Entity par1Entity) {
        return (float) ((IMixinEntity) par1Entity).getLocalPos(this.worldObj)
            .squareDistanceTo((double) this.posXPlus, (double) this.posYPlus, (double) this.posZPlus);
        /*
         * float f = (float)(par1Entity.posX - (double)this.posXPlus);
         * float f1 = (float)(par1Entity.posY - (double)this.posYPlus);
         * float f2 = (float)(par1Entity.posZ - (double)this.posZPlus);
         * return f * f + f1 * f1 + f2 * f2;
         */
    }

    @Overwrite
    public void updateInFrustum(ICamera p_78908_1_) {
        this.isInFrustum = p_78908_1_.isBoundingBoxInFrustum(
            ((IMixinAxisAlignedBB) this.rendererBoundingBox).getTransformedToGlobalBoundingBox(this.worldObj));
    }

    public boolean isInitialized() {
        return this.isInitialized;
    }

}
