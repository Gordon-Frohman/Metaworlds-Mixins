package su.sergiusonesimus.metaworlds.block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.item.MetaworldsItems;
import su.sergiusonesimus.metaworlds.util.BlockVolatilityMap;
import su.sergiusonesimus.metaworlds.util.DisplacementHelper;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class BlockContagiousSubWorldCreator extends Block {

    public static boolean isBusy = false;

    public BlockContagiousSubWorldCreator() {
        super(Material.rock);
        this.setCreativeTab(MetaworldsItems.creativeTab);
    }

    public void onBlockAdded(World world, int x, int y, int z) {
        if (!isBusy) {
            isBusy = true;
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

                HashSet<ChunkCoordinates> newWorld = prevMargin;
                prevMargin = margin;
                margin = newMargin;
                newMargin = newWorld;
                newWorld.clear();
            } while (margin.size() > 0);

            if (isValid) {
                Vec3 center = ((IMixinWorld) world)
                    .transformToGlobal((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D);
                World newWorld1 = ((IMixinWorld) world).createSubWorld(
                    center.xCoord,
                    center.yCoord,
                    center.zCoord,
                    ((IMixinWorld) world).getTranslationX(),
                    ((IMixinWorld) world).getTranslationY(),
                    ((IMixinWorld) world).getTranslationZ(),
                    ((IMixinWorld) world).getRotationPitch(),
                    ((IMixinWorld) world).getRotationYaw(),
                    ((IMixinWorld) world).getRotationRoll(),
                    ((IMixinWorld) world).getScaling());

                ChunkCoordinates curCoord;

                List<List<ChunkCoordinates>> listsToParse = new ArrayList<List<ChunkCoordinates>>();
                listsToParse.add(blocksToTake);
                listsToParse.add(blocksToTakeSolidVolatile);
                listsToParse.add(blocksToTakeVolatile);
                for (List<ChunkCoordinates> currentList : listsToParse) {
                    Iterator<ChunkCoordinates> i$ = currentList.iterator();
                    while (i$.hasNext()) {
                        curCoord = (ChunkCoordinates) i$.next();
                        DisplacementHelper.displaceBlock(curCoord.posX, curCoord.posY, curCoord.posZ, world, newWorld1);
                    }
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

            isBusy = false;
        }
    }

    public boolean expandAtMargin(World par1World, List<ChunkCoordinates> blockList,
        List<ChunkCoordinates> solidVolatileBlockList, List<ChunkCoordinates> volatileBlockList,
        HashSet<ChunkCoordinates> prevMarginList, HashSet<ChunkCoordinates> marginList,
        HashSet<ChunkCoordinates> newMarginList) {
        Iterator<ChunkCoordinates> i$ = marginList.iterator();

        while (i$.hasNext()) {
            ChunkCoordinates curCoord = (ChunkCoordinates) i$.next();

            for (int direction = 0; direction < 18; ++direction) {
                ChunkCoordinates newCoords = new ChunkCoordinates(curCoord.posX, curCoord.posY, curCoord.posZ);
                boolean includePlants = direction == 1;
                boolean includeFromPlants = direction == 0;
                if (direction < 6) {
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
                } else {
                    int prevBlock = direction - 6;
                    if (prevBlock >= 4) {
                        prevBlock -= 4;
                        if (prevBlock >= 4) {
                            ++newCoords.posY;
                            prevBlock -= 4;
                        } else {
                            --newCoords.posY;
                        }

                        switch (prevBlock / 2) {
                            case 0:
                                newCoords.posX += prevBlock % 2 * 2 - 1;
                                break;
                            case 1:
                                newCoords.posZ += prevBlock % 2 * 2 - 1;
                        }
                    } else {
                        newCoords.posX += prevBlock % 2 * 2 - 1;
                        newCoords.posZ += prevBlock / 2 * 2 - 1;
                    }
                }

                if (!prevMarginList.contains(newCoords) && !marginList.contains(newCoords)) {
                    Block var20 = par1World.getBlock(curCoord.posX, curCoord.posY, curCoord.posZ);
                    if (includeFromPlants || var20.getMaterial()
                        .isLiquid()
                        || !var20.getMaterial()
                            .isReplaceable() && var20.getMaterial() != Material.plants) {
                        Block curBlock = par1World.getBlock(newCoords.posX, newCoords.posY, newCoords.posZ);
                        if (curBlock.equals(Blocks.bedrock)) {
                            return false;
                        }

                        if (!curBlock.equals(Blocks.air) && !curBlock.equals(Blocks.flowing_water)
                            && !curBlock.equals(Blocks.flowing_lava)
                            && (includePlants || curBlock.getMaterial()
                                .isLiquid()
                                || !curBlock.getMaterial()
                                    .isReplaceable() && curBlock.getMaterial() != Material.plants)
                            && (!curBlock.equals(Blocks.water) && !curBlock.equals(Blocks.lava)
                                || !par1World.getBlock(newCoords.posX, newCoords.posY + 1, newCoords.posZ)
                                    .equals(curBlock))
                            && newMarginList.add(newCoords)) {
                            if (BlockVolatilityMap.checkBlockVolatility(curBlock)) {
                                if (BlockVolatilityMap.isBlockSolid(
                                    curBlock,
                                    par1World,
                                    newCoords.posX,
                                    newCoords.posY,
                                    newCoords.posZ)) {
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
        }

        return true;
    }
}
