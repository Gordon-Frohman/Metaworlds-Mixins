package su.sergiusonesimus.metaworlds.network.play.client;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import su.sergiusonesimus.metaworlds.controls.ControllerKeyServerStore;

public class C02ControllerKeyUpdatePacket implements IMessage {

    private boolean ctrlDown;

    private boolean sDown;
    private boolean dDown;
    private boolean aDown;
    private boolean wDown;

    private boolean spaceDown;
    private boolean rlDown;
    private boolean rrDown;

    public C02ControllerKeyUpdatePacket() {}

    public C02ControllerKeyUpdatePacket(boolean isCtrlDown, boolean isSDown, boolean isDDown, boolean isADown,
        boolean isWDown, boolean isSpaceDown, boolean isRLDown, boolean isRRDown) {
        this.ctrlDown = isCtrlDown;

        this.sDown = isSDown;
        this.dDown = isDDown;
        this.aDown = isADown;
        this.wDown = isWDown;

        this.spaceDown = isSpaceDown;
        this.rlDown = isRLDown;
        this.rrDown = isRRDown;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.ctrlDown = buf.readBoolean();

        this.sDown = buf.readBoolean();
        this.dDown = buf.readBoolean();
        this.aDown = buf.readBoolean();
        this.wDown = buf.readBoolean();

        this.spaceDown = buf.readBoolean();
        this.rlDown = buf.readBoolean();
        this.rrDown = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.ctrlDown);

        buf.writeBoolean(this.sDown);
        buf.writeBoolean(this.dDown);
        buf.writeBoolean(this.aDown);
        buf.writeBoolean(this.wDown);

        buf.writeBoolean(this.spaceDown);
        buf.writeBoolean(this.rlDown);
        buf.writeBoolean(this.rrDown);
    }

    public static class Handler implements IMessageHandler<C02ControllerKeyUpdatePacket, IMessage> {

        @Override
        public IMessage onMessage(C02ControllerKeyUpdatePacket message, MessageContext ctx) {
            if (!ctx.side.isClient()) {
                EntityPlayerMP player = ((NetHandlerPlayServer) ctx.getServerHandler()).playerEntity;
                ControllerKeyServerStore keyStore = (ControllerKeyServerStore) player.getExtendedProperties("LCTRL");
                if (keyStore == null) {
                    keyStore = new ControllerKeyServerStore();
                    player.registerExtendedProperties("LCTRL", keyStore);
                }

                keyStore.ctrlDown = message.ctrlDown;

                keyStore.sDown = message.sDown;
                keyStore.dDown = message.dDown;
                keyStore.aDown = message.aDown;
                keyStore.wDown = message.wDown;

                keyStore.spaceDown = message.spaceDown;
                keyStore.rlDown = message.rlDown;
                keyStore.rrDown = message.rrDown;
            }
            return null;
        }
    }
}
