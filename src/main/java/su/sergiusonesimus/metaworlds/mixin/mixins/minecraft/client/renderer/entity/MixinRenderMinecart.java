package su.sergiusonesimus.metaworlds.mixin.mixins.minecraft.client.renderer.entity;

import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderMinecart;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import su.sergiusonesimus.metaworlds.mixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(RenderMinecart.class)
public class MixinRenderMinecart extends MixinRender {

    @Shadow(remap = true)
    protected ModelBase modelMinecart;

    // TODO

    @Shadow(remap = true)
    protected void func_147910_a(EntityMinecart p_147910_1_, float p_147910_2_, Block p_147910_3_, int p_147910_4_) {}

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void func_76986_a(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    @Overwrite
    public void doRender(EntityMinecart entity, double x, double y, double z, float rotation, float p_76986_9_) {
        GL11.glPushMatrix();
        this.bindEntityTexture(entity);
        long i = (long) entity.getEntityId() * 493286711L;
        i = i * i * 4392167121L + i * 98761L;
        float f2 = (((float) (i >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float f3 = (((float) (i >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float f4 = (((float) (i >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        GL11.glTranslatef(f2, f3, f4);
        double d3 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) p_76986_9_;
        double d4 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) p_76986_9_;
        double d5 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) p_76986_9_;
        double d6 = 0.30000001192092896D;
        Vec3 vec3 = entity.func_70489_a(d3, d4, d5);
        float f5 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * p_76986_9_;

        if (vec3 != null) {
            Vec3 vec31 = entity.func_70495_a(d3, d4, d5, d6);
            Vec3 vec32 = entity.func_70495_a(d3, d4, d5, -d6);

            if (vec31 == null) {
                vec31 = vec3;
            }

            if (vec32 == null) {
                vec32 = vec3;
            }

            x += vec3.xCoord - d3;
            y += (vec31.yCoord + vec32.yCoord) / 2.0D - d4;
            z += vec3.zCoord - d5;
            Vec3 vec33 = vec32.addVector(-vec31.xCoord, -vec31.yCoord, -vec31.zCoord);

            if (vec33.lengthVector() != 0.0D) {
                vec33 = vec33.normalize();
                rotation = (float) (Math.atan2(vec33.zCoord, vec33.xCoord) * 180.0D / Math.PI)
                    - (float) ((IMixinWorld) entity.worldObj).getRotationYaw() % 360;
                f5 = (float) (Math.atan(vec33.yCoord) * 73.0D);
            }
        }

        GL11.glTranslatef((float) x, (float) y, (float) z);
        GL11.glRotatef(180.0F - rotation, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-f5, 0.0F, 0.0F, 1.0F);
        float f7 = (float) entity.getRollingAmplitude() - p_76986_9_;
        float f8 = entity.getDamage() - p_76986_9_;

        if (f8 < 0.0F) {
            f8 = 0.0F;
        }

        if (f7 > 0.0F) {
            GL11.glRotatef(
                MathHelper.sin(f7) * f7 * f8 / 10.0F * (float) entity.getRollingDirection(),
                1.0F,
                0.0F,
                0.0F);
        }

        int k = entity.getDisplayTileOffset();
        Block block = entity.func_145820_n();
        int j = entity.getDisplayTileData();

        if (block.getRenderType() != -1) {
            GL11.glPushMatrix();
            this.bindTexture(TextureMap.locationBlocksTexture);
            float f6 = 0.75F;
            GL11.glScalef(f6, f6, f6);
            GL11.glTranslatef(0.0F, (float) k / 16.0F, 0.0F);
            this.func_147910_a(entity, p_76986_9_, block, j);
            GL11.glPopMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.bindEntityTexture(entity);
        }

        GL11.glScalef(-1.0F, -1.0F, 1.0F);
        this.modelMinecart.render(entity, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        GL11.glPopMatrix();
    }

}
