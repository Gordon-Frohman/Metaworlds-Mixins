package su.sergiusonesimus.metaworlds;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.api.SubWorldTypeManager;
import su.sergiusonesimus.metaworlds.api.SubWorldTypeManager.SubWorldInfoProvider;
import su.sergiusonesimus.metaworlds.network.MetaMagicNetwork;
import su.sergiusonesimus.metaworlds.network.play.server.S01SubWorldCreatePacket;
import su.sergiusonesimus.metaworlds.network.play.server.S02SubWorldDestroyPacket;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.player.IMixinEntityPlayer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class MWCorePlayerTracker {

    @SubscribeEvent
    public void onPlayerLogout(PlayerLoggedOutEvent event) {
        savePlayerData(event.player);
    }

    public static void savePlayerData(EntityPlayer player) {
        NBTTagCompound entityData = player.getEntityData();
        NBTTagCompound subWorldData = new NBTTagCompound();
        subWorldData.setInteger(
            "WorldBelowFeetId",
            ((IMixinWorld) ((IMixinEntity) player).getWorldBelowFeet()).getSubWorldID());
        IMixinEntityPlayer entityPlayer = (IMixinEntityPlayer) player;
        subWorldData.setDouble("posXOnSubWorld", entityPlayer.getCurrentSubworldPosX());
        subWorldData.setDouble("posYOnSubWorld", entityPlayer.getCurrentSubworldPosY());
        subWorldData.setDouble("posZOnSubWorld", entityPlayer.getCurrentSubworldPosZ());
        entityData.setTag("SubWorldInfo", subWorldData);
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
        regenerateSubworlds(event.player);
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        regenerateSubworlds(event.player);
    }

    private void regenerateSubworlds(EntityPlayer player) {
        MetaMagicNetwork.dispatcher.sendTo(new S02SubWorldDestroyPacket(-1, (Integer[]) null), (EntityPlayerMP) player);
        sendSubWorldCreationPackets(player);
    }

    public static void sendSubWorldCreationPackets(EntityPlayer player) {
        IMixinWorld world = (IMixinWorld) player.worldObj;
        List<Integer> batchPacket = new ArrayList<Integer>();
        List<SubWorld> separatePackets = new ArrayList<SubWorld>();
        for (Map.Entry<Integer, World> entry : world.getSubWorldsMap()
            .entrySet()) {
            SubWorld subworld = (SubWorld) entry.getValue();
            if (needsSeparatePacket(subworld)) {
                separatePackets.add(subworld);
            } else {
                batchPacket.add(entry.getKey());
            }
        }
        MetaMagicNetwork.dispatcher.sendTo(
            new S01SubWorldCreatePacket(batchPacket.size(), batchPacket.toArray(new Integer[0])),
            (EntityPlayerMP) player);
        for (SubWorld subworld : separatePackets) {
            MetaMagicNetwork.dispatcher
                .sendTo(SubWorldTypeManager.getSubWorldCreatePacket(subworld), (EntityPlayerMP) player);
        }
    }

    private static boolean needsSeparatePacket(SubWorld subworld) {
        SubWorldInfoProvider sip = SubWorldTypeManager.getSubWorldInfoProvider(subworld);
        Class<? extends SubWorldInfoProvider> sipClass = sip.getClass();
        try {
            Method getCreatePacket = sipClass.getMethod("getCreatePacket", SubWorld.class);
            return getCreatePacket.getDeclaringClass() != SubWorldInfoProvider.class;
        } catch (NoSuchMethodException | SecurityException e) {
            return false;
        }
    }
}
