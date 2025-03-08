package su.sergiusonesimus.metaworlds.block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.item.MetaworldsItems;
import su.sergiusonesimus.metaworlds.world.SubWorldServer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class BlockSubWorldReintegrator extends Block {

    public static Map<Integer, Boolean> blockVolatilityMap = new TreeMap();

    public BlockSubWorldReintegrator() {
        super(Material.rock);
        this.setCreativeTab(MetaworldsItems.creativeTab);
    }

    public void onBlockAdded(World par1World, int par2, int par3, int par4) {
        if (!BlockContagiousSubWorldCreator.isBusy) {
            BlockContagiousSubWorldCreator.isBusy = true;
            if (((IMixinWorld) par1World).isSubWorld()) {
                SubWorld subWorldPar = (SubWorld) par1World;
                double oldCenterX = subWorldPar.getCenterX();
                double oldCenterY = subWorldPar.getCenterY();
                double oldCenterZ = subWorldPar.getCenterZ();
                subWorldPar.setRotationYaw((double) Math.round(subWorldPar.getRotationYaw() / 90.0D) * 90.0D);
                subWorldPar.setRotationPitch(0.0D);
                subWorldPar.setRotationRoll(0.0D);
                subWorldPar.setTranslation(
                    (double) Math.round(subWorldPar.getTranslationX()),
                    (double) Math.round(subWorldPar.getTranslationY()),
                    (double) Math.round(subWorldPar.getTranslationZ()));
                subWorldPar.setScaling(1.0D);
                subWorldPar.setCenter(0.0D, 0.0D, 0.0D);
                subWorldPar.setMotion(0.0D, 0.0D, 0.0D);
                subWorldPar.setRotationYawSpeed(0.0D);
                subWorldPar.setRotationPitchSpeed(0.0D);
                subWorldPar.setRotationRollSpeed(0.0D);
                subWorldPar.setScaleChangeRate(0.0D);
                byte facingDirection = (byte) ((int) ((Math.round(subWorldPar.getRotationYaw() / 90.0D) % 4L + 4L)
                    % 4L));
                long translationX = Math.round(subWorldPar.getTranslationX());
                long translationY = Math.round(subWorldPar.getTranslationY());
                long translationZ = Math.round(subWorldPar.getTranslationZ());
                byte[] xzTransfMatrix = null;
                switch (facingDirection) {
                    case 0:
                        xzTransfMatrix = new byte[] { (byte) 1, (byte) 0, (byte) 0, (byte) 1 };
                        break;
                    case 1:
                        xzTransfMatrix = new byte[] { (byte) 0, (byte) 1, (byte) -1, (byte) 0 };
                        --translationZ;
                        break;
                    case 2:
                        xzTransfMatrix = new byte[] { (byte) -1, (byte) 0, (byte) 0, (byte) -1 };
                        --translationX;
                        --translationZ;
                        break;
                    case 3:
                        xzTransfMatrix = new byte[] { (byte) 0, (byte) -1, (byte) 1, (byte) 0 };
                        --translationX;
                }

                ArrayList blocksToTake = new ArrayList();
                ArrayList blocksToTakeVolatile = new ArrayList();
                HashSet prevMargin = new HashSet();
                HashSet margin = new HashSet();
                HashSet newMargin = new HashSet();
                blocksToTake.add(new BlockSubWorldReintegrator.BlockCoord3(par2, par3, par4));
                margin.add(new BlockSubWorldReintegrator.BlockCoord3(par2, par3, par4));
                boolean isValid = true;

                do {
                    isValid = this
                        .expandAtMargin(par1World, blocksToTake, blocksToTakeVolatile, prevMargin, margin, newMargin);
                    if (!isValid) {
                        break;
                    }

                    HashSet parentWorld = prevMargin;
                    prevMargin = margin;
                    margin = newMargin;
                    newMargin = parentWorld;
                    parentWorld.clear();
                } while (margin.size() > 0);

                if (isValid) {
                    World var31 = ((IMixinWorld) par1World).getParentWorld();
                    Iterator i$ = blocksToTake.iterator();

                    BlockSubWorldReintegrator.BlockCoord3 curCoord;
                    Block block;
                    int blockMetadata;
                    while (i$.hasNext()) {
                        curCoord = (BlockSubWorldReintegrator.BlockCoord3) i$.next();
                        block = par1World.getBlock(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ);
                        blockMetadata = par1World
                            .getBlockMetadata(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ);
                        var31.setBlock(
                            (int) (translationX + (long) (xzTransfMatrix[0] * curCoord.blockPosX)
                                + (long) (xzTransfMatrix[1] * curCoord.blockPosZ)),
                            (int) (translationY + (long) curCoord.blockPosY),
                            (int) (translationZ + (long) (xzTransfMatrix[2] * curCoord.blockPosX)
                                + (long) (xzTransfMatrix[3] * curCoord.blockPosZ)),
                            block,
                            blockMetadata,
                            3);
                    }

                    i$ = blocksToTakeVolatile.iterator();

                    while (i$.hasNext()) {
                        curCoord = (BlockSubWorldReintegrator.BlockCoord3) i$.next();
                        block = par1World.getBlock(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ);
                        blockMetadata = par1World
                            .getBlockMetadata(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ);
                        var31.setBlock(
                            (int) (translationX + (long) (xzTransfMatrix[0] * curCoord.blockPosX)
                                + (long) (xzTransfMatrix[1] * curCoord.blockPosZ)),
                            (int) (translationY + (long) curCoord.blockPosY),
                            (int) (translationZ + (long) (xzTransfMatrix[2] * curCoord.blockPosX)
                                + (long) (xzTransfMatrix[3] * curCoord.blockPosZ)),
                            block,
                            blockMetadata,
                            3);
                    }

                    i$ = blocksToTakeVolatile.iterator();

                    while (i$.hasNext()) {
                        curCoord = (BlockSubWorldReintegrator.BlockCoord3) i$.next();
                        par1World.setBlockToAir(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ);
                    }

                    i$ = blocksToTake.iterator();

                    while (i$.hasNext()) {
                        curCoord = (BlockSubWorldReintegrator.BlockCoord3) i$.next();
                        par1World.setBlockToAir(curCoord.blockPosX, curCoord.blockPosY, curCoord.blockPosZ);
                    }
                }

                subWorldPar.setCenter(oldCenterX, oldCenterY, oldCenterZ);
                if (subWorldPar instanceof SubWorldServer && ((SubWorldServer) subWorldPar).isEmpty()) {
                    SubWorldServer subWorldServer = ((SubWorldServer) subWorldPar);
                    ((IMixinWorld) subWorldServer.getParentWorld()).getSubWorldsMap()
                        .remove(subWorldServer.getSubWorldID());
                    subWorldServer.removeSubWorld();
                    subWorldServer.flush();
                    subWorldServer.deleteSubWorldDirectory();
                }
            }
            BlockContagiousSubWorldCreator.isBusy = false;
        }
    }

    public boolean expandAtMargin(World par1World, List<BlockSubWorldReintegrator.BlockCoord3> blockList,
        List<BlockSubWorldReintegrator.BlockCoord3> volatileBlockList,
        HashSet<BlockSubWorldReintegrator.BlockCoord3> prevMarginList,
        HashSet<BlockSubWorldReintegrator.BlockCoord3> marginList,
        HashSet<BlockSubWorldReintegrator.BlockCoord3> newMarginList) {
        Iterator i$ = marginList.iterator();

        while (i$.hasNext()) {
            BlockSubWorldReintegrator.BlockCoord3 curCoord = (BlockSubWorldReintegrator.BlockCoord3) i$.next();

            for (int direction = 0; direction < 6; ++direction) {
                BlockSubWorldReintegrator.BlockCoord3 newCoords = new BlockSubWorldReintegrator.BlockCoord3(
                    curCoord.blockPosX,
                    curCoord.blockPosY,
                    curCoord.blockPosZ);
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

                if (!prevMarginList.contains(newCoords) && !marginList.contains(newCoords)) {
                    Block curBlock = par1World.getBlock(newCoords.blockPosX, newCoords.blockPosY, newCoords.blockPosZ);
                    if (curBlock.equals(Blocks.bedrock)) {
                        return false;
                    }

                    if (!curBlock.equals(Blocks.air) && !curBlock.equals(Blocks.flowing_water)
                        && !curBlock.equals(Blocks.flowing_lava)
                        && (!curBlock.equals(Blocks.water) && !curBlock.equals(Blocks.lava)
                            || !par1World.getBlock(newCoords.blockPosX, newCoords.blockPosY + 1, newCoords.blockPosZ)
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
                            } catch (SecurityException var15) {
                                ;
                            } catch (NoSuchMethodException var16) {
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
            if (!(par1Obj instanceof BlockSubWorldReintegrator.BlockCoord3)) {
                return false;
            } else {
                BlockSubWorldReintegrator.BlockCoord3 targetCoord = (BlockSubWorldReintegrator.BlockCoord3) par1Obj;
                return targetCoord.blockPosX == this.blockPosX && targetCoord.blockPosY == this.blockPosY
                    && targetCoord.blockPosZ == this.blockPosZ;
            }
        }

        public int hashCode() {
            return this.blockPosY + this.blockPosX + (this.blockPosZ << 12) << 8;
        }
    }
}
