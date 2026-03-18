package su.sergiusonesimus.metaworlds.zmixin.mixins.angelica;

import java.lang.reflect.Field;

import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.gtnewhorizons.angelica.compat.mojang.ChunkSectionPos;
import com.gtnewhorizons.angelica.rendering.celeritas.threading.ThreadedChunkTaskProvider;
import com.gtnewhorizons.angelica.rendering.celeritas.world.cloned.ChunkRenderContext;
import com.gtnewhorizons.angelica.rendering.celeritas.world.cloned.ClonedChunkSectionCache;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(ThreadedChunkTaskProvider.class)
public class MixinThreadedChunkTaskProvider {

    @WrapOperation(
        method = "createRebuildTask(Lorg/embeddedt/embeddium/impl/render/chunk/RenderSection;ILorg/joml/Vector3d;Lcom/gtnewhorizons/angelica/rendering/celeritas/world/cloned/ClonedChunkSectionCache;)Lorg/embeddedt/embeddium/impl/render/chunk/compile/tasks/ChunkBuilderTask;",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lcom/gtnewhorizons/angelica/rendering/celeritas/world/WorldSlice;prepare(Lnet/minecraft/world/World;Lcom/gtnewhorizons/angelica/compat/mojang/ChunkSectionPos;Lcom/gtnewhorizons/angelica/rendering/celeritas/world/cloned/ClonedChunkSectionCache;)Lcom/gtnewhorizons/angelica/rendering/celeritas/world/cloned/ChunkRenderContext;",
            remap = false))
    public ChunkRenderContext prepare(World world, ChunkSectionPos origin, ClonedChunkSectionCache sectionCache,
        Operation<ChunkRenderContext> original) {
        try {
            Field actualWorld = ClonedChunkSectionCache.class.getDeclaredField("world");
            actualWorld.setAccessible(true);
            return original.call(actualWorld.get(sectionCache), origin, sectionCache);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            return original.call(world, origin, sectionCache);
        }
    }

}
