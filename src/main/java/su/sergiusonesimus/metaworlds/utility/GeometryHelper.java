package su.sergiusonesimus.metaworlds.utility;

import net.minecraft.util.Vec3;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Segment;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class GeometryHelper {

    public static Vector3D transformVector(Vec3 vector) {
        return new Vector3D(vector.xCoord, vector.yCoord, vector.zCoord);
    }

    public static boolean pointOnSegment(Vector3D point, Segment segment) {
        if (segment.getLine()
            .contains(point)) {
            double segmentStart = segment.getLine()
                .getAbscissa(segment.getStart());
            double segmentEnd = segment.getLine()
                .getAbscissa(segment.getEnd());
            double localPoint = segment.getLine()
                .getAbscissa(point);
            return (localPoint >= segmentStart && localPoint <= segmentEnd)
                || (localPoint <= segmentStart && localPoint >= segmentEnd);
        }
        return false;
    }

    public static Segment segmentFromPoints(Vector3D start, Vector3D end) {
        return new Segment(start, end, new Line(start, end));
    }

}
