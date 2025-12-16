package su.sergiusonesimus.metaworlds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.EntityEvent.CanUpdate;
import net.minecraftforge.event.world.WorldEvent.Load;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.api.SubWorldTypeManager;
import su.sergiusonesimus.metaworlds.event.BlockDisplacementEvent;
import su.sergiusonesimus.metaworlds.event.EntityDisplacementEvent;
import su.sergiusonesimus.metaworlds.network.MetaMagicNetwork;
import su.sergiusonesimus.metaworlds.util.RotationHelper;
import su.sergiusonesimus.metaworlds.world.SubWorldInfoHolder;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.storage.IMixinWorldInfo;

public class EventHookContainer {

    public static Map<Integer, List<Consumer<SubWorld>>> subworldEvents = new HashMap<Integer, List<Consumer<SubWorld>>>();

    public static void registerSubworldEvent(int subworldID, Consumer<SubWorld> event) {
        List<Consumer<SubWorld>> events = subworldEvents.get(subworldID);
        if (events == null) {
            events = new ArrayList<Consumer<SubWorld>>();
            subworldEvents.put(subworldID, events);
        }
        events.add(event);
    }

    @SubscribeEvent
    public void worldLoaded(Load event) {
        if (!event.world.isRemote) {
            if (((IMixinWorld) event.world).isSubWorld()) {
                MetaMagicNetwork.dispatcher.sendToDimension(
                    SubWorldTypeManager.getSubWorldCreatePacket((SubWorld) event.world),
                    event.world.provider.dimensionId);
            } else {
                Collection<Integer> subWorldIDs = ((IMixinWorldInfo) DimensionManager.getWorld(0)
                    .getWorldInfo()).getSubWorldIDs(((WorldServer) event.world).provider.dimensionId);
                if (subWorldIDs != null) {
                    Iterator<Integer> i$ = subWorldIDs.iterator();

                    while (i$.hasNext()) {
                        Integer curSubWorldID = (Integer) i$.next();
                        if (((IMixinWorld) event.world).getSubWorldsMap()
                            .get(curSubWorldID) == null) {
                            int worldID = curSubWorldID.intValue();
                            SubWorldInfoHolder curSubWorldInfo = ((IMixinWorldInfo) DimensionManager.getWorld(0)
                                .getWorldInfo()).getSubWorldInfo(worldID);
                            World subworld = SubWorldTypeManager.getSubWorldInfoProvider(curSubWorldInfo.subWorldType)
                                .create(event.world, worldID);
                            if (subworldEvents.containsKey(curSubWorldID)) {
                                for (Consumer<SubWorld> subworldEvent : subworldEvents.get(curSubWorldID)) {
                                    subworldEvent.accept((SubWorld) subworld);
                                }
                                subworldEvents.remove(curSubWorldID);
                            }
                        }
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

    @SubscribeEvent
    public void onBlockReintegration(BlockDisplacementEvent event) {
        if (event.world instanceof SubWorld && !(event.targetWorld instanceof SubWorld)) {
            event.blockMetadata = RotationHelper.getRotatedMeta(event.world, event.x, event.y, event.z);
            if (event.tileEntity != null) {
                RotationHelper.rotateTileEntity(event.world, event.x, event.y, event.z);
                event.tileEntity = event.world.getTileEntity(event.x, event.y, event.z);
            }
        }
    }

    @SubscribeEvent
    public void onEntityReintegration(EntityDisplacementEvent event) {
        if (event.sourceWorld instanceof SubWorld && !(event.targetWorld instanceof SubWorld)) {
            RotationHelper.rotateEntity(event.sourceWorld, event.entity);
        }
    }
}
