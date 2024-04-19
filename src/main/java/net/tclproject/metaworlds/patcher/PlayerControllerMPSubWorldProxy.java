package net.tclproject.metaworlds.patcher;

import net.minecraft.client.multiplayer.PlayerControllerMP;

public class PlayerControllerMPSubWorldProxy extends PlayerControllerMP {

    protected PlayerControllerMP realController;

    public PlayerControllerMPSubWorldProxy(PlayerControllerMP originalController,
        EntityClientPlayerMPSubWorldProxy playerProxy) {
        super(playerProxy.getMinecraft(), playerProxy.getNetHandlerProxy());
        this.realController = originalController;
    }
}
