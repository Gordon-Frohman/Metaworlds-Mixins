package su.sergiusonesimus.metaworlds.zmixin.mixins.littletiles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderHandEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.client.render.PreviewRenderer;
import com.creativemd.littletiles.common.utils.LittleTileBlockPos;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import su.sergiusonesimus.metaworlds.MetaworldsMod;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinMovingObjectPosition;

@Mixin(PreviewRenderer.class)
public class MixinPreviewRenderer {

    private LittleTileBlockPos storedPos;
    private CubeObject storedCube;
    private Vec3 storedSize;

    @Inject(
        method = "tick",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lcom/creativemd/creativecore/client/rendering/RenderHelper3D;renderBlock(DDDDDDDDDDDDD)V",
            shift = Shift.BEFORE),
        locals = LocalCapture.CAPTURE_FAILHARD)
    public void storePos(RenderHandEvent event, CallbackInfo ci, @Local(name = "pos") LittleTileBlockPos pos,
        @Local(name = "cube") CubeObject cube, @Local(name = "size") Vec3 size) {
        storedPos = pos;
        storedCube = cube;
        storedSize = size;
    }

    @WrapOperation(
        method = "tick",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lcom/creativemd/creativecore/client/rendering/RenderHelper3D;renderBlock(DDDDDDDDDDDDD)V"))
    public void renderBlock(double x, double y, double z, double width, double height, double length, double rotateX,
        double rotateY, double rotateZ, double red, double green, double blue, double alpha, Operation<Void> original) {
        World targetWorld = ((IMixinMovingObjectPosition) Minecraft.getMinecraft().objectMouseOver).getWorld();
        if (targetWorld instanceof SubWorld subworld) {
            Vec3 globalPos = subworld.transformToGlobal(
                storedPos.getPosX() + storedCube.minX + storedSize.xCoord / 2D,
                storedPos.getPosY() + storedCube.minY + storedSize.yCoord / 2D,
                storedPos.getPosZ() + storedCube.minZ + storedSize.zCoord / 2D);
            x = globalPos.xCoord - TileEntityRendererDispatcher.staticPlayerX;
            y = globalPos.yCoord - TileEntityRendererDispatcher.staticPlayerY;
            z = globalPos.zCoord - TileEntityRendererDispatcher.staticPlayerZ;
            rotateX = subworld.getRotationRoll() % 360;
            rotateY = subworld.getRotationYaw() % 360;
            rotateZ = subworld.getRotationPitch() % 360;
        }
        original.call(x, y, z, width, height, length, rotateX, rotateY, rotateZ, red, green, blue, alpha);
    }

}
