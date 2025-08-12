package su.sergiusonesimus.metaworlds.world.chunk;

import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.util.Direction;

public class DirectionalChunkProvider {

    private LongHashMap chunkMappingYNeg = new LongHashMap();
    private LongHashMap chunkMappingXPos = new LongHashMap();
    private LongHashMap chunkMappingXNeg = new LongHashMap();
    private LongHashMap chunkMappingZPos = new LongHashMap();
    private LongHashMap chunkMappingZNeg = new LongHashMap();

    private World worldObj;

    public DirectionalChunkProvider(World world) {
        this.worldObj = world;
    }

    public DirectionalChunk provideChunk(Direction dir, int x, int y, int z) {
        int coord1;
        int coord2;
        LongHashMap chunkMapping;
        switch (dir) {
            default:
            case UP:
                return null;
            case DOWN:
                coord1 = x >> 4;
                coord2 = z >> 4;
                chunkMapping = this.chunkMappingYNeg;
                break;
            case EAST:
                coord1 = z >> 4;
                coord2 = y >> 4;
                chunkMapping = this.chunkMappingXPos;
                break;
            case WEST:
                coord1 = z >> 4;
                coord2 = y >> 4;
                chunkMapping = this.chunkMappingXNeg;
                break;
            case SOUTH:
                coord1 = x >> 4;
                coord2 = y >> 4;
                chunkMapping = this.chunkMappingZPos;
                break;
            case NORTH:
                coord1 = x >> 4;
                coord2 = y >> 4;
                chunkMapping = this.chunkMappingZNeg;
                break;
        }
        long key = ChunkCoordIntPair.chunkXZ2Int(coord1, coord2);
        DirectionalChunk chunk = (DirectionalChunk) chunkMapping.getValueByKey(key);
        if (chunk == null) {
            chunk = new DirectionalChunk(worldObj, coord1, coord2, dir);
            chunkMapping.add(key, chunk);
        }
        chunk.generateSkylightMap();
        return chunk;
    }

}
