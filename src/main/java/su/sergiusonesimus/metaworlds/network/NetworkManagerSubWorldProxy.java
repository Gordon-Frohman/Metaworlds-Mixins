package su.sergiusonesimus.metaworlds.network;

import java.net.SocketAddress;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;

import io.netty.channel.Channel;
import io.netty.util.concurrent.GenericFutureListener;
import su.sergiusonesimus.metaworlds.GeneralPacketPipeline;
import su.sergiusonesimus.metaworlds.MetaworldsMod;
import su.sergiusonesimus.metaworlds.network.play.client.CSubWorldProxyPacket;
import su.sergiusonesimus.metaworlds.network.play.server.SSubWorldProxyPacket;

public class NetworkManagerSubWorldProxy extends NetworkManager {

    private NetworkManager parentNetworkManager;
    @SuppressWarnings("unused")
    private INetHandler netHandlerProxy;
    private int subWorldID;

    public NetworkManagerSubWorldProxy(NetworkManager originalNetworkManager, int targetSubWorldID,
        boolean isClientSide) {
        super(isClientSide);
        this.parentNetworkManager = originalNetworkManager;
        this.subWorldID = targetSubWorldID;
    }

    public void setNetHandler(INetHandler nethandler) {
        this.netHandlerProxy = nethandler;
    }

    @SuppressWarnings("rawtypes")
    public void scheduleOutboundPacket(Packet packet, GenericFutureListener... p_150725_2_) {
        GeneralPacketPipeline pipeline = MetaworldsMod.instance.networkHandler;

        if (isClientSide) {
            CSubWorldProxyPacket proxyPacket = new CSubWorldProxyPacket(
                this.subWorldID,
                packet,
                this.parentNetworkManager);
            pipeline.sendToServer(proxyPacket);
        } else {
            SSubWorldProxyPacket proxyPacket = new SSubWorldProxyPacket(
                this.subWorldID,
                packet,
                this.parentNetworkManager);
            EntityPlayerMP player = ((NetHandlerPlayServer) this.parentNetworkManager.getNetHandler()).playerEntity;
            pipeline.sendTo(proxyPacket, player);
        }
    }

    public void processReceivedPackets() {}

    public SocketAddress getRemoteAddress() {
        return null;
    }

    public Channel channel() {
        return this.parentNetworkManager.channel();
    }
}
