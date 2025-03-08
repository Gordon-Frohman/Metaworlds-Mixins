package su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.server;

import java.util.Map;

import net.minecraft.world.World;

public interface IMixinMinecraftServer {

    public Map<Integer, World> getExistingSubWorlds();
}
