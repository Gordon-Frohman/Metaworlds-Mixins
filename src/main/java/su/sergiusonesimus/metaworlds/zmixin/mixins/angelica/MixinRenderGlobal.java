package su.sergiusonesimus.metaworlds.zmixin.mixins.angelica;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;

import org.embeddedt.embeddium.impl.render.viewport.Viewport;
import org.embeddedt.embeddium.impl.render.viewport.ViewportProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import su.sergiusonesimus.metaworlds.zmixin.MixinPriorities;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.angelica.IMixinViewport;

@Mixin(value = RenderGlobal.class, priority = MixinPriorities.ANGELICA)
public class MixinRenderGlobal {

    @Shadow(remap = true)
    private WorldClient theWorld;

    // @Inject(method = "markBlocksForUpdate", at = @At(value = "TAIL"))
    // public void markBlocksForUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, CallbackInfo ci) {
    // markBlocksForUpdateSubworlds(minX, minY, minZ, maxX, maxY, maxZ);
    // }
    //
    // public void markBlocksForUpdateSubworlds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    // int k1 = MathHelper.bucketInt(minX, 16);
    // int l1 = MathHelper.bucketInt(minY, 16);
    // int i2 = MathHelper.bucketInt(minZ, 16);
    // int j2 = MathHelper.bucketInt(maxX, 16);
    // int k2 = MathHelper.bucketInt(maxY, 16);
    // int l2 = MathHelper.bucketInt(maxZ, 16);
    //
    // for (World curWorld : ((IMixinWorld) this.theWorld).getSubWorlds()) {
    // for (int i3 = k1; i3 <= j2; ++i3) {
    // int j3 = i3 % this.renderChunksWide;
    //
    // if (j3 < 0) {
    // j3 += this.renderChunksWide;
    // }
    //
    // for (int k3 = l1; k3 <= k2; ++k3) {
    // int l3 = k3 % this.renderChunksTall;
    //
    // if (l3 < 0) {
    // l3 += this.renderChunksTall;
    // }
    //
    // for (int i4 = i2; i4 <= l2; ++i4) {
    // int j4 = i4 % this.renderChunksDeep;
    //
    // if (j4 < 0) {
    // j4 += this.renderChunksDeep;
    // }
    //
    // int k4 = ((((IMixinWorld) curWorld).getSubWorldID() * this.renderChunksDeep + j4)
    // * this.renderChunksTall + l3) * this.renderChunksWide + j3;
    // WorldRenderer worldrenderer = this.worldRenderersMap.get(k4);
    //
    // if (worldrenderer != null && !worldrenderer.needsUpdate) {
    // this.worldRenderersToUpdate.add(worldrenderer);
    // worldrenderer.markDirty();
    // }
    // }
    // }
    // }
    // }
    // }
    //
    // @Inject(method = "markBlockForUpdate", at = @At(value = "TAIL"))
    // public void markBlockForUpdate(int x, int y, int z, CallbackInfo ci) {
    // this.markBlocksForUpdateSubworlds(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
    // }
    //
    // @Inject(method = "markBlockForRenderUpdate", at = @At(value = "TAIL"))
    // public void markBlockForRenderUpdate(int x, int y, int z, CallbackInfo ci) {
    // this.markBlocksForUpdateSubworlds(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
    // }
    //
    // @Inject(method = "markBlockRangeForRenderUpdate", at = @At(value = "TAIL"))
    // public void markBlockRangeForRenderUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
    // CallbackInfo ci) {
    // this.markBlocksForUpdateSubworlds(minX - 1, minY - 1, minZ - 1, maxX + 1, maxY + 1, maxZ + 1);
    // }

    @WrapOperation(
        method = "clipRenderersByFrustum",
        remap = true,
        at = @At(
            value = "INVOKE",
            target = "Lorg/embeddedt/embeddium/impl/render/viewport/ViewportProvider;sodium$createViewport()Lorg/embeddedt/embeddium/impl/render/viewport/Viewport;",
            remap = false))
    private Viewport setRenderWorld(ViewportProvider instance, Operation<Viewport> original) {
        return ((IMixinViewport) (Object) original.call(instance)).setWorld(theWorld);
    }

}
