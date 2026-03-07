package su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.client.renderer;

import java.nio.IntBuffer;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;

public interface IMixinRenderGlobal {

    public RenderGlobal setMC(Minecraft par1Minecraft);

    public boolean getOcclusionEnabled();

    public IntBuffer getOcclusionQueryBase();

    public List<WorldRenderer> getWorldRenderersToUpdate();

    public int getRenderChunksWide();

    public int getRenderChunksTall();

    public int getRenderChunksDeep();

    public WorldClient getWorld();

    public void setWorld(WorldClient newWorld);

    public void destroyBlockPartially(int p_147587_1_, int p_147587_2_, int p_147587_3_, int p_147587_4_,
        int p_147587_5_, int subWorldId);
}
