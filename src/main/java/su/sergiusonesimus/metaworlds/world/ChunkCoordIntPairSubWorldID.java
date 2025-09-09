package su.sergiusonesimus.metaworlds.world;

import net.minecraft.world.ChunkCoordIntPair;

public class ChunkCoordIntPairSubWorldID extends ChunkCoordIntPair implements Comparable<ChunkCoordIntPairSubWorldID> {

    public int chunkSubWorldID;

    public ChunkCoordIntPairSubWorldID(int posX, int posZ, int subWorldID) {
        super(posX, posZ);
        this.chunkSubWorldID = subWorldID;
    }

    public boolean equals(Object par1Obj) {
        if (!(par1Obj instanceof ChunkCoordIntPairSubWorldID)) {
            return false;
        } else {
            ChunkCoordIntPairSubWorldID var2 = (ChunkCoordIntPairSubWorldID) par1Obj;
            return var2.chunkXPos == this.chunkXPos && var2.chunkZPos == this.chunkZPos
                && var2.chunkSubWorldID == this.chunkSubWorldID;
        }
    }

    public int hashCode() {
        long var1 = chunkXZ2Int(this.chunkXPos, this.chunkZPos);
        int var3 = (int) var1;
        int var4 = (int) (var1 >> 32);
        return var3 ^ var4 + Integer.reverse(this.chunkSubWorldID);
    }

    public String toString() {
        return "[" + this.chunkXPos + ", " + this.chunkZPos + "] SubWorldID [" + this.chunkSubWorldID + "]";
    }

    public int compareTo(ChunkCoordIntPairSubWorldID argCoord) {
        return argCoord.chunkSubWorldID != this.chunkSubWorldID ? this.chunkSubWorldID - argCoord.chunkSubWorldID
            : (argCoord.chunkXPos != this.chunkXPos ? argCoord.chunkXPos - this.chunkXPos
                : (argCoord.chunkZPos != this.chunkZPos ? argCoord.chunkZPos - this.chunkZPos : 0));
    }
}
