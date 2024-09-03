package su.sergiusonesimus.metaworlds.core;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.tclproject.mysteriumlib.network.MetaMagicNetwork;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import su.sergiusonesimus.metaworlds.api.IMixinEntity;
import su.sergiusonesimus.metaworlds.api.IMixinWorld;
import su.sergiusonesimus.metaworlds.compat.packet.SubWorldCreatePacket;
import su.sergiusonesimus.metaworlds.compat.packet.SubWorldDestroyPacket;

public class MWCorePlayerTracker {

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event) {
        NBTTagCompound entityData = event.player.getEntityData();
        if (entityData.hasKey("SubWorldInfo")) {
            NBTTagCompound subWorldData = entityData.getCompoundTag("SubWorldInfo");
            int worldBelowFeetId = subWorldData.getInteger("WorldBelowFeetId");
            World newWorldBelowFeet = ((IMixinWorld) ((IMixinWorld) event.player.worldObj).getParentWorld())
                .getSubWorld(worldBelowFeetId);
            if (worldBelowFeetId != 0 && newWorldBelowFeet != null) {
                double posXOnSubWorld = subWorldData.getDouble("posXOnSubWorld");
                double posYOnSubWorld = subWorldData.getDouble("posYOnSubWorld");
                double posZOnSubWorld = subWorldData.getDouble("posZOnSubWorld");
                ((IMixinEntity) event.player).setWorldBelowFeet(newWorldBelowFeet);
                Vec3 transformedPos = ((IMixinWorld) newWorldBelowFeet)
                    .transformToGlobal(posXOnSubWorld, posYOnSubWorld, posZOnSubWorld);
                event.player.setPositionAndUpdate(transformedPos.xCoord, transformedPos.yCoord, transformedPos.zCoord);
            }
        }

        MetaMagicNetwork.dispatcher.sendTo(
            new SubWorldCreatePacket(
                ((IMixinWorld) event.player.worldObj).getSubWorlds()
                    .size(),
                (Integer[]) ((IMixinWorld) event.player.worldObj).getSubWorldsMap()
                    .keySet()
                    .toArray(new Integer[0])),
            (EntityPlayerMP) event.player);
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerLoggedOutEvent event) {
        NBTTagCompound entityData = event.player.getEntityData();
        NBTTagCompound subWorldData = new NBTTagCompound();
        subWorldData.setInteger(
            "WorldBelowFeetId",
            ((IMixinWorld) ((IMixinEntity) event.player).getWorldBelowFeet()).getSubWorldID());
        Vec3 transformedPos = ((IMixinEntity) event.player)
            .getLocalPos(((IMixinEntity) event.player).getWorldBelowFeet());
        subWorldData.setDouble("posXOnSubWorld", transformedPos.xCoord);
        subWorldData.setDouble("posYOnSubWorld", transformedPos.yCoord);
        subWorldData.setDouble("posZOnSubWorld", transformedPos.zCoord);
        entityData.setTag("SubWorldInfo", subWorldData);
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
        MetaMagicNetwork.dispatcher
            .sendTo(new SubWorldDestroyPacket(-1, (Integer[]) null), (EntityPlayerMP) event.player);
        MetaMagicNetwork.dispatcher.sendTo(
            new SubWorldCreatePacket(
                ((IMixinWorld) event.player.worldObj).getSubWorlds()
                    .size(),
                (Integer[]) ((IMixinWorld) event.player.worldObj).getSubWorldsMap()
                    .keySet()
                    .toArray(new Integer[0])),
            (EntityPlayerMP) event.player);
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        MetaMagicNetwork.dispatcher
            .sendTo(new SubWorldDestroyPacket(-1, (Integer[]) null), (EntityPlayerMP) event.player);
        MetaMagicNetwork.dispatcher.sendTo(
            new SubWorldCreatePacket(
                ((IMixinWorld) event.player.worldObj).getSubWorlds()
                    .size(),
                (Integer[]) ((IMixinWorld) event.player.worldObj).getSubWorldsMap()
                    .keySet()
                    .toArray(new Integer[0])),
            (EntityPlayerMP) event.player);
    }
}
