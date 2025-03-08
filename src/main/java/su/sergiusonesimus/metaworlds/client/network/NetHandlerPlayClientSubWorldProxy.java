package su.sergiusonesimus.metaworlds.client.network;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;

import su.sergiusonesimus.metaworlds.client.MinecraftSubWorldProxy;
import su.sergiusonesimus.metaworlds.client.entity.EntityClientPlayerMPSubWorldProxy;
import su.sergiusonesimus.metaworlds.network.NetworkManagerSubWorldProxy;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class NetHandlerPlayClientSubWorldProxy extends NetHandlerPlayClient {

    public EntityClientPlayerMPSubWorldProxy proxyPlayer;

    public NetHandlerPlayClientSubWorldProxy(MinecraftSubWorldProxy minecraftProxy,
        NetHandlerPlayClient parentNetHandler, WorldClient targetSubWorld) {
        super(
            minecraftProxy,
            null,
            new NetworkManagerSubWorldProxy(
                parentNetHandler.getNetworkManager(),
                ((IMixinWorld) targetSubWorld).getSubWorldID(),
                true));
        this.clientWorldController = targetSubWorld;

        this.currentServerMaxPlayers = parentNetHandler.currentServerMaxPlayers;
    }
}
