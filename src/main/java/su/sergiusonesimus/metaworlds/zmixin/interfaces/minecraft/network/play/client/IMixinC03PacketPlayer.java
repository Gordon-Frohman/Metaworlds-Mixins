package su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.play.client;

public interface IMixinC03PacketPlayer {

    public int getSubWorldBelowFeetId();

    public byte getTractionLoss();

    public boolean getLosingTraction();

    public IMixinC03PacketPlayer setSubWorldBelowFeetId(int ID);

    public IMixinC03PacketPlayer setTractionLoss(byte TL);

    public IMixinC03PacketPlayer setLosingTraction(boolean LT);

    public IMixinC03PacketPlayer setXPosition(double newX);

    public IMixinC03PacketPlayer setYPosition(double newY);

    public IMixinC03PacketPlayer setZPosition(double newZ);

    public IMixinC03PacketPlayer setStance(double newStance);

    public IMixinC03PacketPlayer setSubWorldXPosition(double newX);

    public IMixinC03PacketPlayer setSubWorldYPosition(double newY);

    public IMixinC03PacketPlayer setSubWorldZPosition(double newZ);

    public double getSubWorldXPosition();

    public double getSubWorldYPosition();

    public double getSubWorldZPosition();

}
