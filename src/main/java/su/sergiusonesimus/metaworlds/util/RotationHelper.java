package su.sergiusonesimus.metaworlds.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;

public class RotationHelper {

    private static Map<Block, String> blockTypes = new HashMap<Block, String>();
    private static Map<String, MetaRotator> metaRotators = new HashMap<String, MetaRotator>();
    private static Map<Class<? extends TileEntity>, String> tileEntityTypes = new HashMap<Class<? extends TileEntity>, String>();
    private static Map<String, TileEntityRotator> tileEntityRotators = new HashMap<String, TileEntityRotator>();
    private static Map<Class<? extends Entity>, String> entityTypes = new HashMap<Class<? extends Entity>, String>();
    private static Map<String, EntityRotator> entityRotators = new HashMap<String, EntityRotator>();

    public static void init() {
        // Logs
        registerBlockRotator("log", (meta) -> {
            if (meta < 4) return Vec3.createVectorHelper(0, 1, 0);
            if (meta < 8) return Vec3.createVectorHelper(1, 0, 0);
            if (meta < 12) return Vec3.createVectorHelper(0, 0, 1);
            return null;
        }, (originalMeta, vec) -> {
            int logType = 3;
            if (vec != null) {
                Axis axis = Direction.getNearest(vec)
                    .getAxis();
                switch (axis) {
                    case Y:
                        logType = 0;
                        break;
                    case X:
                        logType = 1;
                        break;
                    case Z:
                        logType = 2;
                        break;
                }
            }
            return originalMeta % 4 + logType * 4;
        });
        registerBlocks("log", Blocks.log, Blocks.log2, Blocks.hay_block);

        // Slabs
        registerBlockRotator("slab", (meta) -> {
            if (meta >= 8) return Vec3.createVectorHelper(0, 1, 0);
            return Vec3.createVectorHelper(0, -1, 0);
        }, (originalMeta, vec) -> {
            int slabType = 0;
            if (vec != null) {
                Direction dir = Direction.getNearest(vec);
                switch (dir) {
                    case DOWN:
                        slabType = 0;
                        break;
                    case UP:
                        slabType = 1;
                        break;
                    default:
                        return originalMeta;
                }
            }
            return originalMeta % 8 + slabType * 8;
        });
        registerBlocks("slab", Blocks.stone_slab, Blocks.wooden_slab);

        // Stairs
        registerBlockRotator("stairs", (meta) -> {
            Vec3 result = Vec3.createVectorHelper(0, 0, 0);
            switch (meta % 4) {
                default:
                case 0:
                    result.xCoord = 1;
                    break;
                case 1:
                    result.xCoord = -1;
                    break;
                case 2:
                    result.zCoord = 1;
                    break;
                case 3:
                    result.zCoord = -1;
                    break;
            }
            if ((meta / 4) % 2 == 0) {
                result.yCoord = -1;
            } else {
                result.yCoord = 1;
            }
            return result;
        }, (originalMeta, vec) -> {
            if (Math.abs(vec.yCoord) < Math.abs(vec.xCoord) && Math.abs(vec.yCoord) < Math.abs(vec.zCoord))
                return originalMeta;
            int stairsType = vec.yCoord > 0 ? 4 : 0;
            if (Math.abs(vec.xCoord) > Math.abs(vec.zCoord)) {
                if (vec.xCoord < 0) stairsType += 1;
            } else {
                if (vec.zCoord > 0) stairsType += 2;
                else stairsType += 3;
            }
            return stairsType;
        });
        registerBlocks(
            "stairs",
            Blocks.acacia_stairs,
            Blocks.birch_stairs,
            Blocks.brick_stairs,
            Blocks.dark_oak_stairs,
            Blocks.jungle_stairs,
            Blocks.nether_brick_stairs,
            Blocks.oak_stairs,
            Blocks.quartz_stairs,
            Blocks.sandstone_stairs,
            Blocks.spruce_stairs,
            Blocks.stone_brick_stairs,
            Blocks.stone_stairs);

        // Quartz pillar
        registerBlockRotator("quartz_pillar", (meta) -> {
            switch (meta) {
                default:
                    return null;
                case 2:
                    return Vec3.createVectorHelper(0, 1, 0);
                case 3:
                    return Vec3.createVectorHelper(1, 0, 0);
                case 4:
                    return Vec3.createVectorHelper(0, 0, 1);
            }
        }, (originalMeta, vec) -> {
            if (originalMeta < 2) return originalMeta;
            int pillarType = 2;
            if (vec != null) {
                Axis axis = Direction.getNearest(vec)
                    .getAxis();
                switch (axis) {
                    case Y:
                        pillarType = 2;
                        break;
                    case X:
                        pillarType = 3;
                        break;
                    case Z:
                        pillarType = 4;
                        break;
                }
            }
            return pillarType;
        });
        registerBlocks("quartz_pillar", Blocks.quartz_block);

        // Torches
        registerBlockRotator("torch", (meta) -> {
            switch (meta) {
                default:
                    return Vec3.createVectorHelper(0, -1, 0);
                case 0:
                case 1:
                    return Vec3.createVectorHelper(-1, 0, 0);
                case 2:
                    return Vec3.createVectorHelper(1, 0, 0);
                case 3:
                    return Vec3.createVectorHelper(0, 0, -1);
                case 4:
                    return Vec3.createVectorHelper(0, 0, 1);
            }
        }, (originalMeta, vec) -> {
            int torchType = 5;
            if (vec != null) {
                Direction dir = Direction.getNearest(vec);
                switch (dir) {
                    case WEST:
                        torchType = 1;
                        break;
                    case EAST:
                        torchType = 2;
                        break;
                    case NORTH:
                        torchType = 3;
                        break;
                    case SOUTH:
                        torchType = 4;
                        break;
                    default:
                    case DOWN:
                        torchType = 5;
                        break;
                }
            }
            return torchType;
        });
        registerBlocks("torch", Blocks.torch, Blocks.redstone_torch, Blocks.unlit_redstone_torch);

        // Hanging blocks
        registerBlockRotator("hanging", (meta) -> {
            switch (meta) {
                default:
                    return null;
                case 2:
                    return Vec3.createVectorHelper(0, 0, 1);
                case 3:
                    return Vec3.createVectorHelper(0, 0, -1);
                case 4:
                    return Vec3.createVectorHelper(1, 0, 0);
                case 5:
                    return Vec3.createVectorHelper(-1, 0, 0);
            }
        }, (originalMeta, vec) -> {
            if (vec != null) {
                Direction dir = Direction.getNearest(vec);
                switch (dir) {
                    case WEST:
                        return 5;
                    case EAST:
                        return 4;
                    case NORTH:
                        return 3;
                    case SOUTH:
                        return 2;
                    default:
                        break;
                }
            }
            return originalMeta;
        });
        registerBlocks("hanging", Blocks.ladder, Blocks.wall_sign);

        // Vine
        registerBlockRotator("vine", (meta) -> {
            switch (meta) {
                default:
                    return null;
                case 1:
                case 5:
                case 11:
                    return Vec3.createVectorHelper(0, 0, 1);
                case 2:
                case 7:
                    return Vec3.createVectorHelper(-1, 0, 0);
                case 3:
                    return Vec3.createVectorHelper(-1, 0, 1);
                case 4:
                case 14:
                    return Vec3.createVectorHelper(0, 0, -1);
                case 6:
                    return Vec3.createVectorHelper(-1, 0, -1);
                case 8:
                case 10:
                case 13:
                    return Vec3.createVectorHelper(1, 0, 0);
                case 9:
                    return Vec3.createVectorHelper(1, 0, 1);
                case 12:
                    return Vec3.createVectorHelper(1, 0, -1);
            }
        }, (originalMeta, vec) -> {
            if (vec != null) {
                Direction dir = Direction.getNearest(vec);
                if (dir.getAxis() != Axis.Y) {
                    switch (originalMeta) {
                        default:
                            switch (originalMeta) {
                                default:
                                    break;
                                case 1:
                                case 2:
                                case 4:
                                case 8:
                                    switch (dir) {
                                        default:
                                            break;
                                        case EAST:
                                            return 8;
                                        case WEST:
                                            return 2;
                                        case SOUTH:
                                            return 1;
                                        case NORTH:
                                            return 4;
                                    }
                                    break;
                                case 5:
                                case 9:
                                    switch (dir.getAxis()) {
                                        default:
                                            break;
                                        case X:
                                            return 9;
                                        case Z:
                                            return 5;
                                    }
                                    break;
                                case 7:
                                case 11:
                                case 13:
                                case 14:
                                    switch (dir) {
                                        default:
                                            break;
                                        case EAST:
                                            return 13;
                                        case WEST:
                                            return 7;
                                        case SOUTH:
                                            return 11;
                                        case NORTH:
                                            return 14;
                                    }
                                    break;
                            }
                            break;
                        case 3:
                        case 6:
                        case 10:
                        case 12:
                            return 0 | (vec.xCoord > 0 ? 8 : 2) | (vec.zCoord > 0 ? 1 : 4);
                    }
                }
            }
            return originalMeta;
        });
        registerBlocks("vine", Blocks.vine);

        // Horizontal directional blocks
        registerBlockRotator("horizontal_directional", (meta) -> {
            switch (meta % 4) {
                default:
                case 0:
                    return Vec3.createVectorHelper(0, 0, 1);
                case 1:
                    return Vec3.createVectorHelper(-1, 0, 0);
                case 2:
                    return Vec3.createVectorHelper(0, 0, -1);
                case 3:
                    return Vec3.createVectorHelper(1, 0, 0);
            }
        }, (originalMeta, vec) -> {
            if (vec != null) {
                int type = -1;
                Direction dir = Direction.getNearest(vec);
                switch (dir) {
                    case WEST:
                        type = 1;
                        break;
                    case EAST:
                        type = 3;
                        break;
                    case NORTH:
                        type = 2;
                        break;
                    case SOUTH:
                        type = 0;
                        break;
                    default:
                        break;
                }
                if (type != -1) return Math.floorDiv(originalMeta, 4) * 4 + type;
            }
            return originalMeta;
        });
        registerBlocks(
            "horizontal_directional",
            Blocks.end_portal_frame,
            Blocks.anvil,
            Blocks.bed,
            Blocks.tripwire_hook,
            Blocks.powered_repeater,
            Blocks.unpowered_repeater,
            Blocks.powered_comparator,
            Blocks.unpowered_comparator,
            Blocks.pumpkin,
            Blocks.lit_pumpkin,
            Blocks.fence_gate);

        // Doors
        registerBlockRotator("door", (meta) -> {
            switch (meta % 4) {
                default:
                case 0:
                    return Vec3.createVectorHelper(0, 0, 1);
                case 1:
                    return Vec3.createVectorHelper(-1, 0, 0);
                case 2:
                    return Vec3.createVectorHelper(0, 0, -1);
                case 3:
                    return Vec3.createVectorHelper(1, 0, 0);
            }
        }, (originalMeta, vec) -> {
            if (originalMeta < 8 && vec != null) {
                int type = -1;
                Direction dir = Direction.getNearest(vec);
                switch (dir) {
                    case WEST:
                        type = 1;
                        break;
                    case EAST:
                        type = 3;
                        break;
                    case NORTH:
                        type = 2;
                        break;
                    case SOUTH:
                        type = 0;
                        break;
                    default:
                        break;
                }
                if (type != -1) return Math.floorDiv(originalMeta, 4) * 4 + type;
            }
            return originalMeta;
        });
        registerBlocks("door", Blocks.wooden_door, Blocks.iron_door);

        // Trapdoors
        registerBlockRotator("trapdoor", (meta) -> {
            Vec3 result;
            double horizontal;
            double vertical;
            if (meta % 8 < 4) {
                vertical = 1;
                horizontal = 0.5;
            } else {
                vertical = 0.5;
                horizontal = 1;
            }
            switch (meta % 4) {
                default:
                case 0:
                    result = Vec3.createVectorHelper(0, 0, horizontal);
                    break;
                case 1:
                    result = Vec3.createVectorHelper(0, 0, -horizontal);
                    break;
                case 2:
                    result = Vec3.createVectorHelper(horizontal, 0, 0);
                    break;
                case 3:
                    result = Vec3.createVectorHelper(-horizontal, 0, 0);
                    break;
            }
            if (meta > 7) {
                result.yCoord = vertical;
            } else {
                result.yCoord = -vertical;
            }
            return result;
        }, (originalMeta, vec) -> {
            if (vec != null
                && (Math.abs(vec.yCoord) > Math.abs(vec.xCoord) || Math.abs(vec.yCoord) > Math.abs(vec.zCoord))) {
                double horizontal;
                int result = 0;
                if (Math.abs(vec.xCoord) > Math.abs(vec.zCoord)) {
                    horizontal = Math.abs(vec.xCoord);
                    if (vec.xCoord > 0) {
                        result = 2;
                    } else {
                        result = 3;
                    }
                } else {
                    horizontal = Math.abs(vec.zCoord);
                    if (vec.zCoord > 0) {
                        result = 0;
                    } else {
                        result = 1;
                    }
                }
                if (Math.abs(vec.yCoord) < horizontal) result += 4;
                if (vec.yCoord > 0) result += 8;
                return result;
            }
            return originalMeta;
        });
        registerBlocks("trapdoor", Blocks.trapdoor);

        // 3D directional blocks
        registerBlockRotator("3d_directional", (meta) -> {
            ChunkCoordinates dir = Direction.from3DDataValue(meta % 8 % 6)
                .getNormal();
            return Vec3.createVectorHelper(dir.posX, dir.posY, dir.posZ);
        }, (originalMeta, vec) -> {
            if (vec != null) return Direction.getNearest(vec)
                .get3DDataValue() + Math.floorDiv(originalMeta, 8) * 8;
            return originalMeta;
        });
        registerBlocks(
            "3d_directional",
            Blocks.piston,
            Blocks.sticky_piston,
            Blocks.piston_extension,
            Blocks.piston_head,
            Blocks.dispenser,
            Blocks.dropper);

        // Lever
        registerBlockRotator("lever", (meta) -> {
            switch (meta % 8) {
                case 0:
                    return Vec3.createVectorHelper(0.5, 1, 0);
                case 7:
                    return Vec3.createVectorHelper(0, 1, 0.5);
                case 1:
                    return Vec3.createVectorHelper(-1, 0.5, 0);
                case 2:
                    return Vec3.createVectorHelper(1, 0.5, 0);
                case 3:
                    return Vec3.createVectorHelper(0, 0.5, -1);
                case 4:
                    return Vec3.createVectorHelper(0, 0.5, 1);
                case 5:
                    return Vec3.createVectorHelper(0, -1, 0.5);
                case 6:
                default:
                    return Vec3.createVectorHelper(0.5, -1, 0);
            }
        }, (originalMeta, vec) -> {
            if (vec != null) {
                int result = 0;
                double horizontal = Math.abs(vec.xCoord) > Math.abs(vec.zCoord) ? vec.xCoord : vec.zCoord;
                if (Math.abs(vec.yCoord) > Math.abs(horizontal)) {
                    if (vec.yCoord > 0) {
                        if (Math.abs(vec.xCoord) > Math.abs(vec.zCoord)) {
                            result = 0;
                        } else {
                            result = 7;
                        }
                    } else {
                        if (Math.abs(vec.xCoord) > Math.abs(vec.zCoord)) {
                            result = 6;
                        } else {
                            result = 5;
                        }
                    }
                } else {
                    vec.yCoord = 0;
                    if (Math.abs(vec.xCoord) > Math.abs(vec.zCoord)) {
                        vec.zCoord = 0;
                    } else {
                        vec.xCoord = 0;
                    }
                    Direction dir = Direction.getNearest(vec);
                    switch (dir) {
                        case WEST:
                            result = 1;
                            break;
                        case EAST:
                            result = 2;
                            break;
                        case NORTH:
                            result = 3;
                            break;
                        case SOUTH:
                            result = 4;
                            break;
                        default:
                            break;
                    }
                }
                return result + Math.floorDiv(originalMeta, 8) * 8;
            }
            return originalMeta;
        });
        registerBlocks("lever", Blocks.lever);

        // Buttons
        registerBlockRotator("button", (meta) -> {
            switch (meta % 8) {
                default:
                case 1:
                    return Vec3.createVectorHelper(-1, 0, 0);
                case 2:
                    return Vec3.createVectorHelper(1, 0, 0);
                case 3:
                    return Vec3.createVectorHelper(0, 0, -1);
                case 4:
                    return Vec3.createVectorHelper(0, 0, 1);
            }
        }, (originalMeta, vec) -> {
            if (vec != null && Math.abs(vec.yCoord) <= Math.abs(vec.xCoord)
                && Math.abs(vec.yCoord) <= Math.abs(vec.zCoord)) {
                Direction dir = Direction.getNearest(vec);
                switch (dir) {
                    case WEST:
                        return 1;
                    case EAST:
                        return 2;
                    case NORTH:
                        return 3;
                    case SOUTH:
                        return 4;
                    default:
                        break;
                }
            }
            return originalMeta;
        });
        registerBlocks("button", Blocks.stone_button, Blocks.wooden_button);

        // Portal
        registerBlockRotator("portal", (meta) -> {
            switch (meta % 4) {
                default:
                    return null;
                case 0:
                case 2:
                    return Vec3.createVectorHelper(1, 0, 0);
                case 1:
                    return Vec3.createVectorHelper(0, 0, 1);
            }
        }, (originalMeta, vec) -> {
            if (vec != null) {
                Axis axis = Direction.getNearest(vec)
                    .getAxis();
                switch (axis) {
                    case X:
                        return 0;
                    case Z:
                        return 1;
                    default:
                        break;
                }
            }
            return originalMeta;
        });
        registerBlocks("portal", Blocks.portal);

        // Standing sign
        registerBlockRotator("sign", (meta) -> {
            Vec3 result = Vec3.createVectorHelper(0, 0, 1);
            if (meta > 0) result.rotateAroundY((float) (-Math.PI / 8f * (float) meta));
            return result;
        }, (originalMeta, vec) -> {
            if (vec != null && Math.abs(vec.yCoord) <= Math.abs(vec.xCoord)
                && Math.abs(vec.yCoord) <= Math.abs(vec.zCoord)) {
                vec.yCoord = 0;
                double angle = getAngleBetweenVectors(
                    vec,
                    Vec3.createVectorHelper(0, 0, 1),
                    Vec3.createVectorHelper(0, 1, 0));
                return (int) Math.floor(angle / 22.5d);
            }
            return originalMeta;
        });
        registerBlocks("sign", Blocks.standing_sign);

        // Horizontal directional blocks with tile entities
        registerBlockRotator("horizontal_tileentity", (meta) -> {
            switch (meta) {
                default:
                    return null;
                case 2:
                    return Vec3.createVectorHelper(0, 0, -1);
                case 3:
                    return Vec3.createVectorHelper(0, 0, 1);
                case 4:
                    return Vec3.createVectorHelper(-1, 0, 0);
                case 5:
                    return Vec3.createVectorHelper(1, 0, 0);
            }
        }, (originalMeta, vec) -> {
            if (vec != null) {
                Direction dir = Direction.getNearest(vec);
                switch (dir) {
                    case NORTH:
                        return 2;
                    case SOUTH:
                        return 3;
                    case WEST:
                        return 4;
                    case EAST:
                        return 5;
                    default:
                        break;
                }
            }
            return originalMeta;
        });
        registerBlocks(
            "horizontal_tileentity",
            Blocks.chest,
            Blocks.trapped_chest,
            Blocks.ender_chest,
            Blocks.furnace,
            Blocks.lit_furnace,
            Blocks.hopper);

        // Linear rails
        registerBlockRotator("rails_linear", (meta) -> {
            switch (meta % 8) {
                default:
                case 0:
                    return Vec3.createVectorHelper(0, 0, 1);
                case 1:
                    return Vec3.createVectorHelper(1, 0, 0);
                case 2:
                    return Vec3.createVectorHelper(1, -1, 0);
                case 3:
                    return Vec3.createVectorHelper(-1, -1, 0);
                case 4:
                    return Vec3.createVectorHelper(0, -1, -1);
                case 5:
                    return Vec3.createVectorHelper(0, -1, 1);
                case 6:
                    return Vec3.createVectorHelper(0, 0, 1);
            }
        }, (originalMeta, vec) -> {
            if (vec != null) {
                int result = -1;
                Direction dir = Direction.getNearest(vec);
                if (originalMeta % 8 < 2) {
                    switch (dir.getAxis()) {
                        default:
                            break;
                        case X:
                            result = 1;
                            break;
                        case Z:
                            result = 0;
                            break;
                    }
                } else {
                    vec.yCoord = 0;
                    switch (Direction.getNearest(vec)) {
                        case EAST:
                            result = 2;
                            break;
                        case WEST:
                            result = 3;
                            break;
                        case NORTH:
                            result = 4;
                            break;
                        case SOUTH:
                            result = 5;
                            break;
                        default:
                            break;
                    }
                }
                if (result != -1) return result + Math.floorDiv(originalMeta, 8) * 8;
            }
            return originalMeta;
        });
        registerBlocks("rails_linear", Blocks.activator_rail, Blocks.detector_rail, Blocks.golden_rail);

        // Rotating rails
        registerBlockRotator("rails_rotating", (meta) -> {
            switch (meta) {
                default:
                case 0:
                    return Vec3.createVectorHelper(0, 0, 1);
                case 1:
                    return Vec3.createVectorHelper(1, 0, 0);
                case 2:
                    return Vec3.createVectorHelper(1, -1, 0);
                case 3:
                    return Vec3.createVectorHelper(-1, -1, 0);
                case 4:
                    return Vec3.createVectorHelper(0, -1, -1);
                case 5:
                    return Vec3.createVectorHelper(0, -1, 1);
                case 6:
                    return Vec3.createVectorHelper(0, 0, 1);
                case 7:
                    return Vec3.createVectorHelper(-1, 0, 0);
                case 8:
                    return Vec3.createVectorHelper(0, 0, -1);
                case 9:
                    return Vec3.createVectorHelper(1, 0, 0);
            }
        }, (originalMeta, vec) -> {
            if (vec != null) {
                Direction dir = Direction.getNearest(vec);
                if (originalMeta < 2) {
                    switch (dir.getAxis()) {
                        default:
                            break;
                        case X:
                            return 1;
                        case Z:
                            return 0;
                    }
                } else {
                    if (originalMeta < 6) {
                        vec.yCoord = 0;
                        switch (Direction.getNearest(vec)) {
                            case EAST:
                                return 2;
                            case WEST:
                                return 3;
                            case NORTH:
                                return 4;
                            case SOUTH:
                                return 5;
                            default:
                                break;
                        }
                    } else {
                        if (originalMeta < 10) {
                            switch (dir) {
                                case EAST:
                                    return 9;
                                case WEST:
                                    return 7;
                                case NORTH:
                                    return 8;
                                case SOUTH:
                                    return 6;
                                default:
                                    break;
                            }
                        }
                    }
                }
            }
            return originalMeta;
        });
        registerBlocks("rails_rotating", Blocks.rail);

        // Skulls
        registerBlockRotator("skull", (meta) -> {
            switch (meta) {
                default:
                    return null;
                case 2:
                    return Vec3.createVectorHelper(0, 0, -1);
                case 3:
                    return Vec3.createVectorHelper(0, 0, 1);
                case 4:
                    return Vec3.createVectorHelper(-1, 0, 0);
                case 5:
                    return Vec3.createVectorHelper(1, 0, 0);
            }
        }, (originalMeta, vec) -> {
            if (vec != null) {
                Direction dir = Direction.getNearest(vec);
                if (originalMeta != 1) {
                    switch (dir) {
                        default:
                            break;
                        case NORTH:
                            return 2;
                        case SOUTH:
                            return 3;
                        case WEST:
                            return 4;
                        case EAST:
                            return 5;
                    }
                }
            }
            return originalMeta;
        });
        registerBlocks("skull", Blocks.skull);
        registerTileEntities("skull", TileEntitySkull.class);
        registerTileEntityRotator("skull", (te, world) -> {
            TileEntitySkull tes = (TileEntitySkull) te;
            double angle = -((SubWorld) world).getRotationActualYaw();
            tes.field_145910_i = (int) ((tes.field_145910_i * 360 / 16f + angle + 360) % 360 * 16f / 360);
        });

        // Hanging entities
        registerEntities("hanging", EntityHanging.class);
        registerEntityRotator("hanging", (entity, world) -> {
            EntityHanging hanging = (EntityHanging) entity;
            SubWorld subworld = (SubWorld) world;
            ChunkCoordinates globalCoords = subworld
                .transformBlockToGlobal(hanging.field_146063_b, hanging.field_146064_c, hanging.field_146062_d);
            hanging.field_146063_b = globalCoords.posX;
            hanging.field_146064_c = globalCoords.posY;
            hanging.field_146062_d = globalCoords.posZ;

            if (hanging.hangingDirection >= 0 && hanging.hangingDirection <= 3) {
                float angleX = (float) (subworld.getRotationRoll() % 360 / 180 * Math.PI);
                float angleY = (float) (subworld.getRotationActualYaw() / 180 * Math.PI);
                float angleZ = (float) (subworld.getRotationPitch() % 360 / 180 * Math.PI);
                Vec3 dir = Vec3.createVectorHelper(
                    net.minecraft.util.Direction.offsetX[hanging.hangingDirection],
                    0,
                    net.minecraft.util.Direction.offsetZ[hanging.hangingDirection]);
                dir.rotateAroundX(angleX);
                dir.rotateAroundY(angleY);
                dir.rotateAroundZ(angleZ);
                Direction hangindDir = Direction.getNearest(dir);
                int rotation = -1;
                switch (hangindDir) {
                    default:
                        break;
                    case SOUTH:
                        rotation = 0;
                        break;
                    case WEST:
                        rotation = 1;
                        break;
                    case NORTH:
                        rotation = 2;
                        break;
                    case EAST:
                        rotation = 3;
                        break;
                }
                if (rotation != -1) {
                    hanging.setDirection(rotation);
                }
            }
        });

        // Item frames
        registerEntities("item frame", EntityItemFrame.class);
        registerEntityRotator("item frame", (entity, world) -> {
            EntityItemFrame itemFrame = (EntityItemFrame) entity;
            SubWorld subworld = (SubWorld) world;

            float angleX = (float) (subworld.getRotationRoll() % 360 / 180 * Math.PI);
            float angleY = (float) (subworld.getRotationActualYaw() / 180 * Math.PI);
            float angleZ = (float) (subworld.getRotationPitch() % 360 / 180 * Math.PI);
            Vec3 dir = Vec3.createVectorHelper(0, 0, 0);
            int horDiv = 1;
            switch (itemFrame.getRotation()) {
                default:
                    break;
                case 0:
                    dir.yCoord = 1;
                    break;
                case 3:
                    horDiv = -1;
                case 1:
                    switch (itemFrame.hangingDirection) {
                        default:
                        case 0:
                            dir.xCoord = -horDiv;
                            break;
                        case 1:
                            dir.zCoord = -horDiv;
                            break;
                        case 2:
                            dir.xCoord = horDiv;
                            break;
                        case 3:
                            dir.zCoord = horDiv;
                            break;
                    }
                    break;
                case 2:
                    dir.yCoord = -1;
                    break;
            }

            entityRotators.get("hanging")
                .rotateEntity(entity, world);
            dir.rotateAroundX(angleX);
            dir.rotateAroundY(angleY);
            dir.rotateAroundZ(angleZ);

            Direction itemDir = Direction.getNearest(dir);
            int rotation = -1;
            switch (itemDir) {
                default:
                    break;
                case UP:
                    rotation = 0;
                    break;
                case DOWN:
                    rotation = 2;
                    break;
                case NORTH:
                    if (itemFrame.hangingDirection == 1) rotation = 1;
                    else if (itemFrame.hangingDirection == 3) rotation = 3;
                    break;
                case SOUTH:
                    if (itemFrame.hangingDirection == 1) rotation = 3;
                    else if (itemFrame.hangingDirection == 3) rotation = 1;
                    break;
                case WEST:
                    if (itemFrame.hangingDirection == 0) rotation = 1;
                    else if (itemFrame.hangingDirection == 2) rotation = 3;
                    break;
                case EAST:
                    if (itemFrame.hangingDirection == 0) rotation = 3;
                    else if (itemFrame.hangingDirection == 2) rotation = 1;
                    break;
            }
            if (rotation != -1) {
                itemFrame.setItemRotation(rotation);
            }
        });

        // Paintings
        registerEntities("painting", EntityPainting.class);
        registerEntityRotator("painting", (entity, world) -> {
            EntityPainting painting = (EntityPainting) entity;
            SubWorld subworld = (SubWorld) world;
            List<ChunkCoordinates> corners = new ArrayList<ChunkCoordinates>();
            corners
                .add(new ChunkCoordinates(painting.field_146063_b, painting.field_146064_c, painting.field_146062_d));
            int height = painting.getHeightPixels() / 16 - 1;
            int width = painting.getWidthPixels() / 16 - 1;
            if (height > 0) corners.add(
                new ChunkCoordinates(
                    painting.field_146063_b,
                    painting.field_146064_c + height,
                    painting.field_146062_d));
            if (width > 0) {
                int dX = 0;
                int dZ = 0;
                switch (painting.hangingDirection) {
                    default:
                    case 0:
                        dX = width;
                        break;
                    case 1:
                        dZ = width;
                        break;
                    case 2:
                        dX = -width;
                        break;
                    case 3:
                        dZ = -width;
                        break;
                }
                corners.add(
                    new ChunkCoordinates(
                        painting.field_146063_b + dX,
                        painting.field_146064_c,
                        painting.field_146062_d + dZ));
                if (height > 0) corners.add(
                    new ChunkCoordinates(
                        painting.field_146063_b + dX,
                        painting.field_146064_c + height,
                        painting.field_146062_d + dZ));
            }

            for (int i = 0; i < corners.size(); i++) {
                ChunkCoordinates corner = corners.get(i);
                corners.set(i, subworld.transformBlockToGlobal(corner.posX, corner.posY, corner.posZ));
            }
            ChunkCoordinates newCorner = corners.get(0);
            corners.remove(0);

            if (painting.hangingDirection >= 0 && painting.hangingDirection <= 3) {
                float angleX = (float) (subworld.getRotationRoll() % 360 / 180 * Math.PI);
                float angleY = (float) (subworld.getRotationActualYaw() / 180 * Math.PI);
                float angleZ = (float) (subworld.getRotationPitch() % 360 / 180 * Math.PI);
                Vec3 dir = Vec3.createVectorHelper(
                    net.minecraft.util.Direction.offsetX[painting.hangingDirection],
                    0,
                    net.minecraft.util.Direction.offsetZ[painting.hangingDirection]);
                dir.rotateAroundX(angleX);
                dir.rotateAroundY(angleY);
                dir.rotateAroundZ(angleZ);
                Direction hangindDir = Direction.getNearest(dir);
                int rotation = -1;
                boolean cornerAxisX = false;
                boolean cornerIsMin = false;
                switch (hangindDir) {
                    default:
                        break;
                    case SOUTH:
                        rotation = 0;
                        cornerAxisX = true;
                        cornerIsMin = true;
                        break;
                    case WEST:
                        rotation = 1;
                        cornerAxisX = false;
                        cornerIsMin = true;
                        break;
                    case NORTH:
                        rotation = 2;
                        cornerAxisX = true;
                        cornerIsMin = false;
                        break;
                    case EAST:
                        rotation = 3;
                        cornerAxisX = false;
                        cornerIsMin = false;
                        break;
                }

                for (ChunkCoordinates corner : corners) {
                    if (corner.posY <= newCorner.posY
                        && ((cornerAxisX && ((cornerIsMin && corner.posX <= newCorner.posX)
                            || (!cornerIsMin && corner.posX >= newCorner.posX)))
                            || (!cornerAxisX && ((cornerIsMin && corner.posZ <= newCorner.posZ)
                                || (!cornerIsMin && corner.posZ >= newCorner.posZ))))) {
                        newCorner = corner;
                    }
                }

                if (rotation != -1) {
                    painting.setDirection(rotation);
                }
            }

            painting.field_146063_b = newCorner.posX;
            painting.field_146064_c = newCorner.posY;
            painting.field_146062_d = newCorner.posZ;
        });
    }

