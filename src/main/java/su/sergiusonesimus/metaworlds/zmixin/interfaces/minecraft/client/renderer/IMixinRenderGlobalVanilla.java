package su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.client.renderer;

import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.world.World;

public interface IMixinRenderGlobalVanilla {

    public List<WorldRenderer> getSortedWorldRenderersList();

    public void setSortedWorldRenderersList(List<WorldRenderer> sortedWorldRenderersList);

    public Map<Integer, WorldRenderer> getWorldRenderersMap();

    public void setWorldRenderersMap(Map<Integer, WorldRenderer> worldRenderersMap);

    public List<WorldRenderer> getWorldRenderersList();

    public void setWorldRenderersList(List<WorldRenderer> worldRenderersList);

    public void loadRenderersForNewSubWorld(int subWorldId);

    public WorldRenderer createWorldRenderer(World targetWorld, int chunkIndexX, int chunkIndexY, int chunkIndexZ);

    public void removeWorldRenderer(World targetWorld, int chunkIndexX, int chunkIndexY, int chunkIndexZ);

    public void unloadRenderersForSubWorld(int subWorldId);

    public void markRenderersForNewPosition(double par1, double par2, double par3);

    public void markRenderersForNewPositionSingle(double par1d, double par2d, double par3d, int subWorldID);

}
