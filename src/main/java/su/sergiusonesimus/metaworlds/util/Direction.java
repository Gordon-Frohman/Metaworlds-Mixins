package su.sergiusonesimus.metaworlds.util;

import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

public enum Direction {

    DOWN(0, 1, -1, "down", Direction.AxisDirection.NEGATIVE, Direction.Axis.Y, new ChunkCoordinates(0, -1, 0)),
    UP(1, 0, -1, "up", Direction.AxisDirection.POSITIVE, Direction.Axis.Y, new ChunkCoordinates(0, 1, 0)),
    NORTH(2, 3, 2, "north", Direction.AxisDirection.NEGATIVE, Direction.Axis.Z, new ChunkCoordinates(0, 0, -1)),
    SOUTH(3, 2, 0, "south", Direction.AxisDirection.POSITIVE, Direction.Axis.Z, new ChunkCoordinates(0, 0, 1)),
    WEST(4, 5, 1, "west", Direction.AxisDirection.NEGATIVE, Direction.Axis.X, new ChunkCoordinates(-1, 0, 0)),
    EAST(5, 4, 0, "east", Direction.AxisDirection.POSITIVE, Direction.Axis.X, new ChunkCoordinates(1, 0, 0));

    private final int data3d;
    private final int oppositeIndex;
    private final int data2d;
    private final String name;
    private final Axis axis;
    private final AxisDirection axisDirection;
    private final ChunkCoordinates normal;
    private static final Direction[] VALUES = values();
    private static final Direction[] BY_3D_DATA = new Direction[6];
    private static final Direction[] BY_2D_DATA = new Direction[4];

    static {
        for (Direction direction : VALUES) {
            BY_3D_DATA[direction.data3d] = direction;
            if (direction.getAxis()
                .isHorizontal()) {
                BY_2D_DATA[direction.data2d] = direction;
            }
        }
    }

    Direction(int data3d, int oppositeIndex, int data2d, String name, AxisDirection axisDirection, Axis axis,
        ChunkCoordinates normal) {
        this.data3d = data3d;
        this.oppositeIndex = oppositeIndex;
        this.data2d = data2d;
        this.name = name;
        this.axis = axis;
        this.axisDirection = axisDirection;
        this.normal = normal;
    }

    public ForgeDirection toForgeDirection() {
        switch (this) {
            default:
                return ForgeDirection.UNKNOWN;
            case DOWN:
                return ForgeDirection.DOWN;
            case UP:
                return ForgeDirection.UP;
            case NORTH:
                return ForgeDirection.NORTH;
            case EAST:
                return ForgeDirection.EAST;
            case SOUTH:
                return ForgeDirection.SOUTH;
            case WEST:
                return ForgeDirection.WEST;
        }
    }

    public static Direction fromForgeDirection(ForgeDirection dir) {
        switch (dir) {
            default:
                return null;
            case DOWN:
                return Direction.DOWN;
            case UP:
                return Direction.UP;
            case NORTH:
                return Direction.NORTH;
            case EAST:
                return Direction.EAST;
            case SOUTH:
                return Direction.SOUTH;
            case WEST:
                return Direction.WEST;
        }
    }

    public static Direction from3DDataValue(int value) {
        return BY_3D_DATA[Math.abs(value % BY_3D_DATA.length)];
    }

    public static Direction from2DDataValue(int value) {
        return BY_2D_DATA[Math.abs(value % BY_2D_DATA.length)];
    }

    public static Direction get(AxisDirection axisDirection, Axis axis) {
        switch (axis) {
            default:
            case X:
                return axisDirection == AxisDirection.POSITIVE ? EAST : WEST;
            case Y:
                return axisDirection == AxisDirection.POSITIVE ? UP : DOWN;
            case Z:
                return axisDirection == AxisDirection.POSITIVE ? SOUTH : NORTH;
        }
    }

    public static Direction getNearest(Vec3 vec) {
        return getNearest(vec.xCoord, vec.yCoord, vec.zCoord);
    }

    public static Direction getNearest(double x, double y, double z) {
        return getNearest((float) x, (float) y, (float) z);
    }

    public static Direction getNearest(float x, float y, float z) {
        Direction direction = NORTH;
        float max = Float.MIN_VALUE;
        for (Direction dir : VALUES) {
            float dot = x * dir.normal.posX + y * dir.normal.posY + z * dir.normal.posZ;
            if (dot > max) {
                max = dot;
                direction = dir;
            }
        }
        return direction;
    }

    public static Direction getNearestLookingDirection(EntityLivingBase entity) {
        Vec3 lookVec = entity.getLookVec();

        Direction closest = Direction
            .getNearest((float) lookVec.xCoord, (float) lookVec.yCoord, (float) lookVec.zCoord);

        return closest;
    }

    public Direction getOpposite() {
        return VALUES[this.oppositeIndex];
    }

