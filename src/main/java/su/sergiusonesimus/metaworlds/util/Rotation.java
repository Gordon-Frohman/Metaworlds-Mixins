package su.sergiusonesimus.metaworlds.util;

import su.sergiusonesimus.metaworlds.util.Direction.Axis;

public enum Rotation {

    NONE("none", 0),
    CLOCKWISE_90("clockwise_90", 1),
    CLOCKWISE_180("180", 2),
    COUNTERCLOCKWISE_90("counterclockwise_90", 3);

    private final String name;
    private final int rotation;

    private Rotation(String name, int rotation) {
        this.name = name;
        this.rotation = rotation;
    }

    public Direction rotate(Direction direction) {
        if (direction.getAxis() == Axis.Y) {
            return direction;
        }
        return switch (this) {
            case CLOCKWISE_90 -> direction.getClockWise();
            case CLOCKWISE_180 -> direction.getOpposite();
            case COUNTERCLOCKWISE_90 -> direction.getCounterClockWise();
            default -> direction;
        };
    }

    public static Rotation fromRotation(int rotation) {
        return switch (rotation) {
            case 1 -> CLOCKWISE_90;
            case 2 -> CLOCKWISE_180;
            case 3 -> COUNTERCLOCKWISE_90;
            default -> NONE;
        };
    }

    public static Rotation fromAngle(double angle) {
        return fromRotation((int) Math.round(angle % 360d / 90d));
    }

    public Rotation compose(Rotation other) {
        return fromRotation((this.rotation + other.rotation) % 4);
    }
}
