package su.sergiusonesimus.metaworlds.api;

import java.util.Collection;

import su.sergiusonesimus.metaworlds.patcher.SubWorldInfoHolder;

public interface IMixinWorldInfo {

    public int getNextSubWorldID();

    public Collection<Integer> getSubWorldIDs(int dimId);

    public void updateSubWorldInfo(SubWorld subWorldToUpdate);

    public void updateSubWorldInfo(SubWorldInfoHolder newInfoHolder);

    public SubWorldInfoHolder getSubWorldInfo(int subWorldId);

    public Collection<SubWorldInfoHolder> getSubWorldInfos();

}
