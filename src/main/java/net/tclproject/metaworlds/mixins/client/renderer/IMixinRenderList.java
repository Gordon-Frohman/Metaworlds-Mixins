package net.tclproject.metaworlds.mixins.client.renderer;

import java.nio.DoubleBuffer;

import net.minecraft.client.renderer.RenderList;

public interface IMixinRenderList {

    public RenderList setSubWorldID(int SWID);

    public RenderList setCustomTransformation(DoubleBuffer CT);

    public int getSubWorldID();

    public DoubleBuffer getCustomTransformation();

    public boolean rendersChunk(int par1, int par2, int par3, int parSubWorldID);

    public void setupRenderList(int par1, int par2, int par3, double par4, double par6, double par8, int parSubWorldID,
        DoubleBuffer parCustomTransformation);

}
