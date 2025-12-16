package su.sergiusonesimus.metaworlds.network.play.client;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class C01UpdateServerHealthPacket implements IMessage {

    public float health;

    public C01UpdateServerHealthPacket() {}

    public C01UpdateServerHealthPacket(float hlth) {
        this.health = hlth;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.health = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeFloat(this.health);
    }

    public static class Handler implements IMessageHandler<C01UpdateServerHealthPacket, IMessage> {

        @Override
        public IMessage onMessage(C01UpdateServerHealthPacket message, MessageContext ctx) {
            Minecraft.getMinecraft().thePlayer.setHealth(message.health);
            return null;
        }
    }
}
