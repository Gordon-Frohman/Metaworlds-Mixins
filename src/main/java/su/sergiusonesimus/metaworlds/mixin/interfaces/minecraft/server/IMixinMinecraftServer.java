package su.sergiusonesimus.metaworlds.mixin.interfaces.minecraft.server;

import java.util.Map;

import net.minecraft.world.World;

public interface IMixinMinecraftServer {

    public Map<Integer, World> getExistingSubWorlds();
}
