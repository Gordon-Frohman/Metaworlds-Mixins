package su.sergiusonesimus.metaworlds.zmixin.interfaces.entity;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.entity.player.EntityPlayerProxy;

public interface IMixinEntity {

    public static final byte tractionLossThreshold = 20;

    World getWorldBelowFeet();

    void setWorldBelowFeet(World var1);

    Vec3 getGlobalPos();

    Vec3 getLocalPos(World var1);

    double getGlobalRotationYaw();

    boolean getIsJumping();

    public HashMap<Integer, EntityPlayerProxy> getPlayerProxyMap();

    public EntityPlayer getProxyPlayer(World subWorld);

    public EntityPlayer getProxyPlayer(int subWorldID);

    public byte getTractionLossTicks();

    public boolean isLosingTraction();

    public void slowlyRemoveWorldBelowFeet();

    public double getTractionFactor();

    public void setTractionTickCount(byte newTickCount);

    public int getServerPosXOnSubWorld();

    public int getServerPosYOnSubWorld();

    public int getServerPosZOnSubWorld();

    public void setServerPosXOnSubWorld(int serverPosXOnSubWorld);

    public void setServerPosYOnSubWorld(int serverPosYOnSubWorld);

    public void setServerPosZOnSubWorld(int serverPosZOnSubWorld);

    public double getDistanceSq(double par1, double par3, double par5, World targetWorld);
}
