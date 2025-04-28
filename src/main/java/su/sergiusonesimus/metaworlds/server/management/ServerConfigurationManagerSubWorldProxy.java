package su.sergiusonesimus.metaworlds.server.management;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.WorldServer;

import su.sergiusonesimus.metaworlds.server.MinecraftServerSubWorldProxy;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;

public class ServerConfigurationManagerSubWorldProxy extends ServerConfigurationManager {

    protected MinecraftServerSubWorldProxy mcServerProxy;
    protected WorldServer targetSubWorld;

    public ServerConfigurationManagerSubWorldProxy(MinecraftServerSubWorldProxy par1MinecraftServer,
        WorldServer targetWorld) {
        super(par1MinecraftServer);
        this.mcServerProxy = par1MinecraftServer;
        this.targetSubWorld = targetWorld;
        this.playerEntityList = new ArrayList<EntityPlayerMP>();
        for (EntityPlayer player : targetWorld.playerEntities) {
            this.playerEntityList.add((EntityPlayerMP) player);
        }
    }

    public NBTTagCompound getHostPlayerData() {
        return this.mcServerProxy.getRealServer()
            .getConfigurationManager()
            .getHostPlayerData();
    }

    /**
     * Gets the View Distance.
     */
    public int getViewDistance() {
        return this.mcServerProxy.getRealServer()
            .getConfigurationManager()
            .getViewDistance();
    }

    /**
     * params: srcPlayer,x,y,z,r,dimension. The packet is not sent to the srcPlayer, but all other players within the
     * search radius
     */
    public void sendToAllNearExcept(EntityPlayer player, double x, double y, double z, double radius,
        int dimension, Packet packet) {
        for (int j = 0; j < this.mcServerProxy.getRealServer()
            .getConfigurationManager().playerEntityList.size(); ++j) {
            EntityPlayerMP entityplayermp = (EntityPlayerMP) ((IMixinEntity) (EntityPlayerMP) this.mcServerProxy
                .getRealServer()
                .getConfigurationManager().playerEntityList.get(j)).getProxyPlayer(targetSubWorld);

            if (entityplayermp != player && entityplayermp.dimension == dimension) {
                double d4 = x - entityplayermp.posX;
                double d5 = y - entityplayermp.posY;
                double d6 = z - entityplayermp.posZ;

                if (d4 * d4 + d5 * d5 + d6 * d6 < radius * radius) {
                    entityplayermp.playerNetServerHandler.sendPacket(packet);
                }
            }
        }
    }
}
