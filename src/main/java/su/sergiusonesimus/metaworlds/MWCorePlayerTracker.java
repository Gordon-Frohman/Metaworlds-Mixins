package su.sergiusonesimus.metaworlds;

import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import su.sergiusonesimus.metaworlds.api.SubWorldTypeManager;
import su.sergiusonesimus.metaworlds.network.MetaMagicNetwork;
import su.sergiusonesimus.metaworlds.world.SubWorldServer;
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

    public static void sendSubWorldCreationPackets(EntityPlayer player) {
        IMixinWorld world = (IMixinWorld) player.worldObj;
        for (Map.Entry<Integer, World> entry : world.getSubWorldsMap()
            .entrySet()) {
            SubWorldServer subworld = (SubWorldServer) entry.getValue();
            MetaMagicNetwork.dispatcher
                .sendTo(SubWorldTypeManager.getSubWorldCreatePacket(subworld), (EntityPlayerMP) player);
            MetaMagicNetwork.dispatcher
                .sendTo(subworld.getUpdatePacket(subworld, 1 | 2 | 4 | 8 | 16), (EntityPlayerMP) player);
        }
    }
}
