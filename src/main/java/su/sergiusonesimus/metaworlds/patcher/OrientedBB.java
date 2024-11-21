package su.sergiusonesimus.metaworlds.patcher;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.Segment;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jblas.DoubleMatrix;

import su.sergiusonesimus.metaworlds.api.IMixinWorld;
import su.sergiusonesimus.metaworlds.mixin.interfaces.util.IMixinAxisAlignedBB;
import su.sergiusonesimus.metaworlds.utility.GeometryHelper;

public class OrientedBB extends AxisAlignedBB {

    public DoubleMatrix vertices = new DoubleMatrix(4, 8);
    public Vec3 dimensions = Vec3.createVectorHelper(0.0D, 0.0D, 0.0D);
    public World lastTransformedBy = null;

    OrientedBB(double par1, double par3, double par5, double par7, double par9, double par11) {
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

    public OrientedBB rotateYaw(double targetYaw) {
        if (targetYaw == 0.0D) {
            return this;
        } else {
            double cosYaw = Math.cos(targetYaw * Math.PI / 180.0D);
            double sinYaw = Math.sin(targetYaw * Math.PI / 180.0D);
            double centerX = (this.minX + this.maxX) * 0.5D;
            double centerZ = (this.minZ + this.maxZ) * 0.5D;
            DoubleMatrix centerMatrix = DoubleMatrix.eye(4);
            DoubleMatrix rotationMatrix = DoubleMatrix.eye(4);
            DoubleMatrix centerMatrixInverse = DoubleMatrix.eye(4);
            centerMatrix.data[12] = centerX;
            centerMatrix.data[14] = centerZ;
            centerMatrixInverse.data[12] = -centerX;
            centerMatrixInverse.data[14] = -centerZ;
            rotationMatrix.data[0] = cosYaw;
            rotationMatrix.data[8] = sinYaw;
            rotationMatrix.data[2] = -sinYaw;
            rotationMatrix.data[10] = cosYaw;
            DoubleMatrix fullTransformMatrix = centerMatrix.mmuli(rotationMatrix.muli(centerMatrixInverse));
            fullTransformMatrix.mmuli(this.vertices, this.vertices);

            for (int i = 0; i < 8; ++i) {
                if (i == 0) {
                    this.minX = this.getX(i);
                    this.maxX = this.getX(i);
                    this.minZ = this.getZ(i);
                    this.maxZ = this.getZ(i);
                } else {
                    this.minX = Math.min(this.minX, this.getX(i));
                    this.maxX = Math.max(this.maxX, this.getX(i));
                    this.minZ = Math.min(this.minZ, this.getZ(i));
                    this.maxZ = Math.max(this.maxZ, this.getZ(i));
                }
            }

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

        // Getting planes for every face
        Plane[] planeFaces1 = new Plane[6];
        Plane[] planeFaces2 = new Plane[6];
        for (int i = 0; i < 6; i++) {
            planeFaces1[i] = new Plane(faces1[i][0], faces1[i][1], faces1[i][2]);
            planeFaces2[i] = new Plane(faces2[i][0], faces2[i][1], faces2[i][2]);
        }

        boolean intersects = false;
        for (int i = 0; i < 6; i++) {
            Plane planeFace1 = planeFaces1[i];
            for (int j = 0; j < 6; j++) {
                // Are our planes parallel?
                Line intersectionLine = planeFace1.intersection(planeFaces2[j]);
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
                    Segment[] segments1 = { GeometryHelper.segmentFromPoints(face1[0], face1[1]),
                        GeometryHelper.segmentFromPoints(face1[1], face1[2]),
                        GeometryHelper.segmentFromPoints(face1[2], face1[3]),
                        GeometryHelper.segmentFromPoints(face1[3], face1[0]) };
                    Segment[] segments2 = { GeometryHelper.segmentFromPoints(face2[0], face2[1]),
                        GeometryHelper.segmentFromPoints(face2[1], face2[2]),
                        GeometryHelper.segmentFromPoints(face2[2], face2[3]),
                        GeometryHelper.segmentFromPoints(face2[3], face2[0]) };
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
                            if (intersection != null && GeometryHelper.pointOnSegment(intersection, segments1[k]))
                                intersection1.add(intersectionLine.getAbscissa(intersection));
                        }
                        if (intersectionLine.contains(segments2[k].getStart())
                            && intersectionLine.contains(segments2[k].getEnd())) {
                            intersection2.add(intersectionLine.getAbscissa(segments2[k].getStart()));
                            intersection2.add(intersectionLine.getAbscissa(segments2[k].getEnd()));
                        } else {
                            Vector3D intersection = segments2[k].getLine()
                                .intersection(intersectionLine);
                            if (intersection != null && GeometryHelper.pointOnSegment(intersection, segments2[k]))
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

    public double calculateXOffset(AxisAlignedBB par1AxisAlignedBB, double par2) {
        // Checking for intersection works perfectly here, but it may be too resource-intensive
        if (this.intersectsWith(par1AxisAlignedBB)) {
            double var4;
            double curMaxX;
            int curMaxIndex;
            int[] neighbourIndexes = new int[3];
            int neighbourIndex;
            Line[] corners = {
                new Line(
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ),
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ)),
                new Line(
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ),
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ)),
                new Line(
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ),
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ)),
                new Line(
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ),
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ)) };
            Plane[] walls = {
                new Plane(
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ),
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ),
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ)),
                new Plane(
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ),
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ),
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ)),
                new Plane(
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ),
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ),
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ)),
                new Plane(
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ),
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ),
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ)) };

            if (par2 > 0.0D) {
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
                    || (this.getY(curMaxIndex) <= par1AxisAlignedBB.maxY
                        && this.getY(curMaxIndex) >= par1AxisAlignedBB.minY
                        && this.getZ(curMaxIndex) <= par1AxisAlignedBB.maxZ
                        && this.getZ(curMaxIndex) >= par1AxisAlignedBB.minZ)) {

                    // Either this BB is not rotated around y/z, or
                    // Target AABB contains the smallest x point
                    if (par1AxisAlignedBB.maxX <= curMaxX + 0.75D) {
                        var4 = curMaxX - par1AxisAlignedBB.maxX - 0.01D;
                        if (var4 < par2) {
                            par2 = var4;
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

                    double localPar2 = par2;
                    boolean liesOnEdge = false;
                    for (Line edge : edges) {
                        for (Plane wall : walls) {
                            Vector3D intersection = wall.intersection(edge);
                            if (intersection != null && intersection.getX() >= curMaxX
                                && intersection.getY() <= par1AxisAlignedBB.maxY
                                && intersection.getY() >= par1AxisAlignedBB.minY
                                && intersection.getZ() <= par1AxisAlignedBB.maxZ
                                && intersection.getZ() >= par1AxisAlignedBB.minZ) {
                                liesOnEdge = true;
                                if (par1AxisAlignedBB.maxX <= intersection.getX() + 0.75D) {
                                    var4 = curMaxX - par1AxisAlignedBB.maxX - 0.01D;
                                    if (var4 < localPar2) {
                                        localPar2 = var4;
                                    }
                                }
                            }
                        }
                    }

                    if (liesOnEdge) {
                        if (localPar2 != par2) par2 = localPar2;
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

                        localPar2 = par2;
                        for (Plane face : faces) {
                            for (int i = 0; i < 4; i++) {
                                Vector3D intersection = face.intersection(corners[i]);

                                if (intersection.getX() >= curMaxX
                                    && par1AxisAlignedBB.maxX <= intersection.getX() + 0.75D) {
                                    var4 = intersection.getX() - par1AxisAlignedBB.maxX - 0.01D;
                                    if (var4 < localPar2) {
                                        localPar2 = var4;
                                    }
                                }
                            }
                        }
                        if (localPar2 != par2) par2 = localPar2;
                    }
                }
            }

            if (par2 < 0.0D) {
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
                    || (this.getY(curMaxIndex) <= par1AxisAlignedBB.maxY
                        && this.getY(curMaxIndex) >= par1AxisAlignedBB.minY
                        && this.getZ(curMaxIndex) <= par1AxisAlignedBB.maxZ
                        && this.getZ(curMaxIndex) >= par1AxisAlignedBB.minZ)) {

                    // Either this BB is not rotated around y/z, or
                    // Target AABB contains the biggest x point
                    if (par1AxisAlignedBB.minX >= curMaxX - 0.75D) {
                        var4 = curMaxX - par1AxisAlignedBB.minX + 0.01D;
                        if (var4 > par2) {
                            par2 = var4;
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

                    double localPar2 = par2;
                    boolean liesOnEdge = false;
                    for (Line edge : edges) {
                        for (Plane wall : walls) {
                            Vector3D intersection = wall.intersection(edge);
                            if (intersection != null && intersection.getX() <= curMaxX
                                && intersection.getY() <= par1AxisAlignedBB.maxY
                                && intersection.getY() >= par1AxisAlignedBB.minY
                                && intersection.getZ() <= par1AxisAlignedBB.maxZ
                                && intersection.getZ() >= par1AxisAlignedBB.minZ) {
                                liesOnEdge = true;
                                if (par1AxisAlignedBB.minX >= intersection.getX() - 0.75D) {
                                    var4 = curMaxX - par1AxisAlignedBB.minX + 0.01D;
                                    if (var4 > localPar2) {
                                        localPar2 = var4;
                                    }
                                }
                            }
                        }
                    }

                    if (liesOnEdge) {
                        if (localPar2 != par2) par2 = localPar2;
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

                        localPar2 = par2;
                        for (Plane face : faces) {
                            for (int i = 0; i < 4; i++) {
                                Vector3D intersection = face.intersection(corners[i]);

                                if (intersection.getX() <= curMaxX
                                    && par1AxisAlignedBB.minX >= intersection.getX() - 0.75D) {
                                    var4 = intersection.getX() - par1AxisAlignedBB.minX + 0.01D;
                                    if (var4 > localPar2) {
                                        localPar2 = var4;
                                    }
                                }
                            }
                        }
                        if (localPar2 != par2) par2 = localPar2;
                    }
                }
            }
        }

        return par2;
    }

    public double calculateYOffset(AxisAlignedBB par1AxisAlignedBB, double par2) {
        if (par1AxisAlignedBB.maxX > this.minX && par1AxisAlignedBB.minX < this.maxX
            && par1AxisAlignedBB.maxZ > this.minZ
            && par1AxisAlignedBB.minZ < this.maxZ) {
            double var4;
            double curMaxY;
            int curMaxIndex;
            int[] neighbourIndexes = new int[3];
            int neighbourIndex;
            Line[] corners = {
                new Line(
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ),
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ)),
                new Line(
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ),
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ)),
                new Line(
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ),
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ)),
                new Line(
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ),
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ)) };
            Plane[] walls = {
                new Plane(
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ),
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ),
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ)),
                new Plane(
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ),
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ),
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ)),
                new Plane(
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ),
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ),
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ)),
                new Plane(
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ),
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ),
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ)) };

            if (par2 > 0.0D) {
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
                    || (this.getX(curMaxIndex) <= par1AxisAlignedBB.maxX
                        && this.getX(curMaxIndex) >= par1AxisAlignedBB.minX
                        && this.getZ(curMaxIndex) <= par1AxisAlignedBB.maxZ
                        && this.getZ(curMaxIndex) >= par1AxisAlignedBB.minZ)) {

                    // Either this BB is not rotated around x/z, or
                    // Target AABB contains the lowest point. It shouldn't go above it
                    if (par1AxisAlignedBB.maxY <= curMaxY + 0.75D) {
                        var4 = curMaxY - par1AxisAlignedBB.maxY - 0.01D;
                        if (var4 < par2) {
                            par2 = var4;
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

                    double localPar2 = par2;
                    boolean liesOnEdge = false;
                    for (Line edge : edges) {
                        for (Plane wall : walls) {
                            Vector3D intersection = wall.intersection(edge);
                            if (intersection != null && intersection.getY() >= curMaxY
                                && intersection.getX() <= par1AxisAlignedBB.maxX
                                && intersection.getX() >= par1AxisAlignedBB.minX
                                && intersection.getZ() <= par1AxisAlignedBB.maxZ
                                && intersection.getZ() >= par1AxisAlignedBB.minZ) {
                                liesOnEdge = true;
                                if (par1AxisAlignedBB.maxY <= intersection.getY() + 0.75D) {
                                    var4 = curMaxY - par1AxisAlignedBB.maxY - 0.01D;
                                    if (var4 < localPar2) {
                                        localPar2 = var4;
                                    }
                                }
                            }
                        }
                    }

                    if (liesOnEdge) {
                        if (localPar2 != par2) par2 = localPar2;
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

                        localPar2 = par2;
                        for (Plane face : faces) {
                            for (int i = 0; i < 4; i++) {
                                Vector3D intersection = face.intersection(corners[i]);

                                if (intersection.getY() >= curMaxY
                                    && par1AxisAlignedBB.maxY <= intersection.getY() + 0.75D) {
                                    var4 = intersection.getY() - par1AxisAlignedBB.maxY - 0.01D;
                                    if (var4 < localPar2) {
                                        localPar2 = var4;
                                    }
                                }
                            }
                        }
                        if (localPar2 != par2) par2 = localPar2;
                    }
                }
            }

            if (par2 < 0.0D) {
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
                    || (this.getX(curMaxIndex) <= par1AxisAlignedBB.maxX
                        && this.getX(curMaxIndex) >= par1AxisAlignedBB.minX
                        && this.getZ(curMaxIndex) <= par1AxisAlignedBB.maxZ
                        && this.getZ(curMaxIndex) >= par1AxisAlignedBB.minZ)) {

                    // Either this BB is not rotated around x/z, or
                    // Target AABB contains the highest point. It should stand right on top of it
                    if (par1AxisAlignedBB.minY >= curMaxY - 0.75D) {
                        var4 = curMaxY - par1AxisAlignedBB.minY + 0.01D;
                        if (var4 > par2) {
                            par2 = var4;
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

                    double localPar2 = par2;
                    boolean liesOnEdge = false;
                    for (Line edge : edges) {
                        for (Plane wall : walls) {
                            Vector3D intersection = wall.intersection(edge);
                            if (intersection != null && intersection.getY() <= curMaxY
                                && intersection.getX() <= par1AxisAlignedBB.maxX
                                && intersection.getX() >= par1AxisAlignedBB.minX
                                && intersection.getZ() <= par1AxisAlignedBB.maxZ
                                && intersection.getZ() >= par1AxisAlignedBB.minZ) {
                                liesOnEdge = true;
                                if (par1AxisAlignedBB.minY >= intersection.getY() - 0.75D) {
                                    var4 = curMaxY - par1AxisAlignedBB.minY + 0.01D;
                                    if (var4 > localPar2) {
                                        localPar2 = var4;
                                    }
                                }
                            }
                        }
                    }

                    if (liesOnEdge) {
                        if (localPar2 != par2) par2 = localPar2;
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

                        localPar2 = par2;
                        for (Plane face : faces) {
                            for (int i = 0; i < 4; i++) {
                                Vector3D intersection = face.intersection(corners[i]);

                                if (intersection.getY() <= curMaxY
                                    && par1AxisAlignedBB.minY >= intersection.getY() - 0.75D) {
                                    var4 = intersection.getY() - par1AxisAlignedBB.minY + 0.01D;
                                    if (var4 > localPar2) {
                                        localPar2 = var4;
                                    }
                                }
                            }
                        }
                        if (localPar2 != par2) par2 = localPar2;
                    }
                }
            }
        }

        return par2;
    }

    public double calculateZOffset(AxisAlignedBB par1AxisAlignedBB, double par2) {
        // Checking for intersection works perfectly here, but it may be too resource-intensive
        if (this.intersectsWith(par1AxisAlignedBB)) {
            double var4;
            double curMaxZ;
            int curMaxIndex;
            int[] neighbourIndexes = new int[3];
            int neighbourIndex;
            Line[] corners = {
                new Line(
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ),
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ)),
                new Line(
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ),
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ)),
                new Line(
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ),
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ)),
                new Line(
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ),
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ)) };
            Plane[] walls = {
                new Plane(
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ),
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ),
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ)),
                new Plane(
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ),
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ),
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ)),
                new Plane(
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ),
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ),
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ)),
                new Plane(
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ),
                    new Vector3D(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ),
                    new Vector3D(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ)) };

            if (par2 > 0.0D) {
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
                    || (this.getY(curMaxIndex) <= par1AxisAlignedBB.maxY
                        && this.getY(curMaxIndex) >= par1AxisAlignedBB.minY
                        && this.getX(curMaxIndex) <= par1AxisAlignedBB.maxX
                        && this.getX(curMaxIndex) >= par1AxisAlignedBB.minX)) {

                    // Either this BB is not rotated around y/x, or
                    // Target AABB contains the smallest z point
                    if (par1AxisAlignedBB.maxZ <= curMaxZ + 0.75D) {
                        var4 = curMaxZ - par1AxisAlignedBB.maxZ - 0.01D;
                        if (var4 < par2) {
                            par2 = var4;
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

                    double localPar2 = par2;
                    boolean liesOnEdge = false;
                    for (Line edge : edges) {
                        for (Plane wall : walls) {
                            Vector3D intersection = wall.intersection(edge);
                            if (intersection != null && intersection.getZ() >= curMaxZ
                                && intersection.getY() <= par1AxisAlignedBB.maxY
                                && intersection.getY() >= par1AxisAlignedBB.minY
                                && intersection.getX() <= par1AxisAlignedBB.maxX
                                && intersection.getX() >= par1AxisAlignedBB.minX) {
                                liesOnEdge = true;
                                if (par1AxisAlignedBB.maxZ <= intersection.getZ() + 0.75D) {
                                    var4 = curMaxZ - par1AxisAlignedBB.maxZ - 0.01D;
                                    if (var4 < localPar2) {
                                        localPar2 = var4;
                                    }
                                }
                            }
                        }
                    }

                    if (liesOnEdge) {
                        if (localPar2 != par2) par2 = localPar2;
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

                        localPar2 = par2;
                        for (Plane face : faces) {
                            for (int i = 0; i < 4; i++) {
                                Vector3D intersection = face.intersection(corners[i]);

                                if (intersection.getZ() >= curMaxZ
                                    && par1AxisAlignedBB.maxZ <= intersection.getZ() + 0.75D) {
                                    var4 = intersection.getZ() - par1AxisAlignedBB.maxZ - 0.01D;
                                    if (var4 < localPar2) {
                                        localPar2 = var4;
                                    }
                                }
                            }
                        }
                        if (localPar2 != par2) par2 = localPar2;
                    }
                }
            }

            if (par2 < 0.0D) {
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
                    || (this.getY(curMaxIndex) <= par1AxisAlignedBB.maxY
                        && this.getY(curMaxIndex) >= par1AxisAlignedBB.minY
                        && this.getX(curMaxIndex) <= par1AxisAlignedBB.maxX
                        && this.getX(curMaxIndex) >= par1AxisAlignedBB.minX)) {

                    // Either this BB is not rotated around y/x, or
                    // Target AABB contains the biggest z point
                    if (par1AxisAlignedBB.minZ >= curMaxZ - 0.75D) {
                        var4 = curMaxZ - par1AxisAlignedBB.minZ + 0.01D;
                        if (var4 > par2) {
                            par2 = var4;
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

                    double localPar2 = par2;
                    boolean liesOnEdge = false;
                    for (Line edge : edges) {
                        for (Plane wall : walls) {
                            Vector3D intersection = wall.intersection(edge);
                            if (intersection != null && intersection.getZ() <= curMaxZ
                                && intersection.getY() <= par1AxisAlignedBB.maxY
                                && intersection.getY() >= par1AxisAlignedBB.minY
                                && intersection.getX() <= par1AxisAlignedBB.maxX
                                && intersection.getX() >= par1AxisAlignedBB.minX) {
                                liesOnEdge = true;
                                if (par1AxisAlignedBB.minZ >= intersection.getZ() - 0.75D) {
                                    var4 = curMaxZ - par1AxisAlignedBB.minZ + 0.01D;
                                    if (var4 > localPar2) {
                                        localPar2 = var4;
                                    }
                                }
                            }
                        }
                    }

                    if (liesOnEdge) {
                        if (localPar2 != par2) par2 = localPar2;
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

                        localPar2 = par2;
                        for (Plane face : faces) {
                            for (int i = 0; i < 4; i++) {
                                Vector3D intersection = face.intersection(corners[i]);

                                if (intersection.getZ() <= curMaxZ
                                    && par1AxisAlignedBB.minZ >= intersection.getZ() - 0.75D) {
                                    var4 = intersection.getZ() - par1AxisAlignedBB.minZ + 0.01D;
                                    if (var4 > localPar2) {
                                        localPar2 = var4;
                                    }
                                }
                            }
                        }
                        if (localPar2 != par2) par2 = localPar2;
                    }
                }
            }
        }

        return par2;
    }

    @Override
    public AxisAlignedBB offset(double x0, double x1, double x2) {
        return this.offsetLocal(x0, x1, x2);
    }

    public Vector3D getVertice(int i) {
        return new Vector3D(this.getX(i), this.getY(i), this.getZ(i));
    }
}
