package su.sergiusonesimus.metaworlds.util;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.twod.Segment;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class Polygon2D {

    private List<Vector2D> vertices;

    public Polygon2D(List<Vector2D> vertices) {
        this.vertices = vertices;
    }

    public boolean intersects(Polygon2D other) {
        for (int i = 0; i < vertices.size(); i++) {
            Vector2D p1 = vertices.get(i);
            Vector2D p2 = vertices.get((i + 1) % vertices.size());
            for (int j = 0; j < other.vertices.size(); j++) {
                Vector2D q1 = other.vertices.get(j);
                Vector2D q2 = other.vertices.get((j + 1) % other.vertices.size());
                if (GeometryHelper2D.checkSegmentsIntersection(
                    GeometryHelper2D.segmentFromPoints(p1, p2),
                    GeometryHelper2D.segmentFromPoints(q1, q2))) {
                    return true;
                }
            }
        }

        for (Vector2D vertex : other.vertices) {
            if (isPointInPolygon(vertex)) {
                return true;
            }
        }

        for (Vector2D vertex : vertices) {
            if (other.isPointInPolygon(vertex)) {
                return true;
            }
        }

        return false;
    }

    private boolean isPointInPolygon(Vector2D point) {
        int intersections = 0;
        Segment ray = GeometryHelper2D.segmentFromPoints(point, new Vector2D(point.getX(), point.getY() + 1000));
        for (int i = 0; i < vertices.size(); i++) {
            Vector2D p1 = vertices.get(i);
            Vector2D p2 = vertices.get((i + 1) % vertices.size());
            if (GeometryHelper2D.checkSegmentsIntersection(GeometryHelper2D.segmentFromPoints(p1, p2), ray)) {
                intersections++;
            }
        }
        return intersections % 2 != 0; // This value is odd if the point is inside of the polygon
    }
}
