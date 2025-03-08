package su.sergiusonesimus.metaworlds.zmixin.interfaces.network.play.server;

public interface IMixinS14PacketEntity {

    public int getSubWorldBelowFeetId();

    public byte getTractionLoss();

    public boolean getLosingTraction();

    public byte getSendSubWorldPosFlag();

    public byte getXPosDiffOnSubWorld();

    public byte getYPosDiffOnSubWorld();

    public byte getZPosDiffOnSubWorld();

    public IMixinS14PacketEntity setSubWorldBelowFeetID(int ID);

    public IMixinS14PacketEntity setTractionLoss(byte TL);

    public IMixinS14PacketEntity setLosingTraction(boolean LT);

    public IMixinS14PacketEntity setSendSubWorldPosFlag(byte SSPF);

    public IMixinS14PacketEntity setXPosDiffOnSubWorld(byte XPDS);

    public IMixinS14PacketEntity setYPosDiffOnSubWorld(byte YPDS);

    public IMixinS14PacketEntity setZPosDiffOnSubWorld(byte ZPDS);

}
