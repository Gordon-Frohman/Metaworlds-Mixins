package su.sergiusonesimus.metaworlds.patcher;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;

import su.sergiusonesimus.metaworlds.api.IMixinWorld;

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
