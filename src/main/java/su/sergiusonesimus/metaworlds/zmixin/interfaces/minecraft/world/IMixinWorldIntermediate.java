package su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public interface IMixinWorldIntermediate {

    public MovingObjectPosition rayTraceBlocks_do_do_single(Vec3 par1Vec3, Vec3 par2Vec3, boolean par3, boolean par4,
        boolean par5);

    @SuppressWarnings("rawtypes")
    public List<Entity> selectEntitiesWithinAABBLocal(Class par1Class, AxisAlignedBB par2AxisAlignedBB,
        IEntitySelector par3IEntitySelector);

    public List<AxisAlignedBB> getCollidingBoundingBoxesLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB);

    public boolean isMaterialInBBLocal(AxisAlignedBB par1AxisAlignedBB, Material par2Material);

    public List<Entity> getEntitiesWithinAABBExcludingEntityLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB);

    public List<Entity> getEntitiesWithinAABBExcludingEntityLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB,
        IEntitySelector par3IEntitySelector);

    public boolean spawnEntityInWorldIntermediate(Entity par1Entity);

    public void removeEntityIntermediate(Entity par1Entity);
}
