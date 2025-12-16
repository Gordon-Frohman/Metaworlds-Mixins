package su.sergiusonesimus.metaworlds.network.play.client;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import su.sergiusonesimus.metaworlds.admin.MwAdminContainer;

public class C03MwAdminClientActionPacket implements IMessage {

    private int actionId;
    private int actionParameter;

    public C03MwAdminClientActionPacket() {}

    public C03MwAdminClientActionPacket(int parActionId) {
        this(parActionId, 0);
    }

    public C03MwAdminClientActionPacket(int parActionId, int parActionParameter) {
        this.actionId = parActionId;
        this.actionParameter = parActionParameter;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.actionId = buf.readInt();
        this.actionParameter = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.actionId);
        buf.writeInt(this.actionParameter);
    }

    public static class Handler implements IMessageHandler<C03MwAdminClientActionPacket, IMessage> {

        @Override
        public IMessage onMessage(C03MwAdminClientActionPacket message, MessageContext ctx) {
            if (!ctx.side.isClient()) {
                EntityPlayerMP player = ((NetHandlerPlayServer) ctx.getServerHandler()).playerEntity;
                if (player.openContainer != null && player.openContainer instanceof MwAdminContainer) {
                    if (message.actionId == 1) {
                        ((MwAdminContainer) player.openContainer).sendSubWorldInfos();
                    } else if (message.actionId == 2) {
                        ((MwAdminContainer) player.openContainer).loadAndSendSaves();
                    } else if (message.actionId == 101) {
                        ((MwAdminContainer) player.openContainer).teleportPlayerToSubWorld(message.actionParameter);
                    } else if (message.actionId == 102) {
                        ((MwAdminContainer) player.openContainer).teleportSubWorldToPlayer(message.actionParameter);
                    } else if (message.actionId == 103) {
                        ((MwAdminContainer) player.openContainer).spawnSubWorld(message.actionParameter);
                    } else if (message.actionId == 104) {
                        ((MwAdminContainer) player.openContainer).despawnSubWorld(message.actionParameter);
                    } else if (message.actionId == 105) {
                        ((MwAdminContainer) player.openContainer).stopSubWorldMotion(message.actionParameter);
                    } else if (message.actionId == 106) {
                        ((MwAdminContainer) player.openContainer).resetSubWorldScale(message.actionParameter);
                    } else if (message.actionId == 201) {
                        ((MwAdminContainer) player.openContainer)
                            .importSubWorld(message.actionParameter & 4095, message.actionParameter >> 12);
                    }
                }
            }
            return null;
        }
    }
}
