package su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.client.entity;

public interface IMixinEntityClientPlayerMP {

    public double getSubworldSpawnX();

    public double getSubworldSpawnY();

    public double getSubworldSpawnZ();

    public void setSubworldSpawnX(double x);

    public void setSubworldSpawnY(double y);

    public void setSubworldSpawnZ(double z);

}
