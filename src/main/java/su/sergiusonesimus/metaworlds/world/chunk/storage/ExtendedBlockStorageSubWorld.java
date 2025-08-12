package su.sergiusonesimus.metaworlds.world.chunk.storage;

import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.util.ForgeDirection;

import su.sergiusonesimus.metaworlds.util.Direction;

public class ExtendedBlockStorageSubWorld extends ExtendedBlockStorage {

    private NibbleArray skylightArrayYNeg;
    private NibbleArray skylightArrayXPos;
    private NibbleArray skylightArrayXNeg;
    private NibbleArray skylightArrayZPos;
    private NibbleArray skylightArrayZNeg;

    public ExtendedBlockStorageSubWorld(int p_i1997_1_, boolean p_i1997_2_) {
        super(p_i1997_1_, p_i1997_2_);

        if (p_i1997_2_) {
            this.skylightArrayYNeg = new NibbleArray(this.getBlockLSBArray().length, 4);
            this.skylightArrayXPos = new NibbleArray(this.getBlockLSBArray().length, 4);
            this.skylightArrayXNeg = new NibbleArray(this.getBlockLSBArray().length, 4);
            this.skylightArrayZPos = new NibbleArray(this.getBlockLSBArray().length, 4);
            this.skylightArrayZNeg = new NibbleArray(this.getBlockLSBArray().length, 4);
        }
    }

    public ExtendedBlockStorageSubWorld(ExtendedBlockStorage ebs) {
        super(ebs.yBase, false);

        this.blockRefCount = ebs.blockRefCount;
        this.tickRefCount = ebs.tickRefCount;
        this.blockLSBArray = ebs.blockLSBArray;
        this.blockMSBArray = ebs.blockMSBArray;
        this.blockMetadataArray = ebs.blockMetadataArray;
        this.blocklightArray = ebs.blocklightArray;
        this.skylightArray = ebs.skylightArray;
        if (this.skylightArray != null) {
            this.skylightArrayYNeg = new NibbleArray(this.getBlockLSBArray().length, 4);
            this.skylightArrayXPos = new NibbleArray(this.getBlockLSBArray().length, 4);
            this.skylightArrayXNeg = new NibbleArray(this.getBlockLSBArray().length, 4);
            this.skylightArrayZPos = new NibbleArray(this.getBlockLSBArray().length, 4);
            this.skylightArrayZNeg = new NibbleArray(this.getBlockLSBArray().length, 4);
        }
    }

    /**
     * Sets the saved Sky-light value in the extended block storage structure.
     */
    public void setExtSkylightValue(Direction dir, int x, int y, int z, int value) {
        this.setExtSkylightValue(dir.toForgeDirection(), x, y, z, value);
    }

    /**
     * Sets the saved Sky-light value in the extended block storage structure.
     */
    public void setExtSkylightValue(ForgeDirection dir, int x, int y, int z, int value) {
        switch (dir) {
            default:
            case UP:
                this.setExtSkylightValue(x, y, z, value);
                break;
            case DOWN:
                this.skylightArrayYNeg.set(x, y, z, value);
                break;
            case EAST:
                this.skylightArrayXPos.set(x, y, z, value);
                break;
            case WEST:
                this.skylightArrayXNeg.set(x, y, z, value);
                break;
            case SOUTH:
                this.skylightArrayZPos.set(x, y, z, value);
                break;
            case NORTH:
                this.skylightArrayZNeg.set(x, y, z, value);
                break;
        }
    }

    /**
     * Gets the saved Sky-light value in the extended block storage structure.
     */
    public int getExtSkylightValue(Direction dir, int x, int y, int z) {
        return this.getExtSkylightValue(dir.toForgeDirection(), x, y, z);
    }

    /**
     * Gets the saved Sky-light value in the extended block storage structure.
     */
    public int getExtSkylightValue(ForgeDirection dir, int x, int y, int z) {
        switch (dir) {
            default:
            case UP:
                return this.getExtSkylightValue(x, y, z);
            case DOWN:
                return this.skylightArrayYNeg.get(x, y, z);
            case EAST:
                return this.skylightArrayXPos.get(x, y, z);
            case WEST:
                return this.skylightArrayXNeg.get(x, y, z);
            case SOUTH:
                return this.skylightArrayZPos.get(x, y, z);
            case NORTH:
                return this.skylightArrayZNeg.get(x, y, z);
        }
    }

    /**
     * Returns the NibbleArray instance containing Sky-light data.
     */
    public NibbleArray getSkylightArray(Direction dir) {
        return this.getSkylightArray(dir.toForgeDirection());
    }

    /**
     * Returns the NibbleArray instance containing Sky-light data.
     */
    public NibbleArray getSkylightArray(ForgeDirection dir) {
        switch (dir) {
            default:
            case UP:
                return this.getSkylightArray();
            case DOWN:
                return this.skylightArrayYNeg;
            case EAST:
                return this.skylightArrayXPos;
            case WEST:
                return this.skylightArrayXNeg;
            case SOUTH:
                return this.skylightArrayZPos;
            case NORTH:
                return this.skylightArrayZNeg;
        }
    }

    /**
     * Sets the NibbleArray instance used for Sky-light values in this particular storage block.
     */
    public void setSkylightArray(ForgeDirection dir, NibbleArray array) {
        switch (dir) {
            default:
            case UP:
                this.setSkylightArray(array);
                break;
            case DOWN:
                this.skylightArrayYNeg = array;
                break;
            case EAST:
                this.skylightArrayXPos = array;
                break;
            case WEST:
                this.skylightArrayXNeg = array;
                break;
            case SOUTH:
                this.skylightArrayZPos = array;
                break;
            case NORTH:
                this.skylightArrayZNeg = array;
                break;
        }
    }

}
