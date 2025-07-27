package su.sergiusonesimus.metaworlds.compat.packet;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.DimensionManager;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.api.SubWorldTypeManager;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class SubWorldCreatePacket implements IMessage {

    public int subWorldsCount;
    public Integer[] subWorldIDs;
    public Integer[] subWorldTypes;

    public SubWorldCreatePacket() {}

    public SubWorldCreatePacket(int numSubWorldsToCreate, Integer[] subWorldIDsArray) {
        this.subWorldsCount = numSubWorldsToCreate;
        this.subWorldIDs = subWorldIDsArray;
        this.subWorldTypes = new Integer[subWorldsCount];
        IMixinWorld parentWorld = (IMixinWorld) DimensionManager.getWorld(0);
        for (int i = 0; i < subWorldsCount; i++) {
            SubWorld subworld = (SubWorld) parentWorld.getSubWorld(subWorldIDs[i]);
            subWorldTypes[i] = subworld == null ? 0 : SubWorldTypeManager.getTypeID(subworld.getSubWorldType());
        }
    }

    public SubWorldCreatePacket(int numSubWorldsToCreate, Integer[] subWorldIDsArray, Integer[] subWorldTypesArray) {
        this.subWorldsCount = numSubWorldsToCreate;
        this.subWorldIDs = subWorldIDsArray;
        this.subWorldTypes = subWorldTypesArray;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.subWorldsCount = buf.readInt();
        this.subWorldIDs = new Integer[this.subWorldsCount];
        this.subWorldTypes = new Integer[this.subWorldsCount];

        for (int i = 0; i < this.subWorldsCount; ++i) {
            this.subWorldIDs[i] = Integer.valueOf(buf.readInt());
            this.subWorldTypes[i] = Integer.valueOf(buf.readInt());
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.subWorldsCount);

        for (int i = 0; i < this.subWorldsCount; ++i) {
            buf.writeInt(this.subWorldIDs[i]);
            buf.writeInt(this.subWorldTypes[i]);
        }
    }

    public static class Handler implements IMessageHandler<SubWorldCreatePacket, IMessage> {

        @Override
        public IMessage onMessage(SubWorldCreatePacket message, MessageContext ctx) {
            if (!ctx.side.isServer()) {
                for (int i = 0; i < message.subWorldsCount; ++i) {
                    Integer curSubWorldID = message.subWorldIDs[i];
                    String curSubWorldType = SubWorldTypeManager.getTypeByID(message.subWorldTypes[i]);
                    SubWorldTypeManager.getSubWorldInfoProvider(curSubWorldType)
                        .create(Minecraft.getMinecraft().theWorld, curSubWorldID);
                }
            }
            return null;
        }
    }
}
