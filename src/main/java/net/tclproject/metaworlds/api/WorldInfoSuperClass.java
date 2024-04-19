package net.tclproject.metaworlds.api;

import java.util.Collection;

import net.tclproject.metaworlds.patcher.SubWorldInfoHolder;

public interface WorldInfoSuperClass {

    public int getNextSubWorldID();

    public Collection<Integer> getSubWorldIDs(int dimId);

    public void updateSubWorldInfo(SubWorld subWorldToUpdate);

    public void updateSubWorldInfo(SubWorldInfoHolder newInfoHolder);

    public SubWorldInfoHolder getSubWorldInfo(int subWorldId);

    public Collection<SubWorldInfoHolder> getSubWorldInfos();

}
