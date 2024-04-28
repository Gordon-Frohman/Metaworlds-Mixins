package net.tclproject.metaworlds.api;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.tclproject.metaworlds.patcher.SubWorldFactory;

public interface IMixinWorldIntermediate {

    public World CreateSubWorld();

    public World CreateSubWorld(int newSubWorldID);

    public Minecraft getMinecraft();

    public void setMinecraft(Minecraft newMinecraft);

    public NetHandlerPlayClient getSendQueue();
    
    public void setSubworldFactory(SubWorldFactory subWorldFactory);
    
    public SubWorldFactory getSubworldFactory();
    
    public MovingObjectPosition rayTraceBlocks_do_do_single(Vec3 par1Vec3, Vec3 par2Vec3, boolean par3, boolean par4, boolean par5);
    
    public List selectEntitiesWithinAABBLocal(Class par1Class, AxisAlignedBB par2AxisAlignedBB, IEntitySelector par3IEntitySelector);
    
    public List getCollidingBoundingBoxesLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB);
    
    public boolean isMaterialInBBLocal(AxisAlignedBB par1AxisAlignedBB, Material par2Material);
    
    public List getEntitiesWithinAABBExcludingEntityLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB);
    
    public List getEntitiesWithinAABBExcludingEntityLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB, IEntitySelector par3IEntitySelector);
}
