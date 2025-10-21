package su.sergiusonesimus.metaworlds.zmixin.interfaces.littletiles;

import net.minecraft.world.World;

import com.creativemd.littletiles.common.utils.LittleTileBlockPos;

public interface IMixinLittleTileBlockPos {

    public LittleTileBlockPos setWorld(World world);

    public World getWorld();

}
