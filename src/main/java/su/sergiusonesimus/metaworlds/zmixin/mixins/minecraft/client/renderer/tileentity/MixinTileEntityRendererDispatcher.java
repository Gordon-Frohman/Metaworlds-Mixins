package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.renderer.tileentity;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.tileentity.IMixinTileEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

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

    // TODO

    @Shadow(remap = true)
    public abstract void renderTileEntityAt(TileEntity p_147549_1_, double p_147549_2_, double p_147549_4_,
        double p_147549_6_, float p_147549_8_);

    // renderTileEntity

    @WrapOperation(
        method = "renderTileEntity(Lnet/minecraft/tileentity/TileEntity;F)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;getDistanceFrom(DDD)D"))
    private double redirectGetDistanceFromGlobal(TileEntity instance, double x, double y, double z,
        Operation<Double> original) {
        return ((IMixinTileEntity) instance).getDistanceFromGlobal(x, y, z);
    }

    @Inject(
        method = "renderTileEntity(Lnet/minecraft/tileentity/TileEntity;F)V",
        at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V",
            remap = false,
            shift = Shift.AFTER))
    private void injectRenderTileEntity1(TileEntity p_147544_1_, float p_147544_2_, CallbackInfo ci) {
        GL11.glPushMatrix();
        if (p_147544_1_.hasWorldObj() && ((IMixinWorld) p_147544_1_.getWorldObj()).isSubWorld()) {
            GL11.glTranslated(-staticPlayerX, -staticPlayerY, -staticPlayerZ);

            SubWorld parentSubWorld = (SubWorld) p_147544_1_.getWorldObj();
            GL11.glMultMatrix(parentSubWorld.getTransformToGlobalMatrixDirectBuffer());

            GL11.glTranslated(staticPlayerX, staticPlayerY, staticPlayerZ);
        }
    }

    @Inject(
        method = "renderTileEntity(Lnet/minecraft/tileentity/TileEntity;F)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;renderTileEntityAt(Lnet/minecraft/tileentity/TileEntity;DDDF)V",
            shift = Shift.AFTER))
    private void injectRenderTileEntity2(TileEntity p_147544_1_, float p_147544_2_, CallbackInfo ci) {
        GL11.glPopMatrix();
    }
}
