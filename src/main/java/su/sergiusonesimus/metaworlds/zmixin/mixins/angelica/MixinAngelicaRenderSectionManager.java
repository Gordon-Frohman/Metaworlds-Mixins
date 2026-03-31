package su.sergiusonesimus.metaworlds.zmixin.mixins.angelica;

import net.minecraft.client.multiplayer.WorldClient;

import org.embeddedt.embeddium.impl.gl.device.CommandList;
import org.embeddedt.embeddium.impl.render.chunk.RenderPassConfiguration;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gtnewhorizons.angelica.rendering.celeritas.AngelicaRenderSectionManager;
import com.gtnewhorizons.angelica.rendering.celeritas.BlockRenderLayer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import su.sergiusonesimus.metaworlds.api.SubWorld;

@Mixin(AngelicaRenderSectionManager.class)
public class MixinAngelicaRenderSectionManager {

    private static RenderPassConfiguration<BlockRenderLayer> rpcGlobal;

    @Inject(method = "create", remap = false, at = @At(value = "HEAD"))
    private static void shareWorld(ChunkVertexType vertexType, WorldClient world, int renderDistance,
        CommandList commandList, CallbackInfoReturnable<AngelicaRenderSectionManager> cir,
        @Share("world") LocalRef<WorldClient> sharedWorld) {
        sharedWorld.set(world);
    }

    @WrapOperation(
        method = "create",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lcom/gtnewhorizons/angelica/rendering/celeritas/AngelicaRenderPassConfiguration;build(Lorg/embeddedt/embeddium/impl/render/chunk/vertex/format/ChunkVertexType;)Lorg/embeddedt/embeddium/impl/render/chunk/RenderPassConfiguration;",
            remap = false))
    private static RenderPassConfiguration<BlockRenderLayer> wrapBuild(ChunkVertexType vertexType,
        Operation<RenderPassConfiguration<BlockRenderLayer>> original,
        @Share("world") LocalRef<WorldClient> sharedWorld) {
        if (!(sharedWorld.get() instanceof SubWorld subworld)) rpcGlobal = original.call(vertexType);
        return rpcGlobal;
    }

}
