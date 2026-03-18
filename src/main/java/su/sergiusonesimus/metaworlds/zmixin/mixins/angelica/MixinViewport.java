package su.sergiusonesimus.metaworlds.zmixin.mixins.angelica;

import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.embeddedt.embeddium.impl.render.viewport.CameraTransform;
import org.embeddedt.embeddium.impl.render.viewport.Viewport;
import org.embeddedt.embeddium.impl.render.viewport.frustum.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.angelica.IMixinViewport;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinAxisAlignedBB;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(Viewport.class)
public class MixinViewport implements IMixinViewport {

    @Shadow(remap = false)
    private CameraTransform transform;

    @Shadow(remap = false)
    private Frustum frustum;

    private World renderWorld;

    @Override
    public Viewport setWorld(World world) {
        renderWorld = world;
        return (Viewport) (Object) this;
    }

    @Override
    public World getWorld() {
        return renderWorld == null ? Minecraft.getMinecraft().theWorld : renderWorld;
    }

    @Overwrite(remap = false)
    public boolean isBoxVisible(int intOriginX, int intOriginY, int intOriginZ, float floatSizeX, float floatSizeY,
        float floatSizeZ) {
        Vec3 globalOrigin = ((IMixinWorld) getWorld()).transformToGlobal(intOriginX, intOriginY, intOriginZ);
        Vec3 globalCamera = ((IMixinWorld) getWorld())
            .transformToGlobal(this.transform.x, this.transform.y, this.transform.z);
        float floatOriginX = (float) (globalOrigin.xCoord - globalCamera.xCoord);
        float floatOriginY = (float) (globalOrigin.yCoord - globalCamera.yCoord);
        float floatOriginZ = (float) (globalOrigin.zCoord - globalCamera.zCoord);

        return this.frustum.testAab(
            floatOriginX - floatSizeX,
            floatOriginY - floatSizeY,
            floatOriginZ - floatSizeZ,

            floatOriginX + floatSizeX,
            floatOriginY + floatSizeY,
            floatOriginZ + floatSizeZ);
    }

    @Inject(method = "isBoxVisible(DDDDDD)Z", remap = false, at = @At(value = "HEAD"), cancellable = true)
    public void isBoxVisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
        CallbackInfoReturnable<Boolean> cir) {
        World world = getWorld();
        if (world instanceof SubWorld) {
            AxisAlignedBB globalBB = ((IMixinAxisAlignedBB) AxisAlignedBB
                .getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ)).getTransformedToGlobalBoundingBox(world);
            Vec3 globalCamera = ((IMixinWorld) getWorld())
                .transformToGlobal(this.transform.x, this.transform.y, this.transform.z);
            cir.setReturnValue(
                this.frustum.testAab(
                    (float) (globalBB.minX - globalCamera.xCoord),
                    (float) (globalBB.minY - globalCamera.yCoord),
                    (float) (globalBB.minZ - globalCamera.zCoord),
                    (float) (globalBB.maxX - globalCamera.xCoord),
                    (float) (globalBB.maxY - globalCamera.yCoord),
                    (float) (globalBB.maxZ - globalCamera.zCoord)));
            cir.cancel();
        }
    }

}
