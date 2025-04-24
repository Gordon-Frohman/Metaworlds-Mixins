package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.renderer;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import net.minecraft.client.renderer.RenderList;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.client.renderer.IMixinRenderList;

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

    @ModifyArgs(
        method = "callLists",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V", opcode = Opcodes.INVOKESTATIC))
    private void modifyCallLists(Args args) {
        GL11.glTranslatef((float) (-this.cameraX), (float) (-this.cameraY), (float) (-this.cameraZ));

        if (this.customTransformation != null) GL11.glMultMatrix(this.customTransformation);

        args.set(0, (float) this.renderChunkX);
        args.set(1, (float) this.renderChunkY);
        args.set(2, (float) this.renderChunkZ);
    }

    public boolean rendersChunk(int par1, int par2, int par3, int parSubWorldID) {
        return !this.valid ? false
            : par1 == this.renderChunkX && par2 == this.renderChunkY
                && par3 == this.renderChunkZ
                && parSubWorldID == this.subWorldID;
    }

}
