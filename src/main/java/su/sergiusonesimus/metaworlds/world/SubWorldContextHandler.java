package su.sergiusonesimus.metaworlds.world;

import java.util.Collection;

import net.minecraft.world.World;

import com.gtnewhorizon.gtnhlib.api.world.WorldContextRegistry;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

/**
 * Lets other mods name a subworld on the wire and resolve it again, without knowing anything about Metaworlds. Every
 * world carries {@link IMixinWorld}, so the whole handler is a delegation to it.
 */
public final class SubWorldContextHandler implements WorldContextRegistry.Handler {

    /** Sub world IDs are ours alone; this keeps them from colliding with another mod's virtual worlds. */
    private static final String NAMESPACE = "metaworlds";

    private SubWorldContextHandler() {}

    /** Must run on both sides: addresses are derived where packets are sent and resolved where they arrive. */
    public static void register() {
        WorldContextRegistry.registerHandler(NAMESPACE, new SubWorldContextHandler());
    }

    @Override
    public int getSubId(World world) {
        return ((IMixinWorld) world).isSubWorld() ? ((IMixinWorld) world).getSubWorldID()
            : WorldContextRegistry.UNKNOWN_SUB_ID;
    }

    @Override
    public World getHostWorld(World world) {
        return ((IMixinWorld) world).isSubWorld() ? ((IMixinWorld) world).getParentWorld() : null;
    }

    @Override
    public Collection<World> getSubWorlds(World hostWorld) {
        return ((IMixinWorld) hostWorld).getSubWorlds();
    }

    @Override
    public World getSubWorld(World hostWorld, int subId) {
        return ((IMixinWorld) hostWorld).getSubWorld(subId);
    }
}
