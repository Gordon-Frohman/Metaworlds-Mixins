package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.entity.player;

import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.entity.player.EntityPlayerProxy;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.entity.MixinEntityLivingBaseC;

@Mixin(EntityPlayer.class)
public class MixinEntityPlayerC extends MixinEntityLivingBaseC {

    @Shadow(remap = false)
    private boolean isPlayerProxy;

    // TODO

    @Shadow(remap = false)
    private boolean tryLockTransformations() {
        return false;
    }

    @Shadow(remap = false)
    private void releaseTransformationLock() {}

    @Shadow(remap = true)
    protected boolean isPlayer() {
        return false;
    }

    public void setAnglesLocal(float par1, float par2) {
        super.setAngles(par1, par2);
    }

    @SideOnly(Side.CLIENT)
    public void setAngles(float par1, float par2) {
        this.setAnglesLocal(par1, par2);
        if (this.tryLockTransformations()) {
            if (this.isPlayerProxy) {
                EntityPlayerProxy i$ = (EntityPlayerProxy) this;
                EntityPlayer curProxy = i$.getRealPlayer();
                if (curProxy == null) {
                    this.releaseTransformationLock();
                    return;
                }

                curProxy.setAngles(par1, par2);
                Iterator<EntityPlayerProxy> curProxyPlayer = ((IMixinEntity) curProxy).getPlayerProxyMap()
                    .values()
                    .iterator();

                while (curProxyPlayer.hasNext()) {
                    EntityPlayerProxy curProxy1 = (EntityPlayerProxy) curProxyPlayer.next();
                    if (curProxy1 != this) {
                        EntityPlayer curProxyPlayer1 = (EntityPlayer) curProxy1;
                        curProxyPlayer1.setAngles(par1, par2);
                    }
                }
            } else if (this.isPlayer()) {
                Iterator<EntityPlayerProxy> i$1 = ((IMixinEntity) (Object) this).getPlayerProxyMap()
                    .values()
                    .iterator();

                while (i$1.hasNext()) {
                    EntityPlayerProxy curProxy2 = (EntityPlayerProxy) i$1.next();
                    EntityPlayer curProxyPlayer2 = (EntityPlayer) curProxy2;
                    curProxyPlayer2.setAngles(par1, par2);
                }
            }

            this.releaseTransformationLock();
        }
    }

}
