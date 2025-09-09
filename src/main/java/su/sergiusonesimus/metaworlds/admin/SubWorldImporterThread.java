package su.sergiusonesimus.metaworlds.admin;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.chunk.storage.RegionFile;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;

import org.apache.commons.io.FileUtils;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameData;
import su.sergiusonesimus.metaworlds.world.SubWorldInfoHolder;

public class SubWorldImporterThread extends Thread {

    protected int newSubWorldId;
    protected File sourceWorldDir;
    protected File sourceSubWorldDir;
    protected WorldInfo sourceWorldInfo;
    protected SubWorldInfoHolder sourceSubWorldInfo;
    private Map<Integer, Integer> blockIdReplacementMap = new HashMap<Integer, Integer>();
    private File targetSaveRegion;
    public SubWorldInfoHolder targetSubWorldInfo;
    protected boolean finished = false;

    public SubWorldImporterThread(int parNewSubWorldId, File parSourceWorldDir, File parSourceSubWorldDir,
        WorldInfo parWorldInfo, SubWorldInfoHolder parSubWorldInfo) {
        this.newSubWorldId = parNewSubWorldId;
        this.sourceWorldDir = parSourceWorldDir;
        this.sourceSubWorldDir = parSourceSubWorldDir;
        this.sourceWorldInfo = parWorldInfo;
        this.sourceSubWorldInfo = parSubWorldInfo;
    }

    public void run() {
        synchronized (MwAdminContainer.importThreads) {
            MwAdminContainer.importThreads.add(this);
        }

        File newSubWorldDir1 = new File(
            DimensionManager.getWorld(0)
                .getSaveHandler()
                .getWorldDirectory(),
            "SUBWORLD" + this.newSubWorldId);
        newSubWorldDir1.mkdir();
        this.copySaveFiles(newSubWorldDir1);
        this.generateSubWorldInfo();
        this.finished = true;
    }

    public boolean isFinished() {
        return this.finished;
    }

    private void copySaveFiles(File newSubWorldDir) {
        File sourceSaveData = new File(this.sourceSubWorldDir, "data");
        File sourceSaveRegion = new File(this.sourceSubWorldDir, "region");
        File sourceSaveForcedChunks = new File(this.sourceSubWorldDir, "forcedchunks.dat");
        File targetSaveForcedChunks;
        if (sourceSaveData.exists() && sourceSaveData.isDirectory()) {
            targetSaveForcedChunks = new File(newSubWorldDir, "data");

            try {
                FileUtils.copyDirectory(sourceSaveData, targetSaveForcedChunks);
            } catch (IOException var9) {
                var9.printStackTrace();
            }
        }

        this.targetSaveRegion = new File(newSubWorldDir, "region");
        if (sourceSaveRegion.exists() && sourceSaveRegion.isDirectory()) {
            try {
                FileUtils.copyDirectory(sourceSaveRegion, this.targetSaveRegion);
            } catch (IOException var8) {
                var8.printStackTrace();
            }
        } else {
            this.targetSaveRegion.mkdir();
        }

        if (sourceSaveForcedChunks.exists() && sourceSaveForcedChunks.isFile()) {
            targetSaveForcedChunks = new File(newSubWorldDir, "forcedchunks.dat");

            try {
                FileUtils.copyFile(sourceSaveForcedChunks, targetSaveForcedChunks);
            } catch (IOException var7) {
                var7.printStackTrace();
            }
        }
    }

    private void generateSubWorldInfo() {
        if (this.sourceSubWorldInfo != null) {
            this.targetSubWorldInfo = this.sourceSubWorldInfo.copy();
            this.targetSubWorldInfo.subWorldId = this.newSubWorldId;
        } else {
            this.targetSubWorldInfo = new SubWorldInfoHolder(this.newSubWorldId);
        }

        this.loadBockIdMappings();
        this.loadRegions();
    }

