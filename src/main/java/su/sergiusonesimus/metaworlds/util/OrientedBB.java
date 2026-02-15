package su.sergiusonesimus.metaworlds.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.Segment;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.jblas.DoubleMatrix;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinAxisAlignedBB;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class OrientedBB extends AxisAlignedBB {

    public DoubleMatrix vertices = new DoubleMatrix(4, 8);
    public Vec3 dimensions = Vec3.createVectorHelper(0.0D, 0.0D, 0.0D);
    public World lastTransformedBy = null;

    public OrientedBB(double par1, double par3, double par5, double par7, double par9, double par11) {
        super(par1, par3, par5, par7, par9, par11);
        this.setVerticesAndDimensions(this);
    }

    public AxisAlignedBB setBounds(double par1, double par3, double par5, double par7, double par9, double par11) {
        super.setBounds(par1, par3, par5, par7, par9, par11);
        this.setVerticesAndDimensions(this);
        return this;
    }

    public AxisAlignedBB copy() {
        OrientedBB newOBB = OBBPool.createOBB(this);
        newOBB.vertices.copy(this.vertices);
        newOBB.dimensions.setComponents(this.dimensions.xCoord, this.dimensions.yCoord, this.dimensions.zCoord);
        return newOBB;
    }

    public void setBB(AxisAlignedBB par1AxisAlignedBB) {
        super.setBB(par1AxisAlignedBB);
        if (par1AxisAlignedBB instanceof OrientedBB) {
            this.vertices.copy(((OrientedBB) par1AxisAlignedBB).vertices);
            this.dimensions.setComponents(
                ((OrientedBB) par1AxisAlignedBB).dimensions.xCoord,
                ((OrientedBB) par1AxisAlignedBB).dimensions.yCoord,
                ((OrientedBB) par1AxisAlignedBB).dimensions.zCoord);
        } else {
            this.setVerticesAndDimensions(par1AxisAlignedBB);
        }
    }

    public OrientedBB rotatePitch(double targetPitch) {
        return rotatePitch(targetPitch, (this.minY + this.maxY) * 0.5D, (this.minZ + this.maxZ) * 0.5D);
    }

    public OrientedBB rotateYaw(double targetYaw) {
        return rotateYaw(targetYaw, (this.minX + this.maxX) * 0.5D, (this.minZ + this.maxZ) * 0.5D);
    }

    public OrientedBB rotateRoll(double targetRoll) {
        return rotateRoll(targetRoll, (this.minX + this.maxX) * 0.5D, (this.minY + this.maxY) * 0.5D);
    }

    public OrientedBB rotatePitch(double targetPitch, double centerY, double centerZ) {
        if (targetPitch == 0.0D) {
            return this;
        } else {
            double cosPitch = Math.cos(targetPitch * Math.PI / 180.0D);
            double sinPitch = Math.sin(targetPitch * Math.PI / 180.0D);

            for (int i = 0; i < 8; i++) {
                double translatedY = this.getY(i) - centerY;
                double translatedZ = this.getZ(i) - centerZ;

                double rotatedY = translatedY * cosPitch - translatedZ * sinPitch;
                double rotatedZ = translatedY * sinPitch + translatedZ * cosPitch;

                vertices.data[i * 4 + 1] = centerY + rotatedY;
                vertices.data[i * 4 + 2] = centerZ + rotatedZ;
            }

            recalcAABB();
            dimensions.setComponents(dimensions.xCoord, maxY - minY, maxZ - minZ);
            return this;
        }
    }

    public OrientedBB rotateYaw(double targetYaw, double centerX, double centerZ) {
        if (targetYaw == 0.0D) {
            return this;
        } else {
            double cosYaw = Math.cos(targetYaw * Math.PI / 180.0D);
            double sinYaw = Math.sin(targetYaw * Math.PI / 180.0D);

            for (int i = 0; i < 8; i++) {
                double translatedX = this.getX(i) - centerX;
                double translatedZ = this.getZ(i) - centerZ;

                double rotatedX = translatedX * cosYaw - translatedZ * sinYaw;
                double rotatedZ = translatedX * sinYaw + translatedZ * cosYaw;

                vertices.data[i * 4] = centerX + rotatedX;
                vertices.data[i * 4 + 2] = centerZ + rotatedZ;
            }

            recalcAABB();
            dimensions.setComponents(maxX - minX, dimensions.yCoord, maxZ - minZ);
            return this;
        }
    }

    public OrientedBB rotateRoll(double targetRoll, double centerX, double centerY) {
        if (targetRoll == 0.0D) {
            return this;
        } else {
            double cosRoll = Math.cos(targetRoll * Math.PI / 180.0D);
            double sinRoll = Math.sin(targetRoll * Math.PI / 180.0D);

            for (int i = 0; i < 8; i++) {
                double translatedX = this.getX(i) - centerX;
                double translatedY = this.getY(i) - centerY;

                double rotatedX = translatedX * cosRoll - translatedY * sinRoll;
                double rotatedY = translatedX * sinRoll + translatedY * cosRoll;

                vertices.data[i * 4] = centerX + rotatedX;
                vertices.data[i * 4 + 1] = centerY + rotatedY;
            }

            recalcAABB();
            dimensions.setComponents(maxX - minX, maxY - minY, dimensions.zCoord);
            return this;
        }
    }

    public int getCWNeighbourIndexXZ(int prevIndex) {
        switch (prevIndex % 4) {
            case 0:
                return 2 + (prevIndex >= 4 ? 4 : 0);
            case 1:
                return 0 + (prevIndex >= 4 ? 4 : 0);
            case 2:
                return 3 + (prevIndex >= 4 ? 4 : 0);
            case 3:
                return 1 + (prevIndex >= 4 ? 4 : 0);
            default:
                return 0;
        }
    }

    public int getCCWNeighbourIndexXZ(int prevIndex) {
        switch (prevIndex % 4) {
            case 0:
                return 1 + (prevIndex >= 4 ? 4 : 0);
            case 1:
                return 3 + (prevIndex >= 4 ? 4 : 0);
            case 2:
                return 0 + (prevIndex >= 4 ? 4 : 0);
            case 3:
                return 2 + (prevIndex >= 4 ? 4 : 0);
            default:
                return 0;
        }
    }

    public double getX(int index) {
        return this.vertices.data[index * 4];
    }

    public double getY(int index) {
        return this.vertices.data[index * 4 + 1];
    }

    public double getZ(int index) {
        return this.vertices.data[index * 4 + 2];
    }

    public void fromAABB(AxisAlignedBB sourceBB) {
        this.setVerticesAndDimensions(sourceBB);
        this.minX = sourceBB.minX;
        this.maxX = sourceBB.maxX;
        this.minY = sourceBB.minY;
        this.maxY = sourceBB.maxY;
        this.minZ = sourceBB.minZ;
        this.maxZ = sourceBB.maxZ;
    }

    public void setVerticesAndDimensions(AxisAlignedBB sourceBB) {
        this.vertices.data[0] = sourceBB.minX;
        this.vertices.data[1] = sourceBB.minY;
        this.vertices.data[2] = sourceBB.minZ;
        this.vertices.data[3] = 1.0D;
        this.vertices.data[4] = sourceBB.minX;
        this.vertices.data[5] = sourceBB.minY;
        this.vertices.data[6] = sourceBB.maxZ;
        this.vertices.data[7] = 1.0D;
        this.vertices.data[8] = sourceBB.maxX;
        this.vertices.data[9] = sourceBB.minY;
        this.vertices.data[10] = sourceBB.minZ;
        this.vertices.data[11] = 1.0D;
        this.vertices.data[12] = sourceBB.maxX;
        this.vertices.data[13] = sourceBB.minY;
        this.vertices.data[14] = sourceBB.maxZ;
        this.vertices.data[15] = 1.0D;
        this.vertices.data[16] = sourceBB.minX;
        this.vertices.data[17] = sourceBB.maxY;
        this.vertices.data[18] = sourceBB.minZ;
        this.vertices.data[19] = 1.0D;
        this.vertices.data[20] = sourceBB.minX;
        this.vertices.data[21] = sourceBB.maxY;
        this.vertices.data[22] = sourceBB.maxZ;
        this.vertices.data[23] = 1.0D;
        this.vertices.data[24] = sourceBB.maxX;
        this.vertices.data[25] = sourceBB.maxY;
        this.vertices.data[26] = sourceBB.minZ;
        this.vertices.data[27] = 1.0D;
        this.vertices.data[28] = sourceBB.maxX;
        this.vertices.data[29] = sourceBB.maxY;
        this.vertices.data[30] = sourceBB.maxZ;
        this.vertices.data[31] = 1.0D;
        this.dimensions
            .setComponents(sourceBB.maxX - sourceBB.minX, sourceBB.maxY - sourceBB.minY, sourceBB.maxZ - sourceBB.minZ);
    }

    public void recalcAABB() {
        this.minX = this.vertices.data[0];
        this.minY = this.vertices.data[1];
        this.minZ = this.vertices.data[2];
        this.maxX = this.vertices.data[0];
        this.maxY = this.vertices.data[1];
        this.maxZ = this.vertices.data[2];

        for (int i = 4; i < 32; i += 4) {
            if (this.vertices.data[i] < this.minX) {
                this.minX = this.vertices.data[i];
            } else if (this.vertices.data[i] > this.maxX) {
                this.maxX = this.vertices.data[i];
            }

            if (this.vertices.data[i + 1] < this.minY) {
                this.minY = this.vertices.data[i + 1];
            } else if (this.vertices.data[i + 1] > this.maxY) {
                this.maxY = this.vertices.data[i + 1];
            }

            if (this.vertices.data[i + 2] < this.minZ) {
                this.minZ = this.vertices.data[i + 2];
            } else if (this.vertices.data[i + 2] > this.maxZ) {
                this.maxZ = this.vertices.data[i + 2];
            }
        }
    }

    public OrientedBB transformBoundingBoxToGlobal(World transformerWorld) {
        ((IMixinWorld) transformerWorld).transformToGlobal(this.vertices, this.vertices);
        this.dimensions.setComponents(
            this.dimensions.xCoord * ((IMixinWorld) transformerWorld).getScaling(),
            this.dimensions.yCoord * ((IMixinWorld) transformerWorld).getScaling(),
            this.dimensions.zCoord * ((IMixinWorld) transformerWorld).getScaling());
        this.recalcAABB();
        this.lastTransformedBy = transformerWorld;
        return this;
    }

    public OrientedBB transformBoundingBoxToLocal(World transformerWorld) {
        ((IMixinWorld) transformerWorld).transformToLocal(this.vertices, this.vertices);
        this.dimensions.setComponents(
            this.dimensions.xCoord / ((IMixinWorld) transformerWorld).getScaling(),
            this.dimensions.yCoord / ((IMixinWorld) transformerWorld).getScaling(),
            this.dimensions.zCoord / ((IMixinWorld) transformerWorld).getScaling());
        this.recalcAABB();
        this.lastTransformedBy = transformerWorld;
        return this;
    }

    public OrientedBB offsetLocal(double par1, double par3, double par5) {
        super.offset(par1, par3, par5);

        for (int i = 0; i < 8; ++i) {
            this.vertices.data[i * 4] += par1;
            this.vertices.data[i * 4 + 1] += par3;
            this.vertices.data[i * 4 + 2] += par5;
        }

        return this;
    }

    public OrientedBB getOrientedBB() {
        return this;
    }

    public boolean intersectsWith(AxisAlignedBB par1AxisAlignedBB) {
        if (par1AxisAlignedBB instanceof OrientedBB) {
            return this.intersectsWithOBB((OrientedBB) par1AxisAlignedBB);
        } else {
            if (!super.intersectsWith(par1AxisAlignedBB)) return false;
            else {
                return this.intersectsWithOBB(((IMixinAxisAlignedBB) par1AxisAlignedBB).getOrientedBB());
            }
        }
    }

    public boolean intersectsWithOBB(OrientedBB par1OrientedBB) {
        // Getting vertices for every face of both OBBs
        Vector3D[][] faces1 = { { this.getVertice(0), this.getVertice(1), this.getVertice(3), this.getVertice(2) },
            { this.getVertice(4), this.getVertice(5), this.getVertice(7), this.getVertice(6) },
            { this.getVertice(0), this.getVertice(1), this.getVertice(5), this.getVertice(4) },
            { this.getVertice(1), this.getVertice(3), this.getVertice(7), this.getVertice(5) },
            { this.getVertice(3), this.getVertice(2), this.getVertice(6), this.getVertice(7) },
            { this.getVertice(2), this.getVertice(0), this.getVertice(4), this.getVertice(6) } };
        Vector3D[][] faces2 = {
            { par1OrientedBB.getVertice(0), par1OrientedBB.getVertice(1), par1OrientedBB.getVertice(3),
                par1OrientedBB.getVertice(2) },
            { par1OrientedBB.getVertice(4), par1OrientedBB.getVertice(5), par1OrientedBB.getVertice(7),
                par1OrientedBB.getVertice(6) },
            { par1OrientedBB.getVertice(0), par1OrientedBB.getVertice(1), par1OrientedBB.getVertice(5),
                par1OrientedBB.getVertice(4) },
            { par1OrientedBB.getVertice(1), par1OrientedBB.getVertice(3), par1OrientedBB.getVertice(7),
                par1OrientedBB.getVertice(5) },
            { par1OrientedBB.getVertice(3), par1OrientedBB.getVertice(2), par1OrientedBB.getVertice(6),
                par1OrientedBB.getVertice(7) },
            { par1OrientedBB.getVertice(2), par1OrientedBB.getVertice(0), par1OrientedBB.getVertice(4),
                par1OrientedBB.getVertice(6) } };
        Vector3D v1 = this.getVertice(0);
        Vector3D v2 = this.getVertice(4);
        Vector3D v3 = this.getVertice(1);
        Vector3D v4 = this.getVertice(2);
        boolean obb1FlatX = (v1.getX() == v3.getX()) && (v1.getY() == v3.getY()) && (v1.getZ() == v3.getZ());
        boolean obb1FlatY = (v1.getX() == v2.getX()) && (v1.getY() == v2.getY()) && (v1.getZ() == v2.getZ());
        boolean obb1FlatZ = (v1.getX() == v4.getX()) && (v1.getY() == v4.getY()) && (v1.getZ() == v4.getZ());
        v1 = par1OrientedBB.getVertice(0);
        v2 = par1OrientedBB.getVertice(4);
        v3 = par1OrientedBB.getVertice(1);
        v4 = par1OrientedBB.getVertice(2);
        boolean obb2FlatX = (v1.getX() == v3.getX()) && (v1.getY() == v3.getY()) && (v1.getZ() == v3.getZ());
        boolean obb2FlatY = (v1.getX() == v2.getX()) && (v1.getY() == v2.getY()) && (v1.getZ() == v2.getZ());
        boolean obb2FlatZ = (v1.getX() == v4.getX()) && (v1.getY() == v4.getY()) && (v1.getZ() == v4.getZ());

        // Calculating BBs' centers
        double centerX1 = 0;
        double centerY1 = 0;
        double centerZ1 = 0;
        double centerX2 = 0;
        double centerY2 = 0;
        double centerZ2 = 0;
        for (int i = 0; i < 8; i++) {
            centerX1 += this.getX(i);
            centerY1 += this.getY(i);
            centerZ1 += this.getZ(i);
            centerX2 += par1OrientedBB.getX(i);
            centerY2 += par1OrientedBB.getY(i);
            centerZ2 += par1OrientedBB.getZ(i);
        }
        Vector3D center1 = new Vector3D(centerX1 / 8, centerY1 / 8, centerZ1 / 8);
        Vector3D center2 = new Vector3D(centerX2 / 8, centerY2 / 8, centerZ2 / 8);

        // Getting planes for every face
        List<Plane> planeFaces1 = new ArrayList<Plane>();
        List<Plane> planeFaces2 = new ArrayList<Plane>();
        for (int i = 0; i < 6; i++) {
            Plane plane;
            if ((obb1FlatY && i == 0) || (obb1FlatX && i == 2)
                || (obb1FlatZ && i == 5)
                || (!obb1FlatY && !obb1FlatX && !obb1FlatZ)) {
                plane = new Plane(faces1[i][0], faces1[i][1], faces1[i][2]);
                // Making sure that center is always on the positive side of the plane
                if (plane.getOffset(center1) < 0) plane.revertSelf();
                planeFaces1.add(plane);
            }
            if ((obb2FlatY && i == 0) || (obb2FlatX && i == 2)
                || (obb2FlatZ && i == 5)
                || (!obb2FlatY && !obb2FlatX && !obb2FlatZ)) {
                plane = new Plane(faces2[i][0], faces2[i][1], faces2[i][2]);
                // Making sure that center is always on the positive side of the plane
                if (plane.getOffset(center2) < 0) plane.revertSelf();
                planeFaces2.add(plane);
            }
        }

        // First let's check if one BB contains any point of the other
        for (int i = 0; i < 8; i++) {
            boolean contains = true;
            Vector3D vertice = this.getVertice(i);
            for (Plane plane : planeFaces2) {
                if (plane.getOffset(vertice) < 0) {
                    contains = false;
                    break;
                }
            }
            if (contains) return true;
            contains = true;
            vertice = par1OrientedBB.getVertice(i);
            for (Plane plane : planeFaces1) {
                if (plane.getOffset(vertice) < 0) {
                    contains = false;
                    break;
                }
            }
            if (contains) return true;
        }

        boolean intersects = false;
        for (int i = 0; i < planeFaces1.size(); i++) {
            Plane planeFace1 = planeFaces1.get(i);
            for (int j = 0; j < planeFaces2.size(); j++) {
                // Are our planes parallel?
                Line intersectionLine = planeFace1.intersection(planeFaces2.get(j));
                if (intersectionLine != null) {
                    // No, they are not
                    // Let's check if all vertices of one face lie on one side of another face, or not
                    Vector3D[] face2 = faces2[j];
                    double[] normals = new double[4];
                    for (int k = 0; k < 4; k++) {
                        normals[k] = planeFace1.getOffset(face2[k]);
                    }
                    if ((normals[0] > 0 && normals[1] > 0 && normals[2] > 0 && normals[3] > 0)
                        || (normals[0] < 0 && normals[1] < 0 && normals[2] < 0 && normals[3] < 0)) continue;
                    // Let's now check if our faces intersect on the remaining axis

                    Vector3D[] face1 = faces1[i];
                    Segment[] segments1 = { GeometryHelper3D.segmentFromPoints(face1[0], face1[1]),
                        GeometryHelper3D.segmentFromPoints(face1[1], face1[2]),
                        GeometryHelper3D.segmentFromPoints(face1[2], face1[3]),
                        GeometryHelper3D.segmentFromPoints(face1[3], face1[0]) };
                    Segment[] segments2 = { GeometryHelper3D.segmentFromPoints(face2[0], face2[1]),
                        GeometryHelper3D.segmentFromPoints(face2[1], face2[2]),
                        GeometryHelper3D.segmentFromPoints(face2[2], face2[3]),
                        GeometryHelper3D.segmentFromPoints(face2[3], face2[0]) };
                    List<Double> intersection1 = new ArrayList<Double>();
                    List<Double> intersection2 = new ArrayList<Double>();
                    for (int k = 0; k < 4; k++) {
                        if (intersectionLine.contains(segments1[k].getStart())
                            && intersectionLine.contains(segments1[k].getEnd())) {
                            intersection1.add(intersectionLine.getAbscissa(segments1[k].getStart()));
                            intersection1.add(intersectionLine.getAbscissa(segments1[k].getEnd()));
                        } else {
                            Vector3D intersection = segments1[k].getLine()
                                .intersection(intersectionLine);
                            if (intersection != null && GeometryHelper3D.pointOnSegment(intersection, segments1[k]))
                                intersection1.add(intersectionLine.getAbscissa(intersection));
                        }
                        if (intersectionLine.contains(segments2[k].getStart())
                            && intersectionLine.contains(segments2[k].getEnd())) {
                            intersection2.add(intersectionLine.getAbscissa(segments2[k].getStart()));
                            intersection2.add(intersectionLine.getAbscissa(segments2[k].getEnd()));
                        } else {
                            Vector3D intersection = segments2[k].getLine()
                                .intersection(intersectionLine);
                            if (intersection != null && GeometryHelper3D.pointOnSegment(intersection, segments2[k]))
                                intersection2.add(intersectionLine.getAbscissa(intersection));
                        }
                    }
                    if (intersection1.isEmpty() || intersection2.isEmpty()) continue;
                    Object[] intersectionArray1 = intersection1.toArray();
                    Object[] intersectionArray2 = intersection2.toArray();
                    if (intersectionArray1.length == 1) {
                        if (intersectionArray2.length == 1) {
                            if (intersectionArray1[0] == intersectionArray2[0]) {
                                intersects = true;
                                break;
                            }
                        } else {
                            if ((Double) intersectionArray2[0] > (Double) intersectionArray2[1]) {
                                Double d = (Double) intersectionArray2[0];
                                intersectionArray2[0] = intersectionArray2[1];
                                intersectionArray2[1] = d;
                            }
                            if (((Double) intersectionArray1[0] >= (Double) intersectionArray2[0]
                                && (Double) intersectionArray1[0] <= (Double) intersectionArray2[1])) {
                                intersects = true;
                                break;
                            }
                        }
                    } else {
                        if ((Double) intersectionArray1[0] > (Double) intersectionArray1[1]) {
                            Double d = (Double) intersectionArray1[0];
                            intersectionArray1[0] = intersectionArray1[1];
                            intersectionArray1[1] = d;
                        }
                        if (intersectionArray2.length == 1) {
                            if (((Double) intersectionArray2[0] >= (Double) intersectionArray1[0]
                                && (Double) intersectionArray2[0] <= (Double) intersectionArray1[1])) {
                                intersects = true;
                                break;
                            }
                        } else {
                            if ((Double) intersectionArray2[0] > (Double) intersectionArray2[1]) {
                                Double d = (Double) intersectionArray2[0];
                                intersectionArray2[0] = intersectionArray2[1];
                                intersectionArray2[1] = d;
                            }
                            if ((((Double) intersectionArray1[0] >= (Double) intersectionArray2[0])
                                && ((Double) intersectionArray1[0] <= (Double) intersectionArray2[1]))
                                || (((Double) intersectionArray1[1] >= (Double) intersectionArray2[0])
                                    && ((Double) intersectionArray1[1] <= (Double) intersectionArray2[1]))
                                || (((Double) intersectionArray1[0] <= (Double) intersectionArray2[0])
                                    && ((Double) intersectionArray1[1] >= (Double) intersectionArray2[1]))) {
                                intersects = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (intersects) break;
        }

        return intersects;
    }

    public boolean isVecInside(Vec3 par1Vec3) {
        return super.isVecInside(par1Vec3);
    }

    private static double subworldExpander = 0.5;
    private static double subworldExtenderHorizontal = 0.01;

    public double calculateXOffset(AxisAlignedBB aabb, double xOffset) {
        double worldRotation = Math.abs(((IMixinWorld) this.lastTransformedBy).getRotationYaw()) % 90;
        if (worldRotation > 45) worldRotation = 90 - worldRotation;
        worldRotation *= Math.PI / 180;
        double globalBBsize = aabb.maxX - aabb.minX;
        double localBBsize = globalBBsize / (Math.cos(worldRotation) + Math.sin(worldRotation)) / 2;
        Vec3 bbCenter = Vec3.createVectorHelper((aabb.maxX + aabb.minX) / 2, aabb.minY, (aabb.maxZ + aabb.minZ) / 2);
        aabb = AxisAlignedBB.getBoundingBox(
            bbCenter.xCoord - localBBsize,
            aabb.minY,
            bbCenter.zCoord - localBBsize,
            bbCenter.xCoord + localBBsize,
            aabb.maxY,
            bbCenter.zCoord + localBBsize);

        if (((IMixinWorld) this.lastTransformedBy).getRotationPitch() % 360 != 0
            || ((IMixinWorld) this.lastTransformedBy).getRotationRoll() % 360 != 0) {
            if (checkYZoverlap(aabb)) {
                double newXOffset;
                double curMaxX;
                int curMaxIndex;
                int[] neighbourIndexes = new int[3];
                int neighbourIndex;
                Line[] corners = {
                    new Line(
                        new Vector3D(aabb.minX, aabb.minY, aabb.minZ),
                        new Vector3D(aabb.maxX, aabb.minY, aabb.minZ)),
                    new Line(
                        new Vector3D(aabb.minX, aabb.minY, aabb.maxZ),
                        new Vector3D(aabb.maxX, aabb.minY, aabb.maxZ)),
                    new Line(
                        new Vector3D(aabb.minX, aabb.maxY, aabb.minZ),
                        new Vector3D(aabb.maxX, aabb.maxY, aabb.minZ)),
                    new Line(
                        new Vector3D(aabb.minX, aabb.maxY, aabb.maxZ),
                        new Vector3D(aabb.maxX, aabb.maxY, aabb.maxZ)) };
                Plane[] walls = {
                    new Plane(
                        new Vector3D(aabb.minX, aabb.minY, aabb.minZ),
                        new Vector3D(aabb.minX, aabb.maxY, aabb.minZ),
                        new Vector3D(aabb.maxX, aabb.minY, aabb.minZ)),
                    new Plane(
                        new Vector3D(aabb.minX, aabb.maxY, aabb.minZ),
                        new Vector3D(aabb.minX, aabb.maxY, aabb.maxZ),
                        new Vector3D(aabb.maxX, aabb.maxY, aabb.minZ)),
                    new Plane(
                        new Vector3D(aabb.minX, aabb.minY, aabb.maxZ),
                        new Vector3D(aabb.minX, aabb.maxY, aabb.maxZ),
                        new Vector3D(aabb.maxX, aabb.minY, aabb.maxZ)),
                    new Plane(
                        new Vector3D(aabb.minX, aabb.minY, aabb.minZ),
                        new Vector3D(aabb.minX, aabb.minY, aabb.maxZ),
                        new Vector3D(aabb.maxX, aabb.minY, aabb.minZ)) };

                if (xOffset > 0.0D) {
                    curMaxX = this.getX(0);
                    curMaxIndex = 0;

                    for (neighbourIndex = 1; neighbourIndex < 8; ++neighbourIndex) {
                        double curX = this.getX(neighbourIndex);
                        if (curX < curMaxX) {
                            curMaxX = curX;
                            curMaxIndex = neighbourIndex;
                        }
                    }

                    neighbourIndexes[0] = this.getCCWNeighbourIndexXZ(curMaxIndex);
                    neighbourIndexes[1] = this.getCWNeighbourIndexXZ(curMaxIndex);
                    neighbourIndexes[2] = curMaxIndex < 4 ? curMaxIndex + 4 : curMaxIndex - 4;

                    while (!(this.getX(neighbourIndexes[0]) <= this.getX(neighbourIndexes[1])
                        && this.getX(neighbourIndexes[1]) <= this.getX(neighbourIndexes[2]))) {
                        if (this.getX(neighbourIndexes[0]) > this.getX(neighbourIndexes[1])) {
                            int t = neighbourIndexes[0];
                            neighbourIndexes[0] = neighbourIndexes[1];
                            neighbourIndexes[1] = t;
                        }
                        if (this.getX(neighbourIndexes[1]) > this.getX(neighbourIndexes[2])) {
                            int t = neighbourIndexes[1];
                            neighbourIndexes[1] = neighbourIndexes[2];
                            neighbourIndexes[2] = t;
                        }
                    }

                    if ((this.getX(curMaxIndex) == this.getX(neighbourIndexes[0])
                        && this.getX(neighbourIndexes[0]) == this.getX(neighbourIndexes[1]))
                        || (this.getY(curMaxIndex) <= aabb.maxY && this.getY(curMaxIndex) >= aabb.minY
                            && this.getZ(curMaxIndex) <= aabb.maxZ
                            && this.getZ(curMaxIndex) >= aabb.minZ)) {

                        // Either this BB is not rotated around y/z, or
                        // Target AABB contains the smallest x point
                        if (aabb.maxX <= curMaxX + subworldExpander) {
                            newXOffset = curMaxX - aabb.maxX - subworldExtenderHorizontal;
                            if (newXOffset < xOffset) {
                                xOffset = newXOffset;
                            }
                        }
                    } else {
                        // Checking if AABB intersects any negative x edges
                        List<Line> edges = new ArrayList<Line>();
                        // Checking the closest edge anyway
                        edges.add(new Line(this.getVertice(curMaxIndex), this.getVertice(neighbourIndexes[0])));
                        // Checking other two edges only if this BB is rotated around both axises
                        if (curMaxX != this.getX(neighbourIndexes[0])) {
                            edges.add(new Line(this.getVertice(curMaxIndex), this.getVertice(neighbourIndexes[1])));
                            edges.add(new Line(this.getVertice(curMaxIndex), this.getVertice(neighbourIndexes[2])));
                        }

                        double localXOffset = xOffset;
                        boolean liesOnEdge = false;
                        for (Line edge : edges) {
                            for (Plane wall : walls) {
                                Vector3D intersection = wall.intersection(edge);
                                if (intersection != null && intersection.getX() >= curMaxX
                                    && intersection.getY() <= aabb.maxY
                                    && intersection.getY() >= aabb.minY
                                    && intersection.getZ() <= aabb.maxZ
                                    && intersection.getZ() >= aabb.minZ) {
                                    liesOnEdge = true;
                                    if (intersection != null && aabb.maxX <= intersection.getX() + subworldExpander) {
                                        newXOffset = curMaxX - aabb.maxX - subworldExtenderHorizontal;
                                        if (newXOffset < localXOffset) {
                                            localXOffset = newXOffset;
                                        }
                                    }
                                }
                            }
                        }

                        if (liesOnEdge) {
                            if (localXOffset != xOffset) xOffset = localXOffset;
                        } else {
                            // AABB doesn't intersect any edges. Let's check negative x faces then
                            List<Plane> faces = new ArrayList<Plane>();
                            // Checking the closest face anyway
                            faces.add(
                                new Plane(
                                    this.getVertice(curMaxIndex),
                                    this.getVertice(neighbourIndexes[0]),
                                    this.getVertice(neighbourIndexes[1])));
                            if (this.getX(neighbourIndexes[1]) != curMaxX) {
                                // Checking second face if BB is rotated around at least one axis
                                faces.add(
                                    new Plane(
                                        this.getVertice(curMaxIndex),
                                        this.getVertice(neighbourIndexes[0]),
                                        this.getVertice(neighbourIndexes[2])));
                                if (this.getX(neighbourIndexes[0]) != curMaxX) {
                                    // Checking third face if BB is rotated around both y and z axises
                                    faces.add(
                                        new Plane(
                                            this.getVertice(curMaxIndex),
                                            this.getVertice(neighbourIndexes[1]),
                                            this.getVertice(neighbourIndexes[2])));
                                }
                            }

                            localXOffset = xOffset;
                            for (Plane face : faces) {
                                for (int i = 0; i < 4; i++) {
                                    Vector3D intersection = face.intersection(corners[i]);

                                    if (intersection != null && intersection.getX() >= curMaxX
                                        && aabb.maxX <= intersection.getX() + subworldExpander) {
                                        newXOffset = intersection.getX() - aabb.maxX - subworldExtenderHorizontal;
                                        if (newXOffset < localXOffset) {
                                            localXOffset = newXOffset;
                                        }
                                    }
                                }
                            }
                            if (localXOffset != xOffset) xOffset = localXOffset;
                        }
                    }
                }

                if (xOffset < 0.0D) {
                    curMaxX = this.getX(0);
                    curMaxIndex = 0;

                    for (neighbourIndex = 1; neighbourIndex < 8; ++neighbourIndex) {
                        double curX = this.getX(neighbourIndex);
                        if (curX > curMaxX) {
                            curMaxX = curX;
                            curMaxIndex = neighbourIndex;
                        }
                    }

                    neighbourIndexes[0] = this.getCCWNeighbourIndexXZ(curMaxIndex);
                    neighbourIndexes[1] = this.getCWNeighbourIndexXZ(curMaxIndex);
                    neighbourIndexes[2] = curMaxIndex < 4 ? curMaxIndex + 4 : curMaxIndex - 4;

                    while (!(this.getX(neighbourIndexes[0]) >= this.getX(neighbourIndexes[1])
                        && this.getX(neighbourIndexes[1]) >= this.getX(neighbourIndexes[2]))) {
                        if (this.getX(neighbourIndexes[0]) < this.getX(neighbourIndexes[1])) {
                            int t = neighbourIndexes[0];
                            neighbourIndexes[0] = neighbourIndexes[1];
                            neighbourIndexes[1] = t;
                        }
                        if (this.getX(neighbourIndexes[1]) < this.getX(neighbourIndexes[2])) {
                            int t = neighbourIndexes[1];
                            neighbourIndexes[1] = neighbourIndexes[2];
                            neighbourIndexes[2] = t;
                        }
                    }

                    if ((this.getX(curMaxIndex) == this.getX(neighbourIndexes[0])
                        && this.getX(neighbourIndexes[0]) == this.getX(neighbourIndexes[1]))
                        || (this.getY(curMaxIndex) <= aabb.maxY && this.getY(curMaxIndex) >= aabb.minY
                            && this.getZ(curMaxIndex) <= aabb.maxZ
                            && this.getZ(curMaxIndex) >= aabb.minZ)) {

                        // Either this BB is not rotated around y/z, or
                        // Target AABB contains the biggest x point
                        if (aabb.minX >= curMaxX - subworldExpander) {
                            newXOffset = curMaxX - aabb.minX + subworldExtenderHorizontal;
                            if (newXOffset > xOffset) {
                                xOffset = newXOffset;
                            }
                        }
                    } else {
                        // Checking if AABB intersects any positive x edges
                        List<Line> edges = new ArrayList<Line>();
                        // Checking the closest edge anyway
                        edges.add(new Line(this.getVertice(curMaxIndex), this.getVertice(neighbourIndexes[0])));
                        // Checking other two edges only if this BB is rotated around both axises
                        if (curMaxX != this.getX(neighbourIndexes[0])) {
                            edges.add(new Line(this.getVertice(curMaxIndex), this.getVertice(neighbourIndexes[1])));
                            edges.add(new Line(this.getVertice(curMaxIndex), this.getVertice(neighbourIndexes[2])));
                        }

                        double localXOffset = xOffset;
                        boolean liesOnEdge = false;
                        for (Line edge : edges) {
                            for (Plane wall : walls) {
                                Vector3D intersection = wall.intersection(edge);
                                if (intersection != null && intersection.getX() <= curMaxX
                                    && intersection.getY() <= aabb.maxY
                                    && intersection.getY() >= aabb.minY
                                    && intersection.getZ() <= aabb.maxZ
                                    && intersection.getZ() >= aabb.minZ) {
                                    liesOnEdge = true;
                                    if (intersection != null && aabb.minX >= intersection.getX() - subworldExpander) {
                                        newXOffset = curMaxX - aabb.minX + subworldExtenderHorizontal;
                                        if (newXOffset > localXOffset) {
                                            localXOffset = newXOffset;
                                        }
                                    }
                                }
                            }
                        }

                        if (liesOnEdge) {
                            if (localXOffset != xOffset) xOffset = localXOffset;
                        } else {
                            // AABB doesn't intersect any edges. Let's check positive x faces then
                            List<Plane> faces = new ArrayList<Plane>();
                            // Checking the closest face anyway
                            faces.add(
                                new Plane(
                                    this.getVertice(curMaxIndex),
                                    this.getVertice(neighbourIndexes[0]),
                                    this.getVertice(neighbourIndexes[1])));
                            if (this.getX(neighbourIndexes[1]) != curMaxX) {
                                // Checking second face if BB is rotated around at least one axis
                                faces.add(
                                    new Plane(
                                        this.getVertice(curMaxIndex),
                                        this.getVertice(neighbourIndexes[0]),
                                        this.getVertice(neighbourIndexes[2])));
                                if (this.getX(neighbourIndexes[0]) != curMaxX) {
                                    // Checking third face if BB is rotated around both y and z axises
                                    faces.add(
                                        new Plane(
                                            this.getVertice(curMaxIndex),
                                            this.getVertice(neighbourIndexes[1]),
                                            this.getVertice(neighbourIndexes[2])));
                                }
                            }

                            localXOffset = xOffset;
                            for (Plane face : faces) {
                                for (int i = 0; i < 4; i++) {
                                    Vector3D intersection = face.intersection(corners[i]);

                                    if (intersection != null && intersection.getX() <= curMaxX
                                        && aabb.minX >= intersection.getX() - subworldExpander) {
                                        newXOffset = intersection.getX() - aabb.minX + subworldExtenderHorizontal;
                                        if (newXOffset > localXOffset) {
                                            localXOffset = newXOffset;
                                        }
                                    }
                                }
                            }
                            if (localXOffset != xOffset) xOffset = localXOffset;
                        }
                    }
                }
            }
        } else {
            if (aabb.maxY > this.minY && aabb.minY < this.maxY && aabb.maxZ > this.minZ && aabb.minZ < this.maxZ) {
                double newXOffset;
                double curMaxX;
                int curMaxIndex;
                int neighbourIndex;
                if (xOffset > 0.0D) {
                    curMaxX = this.getX(0);
                    curMaxIndex = 0;

                    for (neighbourIndex = 1; neighbourIndex < 4; ++neighbourIndex) {
                        if (this.getX(neighbourIndex) < curMaxX) {
                            curMaxX = this.getX(neighbourIndex);
                            curMaxIndex = neighbourIndex;
                        }
                    }

                    if (this.getZ(curMaxIndex) <= aabb.minZ) {
                        neighbourIndex = this.getCCWNeighbourIndexXZ(curMaxIndex);
                        if (this.getZ(neighbourIndex) != this.getZ(curMaxIndex)) {
                            curMaxX = this.getX(neighbourIndex) + (this.getX(curMaxIndex) - this.getX(neighbourIndex))
                                * (aabb.minZ - this.getZ(neighbourIndex))
                                / (this.getZ(curMaxIndex) - this.getZ(neighbourIndex));
                        }
                    } else if (this.getZ(curMaxIndex) >= aabb.maxZ) {
                        neighbourIndex = this.getCWNeighbourIndexXZ(curMaxIndex);
                        if (this.getZ(neighbourIndex) != this.getZ(curMaxIndex)) {
                            curMaxX = this.getX(neighbourIndex) + (this.getX(curMaxIndex) - this.getX(neighbourIndex))
                                * (aabb.maxZ - this.getZ(neighbourIndex))
                                / (this.getZ(curMaxIndex) - this.getZ(neighbourIndex));
                        }
                    }

                    if (aabb.maxX <= curMaxX + subworldExpander) {
                        newXOffset = curMaxX - aabb.maxX - subworldExtenderHorizontal;
                        if (newXOffset < xOffset) {
                            xOffset = newXOffset;
                        }
                    }
                }

                if (xOffset < 0.0D) {
                    curMaxX = this.getX(0);
                    curMaxIndex = 0;

                    for (neighbourIndex = 1; neighbourIndex < 4; ++neighbourIndex) {
                        if (this.getX(neighbourIndex) > curMaxX) {
                            curMaxX = this.getX(neighbourIndex);
                            curMaxIndex = neighbourIndex;
                        }
                    }

                    if (this.getZ(curMaxIndex) <= aabb.minZ) {
                        neighbourIndex = this.getCWNeighbourIndexXZ(curMaxIndex);
                        if (this.getZ(neighbourIndex) != this.getZ(curMaxIndex)) {
                            curMaxX = this.getX(neighbourIndex) + (this.getX(curMaxIndex) - this.getX(neighbourIndex))
                                * (aabb.minZ - this.getZ(neighbourIndex))
                                / (this.getZ(curMaxIndex) - this.getZ(neighbourIndex));
                        }
                    } else if (this.getZ(curMaxIndex) >= aabb.maxZ) {
                        neighbourIndex = this.getCCWNeighbourIndexXZ(curMaxIndex);
                        if (this.getZ(neighbourIndex) != this.getZ(curMaxIndex)) {
                            curMaxX = this.getX(neighbourIndex) + (this.getX(curMaxIndex) - this.getX(neighbourIndex))
                                * (aabb.maxZ - this.getZ(neighbourIndex))
                                / (this.getZ(curMaxIndex) - this.getZ(neighbourIndex));
                        }
                    }

                    if (aabb.minX >= curMaxX - subworldExpander) {
                        newXOffset = curMaxX - aabb.minX + subworldExtenderHorizontal;
                        if (newXOffset > xOffset) {
                            xOffset = newXOffset;
                        }
                    }
                }
            }
        }

        return xOffset;
    }

    public double calculateYOffset(AxisAlignedBB aabb, double yOffset) {
        double worldRotation = Math.abs(((IMixinWorld) this.lastTransformedBy).getRotationYaw()) % 90;
        if (worldRotation != 0) {
            worldRotation *= Math.PI / 180;
            double globalBBsize = aabb.maxX - aabb.minX;
            double localBBsize = globalBBsize / (Math.cos(worldRotation) + Math.sin(worldRotation)) / 2;
            Vec3 bbCenter = Vec3
                .createVectorHelper((aabb.maxX + aabb.minX) / 2, aabb.minY, (aabb.maxZ + aabb.minZ) / 2);
            aabb = AxisAlignedBB.getBoundingBox(
                bbCenter.xCoord - localBBsize,
                aabb.minY,
                bbCenter.zCoord - localBBsize,
                bbCenter.xCoord + localBBsize,
                aabb.maxY,
                bbCenter.zCoord + localBBsize);
        }

        if (checkXZoverlap(aabb)) {
            double newYOffset;
            double curMaxY;
            int curMaxIndex;
            int[] neighbourIndexes = new int[3];
            int neighbourIndex;
            Line[] corners = {
                new Line(new Vector3D(aabb.minX, aabb.minY, aabb.minZ), new Vector3D(aabb.minX, aabb.maxY, aabb.minZ)),
                new Line(new Vector3D(aabb.minX, aabb.minY, aabb.maxZ), new Vector3D(aabb.minX, aabb.maxY, aabb.maxZ)),
                new Line(new Vector3D(aabb.maxX, aabb.minY, aabb.minZ), new Vector3D(aabb.maxX, aabb.maxY, aabb.minZ)),
                new Line(
                    new Vector3D(aabb.maxX, aabb.minY, aabb.maxZ),
                    new Vector3D(aabb.maxX, aabb.maxY, aabb.maxZ)) };
            Plane[] walls = {
                new Plane(
                    new Vector3D(aabb.minX, aabb.minY, aabb.minZ),
                    new Vector3D(aabb.minX, aabb.maxY, aabb.minZ),
                    new Vector3D(aabb.maxX, aabb.minY, aabb.minZ)),
                new Plane(
                    new Vector3D(aabb.minX, aabb.minY, aabb.maxZ),
                    new Vector3D(aabb.minX, aabb.maxY, aabb.maxZ),
                    new Vector3D(aabb.minX, aabb.minY, aabb.minZ)),
                new Plane(
                    new Vector3D(aabb.maxX, aabb.minY, aabb.maxZ),
                    new Vector3D(aabb.maxX, aabb.maxY, aabb.maxZ),
                    new Vector3D(aabb.minX, aabb.minY, aabb.maxZ)),
                new Plane(
                    new Vector3D(aabb.maxX, aabb.minY, aabb.minZ),
                    new Vector3D(aabb.maxX, aabb.maxY, aabb.minZ),
                    new Vector3D(aabb.maxX, aabb.minY, aabb.maxZ)) };

            if (yOffset > 0.0D) {
                curMaxY = this.getY(0);
                curMaxIndex = 0;

                for (neighbourIndex = 1; neighbourIndex < 8; ++neighbourIndex) {
                    double curY = this.getY(neighbourIndex);
                    if (curY < curMaxY) {
                        curMaxY = curY;
                        curMaxIndex = neighbourIndex;
                    }
                }

                neighbourIndexes[0] = this.getCCWNeighbourIndexXZ(curMaxIndex);
                neighbourIndexes[1] = this.getCWNeighbourIndexXZ(curMaxIndex);
                neighbourIndexes[2] = curMaxIndex < 4 ? curMaxIndex + 4 : curMaxIndex - 4;

                while (!(this.getY(neighbourIndexes[0]) <= this.getY(neighbourIndexes[1])
                    && this.getY(neighbourIndexes[1]) <= this.getY(neighbourIndexes[2]))) {
                    if (this.getY(neighbourIndexes[0]) > this.getY(neighbourIndexes[1])) {
                        int t = neighbourIndexes[0];
                        neighbourIndexes[0] = neighbourIndexes[1];
                        neighbourIndexes[1] = t;
                    }
                    if (this.getY(neighbourIndexes[1]) > this.getY(neighbourIndexes[2])) {
                        int t = neighbourIndexes[1];
                        neighbourIndexes[1] = neighbourIndexes[2];
                        neighbourIndexes[2] = t;
                    }
                }

                if ((this.getY(curMaxIndex) == this.getY(neighbourIndexes[0])
                    && this.getY(neighbourIndexes[0]) == this.getY(neighbourIndexes[1]))
                    || (this.getX(curMaxIndex) <= aabb.maxX && this.getX(curMaxIndex) >= aabb.minX
                        && this.getZ(curMaxIndex) <= aabb.maxZ
                        && this.getZ(curMaxIndex) >= aabb.minZ)) {

                    // Either this BB is not rotated around x/z, or
                    // Target AABB contains the lowest point. It shouldn't go above it
                    if (aabb.maxY <= curMaxY + subworldExpander) {
                        newYOffset = curMaxY - aabb.maxY - 0.01D;
                        if (newYOffset < yOffset) {
                            yOffset = newYOffset;
                        }
                    }
                } else {
                    // Checking if AABB intersects any bottom edges
                    List<Line> edges = new ArrayList<Line>();
                    // Checking the lowest edge anyway
                    edges.add(new Line(this.getVertice(curMaxIndex), this.getVertice(neighbourIndexes[0])));
                    // Checking other two edges only if this BB is rotated around both axises
                    if (curMaxY != this.getY(neighbourIndexes[0])) {
                        edges.add(new Line(this.getVertice(curMaxIndex), this.getVertice(neighbourIndexes[1])));
                        edges.add(new Line(this.getVertice(curMaxIndex), this.getVertice(neighbourIndexes[2])));
                    }

                    double localYOffset = yOffset;
                    boolean liesOnEdge = false;
                    for (Line edge : edges) {
                        for (Plane wall : walls) {
                            Vector3D intersection = wall.intersection(edge);
                            if (intersection != null && intersection.getY() >= curMaxY
                                && intersection.getX() <= aabb.maxX
                                && intersection.getX() >= aabb.minX
                                && intersection.getZ() <= aabb.maxZ
                                && intersection.getZ() >= aabb.minZ) {
                                liesOnEdge = true;
                                if (intersection != null && aabb.maxY <= intersection.getY() + subworldExpander) {
                                    newYOffset = curMaxY - aabb.maxY - 0.01D;
                                    if (newYOffset < localYOffset) {
                                        localYOffset = newYOffset;
                                    }
                                }
                            }
                        }
                    }

                    if (liesOnEdge) {
                        if (localYOffset != yOffset) yOffset = localYOffset;
                    } else {
                        // AABB doesn't intersect any edges. Let's check bottom faces then
                        List<Plane> faces = new ArrayList<Plane>();
                        // Checking the lowest face anyway
                        faces.add(
                            new Plane(
                                this.getVertice(curMaxIndex),
                                this.getVertice(neighbourIndexes[0]),
                                this.getVertice(neighbourIndexes[1])));
                        if (this.getY(neighbourIndexes[1]) != curMaxY) {
                            // Checking second face if BB is rotated around at least one axis
                            faces.add(
                                new Plane(
                                    this.getVertice(curMaxIndex),
                                    this.getVertice(neighbourIndexes[0]),
                                    this.getVertice(neighbourIndexes[2])));
                            if (this.getY(neighbourIndexes[0]) != curMaxY) {
                                // Checking third face if BB is rotated around both x and z axises
                                faces.add(
                                    new Plane(
                                        this.getVertice(curMaxIndex),
                                        this.getVertice(neighbourIndexes[1]),
                                        this.getVertice(neighbourIndexes[2])));
                            }
                        }

                        localYOffset = yOffset;
                        for (Plane face : faces) {
                            for (int i = 0; i < 4; i++) {
                                Vector3D intersection = face.intersection(corners[i]);

                                if (intersection != null && intersection.getY() >= curMaxY
                                    && aabb.maxY <= intersection.getY() + subworldExpander) {
                                    newYOffset = intersection.getY() - aabb.maxY - 0.01D;
                                    if (newYOffset < localYOffset) {
                                        localYOffset = newYOffset;
                                    }
                                }
                            }
                        }
                        if (localYOffset != yOffset) yOffset = localYOffset;
                    }
                }
            }

            if (yOffset < 0.0D) {
                curMaxY = this.getY(0);
                curMaxIndex = 0;

                for (neighbourIndex = 1; neighbourIndex < 8; ++neighbourIndex) {
                    double curY = this.getY(neighbourIndex);
                    if (curY > curMaxY) {
                        curMaxY = curY;
                        curMaxIndex = neighbourIndex;
                    }
                }

                neighbourIndexes[0] = this.getCCWNeighbourIndexXZ(curMaxIndex);
                neighbourIndexes[1] = this.getCWNeighbourIndexXZ(curMaxIndex);
                neighbourIndexes[2] = curMaxIndex < 4 ? curMaxIndex + 4 : curMaxIndex - 4;

                while (!(this.getY(neighbourIndexes[0]) >= this.getY(neighbourIndexes[1])
                    && this.getY(neighbourIndexes[1]) >= this.getY(neighbourIndexes[2]))) {
                    if (this.getY(neighbourIndexes[0]) < this.getY(neighbourIndexes[1])) {
                        int t = neighbourIndexes[0];
                        neighbourIndexes[0] = neighbourIndexes[1];
                        neighbourIndexes[1] = t;
                    }
                    if (this.getY(neighbourIndexes[1]) < this.getY(neighbourIndexes[2])) {
                        int t = neighbourIndexes[1];
                        neighbourIndexes[1] = neighbourIndexes[2];
                        neighbourIndexes[2] = t;
                    }
                }

                if ((this.getY(curMaxIndex) == this.getY(neighbourIndexes[0])
                    && this.getY(neighbourIndexes[0]) == this.getY(neighbourIndexes[1]))
                    || (this.getX(curMaxIndex) <= aabb.maxX && this.getX(curMaxIndex) >= aabb.minX
                        && this.getZ(curMaxIndex) <= aabb.maxZ
                        && this.getZ(curMaxIndex) >= aabb.minZ)) {

                    // Either this BB is not rotated around x/z, or
                    // Target AABB contains the highest point. It should stand right on top of it
                    if (aabb.minY >= curMaxY - subworldExpander) {
                        newYOffset = curMaxY - aabb.minY + 0.01D;
                        if (newYOffset > yOffset) {
                            yOffset = newYOffset;
                        }
                    }
                } else {
                    // Checking if AABB intersects any top edges
                    List<Line> edges = new ArrayList<Line>();
                    // Checking the highest edge anyway
                    edges.add(new Line(this.getVertice(curMaxIndex), this.getVertice(neighbourIndexes[0])));
                    // Checking other two edges only if this BB is rotated around both axises
                    if (curMaxY != this.getY(neighbourIndexes[0])) {
                        edges.add(new Line(this.getVertice(curMaxIndex), this.getVertice(neighbourIndexes[1])));
                        edges.add(new Line(this.getVertice(curMaxIndex), this.getVertice(neighbourIndexes[2])));
                    }

                    double localYOffset = yOffset;
                    boolean liesOnEdge = false;
                    for (Line edge : edges) {
                        for (Plane wall : walls) {
                            Vector3D intersection = wall.intersection(edge);
                            if (intersection != null && intersection.getY() <= curMaxY
                                && intersection.getX() <= aabb.maxX
                                && intersection.getX() >= aabb.minX
                                && intersection.getZ() <= aabb.maxZ
                                && intersection.getZ() >= aabb.minZ) {
                                liesOnEdge = true;
                                if (intersection != null && aabb.minY >= intersection.getY() - subworldExpander) {
                                    newYOffset = curMaxY - aabb.minY + 0.01D;
                                    if (newYOffset > localYOffset) {
                                        localYOffset = newYOffset;
                                    }
                                }
                            }
                        }
                    }

                    if (liesOnEdge) {
                        if (localYOffset != yOffset) yOffset = localYOffset;
                    } else {
                        // AABB doesn't intersect any edges. Let's check top faces then
                        List<Plane> faces = new ArrayList<Plane>();
                        // Checking the highest face anyway
                        faces.add(
                            new Plane(
                                this.getVertice(curMaxIndex),
                                this.getVertice(neighbourIndexes[0]),
                                this.getVertice(neighbourIndexes[1])));
                        if (this.getY(neighbourIndexes[1]) != curMaxY) {
                            // Checking second face if BB is rotated around at least one axis
                            faces.add(
                                new Plane(
                                    this.getVertice(curMaxIndex),
                                    this.getVertice(neighbourIndexes[0]),
                                    this.getVertice(neighbourIndexes[2])));
                            if (this.getY(neighbourIndexes[0]) != curMaxY) {
                                // Checking third face if BB is rotated around both x and z axises
                                faces.add(
                                    new Plane(
                                        this.getVertice(curMaxIndex),
                                        this.getVertice(neighbourIndexes[1]),
                                        this.getVertice(neighbourIndexes[2])));
                            }
                        }

                        localYOffset = yOffset;
                        for (Plane face : faces) {
                            for (int i = 0; i < 4; i++) {
                                Vector3D intersection = face.intersection(corners[i]);

                                if (intersection != null && intersection.getY() <= curMaxY
                                    && aabb.minY >= intersection.getY() - subworldExpander) {
                                    newYOffset = intersection.getY() - aabb.minY + 0.01D;
                                    if (newYOffset > localYOffset) {
                                        localYOffset = newYOffset;
                                    }
                                }
                            }
                        }
                        if (localYOffset != yOffset) yOffset = localYOffset;
                    }
                }
            }
        }

        return yOffset;
    }

    public double calculateZOffset(AxisAlignedBB aabb, double zOffset) {
        double worldRotation = Math.abs(((IMixinWorld) this.lastTransformedBy).getRotationYaw()) % 90;
        if (worldRotation > 45) worldRotation = 90 - worldRotation;
        worldRotation *= Math.PI / 180;
        double globalBBsize = aabb.maxX - aabb.minX;
        double localBBsize = globalBBsize / (Math.cos(worldRotation) + Math.sin(worldRotation)) / 2;
        Vec3 bbCenter = Vec3.createVectorHelper((aabb.maxX + aabb.minX) / 2, aabb.minY, (aabb.maxZ + aabb.minZ) / 2);
        aabb = AxisAlignedBB.getBoundingBox(
            bbCenter.xCoord - localBBsize,
            aabb.minY,
            bbCenter.zCoord - localBBsize,
            bbCenter.xCoord + localBBsize,
            aabb.maxY,
            bbCenter.zCoord + localBBsize);

        if (((IMixinWorld) this.lastTransformedBy).getRotationPitch() % 360 != 0
            || ((IMixinWorld) this.lastTransformedBy).getRotationRoll() % 360 != 0) {
            if (checkXYoverlap(aabb)) {
                double newZOffset;
                double curMaxZ;
                int curMaxIndex;
                int[] neighbourIndexes = new int[3];
                int neighbourIndex;
                Line[] corners = {
                    new Line(
                        new Vector3D(aabb.minX, aabb.minY, aabb.minZ),
                        new Vector3D(aabb.minX, aabb.minY, aabb.maxZ)),
                    new Line(
                        new Vector3D(aabb.maxX, aabb.minY, aabb.minZ),
                        new Vector3D(aabb.maxX, aabb.minY, aabb.maxZ)),
                    new Line(
                        new Vector3D(aabb.minX, aabb.maxY, aabb.minZ),
                        new Vector3D(aabb.minX, aabb.maxY, aabb.maxZ)),
                    new Line(
                        new Vector3D(aabb.maxX, aabb.maxY, aabb.minZ),
                        new Vector3D(aabb.maxX, aabb.maxY, aabb.maxZ)) };
                Plane[] walls = {
                    new Plane(
                        new Vector3D(aabb.minX, aabb.minY, aabb.minZ),
                        new Vector3D(aabb.minX, aabb.maxY, aabb.minZ),
                        new Vector3D(aabb.minX, aabb.minY, aabb.maxZ)),
                    new Plane(
                        new Vector3D(aabb.minX, aabb.maxY, aabb.minZ),
                        new Vector3D(aabb.minX, aabb.maxY, aabb.maxZ),
                        new Vector3D(aabb.maxX, aabb.maxY, aabb.minZ)),
                    new Plane(
                        new Vector3D(aabb.maxX, aabb.minY, aabb.minZ),
                        new Vector3D(aabb.maxX, aabb.maxY, aabb.minZ),
                        new Vector3D(aabb.maxX, aabb.minY, aabb.maxZ)),
                    new Plane(
                        new Vector3D(aabb.minX, aabb.minY, aabb.minZ),
                        new Vector3D(aabb.minX, aabb.minY, aabb.maxZ),
                        new Vector3D(aabb.maxX, aabb.minY, aabb.minZ)) };

                if (zOffset > 0.0D) {
                    curMaxZ = this.getZ(0);
                    curMaxIndex = 0;

                    for (neighbourIndex = 1; neighbourIndex < 8; ++neighbourIndex) {
                        double curZ = this.getZ(neighbourIndex);
                        if (curZ < curMaxZ) {
                            curMaxZ = curZ;
                            curMaxIndex = neighbourIndex;
                        }
                    }

                    neighbourIndexes[0] = this.getCCWNeighbourIndexXZ(curMaxIndex);
                    neighbourIndexes[1] = this.getCWNeighbourIndexXZ(curMaxIndex);
                    neighbourIndexes[2] = curMaxIndex < 4 ? curMaxIndex + 4 : curMaxIndex - 4;

                    while (!(this.getZ(neighbourIndexes[0]) <= this.getZ(neighbourIndexes[1])
                        && this.getZ(neighbourIndexes[1]) <= this.getZ(neighbourIndexes[2]))) {
                        if (this.getZ(neighbourIndexes[0]) > this.getZ(neighbourIndexes[1])) {
                            int t = neighbourIndexes[0];
                            neighbourIndexes[0] = neighbourIndexes[1];
                            neighbourIndexes[1] = t;
                        }
                        if (this.getZ(neighbourIndexes[1]) > this.getZ(neighbourIndexes[2])) {
                            int t = neighbourIndexes[1];
                            neighbourIndexes[1] = neighbourIndexes[2];
                            neighbourIndexes[2] = t;
                        }
                    }

                    if ((this.getZ(curMaxIndex) == this.getZ(neighbourIndexes[0])
                        && this.getZ(neighbourIndexes[0]) == this.getZ(neighbourIndexes[1]))
                        || (this.getY(curMaxIndex) <= aabb.maxY && this.getY(curMaxIndex) >= aabb.minY
                            && this.getX(curMaxIndex) <= aabb.maxX
                            && this.getX(curMaxIndex) >= aabb.minX)) {

                        // Either this BB is not rotated around y/x, or
                        // Target AABB contains the smallest z point
                        if (aabb.maxZ <= curMaxZ + subworldExpander) {
                            newZOffset = curMaxZ - aabb.maxZ - subworldExtenderHorizontal;
                            if (newZOffset < zOffset) {
                                zOffset = newZOffset;
                            }
                        }
                    } else {
                        // Checking if AABB intersects any negative z edges
                        List<Line> edges = new ArrayList<Line>();
                        // Checking the closest edge anyway
                        edges.add(new Line(this.getVertice(curMaxIndex), this.getVertice(neighbourIndexes[0])));
                        // Checking other two edges only if this BB is rotated around both axises
                        if (curMaxZ != this.getZ(neighbourIndexes[0])) {
                            edges.add(new Line(this.getVertice(curMaxIndex), this.getVertice(neighbourIndexes[1])));
                            edges.add(new Line(this.getVertice(curMaxIndex), this.getVertice(neighbourIndexes[2])));
                        }

                        double localZOffset = zOffset;
                        boolean liesOnEdge = false;
                        for (Line edge : edges) {
                            for (Plane wall : walls) {
                                Vector3D intersection = wall.intersection(edge);
                                if (intersection != null && intersection.getZ() >= curMaxZ
                                    && intersection.getY() <= aabb.maxY
                                    && intersection.getY() >= aabb.minY
                                    && intersection.getX() <= aabb.maxX
                                    && intersection.getX() >= aabb.minX) {
                                    liesOnEdge = true;
                                    if (intersection != null && aabb.maxZ <= intersection.getZ() + subworldExpander) {
                                        newZOffset = curMaxZ - aabb.maxZ - subworldExtenderHorizontal;
                                        if (newZOffset < localZOffset) {
                                            localZOffset = newZOffset;
                                        }
                                    }
                                }
                            }
                        }

                        if (liesOnEdge) {
                            if (localZOffset != zOffset) zOffset = localZOffset;
                        } else {
                            // AABB doesn't intersect any edges. Let's check negative z faces then
                            List<Plane> faces = new ArrayList<Plane>();
                            // Checking the closest face anyway
                            faces.add(
                                new Plane(
                                    this.getVertice(curMaxIndex),
                                    this.getVertice(neighbourIndexes[0]),
                                    this.getVertice(neighbourIndexes[1])));
                            if (this.getZ(neighbourIndexes[1]) != curMaxZ) {
                                // Checking second face if BB is rotated around at least one axis
                                faces.add(
                                    new Plane(
                                        this.getVertice(curMaxIndex),
                                        this.getVertice(neighbourIndexes[0]),
                                        this.getVertice(neighbourIndexes[2])));
                                if (this.getZ(neighbourIndexes[0]) != curMaxZ) {
                                    // Checking third face if BB is rotated around both y and x axises
                                    faces.add(
                                        new Plane(
                                            this.getVertice(curMaxIndex),
                                            this.getVertice(neighbourIndexes[1]),
                                            this.getVertice(neighbourIndexes[2])));
                                }
                            }

                            localZOffset = zOffset;
                            for (Plane face : faces) {
                                for (int i = 0; i < 4; i++) {
                                    Vector3D intersection = face.intersection(corners[i]);

                                    if (intersection != null && intersection.getZ() >= curMaxZ
                                        && aabb.maxZ <= intersection.getZ() + subworldExpander) {
                                        newZOffset = intersection.getZ() - aabb.maxZ - subworldExtenderHorizontal;
                                        if (newZOffset < localZOffset) {
                                            localZOffset = newZOffset;
                                        }
                                    }
                                }
                            }
                            if (localZOffset != zOffset) zOffset = localZOffset;
                        }
                    }
                }

                if (zOffset < 0.0D) {
                    curMaxZ = this.getZ(0);
                    curMaxIndex = 0;

                    for (neighbourIndex = 1; neighbourIndex < 8; ++neighbourIndex) {
                        double curZ = this.getZ(neighbourIndex);
                        if (curZ > curMaxZ) {
                            curMaxZ = curZ;
                            curMaxIndex = neighbourIndex;
                        }
                    }

                    neighbourIndexes[0] = this.getCCWNeighbourIndexXZ(curMaxIndex);
                    neighbourIndexes[1] = this.getCWNeighbourIndexXZ(curMaxIndex);
                    neighbourIndexes[2] = curMaxIndex < 4 ? curMaxIndex + 4 : curMaxIndex - 4;

                    while (!(this.getZ(neighbourIndexes[0]) >= this.getZ(neighbourIndexes[1])
                        && this.getZ(neighbourIndexes[1]) >= this.getZ(neighbourIndexes[2]))) {
                        if (this.getZ(neighbourIndexes[0]) < this.getZ(neighbourIndexes[1])) {
                            int t = neighbourIndexes[0];
                            neighbourIndexes[0] = neighbourIndexes[1];
                            neighbourIndexes[1] = t;
                        }
                        if (this.getZ(neighbourIndexes[1]) < this.getZ(neighbourIndexes[2])) {
                            int t = neighbourIndexes[1];
                            neighbourIndexes[1] = neighbourIndexes[2];
                            neighbourIndexes[2] = t;
                        }
                    }

                    if ((this.getZ(curMaxIndex) == this.getZ(neighbourIndexes[0])
                        && this.getZ(neighbourIndexes[0]) == this.getZ(neighbourIndexes[1]))
                        || (this.getY(curMaxIndex) <= aabb.maxY && this.getY(curMaxIndex) >= aabb.minY
                            && this.getX(curMaxIndex) <= aabb.maxX
                            && this.getX(curMaxIndex) >= aabb.minX)) {

                        // Either this BB is not rotated around y/x, or
                        // Target AABB contains the biggest z point
                        if (aabb.minZ >= curMaxZ - subworldExpander) {
                            newZOffset = curMaxZ - aabb.minZ + subworldExtenderHorizontal;
                            if (newZOffset > zOffset) {
                                zOffset = newZOffset;
                            }
                        }
                    } else {
                        // Checking if AABB intersects any positive z edges
                        List<Line> edges = new ArrayList<Line>();
                        // Checking the closest edge anyway
                        edges.add(new Line(this.getVertice(curMaxIndex), this.getVertice(neighbourIndexes[0])));
                        // Checking other two edges only if this BB is rotated around both axises
                        if (curMaxZ != this.getZ(neighbourIndexes[0])) {
                            edges.add(new Line(this.getVertice(curMaxIndex), this.getVertice(neighbourIndexes[1])));
                            edges.add(new Line(this.getVertice(curMaxIndex), this.getVertice(neighbourIndexes[2])));
                        }

                        double localZOffset = zOffset;
                        boolean liesOnEdge = false;
                        for (Line edge : edges) {
                            for (Plane wall : walls) {
                                Vector3D intersection = wall.intersection(edge);
                                if (intersection != null && intersection.getZ() <= curMaxZ
                                    && intersection.getY() <= aabb.maxY
                                    && intersection.getY() >= aabb.minY
                                    && intersection.getX() <= aabb.maxX
                                    && intersection.getX() >= aabb.minX) {
                                    liesOnEdge = true;
                                    if (intersection != null && aabb.minZ >= intersection.getZ() - subworldExpander) {
                                        newZOffset = curMaxZ - aabb.minZ + subworldExtenderHorizontal;
                                        if (newZOffset > localZOffset) {
                                            localZOffset = newZOffset;
                                        }
                                    }
                                }
                            }
                        }

                        if (liesOnEdge) {
                            if (localZOffset != zOffset) zOffset = localZOffset;
                        } else {
                            // AABB doesn't intersect any edges. Let's check positive z faces then
                            List<Plane> faces = new ArrayList<Plane>();
                            // Checking the closest face anyway
                            faces.add(
                                new Plane(
                                    this.getVertice(curMaxIndex),
                                    this.getVertice(neighbourIndexes[0]),
                                    this.getVertice(neighbourIndexes[1])));
                            if (this.getZ(neighbourIndexes[1]) != curMaxZ) {
                                // Checking second face if BB is rotated around at least one axis
                                faces.add(
                                    new Plane(
                                        this.getVertice(curMaxIndex),
                                        this.getVertice(neighbourIndexes[0]),
                                        this.getVertice(neighbourIndexes[2])));
                                if (this.getZ(neighbourIndexes[0]) != curMaxZ) {
                                    // Checking third face if BB is rotated around both y and x axises
                                    faces.add(
                                        new Plane(
                                            this.getVertice(curMaxIndex),
                                            this.getVertice(neighbourIndexes[1]),
                                            this.getVertice(neighbourIndexes[2])));
                                }
                            }

                            localZOffset = zOffset;
                            for (Plane face : faces) {
                                for (int i = 0; i < 4; i++) {
                                    Vector3D intersection = face.intersection(corners[i]);

                                    if (intersection != null && intersection.getZ() <= curMaxZ
                                        && aabb.minZ >= intersection.getZ() - subworldExpander) {
                                        newZOffset = intersection.getZ() - aabb.minZ + subworldExtenderHorizontal;
                                        if (newZOffset > localZOffset) {
                                            localZOffset = newZOffset;
                                        }
                                    }
                                }
                            }
                            if (localZOffset != zOffset) zOffset = localZOffset;
                        }
                    }
                }
            }
        } else {
            if (aabb.maxY > this.minY && aabb.minY < this.maxY && aabb.maxX > this.minX && aabb.minX < this.maxX) {
                double newZOffset;
                double curMaxZ;
                int curMaxIndex;
                int neighbourIndex;
                if (zOffset > 0.0D) {
                    curMaxZ = this.getZ(0);
                    curMaxIndex = 0;

                    for (neighbourIndex = 1; neighbourIndex < 4; ++neighbourIndex) {
                        if (this.getZ(neighbourIndex) < curMaxZ) {
                            curMaxZ = this.getZ(neighbourIndex);
                            curMaxIndex = neighbourIndex;
                        }
                    }

                    if (this.getX(curMaxIndex) <= aabb.minX) {
                        neighbourIndex = this.getCWNeighbourIndexXZ(curMaxIndex);
                        if (this.getX(neighbourIndex) != this.getX(curMaxIndex)) {
                            curMaxZ = this.getZ(neighbourIndex) + (this.getZ(curMaxIndex) - this.getZ(neighbourIndex))
                                * (aabb.minX - this.getX(neighbourIndex))
                                / (this.getX(curMaxIndex) - this.getX(neighbourIndex));
                        }
                    } else if (this.getX(curMaxIndex) >= aabb.maxX) {
                        neighbourIndex = this.getCCWNeighbourIndexXZ(curMaxIndex);
                        if (this.getX(neighbourIndex) != this.getX(curMaxIndex)) {
                            curMaxZ = this.getZ(neighbourIndex) + (this.getZ(curMaxIndex) - this.getZ(neighbourIndex))
                                * (aabb.maxX - this.getX(neighbourIndex))
                                / (this.getX(curMaxIndex) - this.getX(neighbourIndex));
                        }
                    }

                    if (aabb.maxZ <= curMaxZ + subworldExpander) {
                        newZOffset = curMaxZ - aabb.maxZ - subworldExtenderHorizontal;
                        if (newZOffset < zOffset) {
                            zOffset = newZOffset;
                        }
                    }
                }

                if (zOffset < 0.0D) {
                    curMaxZ = this.getZ(0);
                    curMaxIndex = 0;

                    for (neighbourIndex = 1; neighbourIndex < 4; ++neighbourIndex) {
                        if (this.getZ(neighbourIndex) > curMaxZ) {
                            curMaxZ = this.getZ(neighbourIndex);
                            curMaxIndex = neighbourIndex;
                        }
                    }

                    if (this.getX(curMaxIndex) <= aabb.minX) {
                        neighbourIndex = this.getCCWNeighbourIndexXZ(curMaxIndex);
                        if (this.getX(neighbourIndex) != this.getX(curMaxIndex)) {
                            curMaxZ = this.getZ(neighbourIndex) + (this.getZ(curMaxIndex) - this.getZ(neighbourIndex))
                                * (aabb.minX - this.getX(neighbourIndex))
                                / (this.getX(curMaxIndex) - this.getX(neighbourIndex));
                        }
                    } else if (this.getX(curMaxIndex) >= aabb.maxX) {
                        neighbourIndex = this.getCWNeighbourIndexXZ(curMaxIndex);
                        if (this.getX(neighbourIndex) != this.getX(curMaxIndex)) {
                            curMaxZ = this.getZ(neighbourIndex) + (this.getZ(curMaxIndex) - this.getZ(neighbourIndex))
                                * (aabb.maxX - this.getX(neighbourIndex))
                                / (this.getX(curMaxIndex) - this.getX(neighbourIndex));
                        }
                    }

                    if (aabb.minZ >= curMaxZ - subworldExpander) {
                        newZOffset = curMaxZ - aabb.minZ + subworldExtenderHorizontal;
                        if (newZOffset > zOffset) {
                            zOffset = newZOffset;
                        }
                    }
                }
            }
        }

        return zOffset;
    }

    public boolean checkYZoverlap(AxisAlignedBB aabb) {
        List<Vector2D> points = new ArrayList<Vector2D>();
        for (int i = 0; i < 8; i++) {
            Vector2D newPoint = new Vector2D(this.getY(i), this.getZ(i));
            boolean shouldAdd = true;
            for (Vector2D point : points) {
                if (newPoint.getX() == point.getX() && newPoint.getY() == point.getY()) {
                    shouldAdd = false;
                    break;
                }
            }
            if (shouldAdd) points.add(newPoint);
        }
        Polygon2D polygon1 = GeometryHelper2D.getConvexPolygon(points);

        points.clear();
        points.add(new Vector2D(aabb.minY, aabb.minZ));
        points.add(new Vector2D(aabb.minY, aabb.maxZ));
        points.add(new Vector2D(aabb.maxY, aabb.maxZ));
        points.add(new Vector2D(aabb.maxY, aabb.minZ));
        Polygon2D polygon2 = new Polygon2D(points);

        return polygon1.intersects(polygon2);
    }

    public boolean checkXZoverlap(AxisAlignedBB aabb) {
        List<Vector2D> points = new ArrayList<Vector2D>();
        for (int i = 0; i < 8; i++) {
            Vector2D newPoint = new Vector2D(this.getX(i), this.getZ(i));
            boolean shouldAdd = true;
            for (Vector2D point : points) {
                if (newPoint.getX() == point.getX() && newPoint.getY() == point.getY()) {
                    shouldAdd = false;
                    break;
                }
            }
            if (shouldAdd) points.add(newPoint);
        }
        Polygon2D polygon1 = GeometryHelper2D.getConvexPolygon(points);

        points.clear();
        points.add(new Vector2D(aabb.minX, aabb.minZ));
        points.add(new Vector2D(aabb.minX, aabb.maxZ));
        points.add(new Vector2D(aabb.maxX, aabb.maxZ));
        points.add(new Vector2D(aabb.maxX, aabb.minZ));
        Polygon2D polygon2 = new Polygon2D(points);

        return polygon1.intersects(polygon2);
    }

    public boolean checkXYoverlap(AxisAlignedBB aabb) {
        List<Vector2D> points = new ArrayList<Vector2D>();
        for (int i = 0; i < 8; i++) {
            Vector2D newPoint = new Vector2D(this.getX(i), this.getY(i));
            boolean shouldAdd = true;
            for (Vector2D point : points) {
                if (newPoint.getX() == point.getX() && newPoint.getY() == point.getY()) {
                    shouldAdd = false;
                    break;
                }
            }
            if (shouldAdd) points.add(newPoint);
        }
        Polygon2D polygon1 = GeometryHelper2D.getConvexPolygon(points);

        points.clear();
        points.add(new Vector2D(aabb.minX, aabb.minY));
        points.add(new Vector2D(aabb.minX, aabb.maxY));
        points.add(new Vector2D(aabb.maxX, aabb.maxY));
        points.add(new Vector2D(aabb.maxX, aabb.minY));
        Polygon2D polygon2 = new Polygon2D(points);

        return polygon1.intersects(polygon2);
    }

    @Override
    public AxisAlignedBB offset(double x0, double x1, double x2) {
        return this.offsetLocal(x0, x1, x2);
    }

    public Vector3D getVertice(int i) {
        return new Vector3D(this.getX(i), this.getY(i), this.getZ(i));
    }

    public int getYneighbourIndex(int currentIndex) {
        return currentIndex < 4 ? currentIndex + 4 : currentIndex + 4;
    }

    // First vertice should always be between second and third
    public Integer getFourthVerticeIndex(int i1, int i2, int i3) {
        if (i1 == i2 || i1 == i3 || i2 == i3) return null;
        else {
            int cwxz = this.getCWNeighbourIndexXZ(i1);
            int ccwxz = this.getCCWNeighbourIndexXZ(i1);
            if ((cwxz == i2 && ccwxz == i3) || (cwxz == i3 && ccwxz == i2)) {
                // All points lay in XZ plane
                return this.getCWNeighbourIndexXZ(cwxz);
            } else {
                if (cwxz == i2 || ccwxz == i2) {
                    if (this.getYneighbourIndex(i1) == i3) return this.getYneighbourIndex(i2);
                    else return null;
                } else {
                    if (cwxz == i3 || ccwxz == i3) {
                        if (this.getYneighbourIndex(i1) == i2) return this.getYneighbourIndex(i3);
                        else return null;
                    }
                }
            }
        }
        return null;
    }
}
