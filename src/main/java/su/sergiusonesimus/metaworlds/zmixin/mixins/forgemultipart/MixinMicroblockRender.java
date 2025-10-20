package su.sergiusonesimus.metaworlds.zmixin.mixins.forgemultipart;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import codechicken.lib.raytracer.ExtendedMOP;
import codechicken.microblock.CommonMicroClass;
import codechicken.multipart.BlockMultipart;
import codechicken.multipart.TileMultipart;
import scala.Tuple2;
import scala.runtime.BoxesRunTime;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.integrations.ForgeMultipartIntegration;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.AxisDirection;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinMovingObjectPosition;

@Mixin(codechicken.microblock.MicroblockRender$.class)
public class MixinMicroblockRender {

    Vec3 dVec;

    @Inject(method = "renderHighlight", remap = false, at = @At(value = "HEAD"))
    public void storeMOP(EntityPlayer player, MovingObjectPosition hit, CommonMicroClass mcrClass, int size,
        int material, CallbackInfo ci) {
        ForgeMultipartIntegration.currentMOP = hit;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @WrapOperation(
        method = "renderHighlight",
        remap = false,
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glTranslated(DDD)V", ordinal = 0))
    public void glTranslated1(double x, double y, double z, Operation<Void> original) {
        World world = ((IMixinMovingObjectPosition) ForgeMultipartIntegration.currentMOP).getWorld();
        if (world instanceof SubWorld subworld) {
            Vec3 globalCenter = subworld.transformToGlobal(
                ForgeMultipartIntegration.currentMOP.blockX + 0.5,
                ForgeMultipartIntegration.currentMOP.blockY + 0.5,
                ForgeMultipartIntegration.currentMOP.blockZ + 0.5);
            dVec = Vec3.createVectorHelper(-0.5D, -0.5D, -0.5D);
            boolean offsetRender = true;

            Block block = world.getBlock(
                ForgeMultipartIntegration.currentMOP.blockX,
                ForgeMultipartIntegration.currentMOP.blockY,
                ForgeMultipartIntegration.currentMOP.blockZ);
            if (block instanceof BlockMultipart multipart) {
                TileMultipart tile = BlockMultipart.getTile(
                    world,
                    ForgeMultipartIntegration.currentMOP.blockX,
                    ForgeMultipartIntegration.currentMOP.blockY,
                    ForgeMultipartIntegration.currentMOP.blockZ);
                if (tile != null) {
                    Tuple2<Object, ExtendedMOP> reduceMOP = BlockMultipart
                        .reduceMOP(ForgeMultipartIntegration.currentMOP);
                    if (reduceMOP != null) {
                        int index = reduceMOP._1$mcI$sp();
                        ExtendedMOP mop = (ExtendedMOP) reduceMOP._2();
                        Tuple2 tuple2 = new Tuple2((Object) BoxesRunTime.boxToInteger(index), (Object) mop);
                        int index2 = tuple2._1$mcI$sp();
                        Vec3 hitHec = ForgeMultipartIntegration.currentMOP.hitVec;
                        if (tile.partList()
                            .apply(index2) != null && !isInteger(hitHec.xCoord, 0.001)
                            && !isInteger(hitHec.yCoord, 0.001)
                            && !isInteger(hitHec.zCoord, 0.001)) {
                            offsetRender = false;
                        }
                    }
                }
            }

            if (offsetRender) {
                Direction dir = Direction.from3DDataValue(ForgeMultipartIntegration.currentMOP.sideHit);
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
            }
            GL11.glTranslated(globalCenter.xCoord, globalCenter.yCoord, globalCenter.zCoord);
            GL11.glRotated(subworld.getRotationYaw() % 360D, 0.0D, 1.0D, 0.0D);
            GL11.glRotated(subworld.getRotationRoll() % 360D, 1.0D, 0.0D, 0.0D);
            GL11.glRotated(subworld.getRotationPitch() % 360D, 0.0D, 0.0D, 1.0D);
        } else {
            original.call(x, y, z);
        }
    }

    private boolean isInteger(double value, double epsilon) {
        return Math.abs(value - Math.round(value)) < epsilon;
    }

    @WrapOperation(
        method = "renderHighlight",
        remap = false,
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glTranslated(DDD)V", ordinal = 1))
    public void glTranslated2(double x, double y, double z, Operation<Void> original) {
        if (((IMixinMovingObjectPosition) ForgeMultipartIntegration.currentMOP)
            .getWorld() instanceof SubWorld subworld) {
            original.call(dVec.xCoord, dVec.yCoord, dVec.zCoord);
        } else {
            original.call(x, y, z);
        }
    }

}
