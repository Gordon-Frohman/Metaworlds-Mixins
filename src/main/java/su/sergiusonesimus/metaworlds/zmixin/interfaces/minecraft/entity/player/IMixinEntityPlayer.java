package su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.player;

public interface IMixinEntityPlayer {

    public void setSleeping(boolean newState);

    public boolean isOnLadderLocal();

}
