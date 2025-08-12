package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.world.chunk.storage;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.world.chunk.ChunkSubWorld;
import su.sergiusonesimus.metaworlds.world.chunk.storage.ExtendedBlockStorageSubWorld;

@Mixin(AnvilChunkLoader.class)
public class MixinAnvilChunkLoader {

    @Inject(
        method = "writeChunkToNBT",
        at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;getSkylightArray()Lnet/minecraft/world/chunk/NibbleArray;",
            shift = Shift.AFTER),
        locals = LocalCapture.CAPTURE_FAILHARD)
    public void setSkylightArrays(Chunk chunk, World world, NBTTagCompound nbt, CallbackInfo ci,
        @Local(name = "extendedblockstorage") ExtendedBlockStorage extendedblockstorage,
        @Local(name = "nbttagcompound1") NBTTagCompound nbttagcompound1) {
        if (world instanceof SubWorld) {
            ForgeDirection[] directions = { ForgeDirection.DOWN, ForgeDirection.EAST, ForgeDirection.WEST,
                ForgeDirection.SOUTH, ForgeDirection.NORTH };
            for (ForgeDirection direction : directions) {
                nbttagcompound1.setByteArray(
                    "SkyLight" + direction.toString(),
                    ((ExtendedBlockStorageSubWorld) extendedblockstorage).getSkylightArray(direction).data);
            }
        }
    }

    @Inject(
        method = "writeChunkToNBT",
        at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;getBlocklightArray()Lnet/minecraft/world/chunk/NibbleArray;",
            shift = Shift.AFTER,
            ordinal = 1),
        locals = LocalCapture.CAPTURE_FAILHARD)
    public void setEmptySkylightArrays(Chunk chunk, World world, NBTTagCompound nbt, CallbackInfo ci,
        @Local(name = "extendedblockstorage") ExtendedBlockStorage extendedblockstorage,
        @Local(name = "nbttagcompound1") NBTTagCompound nbttagcompound1) {
        if (world instanceof SubWorld) {
            ForgeDirection[] directions = { ForgeDirection.DOWN, ForgeDirection.EAST, ForgeDirection.WEST,
                ForgeDirection.SOUTH, ForgeDirection.NORTH };
            for (ForgeDirection direction : directions) {
                nbttagcompound1.setByteArray(
                    "SkyLight" + direction.toString(),
                    new byte[extendedblockstorage.getBlocklightArray().data.length]);
            }
        }
    }

    private World storedWorld;

    @Inject(method = "readChunkFromNBT", at = @At(value = "HEAD"))
    public void storeVariables(World world, NBTTagCompound nbt, CallbackInfoReturnable<Chunk> ci) {
        storedWorld = world;
    }

    @WrapOperation(method = "readChunkFromNBT", at = @At(value = "NEW", target = "Lnet/minecraft/world/chunk/Chunk;"))
    public Chunk getChunk(World world, int xPos, int zPos, Operation<Chunk> original) {
        if (world instanceof SubWorld) return new ChunkSubWorld(world, xPos, zPos);
        else return original.call(world, xPos, zPos);
    }

    @WrapOperation(
        method = "readChunkFromNBT",
        at = @At(value = "NEW", target = "Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;"))
    public ExtendedBlockStorage getExtendedBlockStorage(int yMin, boolean flag,
        Operation<ExtendedBlockStorage> original) {
        if (storedWorld instanceof SubWorld) return new ExtendedBlockStorageSubWorld(yMin, flag);
        else return original.call(yMin, flag);
    }

    @Inject(
        method = "readChunkFromNBT",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;setSkylightArray(Lnet/minecraft/world/chunk/NibbleArray;)V",
            shift = Shift.AFTER),
        locals = LocalCapture.CAPTURE_FAILHARD)
    public void setSkylightArrays(World world, NBTTagCompound nbt, CallbackInfoReturnable<Chunk> ci,
        @Local(name = "chunk") Chunk chunk, @Local(name = "nbttagcompound1") NBTTagCompound nbttagcompound1,
        @Local(name = "extendedblockstorage") ExtendedBlockStorage extendedblockstorage) {
        if (world instanceof SubWorld) {
            ExtendedBlockStorageSubWorld ebs = (ExtendedBlockStorageSubWorld) extendedblockstorage;
            if (!ebs.isEmpty())
                ((SubWorld) world).registerExtendedBlockStorage(ebs, chunk.xPosition, ebs.yBase / 16, chunk.zPosition);
            ForgeDirection[] directions = { ForgeDirection.DOWN, ForgeDirection.EAST, ForgeDirection.WEST,
                ForgeDirection.SOUTH, ForgeDirection.NORTH };
            for (ForgeDirection direction : directions) {
                byte[] light = nbttagcompound1.getByteArray("SkyLight" + direction.toString());
                if (light.length < 4096) light = new byte[4096];
                ebs.setSkylightArray(direction, new NibbleArray(light, 4));
            }
        }
    }

    private boolean wasEmpty;

    @Inject(
        method = "readChunkFromNBT",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;removeInvalidBlocks()V",
            shift = Shift.BEFORE),
        locals = LocalCapture.CAPTURE_FAILHARD)
    public void removeInvalidBlocksBefore(World world, NBTTagCompound nbt, CallbackInfoReturnable<Chunk> ci,
        @Local(name = "extendedblockstorage") ExtendedBlockStorage extendedblockstorage) {
        if (world instanceof SubWorld) {
            wasEmpty = extendedblockstorage.isEmpty();
        }
    }

    @Inject(
        method = "readChunkFromNBT",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;removeInvalidBlocks()V",
            shift = Shift.AFTER),
        locals = LocalCapture.CAPTURE_FAILHARD)
    public void removeInvalidBlocksAfter(World world, NBTTagCompound nbt, CallbackInfoReturnable<Chunk> ci,
        @Local(name = "chunk") Chunk chunk, @Local(name = "b1") byte b1,
        @Local(name = "extendedblockstorage") ExtendedBlockStorage extendedblockstorage) {
        if (world instanceof SubWorld) {
            SubWorld subWorldObj = (SubWorld) world;
            boolean isEmpty = extendedblockstorage.isEmpty();
            if (wasEmpty != isEmpty) {
                if (!isEmpty) {
                    subWorldObj.registerExtendedBlockStorage(
                        (ExtendedBlockStorageSubWorld) extendedblockstorage,
                        chunk.xPosition,
                        b1,
                        chunk.zPosition);
                } else {
                    subWorldObj.unregisterExtendedBlockStorage(chunk.xPosition, b1, chunk.zPosition);
                }
            }
        }
    }

}
