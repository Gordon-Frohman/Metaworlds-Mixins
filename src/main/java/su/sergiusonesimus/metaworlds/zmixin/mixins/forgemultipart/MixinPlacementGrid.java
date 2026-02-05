package su.sergiusonesimus.metaworlds.zmixin.mixins.forgemultipart;

import net.minecraft.util.Vec3;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.PlacementGrid;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.integrations.ForgeMultipartIntegration;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinMovingObjectPosition;

@Mixin(targets = "codechicken.microblock.PlacementGrid$class")
public class MixinPlacementGrid {

    @Inject(method = "glTransformFace", remap = false, at = @At(value = "HEAD"), cancellable = true)
    private static void glTransformFace(PlacementGrid $this, Vector3 hit, int side, CallbackInfo ci) {
        if (((IMixinMovingObjectPosition) ForgeMultipartIntegration.currentMOP)
            .getWorld() instanceof SubWorld subworld) {
            Vec3 localCenter = Vec3.createVectorHelper(
                ForgeMultipartIntegration.currentMOP.blockX + 0.5D,
                ForgeMultipartIntegration.currentMOP.blockY + 0.5D,
                ForgeMultipartIntegration.currentMOP.blockZ + 0.5D);
            Vec3 globalCenter = subworld.transformToGlobal(localCenter);
            GL11.glPushMatrix();
            GL11.glTranslated(globalCenter.xCoord, globalCenter.yCoord, globalCenter.zCoord);
            GL11.glRotated(subworld.getRotationYaw() % 360D, 0.0D, 1.0D, 0.0D);
            GL11.glRotated(subworld.getRotationRoll() % 360D, 1.0D, 0.0D, 0.0D);
            GL11.glRotated(subworld.getRotationPitch() % 360D, 0.0D, 0.0D, 1.0D);
            Rotation.sideRotations[side].glApply();
            Vector3 rhit = new Vector3(localCenter)
                .subtract(new Vector3(subworld.transformToLocal(hit.x, hit.y, hit.z)))
                .apply(Rotation.sideRotations[side ^ 1].inverse());
            GL11.glTranslated(0.0D, rhit.y - 0.002D, 0.0D);
            ci.cancel();
        }
    }

}
