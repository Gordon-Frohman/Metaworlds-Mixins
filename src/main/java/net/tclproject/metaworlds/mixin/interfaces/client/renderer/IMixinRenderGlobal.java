package net.tclproject.metaworlds.mixin.interfaces.client.renderer;

import java.nio.IntBuffer;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.world.World;

public interface IMixinRenderGlobal {

    public RenderGlobal setMC(Minecraft par1Minecraft);

    public boolean getOcclusionEnabled();

    public IntBuffer getOcclusionQueryBase();

    public List getWorldRenderersToUpdate();

    public List<WorldRenderer> getSortedWorldRenderersList();
    
    public void setSortedWorldRenderersList(List<WorldRenderer> sortedWorldRenderersList);

    public Map<Integer, WorldRenderer> getWorldRenderersMap();
    
    public void setWorldRenderersMap(Map<Integer, WorldRenderer> worldRenderersMap);

    public List<WorldRenderer> getWorldRenderersList();

    public void setWorldRenderersList(List<WorldRenderer> worldRenderersList);

    public int getRenderChunksWide();

    public int getRenderChunksTall();

    public int getRenderChunksDeep();

    public WorldRenderer createWorldRenderer(World targetWorld, int chunkIndexX, int chunkIndexY, int chunkIndexZ);

    public void removeWorldRenderer(World targetWorld, int chunkIndexX, int chunkIndexY, int chunkIndexZ);

    public void unloadRenderersForSubWorld(int subWorldId);

    public void markRenderersForNewPosition(double par1, double par2, double par3);

    public void markRenderersForNewPositionSingle(double par1d, double par2d, double par3d, int subWorldID);

    public WorldClient getWorld();

    public void setWorld(WorldClient newWorld);

    public void loadRenderersForNewSubWorld(int subWorldId);
    
    public void destroyBlockPartially(int p_147587_1_, int p_147587_2_, int p_147587_3_, int p_147587_4_, int p_147587_5_, int subWorldId);
}
