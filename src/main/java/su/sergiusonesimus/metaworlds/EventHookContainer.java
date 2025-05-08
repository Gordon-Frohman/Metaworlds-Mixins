package su.sergiusonesimus.metaworlds;

import java.util.Collection;
import java.util.Iterator;

import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.EntityEvent.CanUpdate;
import net.minecraftforge.event.world.WorldEvent.Load;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import su.sergiusonesimus.metaworlds.compat.packet.SubWorldCreatePacket;
import su.sergiusonesimus.metaworlds.network.MetaMagicNetwork;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.storage.IMixinWorldInfo;

public class EventHookContainer {

    @SubscribeEvent
    public void worldLoaded(Load event) {
        if (!event.world.isRemote) {
            if (((IMixinWorld) event.world).isSubWorld()) {
                MetaMagicNetwork.dispatcher.sendToDimension(
                    new SubWorldCreatePacket(
                        1,
                        new Integer[] { Integer.valueOf(((IMixinWorld) event.world).getSubWorldID()) }),
                    event.world.provider.dimensionId);
            } else {
                Collection subWorldIDs = ((IMixinWorldInfo) DimensionManager.getWorld(0)
                    .getWorldInfo()).getSubWorldIDs(((WorldServer) event.world).provider.dimensionId);
                if (subWorldIDs != null) {
                    Iterator i$ = subWorldIDs.iterator();

                    while (i$.hasNext()) {
                        Integer curSubWorldID = (Integer) i$.next();
                        if (((IMixinWorld) event.world).getSubWorldsMap()
                            .get(curSubWorldID) == null)
                            ((IMixinWorld) event.world).createSubWorld(curSubWorldID.intValue());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void canUpdateEntity(CanUpdate event) {
        if (((IMixinWorld) event.entity.worldObj).isSubWorld()) {
            Vec3 transformedPos = ((IMixinEntity) event.entity).getGlobalPos();
            int i = MathHelper.floor_double(transformedPos.xCoord);
            int j = MathHelper.floor_double(transformedPos.zCoord);
            boolean isForced = ((IMixinWorld) event.entity.worldObj).getParentWorld()
                .getPersistentChunks()
                .containsKey(new ChunkCoordIntPair(i >> 4, j >> 4));
            int b0 = isForced ? 0 : 32;
            boolean canUpdate = ((IMixinWorld) event.entity.worldObj).getParentWorld()
                .checkChunksExist(i - b0, 0, j - b0, i + b0, 0, j + b0);
            if (canUpdate) {
                event.canUpdate = true;
            }
        }
    }
}
