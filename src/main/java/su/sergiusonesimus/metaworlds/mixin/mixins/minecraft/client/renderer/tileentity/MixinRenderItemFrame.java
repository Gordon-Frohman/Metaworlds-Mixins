package su.sergiusonesimus.metaworlds.mixin.mixins.minecraft.client.renderer.tileentity;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.RenderItemFrame;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.IIcon;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import su.sergiusonesimus.metaworlds.mixin.mixins.minecraft.client.renderer.entity.MixinRender;

@Mixin(RenderItemFrame.class)
public class MixinRenderItemFrame extends MixinRender {

    @Shadow(remap = true)
    private RenderBlocks field_147916_f;

    @Shadow(remap = true)
    private IIcon field_94147_f;

    // TODO

    @Shadow(remap = true)
    protected void func_147914_a(EntityItemFrame p_147914_1_, double p_147914_2_, double p_147914_4_,
        double p_147914_6_) {}

    @Shadow(remap = true)
    private void func_82402_b(EntityItemFrame p_82402_1_) {}

    @Shadow(remap = true)
    private void func_147915_b(EntityItemFrame p_147915_1_) {}

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void func_76986_a(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    @Overwrite
    public void doRender(EntityItemFrame entity, double x, double y, double z, float rotation, float p_76986_9_) {
        GL11.glPushMatrix();
        double d3 = entity.posX - x - 0.5D;
        double d4 = entity.posY - y - 0.5D;
        double d5 = entity.posZ - z - 0.5D;
        int i = entity.field_146063_b + Direction.offsetX[entity.hangingDirection];
        int j = entity.field_146064_c;
        int k = entity.field_146062_d + Direction.offsetZ[entity.hangingDirection];
        GL11.glTranslated((double) i - d3, (double) j - d4, (double) k - d5);

        if (entity.getDisplayedItem() != null && entity.getDisplayedItem()
            .getItem() == Items.filled_map) {
            this.func_147915_b(entity);
        } else {
            this.renderFrameItemAsBlock(entity);
        }

        this.func_82402_b(entity);
        GL11.glPopMatrix();
        this.func_147914_a(
            entity,
            x + (double) ((float) Direction.offsetX[entity.hangingDirection] * 0.3F),
            y - 0.25D,
            z + (double) ((float) Direction.offsetZ[entity.hangingDirection] * 0.3F));
    }

    /**
     * Render the item frame's item as a block.
     */
    @Overwrite
    private void renderFrameItemAsBlock(EntityItemFrame entity) {
        GL11.glPushMatrix();
        GL11.glRotatef(entity.rotationYaw, 0.0F, 1.0F, 0.0F);
        this.renderManager.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        Block block = Blocks.planks;
        float f = 0.0625F;
        float f1 = 0.75F;
        float f2 = f1 / 2.0F;
        GL11.glPushMatrix();
        this.field_147916_f.overrideBlockBounds(
            0.0D,
            (double) (0.5F - f2 + 0.0625F),
            (double) (0.5F - f2 + 0.0625F),
            (double) (f * 0.5F),
            (double) (0.5F + f2 - 0.0625F),
            (double) (0.5F + f2 - 0.0625F));
        this.field_147916_f.setOverrideBlockTexture(this.field_94147_f);
        this.field_147916_f.renderBlockAsItem(block, 0, 1.0F);
        this.field_147916_f.clearOverrideBlockTexture();
        this.field_147916_f.unlockBlockBounds();
        GL11.glPopMatrix();
        this.field_147916_f.setOverrideBlockTexture(Blocks.planks.getIcon(1, 2));
        GL11.glPushMatrix();
        this.field_147916_f.overrideBlockBounds(
            0.0D,
            (double) (0.5F - f2),
            (double) (0.5F - f2),
            (double) (f + 1.0E-4F),
            (double) (f + 0.5F - f2),
            (double) (0.5F + f2));
        this.field_147916_f.renderBlockAsItem(block, 0, 1.0F);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        this.field_147916_f.overrideBlockBounds(
            0.0D,
            (double) (0.5F + f2 - f),
            (double) (0.5F - f2),
            (double) (f + 1.0E-4F),
            (double) (0.5F + f2),
            (double) (0.5F + f2));
        this.field_147916_f.renderBlockAsItem(block, 0, 1.0F);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        this.field_147916_f.overrideBlockBounds(
            0.0D,
            (double) (0.5F - f2),
            (double) (0.5F - f2),
            (double) f,
            (double) (0.5F + f2),
            (double) (f + 0.5F - f2));
        this.field_147916_f.renderBlockAsItem(block, 0, 1.0F);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        this.field_147916_f.overrideBlockBounds(
            0.0D,
            (double) (0.5F - f2),
            (double) (0.5F + f2 - f),
            (double) f,
            (double) (0.5F + f2),
            (double) (0.5F + f2));
        this.field_147916_f.renderBlockAsItem(block, 0, 1.0F);
        GL11.glPopMatrix();
        this.field_147916_f.unlockBlockBounds();
        this.field_147916_f.clearOverrideBlockTexture();
        GL11.glPopMatrix();
    }

}
