package su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.player;

import java.util.HashMap;

import net.minecraft.util.Vec3;

public interface IMixinEntityPlayer {

    public void setSleeping(boolean newState);

    public boolean isOnLadderLocal();

    public HashMap<Integer, Integer> getSpawnSubworldMap();

    public int getSpawnWorldID();

    public int getSpawnWorldID(int dimension);

    public void setSpawnWorldID(int id);

    public void setSpawnWorldID(int dimension, int id);

    public int getSubworldBelowFeetId();

    public void setSubworldBelowFeetId(int id);

    public double getCurrentSubworldPosX();

    public double getCurrentSubworldPosY();

    public double getCurrentSubworldPosZ();

    public default Vec3 getCurrentSubworldPosition() {
        return Vec3.createVectorHelper(getCurrentSubworldPosX(), getCurrentSubworldPosY(), getCurrentSubworldPosZ());
    }

    public void setCurrentSubworldPosX(double x);

    public void setCurrentSubworldPosY(double y);

    public void setCurrentSubworldPosZ(double z);

    public default void setCurrentSubworldPosition(double x, double y, double z) {
        setCurrentSubworldPosX(x);
        setCurrentSubworldPosY(y);
        setCurrentSubworldPosZ(z);
    }

    public default void setCurrentSubworldPosition(Vec3 pos) {
        setCurrentSubworldPosition(pos.xCoord, pos.yCoord, pos.zCoord);
    }

}
