package su.sergiusonesimus.metaworlds.util;

import java.lang.reflect.Field;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class CubeFace extends Plane {

    private double minX, minY, maxX, maxY;

    /**
     * Build a cube face from three points of a right-angled triangle.
     * <p>
     * Axes of the plane are equal to (p2 - p1) and (p3 - p1) accordingly
     * </p>
     * 
     * @param p1 first point belonging to the face. Must be at the right angle of the triangle
     * @param p2 second point belonging to the plane
     * @param p3 third point belonging to the plane
     * @exception MathArithmeticException if the points do not constitute a plane
     */
    public CubeFace(Vector3D p1, Vector3D p2, Vector3D p3) {
        super(p1, p2, p3);
        try {
            Field u = Plane.class.getDeclaredField("u");
            Field v = Plane.class.getDeclaredField("v");
            u.setAccessible(true);
            v.setAccessible(true);
            u.set(this, p2.subtract(p1));
            v.set(this, p3.subtract(p1));
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        Vector2D lp1 = this.toSubSpace(p1);
        Vector2D lp2 = this.toSubSpace(p2);
        Vector2D lp3 = this.toSubSpace(p3);
        minX = lp1.getX();
        minY = lp1.getY();
        maxX = lp2.getX();
        maxY = lp3.getY();
    }

    @Override
    public boolean contains(final Vector3D point) {
        if (!super.contains(point)) return false;
        Vector2D localPoint = this.toSubSpace(point);
        double localX = localPoint.getX();
        double localY = localPoint.getY();
        return localX >= minX && localX <= maxX && localY >= minY && localY <= maxY;
    }

}
