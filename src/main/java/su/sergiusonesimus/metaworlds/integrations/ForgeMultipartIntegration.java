package su.sergiusonesimus.metaworlds.integrations;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import codechicken.lib.vec.Cuboid6;
import codechicken.microblock.CommonMicroblock;
import codechicken.microblock.CornerMicroblock;
import codechicken.microblock.EdgeMicroblock;
import codechicken.microblock.Microblock;
import codechicken.multipart.BlockMultipart;
import codechicken.multipart.MultipartHelper;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import codechicken.multipart.minecraft.McSidedMetaPart;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.entity.player.EntityPlayerProxy;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.OrientedBB;
import su.sergiusonesimus.metaworlds.util.RotationHelper;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class ForgeMultipartIntegration {

    public static MovingObjectPosition currentMOP;

    public static int getSubworldSpecificEntityId(EntityPlayer player) {
        return player.getEntityId()
            | (player instanceof EntityPlayerProxy ? ((IMixinWorld) player.worldObj).getSubWorldID() << 16 : 0);
    }

    public static boolean isBlockMultipart(Block block) {
        return block instanceof BlockMultipart;
    }

    public static TileEntity createTileEntityFromNBT(NBTTagCompound nbtTagCompound) {
        return TileMultipart.createFromNBT(nbtTagCompound);
    }

    public static void sendMultipartUpdate(World world, TileEntity tileEntity) {
        if (world.isRemote) return;
        MultipartHelper.sendDescPacket(world, tileEntity);
    }

    public static void registerRotators() {
        RotationHelper.registerTileEntities("multipart", TileMultipart.class);
        RotationHelper.registerTileEntityRotator("multipart", (te, world) -> {
            TileMultipart tem = (TileMultipart) te;
            SubWorld subworld = (SubWorld) world;
            Vec3 directionVec;

            for (TMultiPart part : tem.jPartList()) {
                directionVec = Vec3.createVectorHelper(0, 0, 0);
                if (part instanceof CommonMicroblock microblock) {
                    int slot = microblock.getSlot();
                    if (microblock instanceof CornerMicroblock) {
                        /*
                         * Slots mapping:
                         * 7 = WND
                         * 8 = WNU
                         * 9 = WSD
                         * 10 = WSU
                         * 11 = END
                         * 12 = ENU
                         * 13 = ESD
                         * 14 = ESU
                         */
                        slot -= 7;
                        directionVec.xCoord = ((slot >> 2) & 1) == 1 ? 1 : -1;
                        directionVec.zCoord = ((slot >> 1) & 1) == 1 ? 1 : -1;
                        directionVec.yCoord = (slot & 1) == 1 ? 1 : -1;
                    } else if (microblock instanceof EdgeMicroblock) {
                        /*
                         * Slots mapping:
                         * 15 = WN
                         * 16 = WS
                         * 17 = EN
                         * 18 = ES
                         * 19 = DW
                         * 20 = DE
                         * 21 = UW
                         * 22 = UE
                         * 23 = ND
                         * 24 = NU
                         * 25 = SD
                         * 26 = SU
                         */
                        slot -= 15;
                        int axis1 = ((slot >> 1) & 1) == 1 ? 1 : -1;
                        int axis2 = (slot & 1) == 1 ? 1 : -1;
                        switch (slot >> 2) {
                            case 0:
                                directionVec.xCoord = axis1;
                                directionVec.zCoord = axis2;
                                break;
                            case 1:
                                directionVec.xCoord = axis2;
                                directionVec.yCoord = axis1;
                                break;
                            case 2:
                                directionVec.yCoord = axis2;
                                directionVec.zCoord = axis1;
                                break;
                            default:
                                break;
                        }
                    } else {
                        /*
                         * Slots mapping:
                         * 0 = down
                         * 1 = up
                         * 2 = north
                         * 3 = south
                         * 4 = west
                         * 5 = east
                         */
                        switch (slot) {
                            default:
                                break;
                            case 0:
                                directionVec.yCoord = -1;
                                break;
                            case 1:
                                directionVec.yCoord = 1;
                                break;
                            case 2:
                                directionVec.zCoord = -1;
                                break;
                            case 3:
                                directionVec.zCoord = 1;
                                break;
                            case 4:
                                directionVec.xCoord = -1;
                                break;
                            case 5:
                                directionVec.xCoord = 1;
                                break;
                        }
                    }

                    directionVec.rotateAroundX((float) (-subworld.getRotationRoll() % 360 / 180 * Math.PI));
                    directionVec.rotateAroundY((float) (subworld.getRotationYaw() % 360 / 180 * Math.PI));
                    directionVec.rotateAroundZ((float) (-subworld.getRotationPitch() % 360 / 180 * Math.PI));

                    if (microblock instanceof CornerMicroblock) {
                        slot = ((directionVec.yCoord > 0 ? 1 : 0) | ((directionVec.zCoord > 0 ? 1 : 0) << 1)
                            | ((directionVec.xCoord > 0 ? 1 : 0) << 2)) + 7;
                    } else if (microblock instanceof EdgeMicroblock) {
                        slot = 0;
                        double absX = Math.abs(directionVec.xCoord);
                        double absY = Math.abs(directionVec.yCoord);
                        double absZ = Math.abs(directionVec.zCoord);
                        int bit1;
                        int bit2;
                        if (absZ < absX && absZ < absY) {
                            slot |= 1 << 2;
                            bit1 = directionVec.yCoord > 0 ? 1 : 0;
                            bit2 = directionVec.xCoord > 0 ? 1 : 0;
                        } else if (absX < absY && absX < absZ) {
                            slot |= 2 << 2;
                            bit1 = directionVec.zCoord > 0 ? 1 : 0;
                            bit2 = directionVec.yCoord > 0 ? 1 : 0;
                        } else {
                            bit1 = directionVec.xCoord > 0 ? 1 : 0;
                            bit2 = directionVec.zCoord > 0 ? 1 : 0;
                        }
                        slot += (bit1 << 1 | bit2) + 15;
                    } else {
                        slot = Direction.getNearest(directionVec)
                            .get3DDataValue();
                    }
                    ((Microblock) microblock).setShape(microblock.getSize(), slot);
                } else {
                    if (part instanceof McSidedMetaPart multipart) {
                        Block block = multipart.getBlock();
                        multipart.meta = (byte) RotationHelper.getRotatedMeta(world, block, multipart.getMetadata());
                    }
                }
            }
        });
    }

    public static OrientedBB createOBB(Cuboid6 cuboid) {
        return new OrientedBB(cuboid.min.x, cuboid.min.y, cuboid.min.z, cuboid.max.x, cuboid.max.y, cuboid.max.z);
    }

}