    /**
     * Returns metadata of a subworld block after reintegration
     * 
     * @param world - subworld containing block
     * @param x
     * @param y
     * @param z
     * @return Metadata of the block after reintegration
     */
    public static int getRotatedMeta(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        if (world instanceof SubWorld) {
            Block block = world.getBlock(x, y, z);
            String blockType = blockTypes.get(block);
            if (blockType != null) {
                MetaRotator rotator = metaRotators.get(blockType);
                SubWorld subworld = (SubWorld) world;
                Vec3 direction = rotator.getVectorFromMeta(meta);
                if (direction != null) {
                    direction.rotateAroundX((float) (-subworld.getRotationRoll() % 360 / 180 * Math.PI));
                    direction.rotateAroundY((float) (subworld.getRotationYaw() % 360 / 180 * Math.PI));
                    direction.rotateAroundZ((float) (-subworld.getRotationPitch() % 360 / 180 * Math.PI));
                }
                meta = rotator.getMetaFromVector(meta, direction);
            }
        }
        return meta;
    }

    /**
     * Rotates a subworld tile entity if it can be rotated
     * 
     * @param world - subworld containing tile entity
     * @param x
     * @param y
     * @param z
     */
    public static void rotateTileEntity(World world, int x, int y, int z) {
        if (world instanceof SubWorld) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te != null) {
                String teType = getTileEntityType(te);
                if (teType != null) {
                    TileEntityRotator rotator = tileEntityRotators.get(teType);
                    if (rotator != null) rotator.rotateTileEntity(te, world);
                }
            }
        }
    }

    /**
     * Rotates a subworld entity if it can be rotated
     * 
     * @param world  - subworld containing tile entity
     * @param entity
     */
    public static void rotateEntity(World world, Entity entity) {
        if (world instanceof SubWorld && entity != null) {
            String entityType = getEntityType(entity);
            if (entityType != null) {
                EntityRotator rotator = entityRotators.get(entityType);
                if (rotator != null) rotator.rotateEntity(entity, world);
            }
        }
    }

    public static Direction getRotatedDirection(World world, Direction direction) {
        if (world instanceof SubWorld && direction != null) {
            Vec3 vec = Vec3.createVectorHelper(0, 0, 0);
            SubWorld subworld = (SubWorld) world;
            switch (direction) {
                default:
                    break;
                case UP:
                    vec.yCoord = 1;
                    break;
                case DOWN:
                    vec.yCoord = -1;
                    break;
                case EAST:
                    vec.xCoord = 1;
                    break;
                case WEST:
                    vec.xCoord = -1;
                    break;
                case SOUTH:
                    vec.zCoord = 1;
                    break;
                case NORTH:
                    vec.zCoord = -1;
                    break;
            }
            vec.rotateAroundX((float) (-subworld.getRotationRoll() % 360 / 180 * Math.PI));
            vec.rotateAroundY((float) (subworld.getRotationYaw() % 360 / 180 * Math.PI));
            vec.rotateAroundZ((float) (-subworld.getRotationPitch() % 360 / 180 * Math.PI));
            return Direction.getNearest(vec);
        }
        return direction;
    }

    public static ForgeDirection getRotatedDirection(World world, ForgeDirection direction) {
        if (world instanceof SubWorld && direction != null) {
            Vec3 vec = Vec3.createVectorHelper(0, 0, 0);
            SubWorld subworld = (SubWorld) world;
            switch (direction) {
                default:
                    break;
                case UP:
                    vec.yCoord = 1;
                    break;
                case DOWN:
                    vec.yCoord = -1;
                    break;
                case EAST:
                    vec.xCoord = 1;
                    break;
                case WEST:
                    vec.xCoord = -1;
                    break;
                case SOUTH:
                    vec.zCoord = 1;
                    break;
                case NORTH:
                    vec.zCoord = -1;
                    break;
            }
            vec.rotateAroundX((float) (-subworld.getRotationRoll() % 360 / 180 * Math.PI));
            vec.rotateAroundY((float) (subworld.getRotationYaw() % 360 / 180 * Math.PI));
            vec.rotateAroundZ((float) (-subworld.getRotationPitch() % 360 / 180 * Math.PI));
            return Direction.getNearest(vec)
                .toForgeDirection();
        }
        return direction;
    }

    public static float getAngleBetweenVectors(Vec3 vec1, Vec3 vec2, Vec3 normal) {
        Vec3 v1 = vec1.normalize();
        Vec3 v2 = vec2.normalize();

        double dot = v1.xCoord * v2.xCoord + v1.yCoord * v2.yCoord + v1.zCoord * v2.zCoord;
        double angleRad = Math.acos(Math.max(-1, Math.min(1, dot)));

        Vec3 cross = Vec3.createVectorHelper(
            v1.yCoord * v2.zCoord - v1.zCoord * v2.yCoord,
            v1.zCoord * v2.xCoord - v1.xCoord * v2.zCoord,
            v1.xCoord * v2.yCoord - v1.yCoord * v2.xCoord);

        double angle = cross.xCoord * normal.xCoord + cross.yCoord * normal.yCoord + cross.zCoord * normal.zCoord;
        angleRad = angle < 0 ? 2 * Math.PI - angleRad : angleRad;

        return (float) Math.toDegrees(angleRad);
    }

    public static void registerBlocks(String blockType, Block... blocks) {
        for (Block block : blocks) {
            blockTypes.put(block, blockType);
        }
    }

    public static void registerBlockRotator(String name, Function<Integer, Vec3> vectorFromMeta,
        BiFunction<Integer, Vec3, Integer> metaFromVector) {
        registerBlockRotator(name, new MetaRotator(vectorFromMeta, metaFromVector));
    }

    public static void registerBlockRotator(String name, MetaRotator rotator) {
        metaRotators.put(name, rotator);
    }

    @SafeVarargs
    public static void registerTileEntities(String tileEntityType, Class<? extends TileEntity>... tileEntities) {
        for (Class<? extends TileEntity> tileEntity : tileEntities) {
            tileEntityTypes.put(tileEntity, tileEntityType);
        }
    }

    private static String getTileEntityType(TileEntity tileEntity) {
        Class<? extends TileEntity> resultClass = null;
        for (Entry<Class<? extends TileEntity>, String> currentEntry : tileEntityTypes.entrySet()) {
            Class<? extends TileEntity> currentClass = currentEntry.getKey();
            if (currentClass.isInstance(tileEntity)) {
                if (resultClass == null || resultClass.isAssignableFrom(currentClass)) {
                    resultClass = currentClass;
                }
            }
        }
        return resultClass == null ? null : tileEntityTypes.get(resultClass);
    }

    public static void registerTileEntityRotator(String tileEntityType, BiConsumer<TileEntity, World> rotator) {
        registerTileEntityRotator(tileEntityType, new TileEntityRotator(rotator));
    }

    public static void registerTileEntityRotator(String tileEntityType, TileEntityRotator rotator) {
        tileEntityRotators.put(tileEntityType, rotator);
    }

    @SafeVarargs
    public static void registerEntities(String entityType, Class<? extends Entity>... entities) {
        for (Class<? extends Entity> entity : entities) {
            entityTypes.put(entity, entityType);
        }
    }

    private static String getEntityType(Entity entity) {
        Class<? extends Entity> resultClass = null;
        for (Entry<Class<? extends Entity>, String> currentEntry : entityTypes.entrySet()) {
            Class<? extends Entity> currentClass = currentEntry.getKey();
            if (currentClass.isInstance(entity)) {
                if (resultClass == null || resultClass.isAssignableFrom(currentClass)) {
                    resultClass = currentClass;
                }
            }
        }
        return resultClass == null ? null : entityTypes.get(resultClass);
    }

    public static void registerEntityRotator(String entityType, BiConsumer<Entity, World> rotator) {
        registerEntityRotator(entityType, new EntityRotator(rotator));
    }

    public static void registerEntityRotator(String entityType, EntityRotator rotator) {
        entityRotators.put(entityType, rotator);
    }

    private static class MetaRotator {

        private Function<Integer, Vec3> vectorFromMeta;
        private BiFunction<Integer, Vec3, Integer> metaFromVector;

        public MetaRotator(Function<Integer, Vec3> vectorFromMeta, BiFunction<Integer, Vec3, Integer> metaFromVector) {
            this.vectorFromMeta = vectorFromMeta;
            this.metaFromVector = metaFromVector;
        }

        public Vec3 getVectorFromMeta(int meta) {
            return vectorFromMeta.apply(meta);
        }

        public int getMetaFromVector(int originalMeta, Vec3 vector) {
            return metaFromVector.apply(originalMeta, vector);
        }

    }

    private static class TileEntityRotator {

        private BiConsumer<TileEntity, World> rotateTileEntity;

        public TileEntityRotator(BiConsumer<TileEntity, World> rotator) {
            this.rotateTileEntity = rotator;
        }

        public void rotateTileEntity(TileEntity tileEntity, World world) {
            if (tileEntity != null && world instanceof SubWorld) rotateTileEntity.accept(tileEntity, world);
        }

    }

    private static class EntityRotator {

        private BiConsumer<Entity, World> rotateEntity;

        public EntityRotator(BiConsumer<Entity, World> rotator) {
            this.rotateEntity = rotator;
        }

        public void rotateEntity(Entity entity, World world) {
            if (entity != null && world instanceof SubWorld) rotateEntity.accept(entity, world);
        }

    }

}
