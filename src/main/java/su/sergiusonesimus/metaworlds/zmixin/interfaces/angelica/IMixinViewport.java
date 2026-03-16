package su.sergiusonesimus.metaworlds.zmixin.interfaces.angelica;

import net.minecraft.world.World;

import org.embeddedt.embeddium.impl.render.viewport.Viewport;

public interface IMixinViewport {

    public Viewport setWorld(World world);

    public World getWorld();

}
