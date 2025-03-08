package su.sergiusonesimus.metaworlds.admin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.storage.IMixinWorldInfo;

public class SubWorldImportProgressUpdater {

    private List<SubWorldImporterThread> finishedImports = new ArrayList();

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
        List worldInfo = MwAdminContainer.importThreads;
        SubWorldImporterThread curFinishedImport;
        synchronized (MwAdminContainer.importThreads) {
            ListIterator i$ = MwAdminContainer.importThreads.listIterator();

            while (true) {
                if (!i$.hasNext()) {
                    break;
                }

                curFinishedImport = (SubWorldImporterThread) i$.next();
                if (curFinishedImport.isFinished()) {
                    this.finishedImports.add(curFinishedImport);
                    i$.remove();
                }
            }
        }

        WorldInfo worldInfo1 = DimensionManager.getWorld(0)
            .getWorldInfo();
        Iterator i$1 = this.finishedImports.iterator();

        while (i$1.hasNext()) {
            curFinishedImport = (SubWorldImporterThread) i$1.next();
            ((IMixinWorldInfo) worldInfo1).updateSubWorldInfo(curFinishedImport.targetSubWorldInfo);
        }

        this.finishedImports.clear();
    }
}
