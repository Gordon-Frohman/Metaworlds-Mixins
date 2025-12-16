package su.sergiusonesimus.metaworlds.block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.item.MetaworldsItems;
import su.sergiusonesimus.metaworlds.util.BlockVolatilityMap;
import su.sergiusonesimus.metaworlds.util.DisplacementHelper;
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

                    List<List<ChunkCoordinates>> listsToParse = new ArrayList<List<ChunkCoordinates>>();
                    listsToParse.add(blocksToTake);
                    listsToParse.add(blocksToTakeSolidVolatile);
                    listsToParse.add(blocksToTakeVolatile);
                    for (List<ChunkCoordinates> currentList : listsToParse) {
                        Iterator<ChunkCoordinates> i$ = currentList.iterator();
                        while (i$.hasNext()) {
                            curCoord = (ChunkCoordinates) i$.next();
                            DisplacementHelper
                                .displaceBlock(curCoord.posX, curCoord.posY, curCoord.posZ, world, parentWorld);
                        }
                    }

                    Iterator<Entity> iter = world.loadedEntityList.iterator();
                    Entity entity;
                    while (iter.hasNext()) {
                        entity = iter.next();
                        DisplacementHelper.displaceEntity(entity, world, parentWorld);
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
