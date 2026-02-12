package su.sergiusonesimus.metaworlds.network.play.client;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import su.sergiusonesimus.metaworlds.controls.ControllerKeyServerStore;

public class C02ControllerKeyUpdatePacket implements IMessage {

    private boolean upPressed;
    private boolean downPressed;

    private boolean forwardPressed;
    private boolean backwardPressed;
    private boolean leftPressed;
    private boolean rightPressed;

    private boolean rollForwardPressed;
    private boolean rollBackwardPressed;
    private boolean rollLeftPressed;
    private boolean rollRightPressed;

    public C02ControllerKeyUpdatePacket() {}

    public C02ControllerKeyUpdatePacket(boolean isUpPressed, boolean isDownPressed, boolean isForwardPressed,
        boolean isBackwardPressed, boolean isLeftPressed, boolean isRightPressed, boolean isRollForwardPressed,
        boolean isRollBackwardPressed, boolean isRollLeftPressed, boolean isRollRightPressed) {
        this.upPressed = isUpPressed;
        this.downPressed = isDownPressed;

        this.forwardPressed = isForwardPressed;
        this.backwardPressed = isBackwardPressed;
        this.leftPressed = isLeftPressed;
        this.rightPressed = isRightPressed;

        this.rollForwardPressed = isRollForwardPressed;
        this.rollBackwardPressed = isRollBackwardPressed;
        this.rollLeftPressed = isRollLeftPressed;
        this.rollRightPressed = isRollRightPressed;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.upPressed = buf.readBoolean();
        this.downPressed = buf.readBoolean();

        this.forwardPressed = buf.readBoolean();
        this.backwardPressed = buf.readBoolean();
        this.leftPressed = buf.readBoolean();
        this.rightPressed = buf.readBoolean();

        this.rollForwardPressed = buf.readBoolean();
        this.rollBackwardPressed = buf.readBoolean();
        this.rollLeftPressed = buf.readBoolean();
        this.rollRightPressed = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.upPressed);
        buf.writeBoolean(this.downPressed);

        buf.writeBoolean(this.forwardPressed);
        buf.writeBoolean(this.backwardPressed);
        buf.writeBoolean(this.leftPressed);
        buf.writeBoolean(this.rightPressed);

        buf.writeBoolean(this.rollForwardPressed);
        buf.writeBoolean(this.rollBackwardPressed);
        buf.writeBoolean(this.rollLeftPressed);
        buf.writeBoolean(this.rollRightPressed);
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

                keyStore.upPressed = message.upPressed;
                keyStore.downPressed = message.downPressed;

                keyStore.forwardPressed = message.forwardPressed;
                keyStore.backwardPressed = message.backwardPressed;
                keyStore.leftPressed = message.leftPressed;
                keyStore.rightPressed = message.rightPressed;

                keyStore.rollForwardPressed = message.rollForwardPressed;
                keyStore.rollBackwardPressed = message.rollBackwardPressed;
                keyStore.rollLeftPressed = message.rollLeftPressed;
                keyStore.rollRightPressed = message.rollRightPressed;
            }
            return null;
        }
    }
}
