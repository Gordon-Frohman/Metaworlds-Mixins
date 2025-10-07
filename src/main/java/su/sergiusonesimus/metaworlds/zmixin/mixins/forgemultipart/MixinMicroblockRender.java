package su.sergiusonesimus.metaworlds.zmixin.mixins.forgemultipart;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.CommonMicroClass;
import codechicken.microblock.ExecutablePlacement;
import codechicken.microblock.MicroblockClient;
import codechicken.microblock.MicroblockPlacement$;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.integrations.ForgeMultipartIntegration;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.AxisDirection;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinMovingObjectPosition;

@Mixin(codechicken.microblock.MicroblockRender$.class)
public class MixinMicroblockRender {

    // @Inject(method = "renderHighlight", remap = false, at = { @At(value = "HEAD") })
    // public void storeMOP(final EntityPlayer player, final MovingObjectPosition hit, final CommonMicroClass mcrClass,
    // final int size, final int material, CallbackInfo ci) {
    // ForgeMultipartIntegration.currentMOP = hit;
    // }

    @Overwrite(remap = false)
    public void renderHighlight(final EntityPlayer player, final MovingObjectPosition hit,
        final CommonMicroClass mcrClass, final int size, final int material) {
        ForgeMultipartIntegration.currentMOP = hit;
        mcrClass.placementProperties()
            .placementGrid()
            .render(new Vector3(hit.hitVec), hit.sideHit);
        final ExecutablePlacement placement = MicroblockPlacement$.MODULE$
            .apply(player, hit, size, material, !player.capabilities.isCreativeMode, mcrClass.placementProperties());
        if (placement == null) {
            return;
        }
        final BlockCoord pos = placement.pos();
        final MicroblockClient part = (MicroblockClient) placement.part();
        final CCRenderState state = CCRenderState.instance();
        GL11.glPushMatrix();
        if (((IMixinMovingObjectPosition) hit).getWorld() instanceof SubWorld subworld) {
            Vec3 globalCenter = subworld.transformToGlobal(hit.blockX + 0.5, hit.blockY + 0.5, hit.blockZ + 0.5);
            Vec3 dVec = Vec3.createVectorHelper(-0.5D, -0.5D, -0.5D);
            Direction dir = Direction.from3DDataValue(hit.sideHit);
            double d = dir.getAxisDirection() == AxisDirection.POSITIVE ? 1.0D : -1.0D;
            switch (dir.getAxis()) {
                case X:
                    dVec.xCoord += d;
                    break;
                case Y:
                    dVec.yCoord += d;
                    break;
                case Z:
                    dVec.zCoord += d;
                    break;
            }
            GL11.glTranslated(globalCenter.xCoord, globalCenter.yCoord, globalCenter.zCoord);
            GL11.glRotated(subworld.getRotationYaw() % 360D, 0.0D, 1.0D, 0.0D);
            GL11.glRotated(subworld.getRotationRoll() % 360D, 1.0D, 0.0D, 0.0D);
            GL11.glRotated(subworld.getRotationPitch() % 360D, 0.0D, 0.0D, 1.0D);
            GL11.glScaled(1.002, 1.002, 1.002);
            GL11.glTranslated(dVec.xCoord, dVec.yCoord, dVec.zCoord);
        } else {
            GL11.glTranslated(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5);
            GL11.glScaled(1.002, 1.002, 1.002);
            GL11.glTranslated(-0.5, -0.5, -0.5);
        }
        GL11.glEnable(3042);
        GL11.glDepthMask(false);
        GL11.glBlendFunc(770, 771);
        TextureUtils.bindAtlas(0);
        state.resetInstance();
        state.alphaOverride = 80;
        state.useNormals = true;
        state.startDrawingInstance();
        part.render(Vector3.zero, -1);
        state.drawInstance();
        GL11.glDisable(3042);
        GL11.glDepthMask(true);
        GL11.glPopMatrix();
    }

}
