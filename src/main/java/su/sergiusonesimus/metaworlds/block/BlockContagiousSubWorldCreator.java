package su.sergiusonesimus.metaworlds.block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.MetaworldsMod;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.integrations.ForgeMultipartIntegration;
import su.sergiusonesimus.metaworlds.item.MetaworldsItems;
import su.sergiusonesimus.metaworlds.util.BlockVolatilityMap;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class BlockContagiousSubWorldCreator extends Block {

    public static boolean isBusy = false;

    public BlockContagiousSubWorldCreator() {
        super(Material.rock);
        this.setCreativeTab(MetaworldsItems.creativeTab);
    }

    public void onBlockAdded(World par1World, int par2, int par3, int par4) {
        if (!isBusy) {
            isBusy = true;
            ArrayList<ChunkCoordinates> blocksToTake = new ArrayList<ChunkCoordinates>();
            ArrayList<ChunkCoordinates> blocksToTakeSolidVolatile = new ArrayList<ChunkCoordinates>();
            ArrayList<ChunkCoordinates> blocksToTakeVolatile = new ArrayList<ChunkCoordinates>();
            HashSet<ChunkCoordinates> prevMargin = new HashSet<ChunkCoordinates>();
            HashSet<ChunkCoordinates> margin = new HashSet<ChunkCoordinates>();
            HashSet<ChunkCoordinates> newMargin = new HashSet<ChunkCoordinates>();
            blocksToTake.add(new ChunkCoordinates(par2, par3, par4));
            margin.add(new ChunkCoordinates(par2, par3, par4));
            boolean isValid = true;

            do {
                isValid = this.expandAtMargin(
                    par1World,
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
                World newWorld1 = ((IMixinWorld) par1World).createSubWorld();
                SubWorld newSubWorld = (SubWorld) newWorld1;

                ChunkCoordinates curCoord;
                Block block;
                int blockMetadata;
                NBTTagCompound nbttag;
                TileEntity oldTE;
                TileEntity newTE;
                Entity oldEntity;
                Entity newEntity;

                List<List<ChunkCoordinates>> listsToParse = new ArrayList<List<ChunkCoordinates>>();
                listsToParse.add(blocksToTake);
                listsToParse.add(blocksToTakeSolidVolatile);
                listsToParse.add(blocksToTakeVolatile);
                for (List<ChunkCoordinates> currentList : listsToParse) {
                    Iterator<ChunkCoordinates> i$ = currentList.iterator();
                    while (i$.hasNext()) {
                        curCoord = (ChunkCoordinates) i$.next();
                        block = par1World.getBlock(curCoord.posX, curCoord.posY, curCoord.posZ);
                        blockMetadata = par1World.getBlockMetadata(curCoord.posX, curCoord.posY, curCoord.posZ);
                        newWorld1.setBlock(curCoord.posX, curCoord.posY, curCoord.posZ, block, blockMetadata, 0);
                        newWorld1
                            .setBlockMetadataWithNotify(curCoord.posX, curCoord.posY, curCoord.posZ, blockMetadata, 0);
                        if (block.hasTileEntity(blockMetadata)) {
                            oldTE = par1World.getTileEntity(curCoord.posX, curCoord.posY, curCoord.posZ);
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
                            newWorld1.setTileEntity(curCoord.posX, curCoord.posY, curCoord.posZ, newTE);
                            if (blockIsMultipart) {
                                final TileEntity teToSend = newTE;
                                ForgeMultipartIntegration.scheduleTask(newWorld1, new Runnable() {

                                    @Override
                                    public void run() {
                                        ForgeMultipartIntegration.sendMultipartUpdate(newWorld1, teToSend);
                                    }

                                }, 30); // Providing enough delay to let the client world load all the chunks
                            }
                        }
                        List<Entity> entities = par1World.getEntitiesWithinAABBExcludingEntity(
                            null,
                            AxisAlignedBB
                                .getBoundingBox(
                                    curCoord.posX,
                                    curCoord.posY,
                                    curCoord.posZ,
                                    curCoord.posX + 1,
                                    curCoord.posY + 1,
                                    curCoord.posZ + 1)
                                .expand(0.25, 0.25, 0.25));
                        Iterator<Entity> j$ = entities.iterator();
                        while (j$.hasNext()) {
                            oldEntity = j$.next();
                            if (oldEntity instanceof EntityMinecart || (oldEntity instanceof EntityHanging
                                && (((EntityHanging) oldEntity).field_146063_b == curCoord.posX
                                    && ((EntityHanging) oldEntity).field_146064_c == curCoord.posY
                                    && ((EntityHanging) oldEntity).field_146062_d == curCoord.posZ))) {
                                nbttag = new NBTTagCompound();
                                newEntity = EntityList
                                    .createEntityByName(EntityList.getEntityString(oldEntity), newWorld1);
                                newEntity.copyDataFrom(oldEntity, true);
                                newWorld1.spawnEntityInWorld(newEntity);
                                oldEntity.setDead();
                            }
                        }
                    }
                }

                listsToParse.set(0, blocksToTakeVolatile);
                listsToParse.set(2, blocksToTake);
                for (List<ChunkCoordinates> currentList : listsToParse) {
                    Iterator<ChunkCoordinates> i$ = currentList.iterator();
                    while (i$.hasNext()) {
                        curCoord = (ChunkCoordinates) i$.next();
                        par1World.setBlockToAir(curCoord.posX, curCoord.posY, curCoord.posZ);
                    }
                }

                newSubWorld.setCenter(
                    ((IMixinWorld) par1World).getCenterX(),
                    ((IMixinWorld) par1World).getCenterY(),
                    ((IMixinWorld) par1World).getCenterZ());
                newSubWorld.setTranslation(
                    ((IMixinWorld) par1World).getTranslationX(),
                    ((IMixinWorld) par1World).getTranslationY(),
                    ((IMixinWorld) par1World).getTranslationZ());
                newSubWorld.setRotationYaw(((IMixinWorld) par1World).getRotationYaw());
                newSubWorld.setRotationPitch(((IMixinWorld) par1World).getRotationPitch());
                newSubWorld.setRotationRoll(((IMixinWorld) par1World).getRotationRoll());
                newSubWorld.setScaling(((IMixinWorld) par1World).getScaling());
                newSubWorld.setCenter((double) par2 + 0.5D, (double) par3 + 0.5D, (double) par4 + 0.5D);
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
