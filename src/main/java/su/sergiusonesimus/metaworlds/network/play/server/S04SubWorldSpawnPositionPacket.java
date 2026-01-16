package su.sergiusonesimus.metaworlds.network.play.server;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import su.sergiusonesimus.metaworlds.MetaworldsMod;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.player.IMixinEntityPlayer;

public class S04SubWorldSpawnPositionPacket implements IMessage {

    public double subworldSpawnX;
    public double subworldSpawnY;
    public double subworldSpawnZ;

    public S04SubWorldSpawnPositionPacket() {}

    public S04SubWorldSpawnPositionPacket(double x, double y, double z) {
        this.subworldSpawnX = x;
        this.subworldSpawnY = y;
        this.subworldSpawnZ = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.subworldSpawnX = buf.readDouble();
        this.subworldSpawnY = buf.readDouble();
        this.subworldSpawnZ = buf.readDouble();

    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(this.subworldSpawnX);
        buf.writeDouble(this.subworldSpawnY);
        buf.writeDouble(this.subworldSpawnZ);
    }

    public static class Handler implements IMessageHandler<S04SubWorldSpawnPositionPacket, IMessage> {

        @Override
        public IMessage onMessage(S04SubWorldSpawnPositionPacket message, MessageContext ctx) {
            if (!ctx.side.isServer()) {
                IMixinEntityPlayer player = (IMixinEntityPlayer) MetaworldsMod.proxy.getClientPlayer();

                player.setCurrentSubworldPosX(message.subworldSpawnX);
                player.setCurrentSubworldPosY(message.subworldSpawnY);
                player.setCurrentSubworldPosZ(message.subworldSpawnZ);
            }
            return null;
        }
    }

}
