package su.sergiusonesimus.metaworlds.block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.MetaworldsMod;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.integrations.ForgeMultipartIntegration;
import su.sergiusonesimus.metaworlds.item.MetaworldsItems;
import su.sergiusonesimus.metaworlds.util.BlockVolatilityMap;
import su.sergiusonesimus.metaworlds.util.RotationHelper;
import su.sergiusonesimus.metaworlds.world.SubWorldServer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class BlockSubWorldReintegrator extends Block {

    public BlockSubWorldReintegrator() {
        super(Material.wood);
        this.setCreativeTab(MetaworldsItems.creativeTab);
    }

    public void onBlockAdded(World world, int x, int y, int z) {
        if (!BlockContagiousSubWorldCreator.isBusy) {
            BlockContagiousSubWorldCreator.isBusy = true;
            if (((IMixinWorld) world).isSubWorld()) {
                SubWorld subworld = (SubWorld) world;
                double oldCenterX = subworld.getCenterX();
                double oldCenterY = subworld.getCenterY();
                double oldCenterZ = subworld.getCenterZ();
                subworld.alignSubWorld();
                subworld.setScaling(1.0D);

                ArrayList<ChunkCoordinates> blocksToTake = new ArrayList<ChunkCoordinates>();
                ArrayList<ChunkCoordinates> blocksToTakeSolidVolatile = new ArrayList<ChunkCoordinates>();
                ArrayList<ChunkCoordinates> blocksToTakeVolatile = new ArrayList<ChunkCoordinates>();
                HashSet<ChunkCoordinates> prevMargin = new HashSet<ChunkCoordinates>();
                HashSet<ChunkCoordinates> margin = new HashSet<ChunkCoordinates>();
                HashSet<ChunkCoordinates> newMargin = new HashSet<ChunkCoordinates>();
                blocksToTake.add(new ChunkCoordinates(x, y, z));
                margin.add(new ChunkCoordinates(x, y, z));
                boolean isValid = true;

                do {
                    isValid = this.expandAtMargin(
                        world,
                        blocksToTake,
                        blocksToTakeSolidVolatile,
                        blocksToTakeVolatile,
                        prevMargin,
                        margin,
                        newMargin);
                    if (!isValid) {
                        break;
                    }

                    HashSet<ChunkCoordinates> parentWorld = prevMargin;
                    prevMargin = margin;
                    margin = newMargin;
                    newMargin = parentWorld;
                    parentWorld.clear();
                } while (margin.size() > 0);

                if (isValid) {
                    World parentWorld = ((IMixinWorld) world).getParentWorld();

                    ChunkCoordinates curCoord;
                    Block block;
                    int oldMeta;
                    int newMeta;
                    TileEntity oldTE;
                    TileEntity newTE;
                    Entity oldEntity;
                    Entity newEntity;
                    NBTTagCompound nbttag;

                    List<List<ChunkCoordinates>> listsToParse = new ArrayList<List<ChunkCoordinates>>();
                    listsToParse.add(blocksToTake);
                    listsToParse.add(blocksToTakeSolidVolatile);
                    listsToParse.add(blocksToTakeVolatile);
                    for (List<ChunkCoordinates> currentList : listsToParse) {
                        Iterator<ChunkCoordinates> i$ = currentList.iterator();
                        while (i$.hasNext()) {
                            curCoord = (ChunkCoordinates) i$.next();
                            block = world.getBlock(curCoord.posX, curCoord.posY, curCoord.posZ);
                            oldMeta = world.getBlockMetadata(curCoord.posX, curCoord.posY, curCoord.posZ);
                            newMeta = RotationHelper.getRotatedMeta(world, curCoord.posX, curCoord.posY, curCoord.posZ);
                            ChunkCoordinates globalPos = subworld
                                .transformBlockToGlobal(curCoord.posX, curCoord.posY, curCoord.posZ);
                            parentWorld.setBlock(globalPos.posX, globalPos.posY, globalPos.posZ, block, newMeta, 3);
                            parentWorld
                                .setBlockMetadataWithNotify(globalPos.posX, globalPos.posY, globalPos.posZ, newMeta, 3);
                            if (block.hasTileEntity(oldMeta)) {
                                RotationHelper.rotateTileEntity(world, curCoord.posX, curCoord.posY, curCoord.posZ);
                                oldTE = world.getTileEntity(curCoord.posX, curCoord.posY, curCoord.posZ);
                                nbttag = new NBTTagCompound();
                                oldTE.writeToNBT(nbttag);
                                oldTE.invalidate();
                                boolean blockIsMultipart = MetaworldsMod.isForgeMultipartLoaded
                                    && ForgeMultipartIntegration.isBlockMultipart(block);
                                if (blockIsMultipart) {
                                    newTE = ForgeMultipartIntegration.createTileEntityFromNBT(nbttag);
                                } else {
                                    newTE = TileEntity.createAndLoadEntity(nbttag);
                                }
                                if (newTE.blockMetadata != -1) newTE.blockMetadata = newMeta;
                                newTE.xCoord = globalPos.posX;
                                newTE.yCoord = globalPos.posY;
                                newTE.zCoord = globalPos.posZ;
                                newTE.setWorldObj(parentWorld);
                                parentWorld.setTileEntity(globalPos.posX, globalPos.posY, globalPos.posZ, newTE);
                                if (blockIsMultipart) {
                                    ForgeMultipartIntegration.sendMultipartUpdate(parentWorld, newTE);
                                }
                            }
                        }
                    }

                    Iterator<Entity> iter = world.loadedEntityList.iterator();
                    while (iter.hasNext()) {
                        oldEntity = iter.next();
                        if (oldEntity instanceof EntityPlayer) continue;
                        nbttag = new NBTTagCompound();
                        newEntity = EntityList.createEntityByName(EntityList.getEntityString(oldEntity), parentWorld);
                        newEntity.copyDataFrom(oldEntity, true);
                        Vec3 globalCoords = subworld.transformToGlobal(newEntity);
                        newEntity.setLocationAndAngles(
                            globalCoords.xCoord,
                            globalCoords.yCoord,
                            globalCoords.zCoord,
                            newEntity.rotationYaw,
                            newEntity.rotationPitch);
                        RotationHelper.rotateEntity(world, newEntity);
                        parentWorld.spawnEntityInWorld(newEntity);
                        oldEntity.setDead();
                    }

                    listsToParse.set(0, blocksToTakeVolatile);
                    listsToParse.set(2, blocksToTake);
                    for (List<ChunkCoordinates> currentList : listsToParse) {
                        Iterator<ChunkCoordinates> i$ = currentList.iterator();
                        while (i$.hasNext()) {
                            curCoord = (ChunkCoordinates) i$.next();
                            world.setBlockToAir(curCoord.posX, curCoord.posY, curCoord.posZ);
                        }
                    }
                }

                subworld.setCenter(oldCenterX, oldCenterY, oldCenterZ);
                if (subworld instanceof SubWorldServer && ((SubWorldServer) subworld).isEmpty()) {
                    SubWorldServer subWorldServer = ((SubWorldServer) subworld);
                    ((IMixinWorld) subWorldServer.getParentWorld()).getSubWorldsMap()
                        .remove(subWorldServer.getSubWorldID());
                    subWorldServer.removeSubWorld();
                    subWorldServer.flush();
                    subWorldServer.deleteSubWorldData();
                }
            }
            BlockContagiousSubWorldCreator.isBusy = false;
        }
    }

    public boolean expandAtMargin(World par1World, List<ChunkCoordinates> blockList,
        List<ChunkCoordinates> solidVolatileBlockList, List<ChunkCoordinates> volatileBlockList,
        HashSet<ChunkCoordinates> prevMarginList, HashSet<ChunkCoordinates> marginList,
        HashSet<ChunkCoordinates> newMarginList) {
        Iterator<ChunkCoordinates> i$ = marginList.iterator();

        while (i$.hasNext()) {
            ChunkCoordinates curCoord = (ChunkCoordinates) i$.next();

            for (int direction = 0; direction < 6; ++direction) {
                ChunkCoordinates newCoords = new ChunkCoordinates(curCoord.posX, curCoord.posY, curCoord.posZ);
                switch (direction / 2) {
                    case 0:
                        newCoords.posY += direction % 2 * 2 - 1;
                        break;
                    case 1:
                        newCoords.posX += direction % 2 * 2 - 1;
                        break;
                    case 2:
                        newCoords.posZ += direction % 2 * 2 - 1;
                }

                if (!prevMarginList.contains(newCoords) && !marginList.contains(newCoords)) {
                    Block curBlock = par1World.getBlock(newCoords.posX, newCoords.posY, newCoords.posZ);
                    if (curBlock.equals(Blocks.bedrock)) {
                        return false;
                    }

                    if (!curBlock.equals(Blocks.air) && !curBlock.equals(Blocks.flowing_water)
                        && !curBlock.equals(Blocks.flowing_lava)
                        && (!curBlock.equals(Blocks.water) && !curBlock.equals(Blocks.lava)
                            || !par1World.getBlock(newCoords.posX, newCoords.posY + 1, newCoords.posZ)
                                .equals(curBlock))
                        && newMarginList.add(newCoords)) {
                        if (BlockVolatilityMap.checkBlockVolatility(curBlock)) {
                            if (BlockVolatilityMap
                                .isBlockSolid(curBlock, par1World, newCoords.posX, newCoords.posY, newCoords.posZ)) {
                                solidVolatileBlockList.add(newCoords);
                            } else {
                                volatileBlockList.add(newCoords);
                            }
                        } else {
                            blockList.add(newCoords);
                        }
                    }
                }
            }
        }

        return true;
    }
}
