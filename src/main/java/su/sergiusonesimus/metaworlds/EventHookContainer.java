package su.sergiusonesimus.metaworlds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.EntityEvent.CanUpdate;
import net.minecraftforge.event.world.WorldEvent.Load;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.api.SubWorldTypeManager;
import su.sergiusonesimus.metaworlds.entity.player.EntityPlayerProxy;
import su.sergiusonesimus.metaworlds.event.BlockDisplacementEvent;
import su.sergiusonesimus.metaworlds.event.EntityDisplacementEvent;
import su.sergiusonesimus.metaworlds.network.MetaMagicNetwork;
import su.sergiusonesimus.metaworlds.util.RotationHelper;
import su.sergiusonesimus.metaworlds.world.SubWorldInfoHolder;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.player.IMixinEntityPlayer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.storage.IMixinWorldInfo;

public class EventHookContainer {

    private static final ConcurrentHashMap<Integer, ConcurrentLinkedQueue<DelayedTask>> subworldTasks = new ConcurrentHashMap<Integer, ConcurrentLinkedQueue<DelayedTask>>();
    private static int worldTime = 0;

    @SubscribeEvent
    public void executeTasks(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            worldTime++;
            int dimension = event.world.provider.dimensionId;

            ConcurrentLinkedQueue<DelayedTask> tasks = subworldTasks.get(dimension);
            if (tasks != null) {
                Iterator<DelayedTask> it = tasks.iterator();
                while (it.hasNext()) {
                    DelayedTask task = it.next();
                    if (worldTime >= task.scheduledTime) {
                        task.runnable.run();
                        it.remove();
                    }
                }
            }
        }
    }

    public static void scheduleTask(World world, Runnable task, int delayTicks) {
        int dimension = world.provider.dimensionId;
        subworldTasks.computeIfAbsent(dimension, k -> new ConcurrentLinkedQueue<DelayedTask>())
            .add(new DelayedTask(task, worldTime + delayTicks));
    }

    private static class DelayedTask {

        final Runnable runnable;
        final int scheduledTime;

        DelayedTask(Runnable runnable, int scheduledTime) {
            this.runnable = runnable;
            this.scheduledTime = scheduledTime;
        }
    }

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
                            executeSubworldEvents(subworld);
                        }
                    }
                }
            }
        } else {
            executeSubworldEvents(event.world);
        }
    }

    private void executeSubworldEvents(World world) {
        if (world instanceof SubWorld subworld) {
            int subworldId = subworld.getSubWorldID();
            if (subworldEvents.containsKey(subworldId)) {
                for (Consumer<SubWorld> subworldEvent : subworldEvents.get(subworldId)) {
                    subworldEvent.accept((SubWorld) subworld);
                }
                subworldEvents.remove(subworldId);
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

    @SubscribeEvent
    public void displaceSpawnPoint(BlockDisplacementEvent event) {
        if (event.block.isBed(event.world, event.x, event.y, event.z, null)) {
            int dimension = event.world.provider.dimensionId;
            EntityPlayer player;
            Iterator<EntityPlayer> iter = event.world.playerEntities.iterator();
            while (iter.hasNext()) {
                player = iter.next();
                if (player instanceof EntityPlayerProxy proxy) player = proxy.getRealPlayer();
                if (((IMixinEntityPlayer) player).getSpawnWorldID(dimension)
                    == ((IMixinWorld) event.world).getSubWorldID()) {
                    ChunkCoordinates bedLocation = player.getBedLocation(dimension);
                    if (bedLocation != null && bedLocation.posX == event.x
                        && bedLocation.posY == event.y
                        && bedLocation.posZ == event.z) {
                        Vec3 newBedPos = ((IMixinWorld) event.world).transformLocalToOther(
                            event.targetWorld,
                            bedLocation.posX + 0.5D,
                            bedLocation.posY + 0.5D,
                            bedLocation.posZ + 0.5D);
                        int newBedX = MathHelper.floor_double(newBedPos.xCoord);
                        int newBedY = MathHelper.floor_double(newBedPos.yCoord);
                        int newBedZ = MathHelper.floor_double(newBedPos.zCoord);
                        player.setSpawnChunk(
                            new ChunkCoordinates(newBedX, newBedY, newBedZ),
                            player.isSpawnForced(dimension),
                            dimension);
                        ((IMixinEntityPlayer) player)
                            .setSpawnWorldID(dimension, ((IMixinWorld) event.targetWorld).getSubWorldID());
                    }
                }
            }
        }
    }
}
