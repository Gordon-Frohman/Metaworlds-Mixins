package su.sergiusonesimus.metaworlds.creators.contagious;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.api.IMixinWorld;
import su.sergiusonesimus.metaworlds.api.SubWorld;

public class BlockContagiousSubWorldCreator extends Block {

    public static Map<Integer, Boolean> blockVolatilityMap = new TreeMap();
    public static boolean isBusy = false;

    public BlockContagiousSubWorldCreator() {
        super(Material.rock);
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    public void onBlockAdded(World par1World, int par2, int par3, int par4) {
        if (!isBusy) {
            isBusy = true;
            ArrayList blocksToTake = new ArrayList();
            ArrayList blocksToTakeVolatile = new ArrayList();
            HashSet prevMargin = new HashSet();
            HashSet margin = new HashSet();
            HashSet newMargin = new HashSet();
            blocksToTake.add(new BlockContagiousSubWorldCreator.BlockCoord3(par2, par3, par4));
            margin.add(new BlockContagiousSubWorldCreator.BlockCoord3(par2, par3, par4));
            boolean isValid = true;

            do {
                isValid = this
                    .expandAtMargin(par1World, blocksToTake, blocksToTakeVolatile, prevMargin, margin, newMargin);
                if (!isValid) {
                    break;
                }

                HashSet newWorld = prevMargin;
                prevMargin = margin;
                margin = newMargin;
                newMargin = newWorld;
                newWorld.clear();
            } while (margin.size() > 0);

            if (isValid) {
                World newWorld1 = ((IMixinWorld) par1World).CreateSubWorld();
                SubWorld newSubWorld = (SubWorld) newWorld1;
                Iterator i$ = blocksToTake.iterator();

                BlockContagiousSubWorldCreator.BlockCoord3 curCoord;
                Block block;
                int blockMetadata;
                TileEntity origTE;
                NBTTagCompound nbttag;
                TileEntity newTE;
                while (i$.hasNext()) {
                    curCoord = (BlockContagiousSubWorldCreator.BlockCoord3) i$.next();
                    block = par1World.getBlock(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ);
                    blockMetadata = par1World
                        .getBlockMetadata(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ);
                    newWorld1
                        .setBlock(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ, block, blockMetadata, 0);
                    newWorld1.setBlockMetadataWithNotify(
                        curCoord.blockPosX,
                        curCoord.blockPosY,
                        curCoord.blockPosZ,
                        blockMetadata,
                        0);
                    if (block.hasTileEntity(blockMetadata)) {
                        origTE = par1World.getTileEntity(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ);
                        nbttag = new NBTTagCompound();
                        origTE.writeToNBT(nbttag);
                        origTE.invalidate();
                        newTE = TileEntity.createAndLoadEntity(nbttag);
                        newWorld1.setTileEntity(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ, newTE);
                    }
                }

                i$ = blocksToTakeVolatile.iterator();

                while (i$.hasNext()) {
                    curCoord = (BlockContagiousSubWorldCreator.BlockCoord3) i$.next();
                    block = par1World.getBlock(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ);
                    blockMetadata = par1World
                        .getBlockMetadata(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ);
                    newWorld1
                        .setBlock(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ, block, blockMetadata, 0);
                    newWorld1.setBlockMetadataWithNotify(
                        curCoord.blockPosX,
                        curCoord.blockPosY,
                        curCoord.blockPosZ,
                        blockMetadata,
                        0);
                    if (block.hasTileEntity(blockMetadata)) {
                        origTE = par1World.getTileEntity(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ);
                        nbttag = new NBTTagCompound();
                        origTE.writeToNBT(nbttag);
                        origTE.invalidate();
                        newTE = TileEntity.createAndLoadEntity(nbttag);
                        newWorld1.setTileEntity(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ, newTE);
                    }
                }

                i$ = blocksToTakeVolatile.iterator();

                while (i$.hasNext()) {
                    curCoord = (BlockContagiousSubWorldCreator.BlockCoord3) i$.next();
                    par1World.setBlockToAir(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ);
                }

                i$ = blocksToTake.iterator();

                while (i$.hasNext()) {
                    curCoord = (BlockContagiousSubWorldCreator.BlockCoord3) i$.next();
                    par1World.setBlockToAir(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ);
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

    public boolean expandAtMargin(World par1World, List<BlockContagiousSubWorldCreator.BlockCoord3> blockList,
        List<BlockContagiousSubWorldCreator.BlockCoord3> volatileBlockList,
        HashSet<BlockContagiousSubWorldCreator.BlockCoord3> prevMarginList,
        HashSet<BlockContagiousSubWorldCreator.BlockCoord3> marginList,
        HashSet<BlockContagiousSubWorldCreator.BlockCoord3> newMarginList) {
        Iterator i$ = marginList.iterator();

        while (i$.hasNext()) {
            BlockContagiousSubWorldCreator.BlockCoord3 curCoord = (BlockContagiousSubWorldCreator.BlockCoord3) i$
                .next();

            for (int direction = 0; direction < 18; ++direction) {
                BlockContagiousSubWorldCreator.BlockCoord3 newCoords = new BlockContagiousSubWorldCreator.BlockCoord3(
                    curCoord.blockPosX,
                    curCoord.blockPosY,
                    curCoord.blockPosZ);
                boolean includePlants = direction == 1;
                boolean includeFromPlants = direction == 0;
                if (direction < 6) {
                    switch (direction / 2) {
                        case 0:
                            newCoords.blockPosY += direction % 2 * 2 - 1;
                            break;
                        case 1:
                            newCoords.blockPosX += direction % 2 * 2 - 1;
                            break;
                        case 2:
                            newCoords.blockPosZ += direction % 2 * 2 - 1;
                    }
                } else {
                    int prevBlock = direction - 6;
                    if (prevBlock >= 4) {
                        prevBlock -= 4;
                        if (prevBlock >= 4) {
                            ++newCoords.blockPosY;
                            prevBlock -= 4;
                        } else {
                            --newCoords.blockPosY;
                        }

                        switch (prevBlock / 2) {
                            case 0:
                                newCoords.blockPosX += prevBlock % 2 * 2 - 1;
                                break;
                            case 1:
                                newCoords.blockPosZ += prevBlock % 2 * 2 - 1;
                        }
                    } else {
                        newCoords.blockPosX += prevBlock % 2 * 2 - 1;
                        newCoords.blockPosZ += prevBlock / 2 * 2 - 1;
                    }
                }

                if (!prevMarginList.contains(newCoords) && !marginList.contains(newCoords)) {
                    Block var20 = par1World.getBlock(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ);
                    if (includeFromPlants || var20.getMaterial()
                        .isLiquid()
                        || !var20.getMaterial()
                            .isReplaceable() && var20.getMaterial() != Material.plants) {
                        Block curBlock = par1World
                            .getBlock(newCoords.blockPosX, newCoords.blockPosY, newCoords.blockPosZ);
                        if (curBlock.equals(Blocks.bedrock)) {
                            return false;
                        }

                        if (!curBlock.equals(Blocks.air) && !curBlock.equals(Blocks.flowing_water)
                            && !curBlock.equals(Blocks.flowing_lava)
                            && (includePlants || curBlock.getMaterial()
                                .isLiquid()
                                || !curBlock.getMaterial()
                                    .isReplaceable() && curBlock.getMaterial() != Material.plants)
                            && (!curBlock.equals(Blocks.water) && !curBlock.equals(Blocks.lava) || !par1World
                                .getBlock(newCoords.blockPosX, newCoords.blockPosY + 1, newCoords.blockPosZ)
                                .equals(curBlock))
                            && newMarginList.add(newCoords)) {
                            int blockId = Block.getIdFromBlock(curBlock);
                            Boolean isVolatile = (Boolean) blockVolatilityMap.get(Integer.valueOf(blockId));
                            if (isVolatile == null) {
                                try {
                                    if (curBlock.getClass()
                                        .getMethod(
                                            BlockDummyReobfTracker.canBlockStayMethodName,
                                            new Class[] { World.class, Integer.TYPE, Integer.TYPE, Integer.TYPE })
                                        .getDeclaringClass()
                                        .equals(Block.class)
                                        && curBlock.getClass()
                                            .getMethod(
                                                BlockDummyReobfTracker.onNeighborBlockChange,
                                                new Class[] { World.class, Integer.TYPE, Integer.TYPE, Integer.TYPE,
                                                    Block.class })
                                            .getDeclaringClass()
                                            .equals(Block.class)) {
                                        isVolatile = Boolean.valueOf(false);
                                    } else {
                                        isVolatile = Boolean.valueOf(true);
                                    }
                                } catch (SecurityException var18) {
                                    ;
                                } catch (NoSuchMethodException var19) {
                                    ;
                                }

                                blockVolatilityMap.put(Integer.valueOf(blockId), isVolatile);
                            }

                            if (isVolatile.booleanValue()) {
                                volatileBlockList.add(newCoords);
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

    public class BlockCoord3 {

        int blockPosX;
        int blockPosY;
        int blockPosZ;

        BlockCoord3(int x, int y, int z) {
            this.blockPosX = x;
            this.blockPosY = y;
            this.blockPosZ = z;
        }

        public boolean equals(Object par1Obj) {
            if (!(par1Obj instanceof BlockContagiousSubWorldCreator.BlockCoord3)) {
                return false;
            } else {
                BlockContagiousSubWorldCreator.BlockCoord3 targetCoord = (BlockContagiousSubWorldCreator.BlockCoord3) par1Obj;
                return targetCoord.blockPosX == this.blockPosX && targetCoord.blockPosY == this.blockPosY
                    && targetCoord.blockPosZ == this.blockPosZ;
            }
        }

        public int hashCode() {
            return this.blockPosY + this.blockPosX + (this.blockPosZ << 12) << 8;
        }
    }
}
