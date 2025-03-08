package su.sergiusonesimus.metaworlds.mixin.mixins.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Render.class)
public class MixinRender {

    @Shadow(remap = true)
    protected RenderManager renderManager;

    // TODO

    @Shadow(remap = true)
    protected void bindEntityTexture(Entity p_110777_1_) {}

    @Shadow(remap = true)
    protected void bindTexture(ResourceLocation p_110776_1_) {}

}