    private void loadBockIdMappings() {
        if (this.sourceWorldInfo != null) {
            NBTTagCompound leveldat;
            try {
                leveldat = CompressedStreamTools
                    .readCompressed(new FileInputStream(new File(this.sourceWorldDir, "level.dat")));
            } catch (Exception var7) {
                try {
                    leveldat = CompressedStreamTools
                        .readCompressed(new FileInputStream(new File(this.sourceWorldDir, "level.dat_old")));
                } catch (Exception var6) {
                    FMLLog.warning("There appears to be a problem loading a save for import.", new Object[0]);
                    return;
                }
            }

            Set<String> nbtKeys = leveldat.func_150296_c();
            Iterator<String> i$ = nbtKeys.iterator();

            while (i$.hasNext()) {
                String curKey = (String) i$.next();
                NBTBase curTag = leveldat.getTag(curKey);
                if (curTag.getId() == 10 && ((NBTTagCompound) curTag).hasKey("ItemData", 9)) {
                    this.addBlockIdMappings(((NBTTagCompound) curTag).getTagList("ItemData", 10));
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void addBlockIdMappings(NBTTagList itemDataTagList) {
        if (itemDataTagList.tagCount() != 0) {
            for (int i = 0; i < itemDataTagList.tagCount(); ++i) {
                NBTTagCompound dataTag = itemDataTagList.getCompoundTagAt(i);
                String itemName = dataTag.getString("K");
                char discriminator = itemName.charAt(0);
                itemName = itemName.substring(1);
                Integer refId = Integer.valueOf(dataTag.getInteger("V"));
                boolean isBlock = discriminator == 1;
                int currId;
                if (isBlock) {
                    currId = GameData.blockRegistry.getId(itemName);
                } else {
                    currId = GameData.itemRegistry.getId(itemName);
                }

                if (isBlock) {
                    if (currId == -1) {
                        this.blockIdReplacementMap.put(refId, Integer.valueOf(0));
                    } else if (refId.intValue() != currId) {
                        this.blockIdReplacementMap.put(refId, Integer.valueOf(currId));
                    }
                }
            }
        }
    }

    private void loadRegions() {
        Pattern regionFilenameMatcher = Pattern.compile("r\\.-?\\d\\.-?\\d\\.mca");
        File[] regionFiles = this.targetSaveRegion.listFiles();
        HashMap<RegionFile, File> regions = new HashMap<RegionFile, File>();
        File[] i$ = regionFiles;
        int curRegion = regionFiles.length;

        for (int splits = 0; splits < curRegion; ++splits) {
            File regionX = i$[splits];
            if (regionX.isFile() && regionFilenameMatcher.matcher(regionX.getName())
                .matches()) {
                regions.put(new RegionFile(regionX), regionX);
            }
        }

        Iterator<Entry<RegionFile, File>> var9 = regions.entrySet()
            .iterator();

        while (var9.hasNext()) {
            Entry<RegionFile, File> var10 = var9.next();
            String[] var11 = ((File) var10.getValue()).getName()
                .split("\\.", -1);
            int var12 = Integer.parseInt(var11[1]);
            int regionZ = Integer.parseInt(var11[1]);
            this.checkRegionChunks((RegionFile) var10.getKey(), var12, regionZ);
        }
    }

    private void checkRegionChunks(RegionFile region, int regionX, int regionZ) {
        for (int chunkInRegionX = 0; chunkInRegionX < 32; ++chunkInRegionX) {
            for (int chunkInRegionZ = 0; chunkInRegionZ < 32; ++chunkInRegionZ) {
                DataInputStream inputStream = region.getChunkDataInputStream(chunkInRegionX, chunkInRegionZ);
                if (inputStream != null) {
                    NBTTagCompound nbttagcompound = null;

                    try {
                        nbttagcompound = CompressedStreamTools.read(inputStream);
                    } catch (IOException var12) {
                        var12.printStackTrace();
                    }

                    if (nbttagcompound != null && nbttagcompound.hasKey("Level", 10)
                        && nbttagcompound.getCompoundTag("Level")
                            .hasKey("Sections", 9)) {
                        ImportedChunk chunk = new ImportedChunk(
                            nbttagcompound.getCompoundTag("Level"),
                            this.blockIdReplacementMap);
                        chunk.updateNBT();
                        this.updateBoundaries(
                            chunk,
                            chunkInRegionX * 16 + regionX * 512,
                            chunkInRegionZ * 16 + regionZ * 512);
                        DataOutputStream outputStream = region.getChunkDataOutputStream(chunkInRegionX, chunkInRegionZ);

                        try {
                            CompressedStreamTools.write(nbttagcompound, outputStream);
                            outputStream.close();
                        } catch (IOException var11) {
                            var11.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private void updateBoundaries(ImportedChunk chunk, int offsetX, int offsetZ) {
        if (chunk.minX != -1) {
            SubWorldInfoHolder info = this.targetSubWorldInfo;
            if (info.minCoordinates.posX >= info.maxCoordinates.posX) {
                info.minCoordinates.posX = chunk.minX + offsetX;
                info.minCoordinates.posY = chunk.minY;
                info.minCoordinates.posZ = chunk.minZ + offsetZ;
                info.maxCoordinates.posX = chunk.maxX + offsetX;
                info.maxCoordinates.posY = chunk.maxY;
                info.maxCoordinates.posZ = chunk.maxZ + offsetZ;
            } else {
                if (info.minCoordinates.posX > chunk.minX + offsetX) {
                    info.minCoordinates.posX = chunk.minX + offsetX;
                }

                if (info.maxCoordinates.posX < chunk.maxX + offsetX) {
                    info.maxCoordinates.posX = chunk.maxX + offsetX;
                }

                if (info.minCoordinates.posY > chunk.minY) {
                    info.minCoordinates.posY = chunk.minY;
                }

                if (info.maxCoordinates.posY < chunk.maxY) {
                    info.maxCoordinates.posY = chunk.maxY;
                }

                if (info.minCoordinates.posZ > chunk.minZ + offsetZ) {
                    info.minCoordinates.posZ = chunk.minZ + offsetZ;
                }

                if (info.maxCoordinates.posZ < chunk.maxZ + offsetZ) {
                    info.maxCoordinates.posZ = chunk.maxZ + offsetZ;
                }
            }
        }
    }
}
