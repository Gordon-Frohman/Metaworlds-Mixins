package su.sergiusonesimus.metaworlds.network.play.client;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import su.sergiusonesimus.metaworlds.world.SubWorldServer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class C04StopDraggingPlayerPacket implements IMessage {

    private int subworldId;

    public C04StopDraggingPlayerPacket() {}

    public C04StopDraggingPlayerPacket(int subworldId) {
        this.subworldId = subworldId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.subworldId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.subworldId);
    }

    public static class Handler implements IMessageHandler<C04StopDraggingPlayerPacket, IMessage> {

        @Override
        public IMessage onMessage(C04StopDraggingPlayerPacket message, MessageContext ctx) {
            if (!ctx.side.isClient()) {
                EntityPlayerMP player = ((NetHandlerPlayServer) ctx.getServerHandler()).playerEntity;
                SubWorldServer subworld = (SubWorldServer) ((IMixinWorld) player.worldObj)
                    .getSubWorld(message.subworldId);
                if (subworld != null) {
                    subworld.playersToDrag.remove(player);
                }
            }
            return null;
        }
    }
}
