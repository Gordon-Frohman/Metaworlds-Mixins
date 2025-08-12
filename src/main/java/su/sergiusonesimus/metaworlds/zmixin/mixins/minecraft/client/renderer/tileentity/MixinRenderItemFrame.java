package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.renderer.tileentity;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.RenderItemFrame;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.IIcon;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.renderer.entity.MixinRender;

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

    @Shadow(remap = true)
    private void renderFrameItemAsBlock(EntityItemFrame p_82403_1_) {}

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void func_76986_a(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    @Overwrite
    public void doRender(EntityItemFrame itemFrame, double xOffset, double yOffset, double zOffset, float p_76986_8_,
        float p_76986_9_) {
        GL11.glPushMatrix();
        double d3 = itemFrame.posX - 0.5D;
        double d4 = itemFrame.posY - 0.5D;
        double d5 = itemFrame.posZ - 0.5D;
        int i = itemFrame.field_146063_b + Direction.offsetX[itemFrame.hangingDirection];
        int j = itemFrame.field_146064_c;
        int k = itemFrame.field_146062_d + Direction.offsetZ[itemFrame.hangingDirection];
        GL11.glTranslated(xOffset, yOffset, zOffset);
        GL11.glRotatef((float) ((IMixinWorld) itemFrame.worldObj).getRotationRoll() % 360, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef((float) ((IMixinWorld) itemFrame.worldObj).getRotationYaw() % 360, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef((float) ((IMixinWorld) itemFrame.worldObj).getRotationPitch() % 360, 0.0F, 0.0F, 1.0F);
        GL11.glTranslated((double) i - d3, (double) j - d4, (double) k - d5);

        if (itemFrame.getDisplayedItem() != null && itemFrame.getDisplayedItem()
            .getItem() == Items.filled_map) {
            this.func_147915_b(itemFrame);
        } else {
            this.renderFrameItemAsBlock(itemFrame);
        }

        this.func_82402_b(itemFrame);
        GL11.glPopMatrix();
        this.func_147914_a(
            itemFrame,
            xOffset + (double) ((float) Direction.offsetX[itemFrame.hangingDirection] * 0.3F),
            yOffset - 0.25D,
            zOffset + (double) ((float) Direction.offsetZ[itemFrame.hangingDirection] * 0.3F));
    }

}
