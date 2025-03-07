package su.sergiusonesimus.metaworlds.patcher;

import java.net.SocketAddress;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;

import io.netty.channel.Channel;
import io.netty.util.concurrent.GenericFutureListener;
import su.sergiusonesimus.metaworlds.core.CSubWorldProxyPacket;
import su.sergiusonesimus.metaworlds.core.GeneralPacketPipeline;
import su.sergiusonesimus.metaworlds.core.MetaworldsMod;
import su.sergiusonesimus.metaworlds.core.SSubWorldProxyPacket;

public class NetworkManagerSubWorldProxy extends NetworkManager {

    private NetworkManager parentNetworkManager;
    private INetHandler netHandlerProxy;
    private int subWorldID;
    protected boolean clientSide;

    public NetworkManagerSubWorldProxy(NetworkManager originalNetworkManager, int targetSubWorldID,
        boolean isClientSide) {
        super(isClientSide);
        this.clientSide = isClientSide;
        this.parentNetworkManager = originalNetworkManager;
        this.subWorldID = targetSubWorldID;
    }

    public void setNetHandler(INetHandler nethandler) {
        this.netHandlerProxy = nethandler;
    }

    public void scheduleOutboundPacket(Packet packet, GenericFutureListener... p_150725_2_) {
        GeneralPacketPipeline pipeline = MetaworldsMod.instance.networkHandler;

        if (clientSide) {
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

        // this.parentNetworkManager.scheduleOutboundPacket(proxyPacket, p_150725_2_);
    }

    public void processReceivedPackets() {}

    public SocketAddress getRemoteAddress() {
        return null;
    }

    public Channel channel() {
        return this.parentNetworkManager.channel();
    }
}
