package su.sergiusonesimus.metaworlds.core;

import net.minecraft.network.INetHandler;

import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public abstract class MetaWorldsPacket {

    public abstract void read(ChannelHandlerContext ctx, ByteBuf buf);

    public abstract void write(ChannelHandlerContext ctx, ByteBuf buf);

    public abstract void execute(INetHandler netHandler, Side side, ChannelHandlerContext ctx);
}
