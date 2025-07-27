package su.sergiusonesimus.metaworlds.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.minecraft.block.Block;
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
    private static Map<Class<? extends TileEntity>, TileEntityRotator> tileEntityRotators = new HashMap<Class<? extends TileEntity>, TileEntityRotator>();

    public static void init() {
        // Logs
        registerRotator("log", (meta) -> {
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
        registerRotator("slab", (meta) -> {
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
        registerRotator("stairs", (meta) -> {
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
        registerRotator("quartz_pillar", (meta) -> {
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
        registerRotator("torch", (meta) -> {
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
        registerRotator("hanging", (meta) -> {
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
        registerRotator("vine", (meta) -> {
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
        registerRotator("horizontal_directional", (meta) -> {
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
        registerRotator("door", (meta) -> {
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
        registerRotator("trapdoor", (meta) -> {
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
        registerRotator("3d_directional", (meta) -> {
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
        registerRotator("lever", (meta) -> {
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
        registerRotator("button", (meta) -> {
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
        registerRotator("portal", (meta) -> {
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
        registerRotator("sign", (meta) -> {
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
        registerRotator("horizontal_tileentity", (meta) -> {
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
        registerRotator("rails_linear", (meta) -> {
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
        registerRotator("rails_rotating", (meta) -> {
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
        registerRotator("skull", (meta) -> {
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
        registerRotator(TileEntitySkull.class, (te, angle) -> {
            TileEntitySkull tes = (TileEntitySkull) te;
            tes.field_145910_i = (int) ((tes.field_145910_i * 360 / 16f + angle + 360) % 360 * 16f / 360);
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
                TileEntityRotator rotator = tileEntityRotators.get(te.getClass());
                if (rotator != null) rotator.rotateTileEntity(te, -((SubWorld) world).getRotationActualYaw());
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

    public static void registerRotator(String name, Function<Integer, Vec3> vectorFromMeta,
        BiFunction<Integer, Vec3, Integer> metaFromVector) {
        registerRotator(name, new MetaRotator(vectorFromMeta, metaFromVector));
    }

    public static void registerRotator(String name, MetaRotator rotator) {
        metaRotators.put(name, rotator);
    }

    public static void registerRotator(Class<? extends TileEntity> teClass, BiConsumer<TileEntity, Double> rotator) {
        registerRotator(teClass, new TileEntityRotator(rotator));
    }

    public static void registerRotator(Class<? extends TileEntity> teClass, TileEntityRotator rotator) {
        tileEntityRotators.put(teClass, rotator);
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

        private BiConsumer<TileEntity, Double> rotateTileEntity;

        public TileEntityRotator(BiConsumer<TileEntity, Double> rotator) {
            this.rotateTileEntity = rotator;
        }

        public void rotateTileEntity(TileEntity tileEntity, double angle) {
            rotateTileEntity.accept(tileEntity, angle);
        }

    }

}
