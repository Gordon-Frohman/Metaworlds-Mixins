package su.sergiusonesimus.metaworlds.zmixin.interfaces.network.play.server;

public interface IMixinS18PacketEntityTeleport {

    public int getSubWorldBelowFeetId();

    public byte getTractionLoss();

    public boolean getLosingTraction();

    public byte getSendSubWorldPosFlag();

    public int getXPosOnSubWorld();

    public int getYPosOnSubWorld();

    public int getZPosOnSubWorld();

    public IMixinS18PacketEntityTeleport setSubWorldId(int ID);

    public IMixinS18PacketEntityTeleport setTractionLoss(byte TL);

    public IMixinS18PacketEntityTeleport setLosingTraction(boolean LT);

    public IMixinS18PacketEntityTeleport setSendSubWorldPosFlag(byte SSPF);

    public IMixinS18PacketEntityTeleport setXPosOnSubWorld(int XPDS);

    public IMixinS18PacketEntityTeleport setYPosOnSubWorld(int YPDS);

    public IMixinS18PacketEntityTeleport setZPosOnSubWorld(int ZPDS);

}
