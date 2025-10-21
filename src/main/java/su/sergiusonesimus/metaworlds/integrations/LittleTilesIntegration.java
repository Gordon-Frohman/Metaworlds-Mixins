package su.sergiusonesimus.metaworlds.integrations;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.Vec3;

import com.creativemd.creativecore.common.utils.Rotation;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.util.RotationHelper;

public class LittleTilesIntegration {

    public static void registerRotators() {
        RotationHelper.registerTileEntities("littletiles", TileEntityLittleTiles.class);
        RotationHelper.registerTileEntityRotator("littletiles", (te, world) -> {
            TileEntityLittleTiles telt = (TileEntityLittleTiles) te;
            SubWorld subworld = (SubWorld) world;
            int subworldRoll = (int) Math.round(subworld.getRotationRoll() % 360 / 90);
            int subworldYaw = (int) Math.round(subworld.getRotationYaw() % 360 / 90);
            int subworldPitch = (int) Math.round(subworld.getRotationPitch() % 360 / 90);
            Vec3 center = Vec3.createVectorHelper(0.5D, 0.5D, 0.5D);
            List<Rotation> rotationsList = new ArrayList<Rotation>();
            while (subworldRoll != 0) {
                if (subworldRoll > 0) {
                    rotationsList.add(Rotation.DOWNX);
                    subworldRoll--;
                } else {
                    rotationsList.add(Rotation.UPX);
                    subworldRoll++;
                }
            }
            while (subworldYaw != 0) {
                if (subworldYaw > 0) {
                    rotationsList.add(Rotation.NORTH);
                    subworldYaw--;
                } else {
                    rotationsList.add(Rotation.SOUTH);
                    subworldYaw++;
                }
            }
            while (subworldPitch != 0) {
                if (subworldPitch > 0) {
                    rotationsList.add(Rotation.DOWN);
                    subworldPitch--;
                } else {
                    rotationsList.add(Rotation.UP);
                    subworldPitch++;
                }
            }

            for (LittleTile tile : telt.getTiles()) {
                for (LittleTileBox boundingBox : tile.boundingBoxes) {
                    for (Rotation rotation : rotationsList) {
                        boundingBox.rotateBoxWithCenter(rotation, center);
                    }
                }
            }
        });
    }

}
