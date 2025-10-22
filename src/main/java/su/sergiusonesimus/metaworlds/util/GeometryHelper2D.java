package su.sergiusonesimus.metaworlds.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Segment;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class GeometryHelper2D {

    public static boolean pointOnSegment(Vector2D point, Segment segment) {
        if (segment.getLine()
            .contains(point)) {
            Vector2D segmentStart = segment.getStart();
            Vector2D segmentEnd = segment.getEnd();
            double pointX = Math.round(point.getX() * 1000000) / 1000000d;
            double pointY = Math.round(point.getY() * 1000000) / 1000000d;
            double startX = Math.round(segmentStart.getX() * 1000000) / 1000000d;
            double startY = Math.round(segmentStart.getY() * 1000000) / 1000000d;
            double endX = Math.round(segmentEnd.getX() * 1000000) / 1000000d;
            double endY = Math.round(segmentEnd.getY() * 1000000) / 1000000d;
            boolean x1 = pointX >= startX && pointX <= endX;
            boolean x2 = pointX <= startX && pointX >= endX;
            boolean y1 = pointY >= startY && pointY <= endY;
            boolean y2 = pointY <= startY && pointY >= endY;
            return (x1 || x2) && (y1 || y2);
        }
        return false;
    }

    public static boolean checkSegmentsIntersection(Segment segment1, Segment segment2) {
        Vector2D intersection = segment1.getLine()
            .intersection(segment2.getLine());
        if (intersection != null) {
            return pointOnSegment(intersection, segment1) && pointOnSegment(intersection, segment2);
        }
        return false;
    }

    public static Segment segmentFromPoints(Vector2D start, Vector2D end) {
        return new Segment(start, end, new Line(start, end));
    }

    public static Polygon2D getConvexPolygon(List<Vector2D> points) {
        if (points.size() <= 2) {
            return null;
        }

        Collections.sort(points, new Vector2DComparator());

        List<Vector2D> hull = new ArrayList<>();

        for (Vector2D p : points) {
            while (hull.size() >= 2 && crossProduct(hull.get(hull.size() - 2), hull.get(hull.size() - 1), p) <= 0) {
                hull.remove(hull.size() - 1);
            }
            hull.add(p);
        }

        int lowerSize = hull.size();
        for (int i = points.size() - 1; i >= 0; i--) {
            Vector2D p = points.get(i);
            while (hull.size() > lowerSize
                && crossProduct(hull.get(hull.size() - 2), hull.get(hull.size() - 1), p) <= 0) {
                hull.remove(hull.size() - 1);
            }
            hull.add(p);
        }

        hull.remove(hull.size() - 1);

        return new Polygon2D(hull);
    }

    public static class Vector2DComparator implements Comparator<Vector2D> {

        @Override
        public int compare(Vector2D p1, Vector2D p2) {
            if (p1.getX() != p2.getX()) {
                return Double.compare(p1.getX(), p2.getX());
            }
            return Double.compare(p1.getY(), p2.getY());
        }
    }

    private static double crossProduct(Vector2D p1, Vector2D p2, Vector2D p3) {
        return (p2.getX() - p1.getX()) * (p3.getY() - p1.getY()) - (p2.getY() - p1.getY()) * (p3.getX() - p1.getX());
    }

}
