package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderPainting;
import net.minecraft.entity.item.EntityPainting;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(RenderPainting.class)
public class MixinRenderPainting extends MixinRender {

    // TODO

    @Shadow(remap = true)
    private void func_77010_a(EntityPainting p_77010_1_, int p_77010_2_, int p_77010_3_, int p_77010_4_,
        int p_77010_5_) {}

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void func_76986_a(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    @Overwrite
    public void doRender(EntityPainting entity, double x, double y, double z, float rotationYaw, float p_76986_9_) {
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glRotatef(rotationYaw, 0.0F, 1.0F, 0.0F);
        float xAngle = 0;
        float zAngle = 0;
        switch (entity.hangingDirection) {
            default:
                break;
            case 0:
                xAngle = -(float) ((IMixinWorld) entity.worldObj).getRotationRoll() % 360;
                zAngle = -(float) ((IMixinWorld) entity.worldObj).getRotationPitch() % 360;
                break;
            case 1:
                xAngle = (float) ((IMixinWorld) entity.worldObj).getRotationPitch() % 360;
                zAngle = (float) ((IMixinWorld) entity.worldObj).getRotationRoll() % 360;
                break;
            case 2:
                xAngle = (float) ((IMixinWorld) entity.worldObj).getRotationRoll() % 360;
                zAngle = (float) ((IMixinWorld) entity.worldObj).getRotationPitch() % 360;
                break;
            case 3:
                xAngle = -(float) ((IMixinWorld) entity.worldObj).getRotationPitch() % 360;
                zAngle = -(float) ((IMixinWorld) entity.worldObj).getRotationRoll() % 360;
                break;
        }
        GL11.glRotatef(xAngle, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(zAngle, 0.0F, 0.0F, 1.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        this.bindEntityTexture(entity);
        EntityPainting.EnumArt enumart = entity.art;
        float f2 = 0.0625F;
        GL11.glScalef(f2, f2, f2);
        this.func_77010_a(entity, enumart.sizeX, enumart.sizeY, enumart.offsetX, enumart.offsetY);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }

}