    public Direction getClockWise() {
        switch (this) {
            default:
                return this;
            case NORTH:
                return EAST;
            case EAST:
                return SOUTH;
            case SOUTH:
                return WEST;
            case WEST:
                return NORTH;
        }
    }

    public Direction getClockWise(Axis axis) {
        switch (axis) {
            case X:
                return this == WEST ? DOWN : this == EAST ? UP : this == DOWN ? SOUTH : this == UP ? NORTH : this;
            case Y:
                return this == NORTH ? EAST : this == EAST ? SOUTH : this == SOUTH ? WEST : this == WEST ? NORTH : this;
            case Z:
                return this == NORTH ? UP : this == UP ? SOUTH : this == SOUTH ? DOWN : this == DOWN ? NORTH : this;
            default:
                throw new IllegalStateException("Unable to get clockwise for axis " + axis);
        }
    }

    public Direction getCounterClockWise() {
        if (this.ordinal() < 2) return this;
        return this.getClockWise()
            .getOpposite();
    }

    public Direction getCounterClockWise(Axis axis) {
        return this.getClockWise(axis)
            .getOpposite();
    }

    public Direction rotateAround(Axis axis) {
        switch (axis) {
            case X:
                if (this != WEST && this != EAST) {
                    return this.rotateX();
                }

                return this;
            case Y:
                if (this != UP && this != DOWN) {
                    return this.getClockWise();
                }

                return this;
            case Z:
                if (this != NORTH && this != SOUTH) {
                    return this.rotateZ();
                }

                return this;
            default:
                throw new IllegalStateException("Unable to get CW facing for axis " + axis);
        }
    }

    public Direction rotateX() {
        switch (this) {
            case NORTH:
                return DOWN;
            case EAST:
            case WEST:
            default:
                throw new IllegalStateException("Unable to get X-rotated facing of " + this);
            case SOUTH:
                return UP;
            case UP:
                return NORTH;
            case DOWN:
                return SOUTH;
        }
    }

    public Direction rotateZ() {
        switch (this) {
            case EAST:
                return DOWN;
            case SOUTH:
            default:
                throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
            case WEST:
                return UP;
            case UP:
                return EAST;
            case DOWN:
                return WEST;
        }
    }

    public int get3DDataValue() {
        return this.data3d;
    }

    public int get2DDataValue() {
        return this.data2d;
    }

    public AxisDirection getAxisDirection() {
        return this.axisDirection;
    }

    public ChunkCoordinates getNormal() {
        return new ChunkCoordinates(normal);
    }

    public boolean isFacingAngle(float angle) {
        float minAngle = this.get2DDataValue() * 90;
        float maxAngle = minAngle + 90.0F;
        return angle >= minAngle && angle < maxAngle;
    }

    public static Direction getRandom(Random random) {
        return VALUES[random.nextInt(VALUES.length)];
    }

    public String getSerializedName() {
        return this.name;
    }

    public Axis getAxis() {
        return this.axis;
    }

    public float toYRot() {
        switch (this) {
            default:
            case SOUTH:
                return 0.0F;
            case WEST:
                return 90.0F;
            case NORTH:
                return 180.0F;
            case EAST:
                return 270.0F;
        }
    }

    public static enum AxisDirection {

        POSITIVE(1, "Towards positive"),
        NEGATIVE(-1, "Towards negative");

        private final int step;
        private final String name;

        private AxisDirection(int step, String name) {
            this.step = step;
            this.name = name;
        }

        public int getStep() {
            return this.step;
        }

        public String getName() {
            return this.name;
        }

        public String toString() {
            return this.name;
        }

        public Direction.AxisDirection opposite() {
            return this == POSITIVE ? NEGATIVE : POSITIVE;
        }
    }

    public enum Axis {

        X("x"),
        Y("y"),
        Z("z");

        private final String name;
        public static final Axis[] VALUES = values();

        Axis(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public boolean isVertical() {
            return this == Y;
        }

        public boolean isHorizontal() {
            return this == X || this == Z;
        }

        public static Axis fromName(String name) {
            switch (name) {
                default:
                case "x":
                case "X":
                    return X;
                case "y":
                case "Y":
                    return Y;
                case "z":
                case "Z":
                    return Z;
            }
        }

        public static Axis getRandom(Random random) {
            return VALUES[random.nextInt(VALUES.length)];
        }

        public int choose(int xCoord, int yCoord, int zCoord) {
            return (int) choose((double) xCoord, (double) yCoord, (double) zCoord);
        }

        public double choose(double xCoord, double yCoord, double zCoord) {
            switch (this) {
                default:
                case X:
                    return xCoord;
                case Y:
                    return yCoord;
                case Z:
                    return zCoord;
            }
        }

        /**
         * 
         * @return Positive direction perpendicular to this horizontal axis
         */
        public Direction getPositivePerpendicular() {
            return this == Axis.X ? SOUTH : EAST;
        }
    }

}
