package net.tclproject.metaworlds.mixin.interfaces.network.play.client;

public interface IMixinC03PacketPlayer {

    public int getSubWorldBelowFeetId();

    public byte getTractionLoss();

    public boolean getLosingTraction();

    public IMixinC03PacketPlayer setSubWorldBelowFeetId(int ID);

    public IMixinC03PacketPlayer setTractionLoss(byte TL);

    public IMixinC03PacketPlayer setLosingTraction(boolean LT);

    public void setXPosition(double newX);

    public void setYPosition(double newY);

    public void setZPosition(double newZ);

    public void setStance(double newStance);

}
