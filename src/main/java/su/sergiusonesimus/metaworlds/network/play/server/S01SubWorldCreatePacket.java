package su.sergiusonesimus.metaworlds.network.play.server;

import net.minecraftforge.common.DimensionManager;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import su.sergiusonesimus.metaworlds.MetaworldsMod;
import su.sergiusonesimus.metaworlds.api.SubWorldTypeManager;
import su.sergiusonesimus.metaworlds.world.SubWorldServer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class S01SubWorldCreatePacket implements IMessage {

    public Integer subWorldID;
    public Integer subWorldType = 0;
    public S03SubWorldUpdatePacket subworldData = null;

    public S01SubWorldCreatePacket() {}

    public S01SubWorldCreatePacket(Integer subWorldID) {
        this.subWorldID = subWorldID;
        IMixinWorld parentWorld = (IMixinWorld) DimensionManager.getWorld(0);
        SubWorldServer subworld = (SubWorldServer) parentWorld.getSubWorldsMap()
            .get(subWorldID);
        if (subworld != null) {
            subWorldType = SubWorldTypeManager.getTypeID(subworld.getSubWorldType());
            subworldData = subworld.getUpdatePacket(subworld, 1 | 2 | 4 | 8 | 16);
        }
    }

    public S01SubWorldCreatePacket(Integer subWorldID, Integer subWorldType, S03SubWorldUpdatePacket subworldData) {
        this.subWorldID = subWorldID;
        this.subWorldType = subWorldType;
        this.subworldData = subworldData;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.subWorldID = buf.readInt();
        this.subWorldType = buf.readInt();
        if (buf.readBoolean()) {
            this.subworldData = new S03SubWorldUpdatePacket();
            this.subworldData.fromBytes(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.subWorldID);
        buf.writeInt(this.subWorldType);

        if (this.subworldData != null) {
            buf.writeBoolean(true);
            this.subworldData.toBytes(buf);
        } else {
            buf.writeBoolean(false);
        }
    }

    public static class Handler implements IMessageHandler<S01SubWorldCreatePacket, IMessage> {

        @Override
        public IMessage onMessage(S01SubWorldCreatePacket message, MessageContext ctx) {
            if (!ctx.side.isServer()) {
                Integer curSubWorldID = message.subWorldID;
                String curSubWorldType = SubWorldTypeManager.getTypeByID(message.subWorldType);
                SubWorldTypeManager.getSubWorldInfoProvider(curSubWorldType)
                    .create(MetaworldsMod.proxy.getMainWorld(), curSubWorldID);
                if (message.subworldData != null) message.subworldData.executeOnTick();
            }
            return null;
        }
    }
}
