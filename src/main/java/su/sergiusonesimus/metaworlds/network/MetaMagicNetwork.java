package su.sergiusonesimus.metaworlds.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import su.sergiusonesimus.metaworlds.compat.packet.ControllerKeyUpdatePacket;
import su.sergiusonesimus.metaworlds.compat.packet.MwAdminClientActionPacket;
import su.sergiusonesimus.metaworlds.compat.packet.MwAdminGuiInitPacket;
import su.sergiusonesimus.metaworlds.compat.packet.MwAdminGuiSubWorldInfosPacket;
import su.sergiusonesimus.metaworlds.compat.packet.SubWorldCreatePacket;
import su.sergiusonesimus.metaworlds.compat.packet.SubWorldDestroyPacket;
import su.sergiusonesimus.metaworlds.compat.packet.SubWorldSpawnPositionPacket;
import su.sergiusonesimus.metaworlds.compat.packet.SubWorldUpdatePacket;
import su.sergiusonesimus.metaworlds.compat.packet.UpdateServerHealthPacket;

public final class MetaMagicNetwork {

    public static final SimpleNetworkWrapper dispatcher = NetworkRegistry.INSTANCE.newSimpleChannel("metaworlds");
    private static int packetId = 0;

    public static boolean registered = false;

    public static final void registerPackets() {
        // Registration
        if (!registered) {
            // Client -> Server
            registerPacket(ControllerKeyUpdatePacket.Handler.class, ControllerKeyUpdatePacket.class, Side.SERVER);
            registerPacket(MwAdminClientActionPacket.Handler.class, MwAdminClientActionPacket.class, Side.SERVER);
            registerPacket(UpdateServerHealthPacket.Handler.class, UpdateServerHealthPacket.class, Side.SERVER);

            // Server -> Client
            registerPacket(MwAdminGuiInitPacket.Handler.class, MwAdminGuiInitPacket.class, Side.CLIENT);
            registerPacket(
                MwAdminGuiSubWorldInfosPacket.Handler.class,
                MwAdminGuiSubWorldInfosPacket.class,
                Side.CLIENT);
            registerPacket(SubWorldCreatePacket.Handler.class, SubWorldCreatePacket.class, Side.CLIENT);
            registerPacket(SubWorldDestroyPacket.Handler.class, SubWorldDestroyPacket.class, Side.CLIENT);
            registerPacket(SubWorldUpdatePacket.Handler.class, SubWorldUpdatePacket.class, Side.CLIENT);
            registerPacket(SubWorldSpawnPositionPacket.Handler.class, SubWorldSpawnPositionPacket.class, Side.CLIENT);

            registered = true;
        }
    }

    public static <T extends IMessage> void registerPacket(Class<? extends IMessageHandler<T, IMessage>> handler,
        Class<T> type, Side side) {
        dispatcher.registerMessage(handler, type, packetId++, side);
    }

}
