package net.tclproject.metaworlds.admin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.client.AnvilConverterException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.SaveFormatComparator;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.tclproject.metaworlds.api.IMixinWorld;
import net.tclproject.metaworlds.api.SubWorld;
import net.tclproject.metaworlds.api.WorldInfoSuperClass;
import net.tclproject.metaworlds.compat.packet.MwAdminGuiInitPacket;
import net.tclproject.metaworlds.compat.packet.MwAdminGuiSubWorldInfosPacket;
import net.tclproject.metaworlds.patcher.SubWorldInfoHolder;
import net.tclproject.mysteriumlib.network.MetaMagicNetwork;

import cpw.mods.fml.client.FMLClientHandler;

public class MwAdminContainer extends Container {

    protected EntityPlayerMP player;
    protected List<MwAdminContainer.SaveGameInfo> saveList;
    protected Map<Integer, MwAdminContainer.AdminSubWorldInfo> adminSubWorldInfos;
    public static List<SubWorldImporterThread> importThreads = new ArrayList();

    public MwAdminContainer(EntityPlayer playerPar) {
        this.player = (EntityPlayerMP) playerPar;
    }

    public boolean canInteractWith(EntityPlayer var1) {
        return true;
    }

    public void loadAndSendSaves() {
        this.saveList = new ArrayList();
        if (this.player.mcServer instanceof IntegratedServer) {
            List i$ = null;

            try {
                i$ = this.player.mcServer.getActiveAnvilConverter()
                    .getSaveList();
            } catch (AnvilConverterException var10) {
                var10.printStackTrace();
            }

            Iterator curSaveInfo = i$.iterator();

            while (curSaveInfo.hasNext()) {
                Object subFiles = curSaveInfo.next();
                SaveFormatComparator arr$ = (SaveFormatComparator) subFiles;
                File len$ = new File(
                    FMLClientHandler.instance()
                        .getSavesDir(),
                    arr$.getFileName());
                this.saveList.add(new MwAdminContainer.SaveGameInfo(arr$.getFileName(), len$));
            }
        } else {
            String var11 = DimensionManager.getWorld(0)
                .getSaveHandler()
                .getWorldDirectoryName();
            this.saveList.add(
                new MwAdminContainer.SaveGameInfo(
                    var11,
                    DimensionManager.getWorld(0)
                        .getSaveHandler()
                        .getWorldDirectory()));
        }

        Iterator var12 = this.saveList.iterator();

        while (var12.hasNext()) {
            MwAdminContainer.SaveGameInfo var13 = (MwAdminContainer.SaveGameInfo) var12.next();
            File[] var14 = var13.saveDir.listFiles();
            var13.subWorldsList.add(new MwAdminContainer.SaveGameSubWorldInfo("Main world", ".", 0));
            File[] var15 = var14;
            int var16 = var14.length;

            for (int i$1 = 0; i$1 < var16; ++i$1) {
                File curFile = var15[i$1];
                if (curFile.isDirectory()) {
                    String curFileName = curFile.getName();
                    if (curFileName.matches("^SUBWORLD\\d+$")) {
                        int curSubWorldId = Integer.parseInt(curFileName.substring(8));
                        var13.subWorldsList.add(
                            new MwAdminContainer.SaveGameSubWorldInfo(
                                "SubWorld " + curSubWorldId,
                                curFileName,
                                curSubWorldId));
                    }
                }
            }
        }

        MetaMagicNetwork.dispatcher.sendTo(new MwAdminGuiInitPacket(this.saveList), this.player);
    }

    public void sendSubWorldInfos() {
        Collection subWorldInfos = ((WorldInfoSuperClass) DimensionManager.getWorld(0)
            .getWorldInfo()).getSubWorldInfos();
        this.adminSubWorldInfos = new TreeMap();
        Iterator arr$ = subWorldInfos.iterator();

        while (arr$.hasNext()) {
            SubWorldInfoHolder len$ = (SubWorldInfoHolder) arr$.next();
            this.adminSubWorldInfos.put(Integer.valueOf(len$.subWorldId), new MwAdminContainer.AdminSubWorldInfo(len$));
        }

        WorldServer[] var10 = DimensionManager.getWorlds();
        int var11 = var10.length;

        for (int i$ = 0; i$ < var11; ++i$) {
            WorldServer curDimensionWorld = var10[i$];
            Iterator i$1 = ((IMixinWorld) curDimensionWorld).getSubWorlds()
                .iterator();

            while (i$1.hasNext()) {
                World curSubWorldObj = (World) i$1.next();
                SubWorld curSubWorld = (SubWorld) curSubWorldObj;
                MwAdminContainer.AdminSubWorldInfo curInfo = (MwAdminContainer.AdminSubWorldInfo) this.adminSubWorldInfos
                    .get(Integer.valueOf(curSubWorld.getSubWorldID()));
                if (curInfo == null) {
                    this.adminSubWorldInfos.put(
                        Integer.valueOf(curSubWorld.getSubWorldID()),
                        new MwAdminContainer.AdminSubWorldInfo(
                            curSubWorld.getSubWorldID(),
                            true,
                            curDimensionWorld.provider.dimensionId));
                } else {
                    curInfo.isSpawned = true;
                    curInfo.dimensionId = curDimensionWorld.provider.dimensionId;
                }
            }
        }

        MetaMagicNetwork.dispatcher
            .sendTo(new MwAdminGuiSubWorldInfosPacket(this.adminSubWorldInfos.values()), this.player);
    }

