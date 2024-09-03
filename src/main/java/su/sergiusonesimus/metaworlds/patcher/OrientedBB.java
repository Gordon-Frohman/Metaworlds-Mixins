package su.sergiusonesimus.metaworlds.patcher;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.jblas.DoubleMatrix;

import su.sergiusonesimus.metaworlds.api.IMixinWorld;

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
                return 2 + (prevIndex > 4 ? 4 : 0);
            case 1:
                return 0 + (prevIndex > 4 ? 4 : 0);
            case 2:
                return 3 + (prevIndex > 4 ? 4 : 0);
            case 3:
                return 1 + (prevIndex > 4 ? 4 : 0);
            default:
                return 0;
        }
    }

    public int getCCWNeighbourIndexXZ(int prevIndex) {
        switch (prevIndex % 4) {
            case 0:
                return 1 + (prevIndex > 4 ? 4 : 0);
            case 1:
                return 3 + (prevIndex > 4 ? 4 : 0);
            case 2:
                return 0 + (prevIndex > 4 ? 4 : 0);
            case 3:
                return 2 + (prevIndex > 4 ? 4 : 0);
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
        return par1AxisAlignedBB instanceof OrientedBB ? this.intersectsWithOBB((OrientedBB) par1AxisAlignedBB)
            : (!super.intersectsWith(par1AxisAlignedBB) ? false
                : this.checkIntersectsWithXZdirection2(par1AxisAlignedBB));
    }

    private boolean checkIntersectsWithXZdirection2(AxisAlignedBB par1AxisAlignedBB) {
        double vertex_x = this.getX(2) - this.getX(0);
        double vertex_z = this.getZ(2) - this.getZ(0);
        double dotProduct = (par1AxisAlignedBB.minX - this.getX(0)) * vertex_x
            + (par1AxisAlignedBB.minZ - this.getZ(0)) * vertex_z;
        dotProduct /= this.dimensions.xCoord;
        double curMinProjCoord = dotProduct;
        double curMaxProjCoord = dotProduct;
        dotProduct = (par1AxisAlignedBB.minX - this.getX(0)) * vertex_x
            + (par1AxisAlignedBB.maxZ - this.getZ(0)) * vertex_z;
        dotProduct /= this.dimensions.xCoord;
        curMinProjCoord = Math.min(curMinProjCoord, dotProduct);
        curMaxProjCoord = Math.max(curMaxProjCoord, dotProduct);
        dotProduct = (par1AxisAlignedBB.maxX - this.getX(0)) * vertex_x
            + (par1AxisAlignedBB.minZ - this.getZ(0)) * vertex_z;
        dotProduct /= this.dimensions.xCoord;
        curMinProjCoord = Math.min(curMinProjCoord, dotProduct);
        curMaxProjCoord = Math.max(curMaxProjCoord, dotProduct);
        dotProduct = (par1AxisAlignedBB.maxX - this.getX(0)) * vertex_x
            + (par1AxisAlignedBB.maxZ - this.getZ(0)) * vertex_z;
        dotProduct /= this.dimensions.xCoord;
        curMinProjCoord = Math.min(curMinProjCoord, dotProduct);
        curMaxProjCoord = Math.max(curMaxProjCoord, dotProduct);
        if (curMaxProjCoord > 0.0D && curMinProjCoord < this.dimensions.xCoord) {
            vertex_x = this.getX(1) - this.getX(0);
            vertex_z = this.getZ(1) - this.getZ(0);
            dotProduct = (par1AxisAlignedBB.minX - this.getX(0)) * vertex_x
                + (par1AxisAlignedBB.minZ - this.getZ(0)) * vertex_z;
            dotProduct /= this.dimensions.zCoord;
            curMinProjCoord = dotProduct;
            curMaxProjCoord = dotProduct;
            dotProduct = (par1AxisAlignedBB.minX - this.getX(0)) * vertex_x
                + (par1AxisAlignedBB.maxZ - this.getZ(0)) * vertex_z;
            dotProduct /= this.dimensions.zCoord;
            curMinProjCoord = Math.min(curMinProjCoord, dotProduct);
            curMaxProjCoord = Math.max(curMaxProjCoord, dotProduct);
            dotProduct = (par1AxisAlignedBB.maxX - this.getX(0)) * vertex_x
                + (par1AxisAlignedBB.minZ - this.getZ(0)) * vertex_z;
            dotProduct /= this.dimensions.zCoord;
            curMinProjCoord = Math.min(curMinProjCoord, dotProduct);
            curMaxProjCoord = Math.max(curMaxProjCoord, dotProduct);
            dotProduct = (par1AxisAlignedBB.maxX - this.getX(0)) * vertex_x
                + (par1AxisAlignedBB.maxZ - this.getZ(0)) * vertex_z;
            dotProduct /= this.dimensions.zCoord;
            curMinProjCoord = Math.min(curMinProjCoord, dotProduct);
            curMaxProjCoord = Math.max(curMaxProjCoord, dotProduct);
            return curMaxProjCoord > 0.0D && curMinProjCoord < this.dimensions.zCoord;
        } else {
            return false;
        }
    }

    public boolean intersectsWithOBB(OrientedBB par1OrientedBB) {
        return par1OrientedBB.maxY > this.minY && par1OrientedBB.minY < this.maxY
            ? (!this.checkIntersectsWithXZ_OBB_oneDirection(par1OrientedBB) ? false
                : par1OrientedBB.checkIntersectsWithXZ_OBB_oneDirection(this))
            : false;
    }

    public boolean checkIntersectsWithXZ_OBB_oneDirection(OrientedBB par1OrientedBB) {
        double vertex_x = this.getX(2) - this.getX(0);
        double vertex_z = this.getZ(2) - this.getZ(0);
        double curMinProjCoord = 0.0D;
        double curMaxProjCoord = 0.0D;

        int i;
        double dotProduct;
        for (i = 0; i < 4; ++i) {
            dotProduct = (par1OrientedBB.getX(i) - this.getX(0)) * vertex_x
                + (par1OrientedBB.getZ(i) - this.getZ(0)) * vertex_z;
            dotProduct /= this.dimensions.xCoord;
            if (i == 0) {
                curMinProjCoord = dotProduct;
                curMaxProjCoord = dotProduct;
            } else {
                curMinProjCoord = Math.min(curMinProjCoord, dotProduct);
                curMaxProjCoord = Math.max(curMaxProjCoord, dotProduct);
            }
        }

        if (curMaxProjCoord > 0.0D && curMinProjCoord < this.dimensions.xCoord) {
            vertex_x = this.getX(1) - this.getX(0);
            vertex_z = this.getZ(1) - this.getZ(0);

            for (i = 0; i < 4; ++i) {
                dotProduct = (par1OrientedBB.getX(i) - this.getX(0)) * vertex_x
                    + (par1OrientedBB.getZ(i) - this.getZ(0)) * vertex_z;
                dotProduct /= this.dimensions.zCoord;
                if (i == 0) {
                    curMinProjCoord = dotProduct;
                    curMaxProjCoord = dotProduct;
                } else {
                    curMinProjCoord = Math.min(curMinProjCoord, dotProduct);
                    curMaxProjCoord = Math.max(curMaxProjCoord, dotProduct);
                }
            }

            if (curMaxProjCoord > 0.0D && curMinProjCoord < this.dimensions.zCoord) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isVecInside(Vec3 par1Vec3) {
        return super.isVecInside(par1Vec3);
    }

    public double calculateXOffset(AxisAlignedBB par1AxisAlignedBB, double par2) {
        if (par1AxisAlignedBB.maxY > this.minY && par1AxisAlignedBB.minY < this.maxY
            && par1AxisAlignedBB.maxZ > this.minZ
            && par1AxisAlignedBB.minZ < this.maxZ) {
            double var4;
            double curMaxX;
            int curMaxIndex;
            int neighbourIndex;
            if (par2 > 0.0D) {
                curMaxX = this.getX(0);
                curMaxIndex = 0;

                for (neighbourIndex = 1; neighbourIndex < 4; ++neighbourIndex) {
                    if (this.getX(neighbourIndex) < curMaxX) {
                        curMaxX = this.getX(neighbourIndex);
                        curMaxIndex = neighbourIndex;
                    }
                }

                if (this.getZ(curMaxIndex) <= par1AxisAlignedBB.minZ) {
                    neighbourIndex = this.getCCWNeighbourIndexXZ(curMaxIndex);
                    if (this.getZ(neighbourIndex) != this.getZ(curMaxIndex)) {
                        curMaxX = this.getX(neighbourIndex) + (this.getX(curMaxIndex) - this.getX(neighbourIndex))
                            * (par1AxisAlignedBB.minZ - this.getZ(neighbourIndex))
                            / (this.getZ(curMaxIndex) - this.getZ(neighbourIndex));
                    }
                } else if (this.getZ(curMaxIndex) >= par1AxisAlignedBB.maxZ) {
                    neighbourIndex = this.getCWNeighbourIndexXZ(curMaxIndex);
                    if (this.getZ(neighbourIndex) != this.getZ(curMaxIndex)) {
                        curMaxX = this.getX(neighbourIndex) + (this.getX(curMaxIndex) - this.getX(neighbourIndex))
                            * (par1AxisAlignedBB.maxZ - this.getZ(neighbourIndex))
                            / (this.getZ(curMaxIndex) - this.getZ(neighbourIndex));
                    }
                }

                if (par1AxisAlignedBB.maxX <= curMaxX + 0.75D) {
                    var4 = curMaxX - par1AxisAlignedBB.maxX - 0.01D;
                    if (var4 < par2) {
                        par2 = var4;
                    }
                }
            }

            if (par2 < 0.0D) {
                curMaxX = this.getX(0);
                curMaxIndex = 0;

                for (neighbourIndex = 1; neighbourIndex < 4; ++neighbourIndex) {
                    if (this.getX(neighbourIndex) > curMaxX) {
                        curMaxX = this.getX(neighbourIndex);
                        curMaxIndex = neighbourIndex;
                    }
                }

                if (this.getZ(curMaxIndex) <= par1AxisAlignedBB.minZ) {
                    neighbourIndex = this.getCWNeighbourIndexXZ(curMaxIndex);
                    if (this.getZ(neighbourIndex) != this.getZ(curMaxIndex)) {
                        curMaxX = this.getX(neighbourIndex) + (this.getX(curMaxIndex) - this.getX(neighbourIndex))
                            * (par1AxisAlignedBB.minZ - this.getZ(neighbourIndex))
                            / (this.getZ(curMaxIndex) - this.getZ(neighbourIndex));
                    }
                } else if (this.getZ(curMaxIndex) >= par1AxisAlignedBB.maxZ) {
                    neighbourIndex = this.getCCWNeighbourIndexXZ(curMaxIndex);
                    if (this.getZ(neighbourIndex) != this.getZ(curMaxIndex)) {
                        curMaxX = this.getX(neighbourIndex) + (this.getX(curMaxIndex) - this.getX(neighbourIndex))
                            * (par1AxisAlignedBB.maxZ - this.getZ(neighbourIndex))
                            / (this.getZ(curMaxIndex) - this.getZ(neighbourIndex));
                    }
                }

                if (par1AxisAlignedBB.minX >= curMaxX - 0.75D) {
                    var4 = curMaxX - par1AxisAlignedBB.minX + 0.01D;
                    if (var4 > par2) {
                        par2 = var4;
                    }
                }
            }
        }

        return par2;
    }

    public double calculateYOffset(AxisAlignedBB par1AxisAlignedBB, double par2) {
        if (par1AxisAlignedBB.maxX > this.minX && par1AxisAlignedBB.minX < this.maxX
            && par1AxisAlignedBB.maxZ > this.minZ
            && par1AxisAlignedBB.minZ < this.maxZ
            && this.checkIntersectsWithXZdirection2(par1AxisAlignedBB)) {
            double var4;
            if (par2 > 0.0D && par1AxisAlignedBB.maxY <= this.minY + 0.75D) {
                var4 = this.minY - par1AxisAlignedBB.maxY;
                if (var4 < par2) {
                    par2 = var4;
                }
            }

            if (par2 < 0.0D && par1AxisAlignedBB.minY >= this.maxY - 0.75D) {
                var4 = this.maxY - par1AxisAlignedBB.minY;
                if (var4 > par2) {
                    par2 = var4;
                }
            }

            return par2;
        } else {
            return par2;
        }
    }

    public double calculateZOffset(AxisAlignedBB par1AxisAlignedBB, double par2) {
        if (par1AxisAlignedBB.maxY > this.minY && par1AxisAlignedBB.minY < this.maxY
            && par1AxisAlignedBB.maxX > this.minX
            && par1AxisAlignedBB.minX < this.maxX) {
            double var4;
            double curMaxZ;
            int curMaxIndex;
            int neighbourIndex;
            if (par2 > 0.0D) {
                curMaxZ = this.getZ(0);
                curMaxIndex = 0;

                for (neighbourIndex = 1; neighbourIndex < 4; ++neighbourIndex) {
                    if (this.getZ(neighbourIndex) < curMaxZ) {
                        curMaxZ = this.getZ(neighbourIndex);
                        curMaxIndex = neighbourIndex;
                    }
                }

                if (this.getX(curMaxIndex) <= par1AxisAlignedBB.minX) {
                    neighbourIndex = this.getCWNeighbourIndexXZ(curMaxIndex);
                    if (this.getX(neighbourIndex) != this.getX(curMaxIndex)) {
                        curMaxZ = this.getZ(neighbourIndex) + (this.getZ(curMaxIndex) - this.getZ(neighbourIndex))
                            * (par1AxisAlignedBB.minX - this.getX(neighbourIndex))
                            / (this.getX(curMaxIndex) - this.getX(neighbourIndex));
                    }
                } else if (this.getX(curMaxIndex) >= par1AxisAlignedBB.maxX) {
                    neighbourIndex = this.getCCWNeighbourIndexXZ(curMaxIndex);
                    if (this.getX(neighbourIndex) != this.getX(curMaxIndex)) {
                        curMaxZ = this.getZ(neighbourIndex) + (this.getZ(curMaxIndex) - this.getZ(neighbourIndex))
                            * (par1AxisAlignedBB.maxX - this.getX(neighbourIndex))
                            / (this.getX(curMaxIndex) - this.getX(neighbourIndex));
                    }
                }

                if (par1AxisAlignedBB.maxZ <= curMaxZ + 0.75D) {
                    var4 = curMaxZ - par1AxisAlignedBB.maxZ - 0.01D;
                    if (var4 < par2) {
                        par2 = var4;
                    }
                }
            }

            if (par2 < 0.0D) {
                curMaxZ = this.getZ(0);
                curMaxIndex = 0;

                for (neighbourIndex = 1; neighbourIndex < 4; ++neighbourIndex) {
                    if (this.getZ(neighbourIndex) > curMaxZ) {
                        curMaxZ = this.getZ(neighbourIndex);
                        curMaxIndex = neighbourIndex;
                    }
                }

                if (this.getX(curMaxIndex) <= par1AxisAlignedBB.minX) {
                    neighbourIndex = this.getCCWNeighbourIndexXZ(curMaxIndex);
                    if (this.getX(neighbourIndex) != this.getX(curMaxIndex)) {
                        curMaxZ = this.getZ(neighbourIndex) + (this.getZ(curMaxIndex) - this.getZ(neighbourIndex))
                            * (par1AxisAlignedBB.minX - this.getX(neighbourIndex))
                            / (this.getX(curMaxIndex) - this.getX(neighbourIndex));
                    }
                } else if (this.getX(curMaxIndex) >= par1AxisAlignedBB.maxX) {
                    neighbourIndex = this.getCWNeighbourIndexXZ(curMaxIndex);
                    if (this.getX(neighbourIndex) != this.getX(curMaxIndex)) {
                        curMaxZ = this.getZ(neighbourIndex) + (this.getZ(curMaxIndex) - this.getZ(neighbourIndex))
                            * (par1AxisAlignedBB.maxX - this.getX(neighbourIndex))
                            / (this.getX(curMaxIndex) - this.getX(neighbourIndex));
                    }
                }

                if (par1AxisAlignedBB.minZ >= curMaxZ - 0.75D) {
                    var4 = curMaxZ - par1AxisAlignedBB.minZ + 0.01D;
                    if (var4 > par2) {
                        par2 = var4;
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
}
