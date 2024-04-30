package su.sergiusonesimus.metaworlds.patcher;

import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;

public class ChunkPositionSubWorldID extends ChunkPosition {

    public final int subWorldID;

    public ChunkPositionSubWorldID(int par1, int par2, int par3, int newSubWorldID) {
        super(par1, par2, par3);
        this.subWorldID = newSubWorldID;
    }

    public ChunkPositionSubWorldID(Vec3 par1Vec3, int newSubWorldID) {
        this(
            MathHelper.floor_double(par1Vec3.xCoord),
            MathHelper.floor_double(par1Vec3.yCoord),
            MathHelper.floor_double(par1Vec3.zCoord),
            newSubWorldID);
    }

    public boolean equals(Object par1Obj) {
        if (!(par1Obj instanceof ChunkPositionSubWorldID)) {
            return false;
        } else {
            ChunkPositionSubWorldID var2 = (ChunkPositionSubWorldID) par1Obj;
            return var2.chunkPosX == this.chunkPosX && var2.chunkPosY == this.chunkPosY
                && var2.chunkPosZ == this.chunkPosZ
                && var2.subWorldID == this.subWorldID;
        }
    }

    public int hashCode() {
        return this.chunkPosX * 8976890 + this.chunkPosY * 981131 + this.chunkPosZ + this.subWorldID * 1024;
    }
}
