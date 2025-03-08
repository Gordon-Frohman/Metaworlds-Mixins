package su.sergiusonesimus.metaworlds.zmixin.interfaces.client.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;

public class NetHandlerPlayClientProvider {

    public static NetHandlerPlayClient getNetHandlerPlayClient(Minecraft par1Minecraft,
        NetworkManager par2NetworkManager, WorldClient targetWorld) {
        NetHandlerPlayClient nhpc = new NetHandlerPlayClient(par1Minecraft, null, par2NetworkManager);
        nhpc.clientWorldController = targetWorld;
        return nhpc;
    }

}
