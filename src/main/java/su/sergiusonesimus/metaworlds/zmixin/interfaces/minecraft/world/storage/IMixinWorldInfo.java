package su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.storage;

import java.util.Collection;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.world.SubWorldInfoHolder;

public interface IMixinWorldInfo {

    public int getNextSubWorldID();

    public int getRespawnWorldID();

    public void setRespawnWorldID(int id);

    public Collection<Integer> getSubWorldIDs(int dimId);

    public void updateSubWorldInfo(SubWorld subWorldToUpdate);

    public void updateSubWorldInfo(SubWorldInfoHolder newInfoHolder);

    public void removeSubWorldInfo(SubWorld subWorldToUpdate);

    public SubWorldInfoHolder getSubWorldInfo(int subWorldId);

    public Collection<SubWorldInfoHolder> getSubWorldInfos();

}
