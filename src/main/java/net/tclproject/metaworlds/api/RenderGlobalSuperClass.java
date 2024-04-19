package net.tclproject.metaworlds.api;

import java.nio.IntBuffer;

public interface RenderGlobalSuperClass {

    public void markRenderersForNewPositionSingle(double par1d, double par2d, double par3d, int subWorldID);

    public void unloadRenderersForSubWorld(int subWorldId);

    public boolean getOcclusionEnabled();

    public IntBuffer getOcclusionQueryBase();
}
