package su.sergiusonesimus.metaworlds.client.multiplayer;

import net.minecraft.client.multiplayer.PlayerControllerMP;

import su.sergiusonesimus.metaworlds.client.entity.EntityClientPlayerMPSubWorldProxy;

public class PlayerControllerMPSubWorldProxy extends PlayerControllerMP {

    protected PlayerControllerMP realController;

    public PlayerControllerMPSubWorldProxy(PlayerControllerMP originalController,
        EntityClientPlayerMPSubWorldProxy playerProxy) {
        super(playerProxy.getMinecraft(), playerProxy.getNetHandlerProxy());
        this.realController = originalController;
    }
}
