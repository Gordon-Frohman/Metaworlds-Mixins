package su.sergiusonesimus.metaworlds.integrations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import codechicken.multipart.BlockMultipart;
import codechicken.multipart.MultipartHelper;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import su.sergiusonesimus.metaworlds.entity.player.EntityPlayerProxy;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class ForgeMultipartIntegration {

    private static ForgeMultipartIntegration instance = new ForgeMultipartIntegration();
    public static MovingObjectPosition currentMOP;

    public static int getSubworldSpecificEntityId(EntityPlayer player) {
        return player.getEntityId()
            | (player instanceof EntityPlayerProxy ? ((IMixinWorld) player.worldObj).getSubWorldID() << 16 : 0);
    }

    public static boolean isBlockMultipart(Block block) {
        return block instanceof BlockMultipart;
    }

    public static TileMultipart createTileEntityFromNBT(NBTTagCompound nbtTagCompound) {
        return TileMultipart.createFromNBT(nbtTagCompound);
    }

    public static void sendMultipartUpdate(World world, TileEntity tileEntity) {
        if (world.isRemote) return;
        MultipartHelper.sendDescPacket(world, tileEntity);
    }

    public static void preInit() {
        FMLCommonHandler.instance()
            .bus()
            .register(instance);
    }

    private static final Map<Integer, List<DelayedTask>> worldTasks = new HashMap<Integer, List<DelayedTask>>();
    private static int worldTime = 0;

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            worldTime++;
            int dimension = event.world.provider.dimensionId;

            List<DelayedTask> tasks = worldTasks.get(dimension);
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
        worldTasks.computeIfAbsent(dimension, k -> new ArrayList<DelayedTask>())
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

}
