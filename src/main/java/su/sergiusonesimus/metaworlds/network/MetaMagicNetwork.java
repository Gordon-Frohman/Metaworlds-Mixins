package su.sergiusonesimus.metaworlds.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import su.sergiusonesimus.metaworlds.network.play.client.C01UpdateServerHealthPacket;
import su.sergiusonesimus.metaworlds.network.play.client.C02ControllerKeyUpdatePacket;
import su.sergiusonesimus.metaworlds.network.play.client.C03MwAdminClientActionPacket;
import su.sergiusonesimus.metaworlds.network.play.server.S01SubWorldCreatePacket;
import su.sergiusonesimus.metaworlds.network.play.server.S02SubWorldDestroyPacket;
import su.sergiusonesimus.metaworlds.network.play.server.S03SubWorldUpdatePacket;
import su.sergiusonesimus.metaworlds.network.play.server.S04SubWorldSpawnPositionPacket;
import su.sergiusonesimus.metaworlds.network.play.server.S05MwAdminGuiInitPacket;
import su.sergiusonesimus.metaworlds.network.play.server.S06MwAdminGuiSubWorldInfosPacket;

public final class MetaMagicNetwork {

    public static final SimpleNetworkWrapper dispatcher = NetworkRegistry.INSTANCE.newSimpleChannel("metaworlds");
    private static int packetId = 0;

    public static boolean registered = false;

    public static final void registerPackets() {
        // Registration
        if (!registered) {
            // Client -> Server
            registerPacket(C01UpdateServerHealthPacket.Handler.class, C01UpdateServerHealthPacket.class, Side.SERVER);
            registerPacket(C02ControllerKeyUpdatePacket.Handler.class, C02ControllerKeyUpdatePacket.class, Side.SERVER);
            registerPacket(C03MwAdminClientActionPacket.Handler.class, C03MwAdminClientActionPacket.class, Side.SERVER);

            // Server -> Client
            registerPacket(S01SubWorldCreatePacket.Handler.class, S01SubWorldCreatePacket.class, Side.CLIENT);
            registerPacket(S02SubWorldDestroyPacket.Handler.class, S02SubWorldDestroyPacket.class, Side.CLIENT);
            registerPacket(S03SubWorldUpdatePacket.Handler.class, S03SubWorldUpdatePacket.class, Side.CLIENT);
            registerPacket(
                S04SubWorldSpawnPositionPacket.Handler.class,
                S04SubWorldSpawnPositionPacket.class,
                Side.CLIENT);
            registerPacket(S05MwAdminGuiInitPacket.Handler.class, S05MwAdminGuiInitPacket.class, Side.CLIENT);
            registerPacket(
                S06MwAdminGuiSubWorldInfosPacket.Handler.class,
                S06MwAdminGuiSubWorldInfosPacket.class,
                Side.CLIENT);

            registered = true;
        }
    }

    public static <T extends IMessage> void registerPacket(Class<? extends IMessageHandler<T, IMessage>> handler,
        Class<T> type, Side side) {
        dispatcher.registerMessage(handler, type, packetId++, side);
    }

}
