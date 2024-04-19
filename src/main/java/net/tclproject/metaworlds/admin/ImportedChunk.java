package net.tclproject.metaworlds.admin;

import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.chunk.NibbleArray;

public class ImportedChunk {

    private NBTTagCompound nbtData;
    private Map<Integer, Integer> blockReplacementMap;
    private ImportedExtendedBlockStorage[] aextendedblockstorage = new ImportedExtendedBlockStorage[16];
    public int minX = -1;
    public int minY = -1;
    public int minZ = -1;
    public int maxX = -1;
    public int maxY = -1;
    public int maxZ = -1;

    public ImportedChunk(NBTTagCompound parNBTTagCompound, Map<Integer, Integer> replacementMap) {
        this.nbtData = parNBTTagCompound;
        this.blockReplacementMap = replacementMap;
        this.readFromNBT(parNBTTagCompound);
    }

    private void readFromNBT(NBTTagCompound parNBTTagCompound) {
        boolean flag = parNBTTagCompound.hasKey("SkyLight");
        NBTTagList nbttaglist = parNBTTagCompound.getTagList("Sections", 10);

        for (int k = 0; k < nbttaglist.tagCount(); ++k) {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(k);
            byte b1 = nbttagcompound1.getByte("Y");
            ImportedExtendedBlockStorage extendedblockstorage = new ImportedExtendedBlockStorage(
                b1 << 4,
                flag,
                this.blockReplacementMap);
            extendedblockstorage.setBlockLSBArray(nbttagcompound1.getByteArray("Blocks"));
            if (nbttagcompound1.hasKey("Add", 7)) {
                extendedblockstorage.setBlockMSBArray(new NibbleArray(nbttagcompound1.getByteArray("Add"), 4));
            }

            extendedblockstorage.setBlockMetadataArray(new NibbleArray(nbttagcompound1.getByteArray("Data"), 4));
            extendedblockstorage.setBlocklightArray(new NibbleArray(nbttagcompound1.getByteArray("BlockLight"), 4));
            if (flag) {
                extendedblockstorage.setSkylightArray(new NibbleArray(nbttagcompound1.getByteArray("SkyLight"), 4));
            }

            extendedblockstorage.removeInvalidBlocks();
            this.aextendedblockstorage[b1] = extendedblockstorage;
            if (!extendedblockstorage.isEmpty()) {
                if (this.minX == -1) {
                    this.minX = extendedblockstorage.minX;
                    this.minY = extendedblockstorage.minY + extendedblockstorage.getYLocation();
                    this.minZ = extendedblockstorage.minZ;
                    this.maxX = extendedblockstorage.maxX;
                    this.maxY = extendedblockstorage.maxY + extendedblockstorage.getYLocation();
                    this.maxZ = extendedblockstorage.maxZ;
                } else {
                    if (this.minX > extendedblockstorage.minX) {
                        this.minX = extendedblockstorage.minX;
                    }

                    if (this.maxX < extendedblockstorage.maxX) {
                        this.maxX = extendedblockstorage.maxX;
                    }

                    if (this.minY > extendedblockstorage.minY + extendedblockstorage.getYLocation()) {
                        this.minY = extendedblockstorage.minY + extendedblockstorage.getYLocation();
                    }

                    if (this.maxY < extendedblockstorage.maxY + extendedblockstorage.getYLocation()) {
                        this.maxY = extendedblockstorage.maxY + extendedblockstorage.getYLocation();
                    }

                    if (this.minZ > extendedblockstorage.minZ) {
                        this.minZ = extendedblockstorage.minZ;
                    }

                    if (this.maxZ < extendedblockstorage.maxZ) {
                        this.maxZ = extendedblockstorage.maxZ;
                    }
                }
            }
        }
    }

    public void updateNBT() {
        NBTTagList nbttaglist = this.nbtData.getTagList("Sections", 10);

        for (int k = 0; k < nbttaglist.tagCount(); ++k) {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(k);
            byte b1 = nbttagcompound1.getByte("Y");
            ImportedExtendedBlockStorage extendedblockstorage = this.aextendedblockstorage[b1];
            if (extendedblockstorage == null || extendedblockstorage.isEmpty()) {
                nbttaglist.removeTag(k--);
            }

            nbttagcompound1.removeTag("Blocks");
            nbttagcompound1.setByteArray("Blocks", extendedblockstorage.getBlockLSBArray());
            nbttagcompound1.removeTag("Add");
            if (extendedblockstorage.getBlockMSBArray() != null) {
                nbttagcompound1.setByteArray("Add", extendedblockstorage.getBlockMSBArray().data);
            }

            nbttagcompound1.removeTag("Data");
            nbttagcompound1.setByteArray("Data", extendedblockstorage.getMetadataArray().data);
        }
    }
}
