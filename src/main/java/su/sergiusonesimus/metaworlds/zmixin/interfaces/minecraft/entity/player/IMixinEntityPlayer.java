package su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.player;

import java.util.HashMap;

public interface IMixinEntityPlayer {

    public void setSleeping(boolean newState);

    public boolean isOnLadderLocal();

    public HashMap<Integer, Integer> getSpawnSubworldMap();

    public int getSpawnWorldID();

    public int getSpawnWorldID(int dimension);

    public void setSpawnWorldID(int id);

    public void setSpawnWorldID(int dimension, int id);

}
