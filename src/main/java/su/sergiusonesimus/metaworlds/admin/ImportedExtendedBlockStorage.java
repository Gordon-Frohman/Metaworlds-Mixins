package su.sergiusonesimus.metaworlds.admin;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class ImportedExtendedBlockStorage extends ExtendedBlockStorage {

    public byte minX = -1;
    public byte minY = -1;
    public byte minZ = -1;
    public byte maxX = -1;
    public byte maxY = -1;
    public byte maxZ = -1;
    private Map<Integer, Integer> blockReplacementMap;

    public ImportedExtendedBlockStorage(int par1, boolean par2, Map<Integer, Integer> replacementMap) {
        super(par1, par2);
        this.blockReplacementMap = replacementMap;
    }

    public void removeInvalidBlocks() {
        Integer curReplacement = null;

        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    int blockId = this.getBlockId(i, j, k);
                    if (blockId != 0) {
                        curReplacement = (Integer) this.blockReplacementMap.get(Integer.valueOf(blockId));
                        int newBlockId = blockId;
                        if (curReplacement != null) {
                            newBlockId = curReplacement.intValue();
                        } else if (Block.getBlockById(blockId) == Blocks.air) {
                            newBlockId = 0;
                        }

                        if (newBlockId != blockId) {
                            this.setBlockId(i, j, k, newBlockId);
                            if (newBlockId == 0) {
                                this.setExtBlockMetadata(i, j, k, 0);
                            }
                        }

                        if (newBlockId != 0) {
                            if (this.minX == -1) {
                                this.minX = (byte) i;
                                this.minY = (byte) j;
                                this.minZ = (byte) k;
                                this.maxX = (byte) (i + 1);
                                this.maxY = (byte) (j + 1);
                                this.maxZ = (byte) (k + 1);
                            } else {
                                if (this.minX > i) {
                                    this.minX = (byte) i;
                                } else if (this.maxX < i + 1) {
                                    this.maxX = (byte) (i + 1);
                                }

                                if (this.minY > j) {
                                    this.minY = (byte) j;
                                } else if (this.maxY < j + 1) {
                                    this.maxY = (byte) (j + 1);
                                }

                                if (this.minZ > k) {
                                    this.minZ = (byte) k;
                                } else if (this.maxZ < k + 1) {
                                    this.maxZ = (byte) (k + 1);
                                }
                            }
                        }
                    }
                }
            }
        }

        super.removeInvalidBlocks();
    }

    public int getBlockId(int p_150819_1_, int p_150819_2_, int p_150819_3_) {
        int l = this.getBlockLSBArray()[p_150819_2_ << 8 | p_150819_3_ << 4 | p_150819_1_] & 255;
        if (this.getBlockMSBArray() != null) {
            l |= this.getBlockMSBArray()
                .get(p_150819_1_, p_150819_2_, p_150819_3_) << 8;
        }

        return l;
    }

    public void setBlockId(int p_150818_1_, int p_150818_2_, int p_150818_3_, int i1) {
        this.getBlockLSBArray()[p_150818_2_ << 8 | p_150818_3_ << 4 | p_150818_1_] = (byte) (i1 & 255);
        if (i1 > 255) {
            if (this.getBlockMSBArray() == null) {
                this.setBlockMSBArray(new NibbleArray(this.getBlockLSBArray().length, 4));
            }

            this.getBlockMSBArray()
                .set(p_150818_1_, p_150818_2_, p_150818_3_, (i1 & 3840) >> 8);
        } else if (this.getBlockMSBArray() != null) {
            this.getBlockMSBArray()
                .set(p_150818_1_, p_150818_2_, p_150818_3_, 0);
        }
    }
}
