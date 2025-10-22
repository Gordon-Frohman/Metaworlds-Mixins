package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.world;

import java.lang.reflect.Method;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import su.sergiusonesimus.metaworlds.MetaworldsMod;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.entity.player.EntityPlayerMPSubWorldProxy;
import su.sergiusonesimus.metaworlds.world.SubWorldInfoHolder;
import su.sergiusonesimus.metaworlds.world.WorldManagerSubWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.server.IMixinMinecraftServer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorldIntermediate;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.storage.IMixinWorldInfo;

@Mixin(WorldServer.class)
public class MixinWorldServer extends MixinWorld {

    // TODO

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/common/DimensionManager;setWorld(ILnet/minecraft/world/WorldServer;)V"))
    public void setWorld(int par1int, WorldServer par2worldServer) {
        if (!((IMixinWorld) par2worldServer).isSubWorld()) DimensionManager.setWorld(par1int, par2worldServer);
    }

    @Redirect(
        method = "tick()V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setWorldTime(J)V"))
    public void setWorldTimeRedirected(WorldInfo worldInfo, long par1long) {
        this.setWorldTime(par1long);
    }

    @Redirect(
        method = "tick()V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;incrementTotalWorldTime(J)V"))
    public void incrementTotalWorldTime(WorldInfo worldInfo, long par1long) {
        this.worldInfo.incrementTotalWorldTime(this.getTotalWorldTime() + 1L);
    }

    @Redirect(
        method = "tick()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/GameRules;getGameRuleBooleanValue(Ljava/lang/String;)Z"))
    public boolean getGameRuleBooleanValue(GameRules gameRules, String string) {
        if (string == "doMobSpawning") return !this.isSubWorld() && this.getGameRules()
            .getGameRuleBooleanValue("doMobSpawning");
        else return gameRules.getGameRuleBooleanValue(string);
    }

    @Inject(method = "saveAllChunks(ZLnet/minecraft/util/IProgressUpdate;)V", at = @At("TAIL"))
    public void saveAllChunks(boolean par1, IProgressUpdate par2IProgressUpdate, CallbackInfo ci) {
        if (!this.isSubWorld()) {
            for (World curSubWorld : this.getSubWorlds()) try {
                ((WorldServer) curSubWorld).saveAllChunks(par1, par2IProgressUpdate);
            } catch (MinecraftException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public World createSubWorld() {
        return this.createSubWorld(this.getUnoccupiedSubworldID());
    }

    @SuppressWarnings("rawtypes")
    public World createSubWorld(int newSubWorldID) {
        World newSubWorld = MetaworldsMod.proxy.createSubWorld((World) (Object) this, newSubWorldID);
        if (((IMixinMinecraftServer) MinecraftServer.mcServer).getExistingSubWorlds()
            .put(((IMixinWorld) newSubWorld).getSubWorldID(), newSubWorld) != null) {
            throw new IllegalArgumentException("SubWorld with this ID already exists!");
        } else this.childSubWorlds.put(((IMixinWorld) newSubWorld).getSubWorldID(), newSubWorld);

        newSubWorld.worldInfo = this.worldInfo;
        ((WorldServer) newSubWorld).difficultySetting = EnumDifficulty.EASY;// Fixes AI crashes

        SubWorldInfoHolder curSubWorldInfo = ((IMixinWorldInfo) DimensionManager.getWorld(0)
            .getWorldInfo()).getSubWorldInfo(((IMixinWorld) newSubWorld).getSubWorldID());
        if (curSubWorldInfo != null) curSubWorldInfo.applyToSubWorld((SubWorld) newSubWorld);

        try {
            Class[] cArg = new Class[1];
            cArg[0] = World.class;
            Method loadWorldMethod = ForgeChunkManager.class.getDeclaredMethod("loadWorld", cArg);
            loadWorldMethod.setAccessible(true);
            try {
                loadWorldMethod.invoke(null, newSubWorld);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e) {
            System.out.println(e.toString());
        }

        newSubWorld.addWorldAccess(
            new WorldManagerSubWorld(((WorldServer) newSubWorld).func_73046_m(), (WorldServer) newSubWorld));

        for (Object curPlayer : this.playerEntities) {
            EntityPlayerMPSubWorldProxy proxyPlayer = (EntityPlayerMPSubWorldProxy) ((IMixinEntity) curPlayer)
                .getPlayerProxyMap()
                .get(((IMixinWorld) newSubWorld).getSubWorldID());

            if (proxyPlayer == null) {
                proxyPlayer = new EntityPlayerMPSubWorldProxy((EntityPlayerMP) curPlayer, newSubWorld);
                // TODO: newManager.setGameType(this.getGameType()); make this synchronized over all proxies and the
                // real player
            }

            proxyPlayer.theItemInWorldManager.setWorld((WorldServer) newSubWorld);

            newSubWorld.spawnEntityInWorld(proxyPlayer);
        }

        for (Object curPlayer : ((WorldServer) (Object) this).getPlayerManager().players) {
            EntityPlayerMPSubWorldProxy proxyPlayer = (EntityPlayerMPSubWorldProxy) ((IMixinEntity) curPlayer)
                .getPlayerProxyMap()
                .get(((IMixinWorld) newSubWorld).getSubWorldID());

            if (proxyPlayer == null) {
                proxyPlayer = new EntityPlayerMPSubWorldProxy((EntityPlayerMP) curPlayer, newSubWorld);
                // TODO: newManager.setGameType(this.getGameType()); make this synchronized over all proxies and the
                // real player
            }

            proxyPlayer.theItemInWorldManager.setWorld((WorldServer) newSubWorld);// make sure the right one is assigned
                                                                                  // if the player is not in
                                                                                  // playerEntities somehow

            ((WorldServer) newSubWorld).getPlayerManager()
                .addPlayer(proxyPlayer);
        }

        MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(newSubWorld));

        return newSubWorld;
    }

    public boolean spawnEntityInWorld(Entity par1Entity) {
        return ((IMixinWorldIntermediate) this).spawnEntityInWorldIntermediate(par1Entity);
    }

    public void removeEntity(Entity par1Entity) {
        ((IMixinWorldIntermediate) this).removeEntityIntermediate(par1Entity);
    }

}
