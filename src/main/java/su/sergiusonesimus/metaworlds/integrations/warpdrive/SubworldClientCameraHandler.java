package su.sergiusonesimus.metaworlds.integrations.warpdrive;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.lwjgl.input.Keyboard;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.EnumCameraType;
import cr0s.warpdrive.render.ClientCameraHandler;
import cr0s.warpdrive.render.EntityCamera;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class SubworldClientCameraHandler {

    public static void setupViewpoint(EnumCameraType enumCameraType, EntityPlayer entityPlayer, float initialYaw,
        float initialPitch, int monitor_x, int monitor_y, int monitor_z, Block blockMonitor, int camera_x, int camera_y,
        int camera_z, Block blockCamera, World cameraWorld) {
        final Minecraft mc = Minecraft.getMinecraft();

        if (entityPlayer == null) {
            WarpDrive.logger.error("setupViewpoint with null player => denied");
            return;
        }

        // Save initial state
        ClientCameraHandler.originalFOV = mc.gameSettings.fovSetting;
        ClientCameraHandler.originalSensitivity = mc.gameSettings.mouseSensitivity;
        ClientCameraHandler.overlayType = enumCameraType;
        ClientCameraHandler.entityPlayer = entityPlayer;
        ClientCameraHandler.dimensionId = ClientCameraHandler.entityPlayer.worldObj.provider.dimensionId;
        ClientCameraHandler.check1_x = monitor_x;
        ClientCameraHandler.check1_y = monitor_y;
        ClientCameraHandler.check1_z = monitor_z;
        ClientCameraHandler.check1_blockId = blockMonitor;
        ClientCameraHandler.check2_x = camera_x;
        ClientCameraHandler.check2_y = camera_y;
        ClientCameraHandler.check2_z = camera_z;
        ClientCameraHandler.check2_blockId = blockCamera;

        // Spawn camera entity
        Vec3 globalPos = ((IMixinWorld) cameraWorld)
            .transformToGlobal(camera_x + 0.5D, camera_y + 0.5D, camera_z + 0.5D);
        final EntityCamera entityCamera = new EntityCamera(
            cameraWorld,
            MathHelper.floor_double(globalPos.xCoord),
            MathHelper.floor_double(globalPos.yCoord),
            MathHelper.floor_double(globalPos.zCoord),
            ClientCameraHandler.entityPlayer);
        cameraWorld.spawnEntityInWorld(entityCamera);
        entityCamera
            .setLocationAndAngles(globalPos.xCoord, globalPos.yCoord, globalPos.zCoord, initialYaw, initialPitch);

        // Update view
        if (WarpDriveConfig.LOGGING_CAMERA) {
            WarpDrive.logger.info("Setting viewpoint to " + entityCamera);
        }
        mc.renderViewEntity = entityCamera;
        mc.gameSettings.thirdPersonView = 0;
        Method refreshViewPoint;
		try {
			refreshViewPoint = ClientCameraHandler.class.getDeclaredMethod("refreshViewPoint");
	        refreshViewPoint.setAccessible(true);
	        refreshViewPoint.invoke(null);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
        ClientCameraHandler.isOverlayEnabled = true;

        Keyboard.enableRepeatEvents(true);
    }

}
