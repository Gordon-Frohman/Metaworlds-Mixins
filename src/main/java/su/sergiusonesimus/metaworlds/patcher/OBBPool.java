package su.sergiusonesimus.metaworlds.patcher;

import net.minecraft.util.AxisAlignedBB;

public class OBBPool {

    public static OrientedBB createOBB(AxisAlignedBB sourceBB) {
        if (sourceBB instanceof OrientedBB) return (OrientedBB) sourceBB;

        OrientedBB var13 = new OrientedBB(
            sourceBB.minX,
            sourceBB.minY,
            sourceBB.minZ,
            sourceBB.maxX,
            sourceBB.maxY,
            sourceBB.maxZ);
        var13.fromAABB(sourceBB);
        return var13;
    }

}
