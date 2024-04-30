package su.sergiusonesimus.metaworlds.mixin.mixins.client.renderer;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import net.minecraft.client.renderer.RenderList;
import su.sergiusonesimus.metaworlds.mixin.interfaces.client.renderer.IMixinRenderList;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderList.class)
public class MixinRenderList implements IMixinRenderList {

    private int subWorldID;
    private DoubleBuffer customTransformation;

    @Shadow(remap = true)
    private boolean valid;

    @Shadow(remap = true)
    private boolean bufferFlipped;

    @Shadow(remap = true)
    private IntBuffer glLists;

    /** The location of the 16x16x16 render chunk rendered by this RenderList. */
    @Shadow(remap = true)
    public int renderChunkX;

    @Shadow(remap = true)
    public int renderChunkY;

    @Shadow(remap = true)
    public int renderChunkZ;

    /** The in-world location of the camera, used to translate the world into the proper position for rendering. */
    @Shadow(remap = true)
    private double cameraX;

    @Shadow(remap = true)
    private double cameraY;

    @Shadow(remap = true)
    private double cameraZ;

    public void setupRenderList(int par1, int par2, int par3, double par4, double par6, double par8, int parSubWorldID,
        DoubleBuffer parCustomTransformation) {
        this.valid = true;
        this.glLists.clear();
        this.renderChunkX = par1;
        this.renderChunkY = par2;
        this.renderChunkZ = par3;
        this.cameraX = par4;
        this.cameraY = par6;
        this.cameraZ = par8;
        this.subWorldID = parSubWorldID;
        this.customTransformation = parCustomTransformation;
    }

    public RenderList setSubWorldID(int SWID) {
        subWorldID = SWID;
        return (RenderList) (Object) this;
    }

    public RenderList setCustomTransformation(DoubleBuffer CT) {
        customTransformation = CT;
        return (RenderList) (Object) this;
    }

    public int getSubWorldID() {
        return subWorldID;
    }

    public DoubleBuffer getCustomTransformation() {
        return customTransformation;
    }

    @Overwrite
    public void callLists() {
        if (this.valid) {
            if (!this.bufferFlipped) {
                this.glLists.flip();
                this.bufferFlipped = true;
            }

            if (this.glLists.remaining() > 0) {
                GL11.glPushMatrix();
                GL11.glTranslatef((float) (-this.cameraX), (float) (-this.cameraY), (float) (-this.cameraZ));

                if (this.customTransformation != null) GL11.glMultMatrix(this.customTransformation);

                GL11.glTranslatef((float) this.renderChunkX, (float) this.renderChunkY, (float) this.renderChunkZ);
                GL11.glCallLists(this.glLists);
                GL11.glPopMatrix();
            }
        }
    }

    public boolean rendersChunk(int par1, int par2, int par3, int parSubWorldID) {
        return !this.valid ? false
            : par1 == this.renderChunkX && par2 == this.renderChunkY
                && par3 == this.renderChunkZ
                && parSubWorldID == this.subWorldID;
    }

}