    public void teleportPlayerToSubWorld(int subWorldId) {
        SubWorld subWorld = (SubWorld) ((IMixinWorld) this.player.worldObj).getSubWorld(subWorldId);
        if (subWorld != null) {
            double bbCenterX = (double) (subWorld.getMaxX() + subWorld.getMinX()) / 2.0D;
            double bbCenterY = (double) subWorld.getMaxY();
            double bbCenterZ = (double) (subWorld.getMaxZ() + subWorld.getMinZ()) / 2.0D;
            Vec3 transformedPos = subWorld.transformToGlobal(bbCenterX, bbCenterY, bbCenterZ);
            this.player.mountEntity((Entity) null);
            this.player.setPositionAndUpdate(transformedPos.xCoord, transformedPos.yCoord, transformedPos.zCoord);
        }
    }

    public void teleportSubWorldToPlayer(int subWorldId) {
        SubWorld subWorld = (SubWorld) ((IMixinWorld) this.player.worldObj).getSubWorld(subWorldId);
        if (subWorld != null) {
            double bbCenterX = (double) (subWorld.getMaxX() + subWorld.getMinX()) / 2.0D;
            double bbCenterY = (double) subWorld.getMinY();
            double bbCenterZ = (double) (subWorld.getMaxZ() + subWorld.getMinZ()) / 2.0D;
            Vec3 transformedPos = subWorld.transformToGlobal(bbCenterX, bbCenterY, bbCenterZ);
            subWorld.setTranslation(
                subWorld.getTranslationX() + this.player.posX - transformedPos.xCoord,
                subWorld.getTranslationY() + this.player.posY + 2.0D - transformedPos.yCoord,
                subWorld.getTranslationZ() + this.player.posZ - transformedPos.zCoord);
        }
    }

    public void spawnSubWorld(int subWorldId) {
        MwAdminContainer.AdminSubWorldInfo info = (MwAdminContainer.AdminSubWorldInfo) this.adminSubWorldInfos
            .get(Integer.valueOf(subWorldId));
        if (((IMixinWorld) this.player.worldObj).getSubWorld(subWorldId) != null) {
            this.sendSubWorldInfos();
        } else {
            if (info != null && !info.isSpawned) {
                World restoredWorld = ((IMixinWorld) this.player.worldObj).CreateSubWorld(subWorldId);
                info.isSpawned = true;
                info.dimensionId = ((WorldServer) restoredWorld).provider.dimensionId;
                MetaMagicNetwork.dispatcher
                    .sendTo(new MwAdminGuiSubWorldInfosPacket(this.adminSubWorldInfos.values()), this.player);
            }
        }
    }

    public void despawnSubWorld(int subWorldId) {
        MwAdminContainer.AdminSubWorldInfo info = (MwAdminContainer.AdminSubWorldInfo) this.adminSubWorldInfos
            .get(Integer.valueOf(subWorldId));
        if (info != null && info.isSpawned) {
            World targetSubWorld = ((IMixinWorld) DimensionManager.getWorld(info.dimensionId)).getSubWorld(subWorldId);
            if (targetSubWorld != null) {
                ((SubWorld) targetSubWorld).removeSubWorld();
            }

            info.isSpawned = false;
            MetaMagicNetwork.dispatcher
                .sendTo(new MwAdminGuiSubWorldInfosPacket(this.adminSubWorldInfos.values()), this.player);
        }
    }

