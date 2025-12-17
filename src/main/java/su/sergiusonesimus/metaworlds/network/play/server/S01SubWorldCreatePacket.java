package su.sergiusonesimus.metaworlds.network.play.server;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import su.sergiusonesimus.metaworlds.MetaworldsMod;
import su.sergiusonesimus.metaworlds.api.SubWorldTypeManager;

public class S01SubWorldCreatePacket implements IMessage {

    public int subworldId;
    public int subworldType;
    public S03SubWorldUpdatePacket generationData;

    public S01SubWorldCreatePacket() {}

    public S01SubWorldCreatePacket(int subworldId) {
        this(subworldId, 0, null);
    }

    public S01SubWorldCreatePacket(int subworldId, Integer subworldType) {
        this(subworldId, subworldType, null);
    }

    public S01SubWorldCreatePacket(int subworldId, S03SubWorldUpdatePacket generationData) {
        this(subworldId, 0, generationData);
    }

    public S01SubWorldCreatePacket(int subworldId, int subworldType, S03SubWorldUpdatePacket generationData) {
        this.subworldId = subworldId;
        this.subworldType = subworldType;
        this.generationData = generationData;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.subworldId);
        buf.writeInt(this.subworldType);
        if (generationData != null) {
            buf.writeBoolean(true);
            generationData.toBytes(buf);
        } else buf.writeBoolean(false);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.subworldId = buf.readInt();
        this.subworldType = buf.readInt();
        if (buf.readBoolean()) {
            generationData = new S03SubWorldUpdatePacket();
            generationData.fromBytes(buf);
        }
    }

    public static class Handler implements IMessageHandler<S01SubWorldCreatePacket, IMessage> {

        @Override
        public IMessage onMessage(S01SubWorldCreatePacket message, MessageContext ctx) {
            if (!ctx.side.isServer()) {
                SubWorldTypeManager.getSubWorldInfoProvider(SubWorldTypeManager.getTypeByID(message.subworldType))
                    .create(MetaworldsMod.proxy.getMainWorld(), message.subworldId);
                if (message.generationData != null) message.generationData.executeOnTick();
            }
            return null;
        }
    }
}
