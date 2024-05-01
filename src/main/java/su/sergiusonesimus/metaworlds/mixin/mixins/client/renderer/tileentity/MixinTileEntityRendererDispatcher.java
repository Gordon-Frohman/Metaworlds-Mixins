package su.sergiusonesimus.metaworlds.mixin.mixins.client.renderer.tileentity;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.api.IMixinTileEntity;
import su.sergiusonesimus.metaworlds.api.IMixinWorld;

@Mixin(TileEntityRendererDispatcher.class)
public abstract class MixinTileEntityRendererDispatcher {
	
	@Shadow(remap = true)
    public static double staticPlayerX;
	
	@Shadow(remap = true)
    public static double staticPlayerY;
	
	@Shadow(remap = true)
    public static double staticPlayerZ;
	
	@Shadow(remap = true)
    public double field_147560_j;
	
	@Shadow(remap = true)
    public double field_147561_k;
	
	@Shadow(remap = true)
    public double field_147558_l;
	
	//TODO

	@Shadow(remap = true)
	public abstract void renderTileEntityAt(TileEntity p_147549_1_, double p_147549_2_, double p_147549_4_, double p_147549_6_, float p_147549_8_);

	@Overwrite
    public void renderTileEntity(TileEntity p_147544_1_, float p_147544_2_)
    {
        if (((IMixinTileEntity)p_147544_1_).getDistanceFromGlobal(this.field_147560_j, this.field_147561_k, this.field_147558_l) < p_147544_1_.getMaxRenderDistanceSquared())
        {
            int i = p_147544_1_.getWorldObj().getLightBrightnessForSkyBlocks(p_147544_1_.xCoord, p_147544_1_.yCoord, p_147544_1_.zCoord, 0);
            int j = i % 65536;
            int k = i / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glPushMatrix();
            if (p_147544_1_.hasWorldObj() && ((IMixinWorld)p_147544_1_.getWorldObj()).isSubWorld())
            {
                GL11.glTranslated(-staticPlayerX, -staticPlayerY, -staticPlayerZ);
                
                SubWorld parentSubWorld = (SubWorld)p_147544_1_.getWorldObj();
                GL11.glMultMatrix(parentSubWorld.getTransformToGlobalMatrixDirectBuffer());
                
                GL11.glTranslated(staticPlayerX, staticPlayerY, staticPlayerZ);
            }
            this.renderTileEntityAt(p_147544_1_, (double)p_147544_1_.xCoord - staticPlayerX, (double)p_147544_1_.yCoord - staticPlayerY, (double)p_147544_1_.zCoord - staticPlayerZ, p_147544_2_);
            
            GL11.glPopMatrix();
        }
    }
}
