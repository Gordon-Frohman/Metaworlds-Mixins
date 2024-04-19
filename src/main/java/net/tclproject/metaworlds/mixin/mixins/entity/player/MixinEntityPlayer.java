package net.tclproject.metaworlds.mixin.mixins.entity.player;

import java.util.Iterator;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.tclproject.metaworlds.api.IMixinEntity;
import net.tclproject.metaworlds.api.IMixinWorld;
import net.tclproject.metaworlds.mixin.interfaces.entity.player.IMixinEntityPlayer;
import net.tclproject.metaworlds.patcher.EntityPlayerProxy;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer implements IMixinEntityPlayer {

    private final boolean isProxyPlayer = this instanceof EntityPlayerProxy;

    @Shadow(remap = true)
    public World worldObj;

    @Shadow(remap = true)
    public ChunkCoordinates playerLocation;

    @Shadow(remap = true)
    protected boolean sleeping;

    @Overwrite
    public boolean isInBed() {
        if (this.worldObj.getBlock(this.playerLocation.posX, this.playerLocation.posY, this.playerLocation.posZ)
            .isBed(
                worldObj,
                playerLocation.posX,
                playerLocation.posY,
                playerLocation.posZ,
                (EntityLivingBase) (Object) this)) {
            return true;
        } else {
            if (!((IMixinWorld) this.worldObj).isSubWorld()) {
                Iterator i$ = ((IMixinEntity) (Object) this).getPlayerProxyMap()
                    .values()
                    .iterator();

                while (i$.hasNext()) {
                    EntityPlayerProxy curProxy = (EntityPlayerProxy) i$.next();
                    if (((EntityPlayer) curProxy).isPlayerSleeping() && ((EntityPlayer) curProxy).isInBed()) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public void setSleeping(boolean newState) {
        this.sleeping = newState;
    }

    @Inject(method = "wakeUpPlayer", at = @At("TAIL"))
    public void wakeUpPlayer(boolean par1, boolean par2, boolean par3, CallbackInfo ci) {
        if (!this.isProxyPlayer) {
            Iterator i$ = ((IMixinEntity) (Object) this).getPlayerProxyMap()
                .values()
                .iterator();

            while (i$.hasNext()) {
                EntityPlayerProxy curPlayerProxy = (EntityPlayerProxy) i$.next();
                ((EntityPlayer) curPlayerProxy).wakeUpPlayer(par1, par2, par3);
            }
        }
    }

    /**
     * returns true if this entity is by a ladder, false otherwise
     */
    public boolean isOnLadderOriginal() {
        int i = MathHelper.floor_double(((Entity) (Object) this).posX);
        int j = MathHelper.floor_double(((Entity) (Object) this).boundingBox.minY);
        int k = MathHelper.floor_double(((Entity) (Object) this).posZ);
        Block block = this.worldObj.getBlock(i, j, k);
        return ForgeHooks.isLivingOnLadder(block, worldObj, i, j, k, (EntityLivingBase) (Object) this);
    }

    @Overwrite
    public boolean isOnLadder() {
        if (this.isProxyPlayer) {
            return ((EntityPlayerProxy) this).getRealPlayer()
                .isOnLadder();
        } else if (this.isOnLadderOriginal()) {
            return true;
        } else {
            Iterator i$ = ((IMixinEntity) (Object) this).getPlayerProxyMap()
                .values()
                .iterator();

            EntityPlayerProxy curPlayerProxy;
            do {
                if (!i$.hasNext()) {
                    return false;
                }

                curPlayerProxy = (EntityPlayerProxy) i$.next();
            } while (!((MixinEntityPlayer) curPlayerProxy).isOnLadderLocal());

            return true;
        }
    }

    public boolean isOnLadderLocal() {
        return this.isOnLadderOriginal();
    }

    public boolean shouldRenderInPass(int pass) {
        return ((IMixinWorld) this.worldObj).isSubWorld() ? false
            : ((EntityPlayer) (Object) this).shouldRenderInPass(pass);
    }
}
