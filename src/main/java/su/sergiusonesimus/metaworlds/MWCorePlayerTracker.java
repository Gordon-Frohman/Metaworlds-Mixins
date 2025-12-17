package su.sergiusonesimus.metaworlds;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.api.SubWorldTypeManager;
import su.sergiusonesimus.metaworlds.network.MetaMagicNetwork;
import su.sergiusonesimus.metaworlds.network.play.server.S02SubWorldDestroyPacket;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class MWCorePlayerTracker {

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event) {
        sendSubWorldCreationPackets(event.player);
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerLoggedOutEvent event) {
        // This only works on dedicated servers
        savePlayerData(event.player);
    }

    public static void savePlayerData(EntityPlayer player) {
        NBTTagCompound entityData = player.getEntityData();
        NBTTagCompound subWorldData = new NBTTagCompound();
        subWorldData.setInteger(
            "WorldBelowFeetId",
            ((IMixinWorld) ((IMixinEntity) player).getWorldBelowFeet()).getSubWorldID());
        Vec3 transformedPos = ((IMixinEntity) player).getLocalPos(((IMixinEntity) player).getWorldBelowFeet());
        subWorldData.setDouble("posXOnSubWorld", transformedPos.xCoord);
        subWorldData.setDouble("posYOnSubWorld", transformedPos.yCoord);
        subWorldData.setDouble("posZOnSubWorld", transformedPos.zCoord);
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

    private void sendSubWorldCreationPackets(EntityPlayer player) {
        IMixinWorld world = (IMixinWorld) player.worldObj;
        for (World subworld : world.getSubWorldsMap()
            .values()) {
            MetaMagicNetwork.dispatcher
                .sendTo(SubWorldTypeManager.getSubWorldCreatePacket((SubWorld) subworld), (EntityPlayerMP) player);
        }
    }
}