    public void stopSubWorldMotion(int subWorldId) {
        MwAdminContainer.AdminSubWorldInfo info = (MwAdminContainer.AdminSubWorldInfo) this.adminSubWorldInfos
            .get(Integer.valueOf(subWorldId));
        if (info != null && info.isSpawned) {
            World subWorld = ((IMixinWorld) DimensionManager.getWorld(info.dimensionId)).getSubWorld(subWorldId);
            if (subWorld == null) {
                this.sendSubWorldInfos();
            } else {
                SubWorld subWorldToStop = (SubWorld) subWorld;
                subWorldToStop.setMotion(0.0D, 0.0D, 0.0D);
                subWorldToStop.setRotationYawSpeed(0.0D);
                subWorldToStop.setRotationPitchSpeed(0.0D);
                subWorldToStop.setRotationRollSpeed(0.0D);
                subWorldToStop.setScaleChangeRate(0.0D);
            }
        }
    }

    public void resetSubWorldScale(int subWorldId) {
        MwAdminContainer.AdminSubWorldInfo info = (MwAdminContainer.AdminSubWorldInfo) this.adminSubWorldInfos
            .get(Integer.valueOf(subWorldId));
        if (info != null && info.isSpawned) {
            World subWorld = ((IMixinWorld) DimensionManager.getWorld(info.dimensionId)).getSubWorld(subWorldId);
            if (subWorld == null) {
                this.sendSubWorldInfos();
            } else {
                SubWorld subWorldToStop = (SubWorld) subWorld;
                subWorldToStop.setScaling(1.0D);
            }
        }
    }

    public void importSubWorld(int worldListIndex, int subWorldListIndex) {
        if (worldListIndex >= 0 && worldListIndex < this.saveList.size()) {
            MwAdminContainer.SaveGameInfo saveInfo = (MwAdminContainer.SaveGameInfo) this.saveList.get(worldListIndex);
            if (subWorldListIndex >= 0 && subWorldListIndex < saveInfo.subWorldsList.size()) {
                MwAdminContainer.SaveGameSubWorldInfo subWorldInfo = (MwAdminContainer.SaveGameSubWorldInfo) saveInfo.subWorldsList
                    .get(subWorldListIndex);
                WorldInfo saveWorldInfo = this.player.mcServer.getActiveAnvilConverter()
                    .getWorldInfo(saveInfo.worldFileName);
                SubWorldInfoHolder sourceSubWorldInfo = null;
                if (saveWorldInfo != null && subWorldInfo.subWorldId != 0) {
                    sourceSubWorldInfo = ((WorldInfoSuperClass) saveWorldInfo).getSubWorldInfo(subWorldInfo.subWorldId);
                    if (sourceSubWorldInfo == null) {
                        saveWorldInfo = null;
                    }
                }

                File subWorldDir = new File(saveInfo.saveDir, subWorldInfo.subWorldSaveDirName);
                int newSubWorldId = ((WorldInfoSuperClass) DimensionManager.getWorld(0)
                    .getWorldInfo()).getNextSubWorldID();
                SubWorldImporterThread newImportThread = new SubWorldImporterThread(
                    newSubWorldId,
                    saveInfo.saveDir,
                    subWorldDir,
                    saveWorldInfo,
                    sourceSubWorldInfo);
                newImportThread.start();
            }
        }
    }

    public static class AdminSubWorldInfo implements Comparable {

        public int subWorldId;
        public boolean isSpawned;
        public int dimensionId;

        public AdminSubWorldInfo(int parSubWorldId, boolean parIsSpawned, int parDimensionId) {
            this.subWorldId = parSubWorldId;
            this.isSpawned = parIsSpawned;
            this.dimensionId = parDimensionId;
        }

        public AdminSubWorldInfo(SubWorldInfoHolder parInfoHolder) {
            this(parInfoHolder.subWorldId, false, 0);
        }

        public int compareTo(Object o) {
            return this.subWorldId - ((MwAdminContainer.AdminSubWorldInfo) o).subWorldId;
        }

        public String toString() {
            return "SubWorld " + this.subWorldId;
        }
    }

    public static class SaveGameInfo {

        public String worldFileName;
        public File saveDir;
        public List<MwAdminContainer.SaveGameSubWorldInfo> subWorldsList = new ArrayList();

        public SaveGameInfo(String parFileName, File parSaveDir) {
            this.worldFileName = parFileName;
            this.saveDir = parSaveDir;
        }
    }

    public static class SaveGameSubWorldInfo {

        public String subWorldName;
        public String subWorldSaveDirName;
        public int subWorldId;

        public SaveGameSubWorldInfo(String parSubWorldName, String parSaveDirName, int parSubWorldId) {
            this.subWorldName = parSubWorldName;
            this.subWorldSaveDirName = parSaveDirName;
            this.subWorldId = parSubWorldId;
        }
    }
}
