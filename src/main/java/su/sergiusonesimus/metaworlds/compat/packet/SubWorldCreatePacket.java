package su.sergiusonesimus.metaworlds.compat.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class SubWorldCreatePacket implements IMessage {

    public int subWorldsCount;
    public Integer[] subWorldIDs;

    public SubWorldCreatePacket() {}

    public SubWorldCreatePacket(int numSubWorldsToCreate, Integer[] subWorldIDsArray) {
        this.subWorldsCount = numSubWorldsToCreate;
        this.subWorldIDs = subWorldIDsArray;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.subWorldsCount = buf.readInt();
        this.subWorldIDs = new Integer[this.subWorldsCount];

        for (int i = 0; i < this.subWorldsCount; ++i) {
            this.subWorldIDs[i] = Integer.valueOf(buf.readInt());
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.subWorldsCount);
        Integer[] arr$ = this.subWorldIDs;
        int len$ = arr$.length;

        for (int i$ = 0; i$ < len$; ++i$) {
            Integer curSubWorldID = arr$[i$];
            buf.writeInt(curSubWorldID.intValue());
        }
    }

    public static class Handler implements IMessageHandler<SubWorldCreatePacket, IMessage> {

        @Override
        public IMessage onMessage(SubWorldCreatePacket message, MessageContext ctx) {
            if (!ctx.side.isServer()) {
                EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;

                Integer[] arr$ = message.subWorldIDs;
                int len$ = arr$.length;

                for (int i$ = 0; i$ < len$; ++i$) {
                    Integer curSubWorldID = arr$[i$];
                    World newSubWorld = ((IMixinWorld) player.worldObj).createSubWorld(curSubWorldID.intValue());
                }
            }
            return null;
        }
    }
}
