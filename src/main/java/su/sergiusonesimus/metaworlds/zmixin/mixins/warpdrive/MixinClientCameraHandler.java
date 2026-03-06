package su.sergiusonesimus.metaworlds.zmixin.mixins.warpdrive;

import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.render.ClientCameraHandler;
import su.sergiusonesimus.metaworlds.integrations.warpdrive.SubworldClientCameraHandler;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(ClientCameraHandler.class)
public class MixinClientCameraHandler {

    @Overwrite(remap = false)
    public static boolean isValidContext(World world) {
        if (SubworldClientCameraHandler.check1_world == null || SubworldClientCameraHandler.check2_world == null
            || world.provider.dimensionId != ClientCameraHandler.dimensionId) {
            return false;
        }
        if (!SubworldClientCameraHandler.check1_world
            .getBlock(ClientCameraHandler.check1_x, ClientCameraHandler.check1_y, ClientCameraHandler.check1_z)
            .isAssociatedBlock(ClientCameraHandler.check1_blockId)) {
            WarpDrive.logger.error(
                "checking viewpoint, found invalid block1 in subworld "
                    + ((IMixinWorld) SubworldClientCameraHandler.check1_world).getSubWorldID()
                    + " at ("
                    + ClientCameraHandler.check1_x
                    + ", "
                    + ClientCameraHandler.check1_y
                    + ", "
                    + ClientCameraHandler.check1_z
                    + ")");
            return false;
        }
        if (!SubworldClientCameraHandler.check2_world
            .getBlock(ClientCameraHandler.check2_x, ClientCameraHandler.check2_y, ClientCameraHandler.check2_z)
            .isAssociatedBlock(ClientCameraHandler.check2_blockId)) {
            WarpDrive.logger.error(
                "checking viewpoint, found invalid block2 in subworld "
                    + ((IMixinWorld) SubworldClientCameraHandler.check2_world).getSubWorldID()
                    + " at ("
                    + ClientCameraHandler.check2_x
                    + ", "
                    + ClientCameraHandler.check2_y
                    + ", "
                    + ClientCameraHandler.check2_z
                    + ")");
            return false;
        }
        return true;
    }

}
