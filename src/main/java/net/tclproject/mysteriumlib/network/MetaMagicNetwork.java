package net.tclproject.mysteriumlib.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import su.sergiusonesimus.metaworlds.compat.packet.*;

public final class MetaMagicNetwork {

    public static final SimpleNetworkWrapper dispatcher = NetworkRegistry.INSTANCE.newSimpleChannel("mwcore");

    public static boolean registered = false;

    public static final void registerPackets() {
        // Registration
        if (!registered) {
            dispatcher.registerMessage(
                ControllerKeyUpdatePacket.Handler.class,
                ControllerKeyUpdatePacket.class,
                2,
                Side.SERVER);
            dispatcher.registerMessage(
                MwAdminClientActionPacket.Handler.class,
                MwAdminClientActionPacket.class,
                3,
                Side.SERVER);
            dispatcher.registerMessage(
                UpdateServerHealthPacket.Handler.class,
                UpdateServerHealthPacket.class,
                9,
                Side.SERVER);

            dispatcher.registerMessage(MwAdminGuiInitPacket.Handler.class, MwAdminGuiInitPacket.class, 4, Side.CLIENT);
            dispatcher.registerMessage(
                MwAdminGuiSubWorldInfosPacket.Handler.class,
                MwAdminGuiSubWorldInfosPacket.class,
                5,
                Side.CLIENT);
            dispatcher.registerMessage(SubWorldCreatePacket.Handler.class, SubWorldCreatePacket.class, 6, Side.CLIENT);
            dispatcher
                .registerMessage(SubWorldDestroyPacket.Handler.class, SubWorldDestroyPacket.class, 7, Side.CLIENT);
            dispatcher.registerMessage(SubWorldUpdatePacket.Handler.class, SubWorldUpdatePacket.class, 8, Side.CLIENT);

            registered = true;
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked", "unused" })
    private static final void registerParallelPacket(Class handlerClass, Class messageClass) {
        dispatcher.registerMessage(handlerClass, messageClass, 0, Side.CLIENT);
        dispatcher.registerMessage(handlerClass, messageClass, 1, Side.SERVER);
    }

}
