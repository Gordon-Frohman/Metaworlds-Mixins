package net.tclproject.metaworlds.core;

import java.lang.ref.WeakReference;
import java.util.EnumMap;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

@ChannelHandler.Sharable
public class GeneralPacketPipeline extends FMLIndexedMessageToMessageCodec<MetaWorldsPacket> {

    private EnumMap<Side, FMLEmbeddedChannel> channels;

    @Override
    public void encodeInto(ChannelHandlerContext ctx, MetaWorldsPacket msg, ByteBuf target) throws Exception {
        msg.write(ctx, target);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf source, MetaWorldsPacket msg) {
        msg.read(ctx, source);

        INetHandler netHandler = ctx.channel()
            .attr(NetworkRegistry.NET_HANDLER)
            .get();
        msg.execute(
            netHandler,
            FMLCommonHandler.instance()
                .getEffectiveSide(),
            ctx);
    }

    public void initialize(String channelName) {
        this.channels = NetworkRegistry.INSTANCE.newChannel(channelName, this);
    }

    public static final AttributeKey<ThreadLocal<WeakReference<FMLProxyPacket>>> getInboundPacketTrackerAttributeKey() {
        return INBOUNDPACKETTRACKER;
    }

    /**
     * Send this message to everyone.
     * <p/>
     * Adapted from CPW's code in cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
     *
     * @param message The message to send
     */
    public void sendToAll(MetaWorldsPacket message) {
        this.channels.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(FMLOutboundHandler.OutboundTarget.ALL);
        this.channels.get(Side.SERVER)
            .writeAndFlush(message);
    }

    /**
     * Send this message to the specified player.
     * <p/>
     * Adapted from CPW's code in cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
     *
     * @param message The message to send
     * @param player  The player to send it to
     */
    public void sendTo(MetaWorldsPacket message, EntityPlayerMP player) {
        this.channels.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(FMLOutboundHandler.OutboundTarget.PLAYER);
        this.channels.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGETARGS)
            .set(player);
        this.channels.get(Side.SERVER)
            .writeAndFlush(message);
    }

    /**
     * Send this message to everyone within a certain range of a point.
     * <p/>
     * Adapted from CPW's code in cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
     *
     * @param message The message to send
     * @param point   The {@link cpw.mods.fml.common.network.NetworkRegistry.TargetPoint} around which to send
     */
    public void sendToAllAround(MetaWorldsPacket message, NetworkRegistry.TargetPoint point) {
        this.channels.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
        this.channels.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGETARGS)
            .set(point);
        this.channels.get(Side.SERVER)
            .writeAndFlush(message);
    }

    /**
     * Send this message to everyone within the supplied dimension.
     * <p/>
     * Adapted from CPW's code in cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
     *
     * @param message     The message to send
     * @param dimensionId The dimension id to target
     */
    public void sendToDimension(MetaWorldsPacket message, int dimensionId) {
        this.channels.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(FMLOutboundHandler.OutboundTarget.DIMENSION);
        this.channels.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGETARGS)
            .set(dimensionId);
        this.channels.get(Side.SERVER)
            .writeAndFlush(message);
    }

    /**
     * Send this message to the server.
     * <p/>
     * Adapted from CPW's code in cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
     *
     * @param message The message to send
     */
    public void sendToServer(MetaWorldsPacket message) {
        this.channels.get(Side.CLIENT)
            .attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        this.channels.get(Side.CLIENT)
            .writeAndFlush(message);
    }
}
