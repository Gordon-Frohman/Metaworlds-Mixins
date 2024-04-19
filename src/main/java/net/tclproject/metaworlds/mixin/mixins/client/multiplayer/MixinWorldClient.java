package net.tclproject.metaworlds.mixin.mixins.client.multiplayer;

import java.util.Random;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.RenderList;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.tclproject.metaworlds.api.IMixinWorld;
import net.tclproject.metaworlds.core.SubWorldClientFactory;
import net.tclproject.metaworlds.mixin.interfaces.client.multiplayer.IMixinWorldClient;
import net.tclproject.metaworlds.mixin.interfaces.client.renderer.IMixinRenderGlobal;
import net.tclproject.metaworlds.patcher.EntityClientPlayerMPSubWorldProxy;
import net.tclproject.metaworlds.patcher.SubWorldFactory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldClient.class)
public abstract class MixinWorldClient implements IMixinWorldClient {

    public int worldDimension;
    public static SubWorldFactory subWorldFactory = null;

    @Shadow(remap = true)
    private Minecraft mc;

    @Shadow(remap = true)
    private IntHashMap entityHashSet;

    @Shadow(remap = true)
    private Set entityList;

    @Shadow(remap = true)
    private Random rand;

    @Shadow(remap = true)
    public WorldProvider provider;

    @Shadow(remap = true)
    public NetHandlerPlayClient sendQueue;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void WorldClient(NetHandlerPlayClient p_i45063_1_, WorldSettings p_i45063_2_, int p_i45063_3_,
        EnumDifficulty p_i45063_4_, Profiler p_i45063_5_, CallbackInfo ci) {
        this.worldDimension = p_i45063_3_;
        if (subWorldFactory == null)
        	subWorldFactory = new SubWorldClientFactory();
    }

    public World CreateSubWorld() {
        return this.CreateSubWorld(((IMixinWorld) this).getWorldsCount());
    }

    public World CreateSubWorld(int newSubWorldID) {
        if (this.subWorldFactory == null) return null;
        World newSubWorld = this.subWorldFactory.CreateSubWorld(((World) (Object) this), newSubWorldID);
        if (((IMixinWorld) this).getSubWorldsMap()
            .get(((IMixinWorld) newSubWorld).getSubWorldID()) == null) {
            ((IMixinWorld) this).getSubWorldsMap()
                .put(((IMixinWorld) newSubWorld).getSubWorldID(), newSubWorld);

            // The constructor assigns the proxy to the real player
            EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
            EntityClientPlayerMPSubWorldProxy proxyPlayer = new EntityClientPlayerMPSubWorldProxy(player, newSubWorld);
            proxyPlayer.getMinecraft().renderViewEntity = proxyPlayer;

            newSubWorld.addWorldAccess(proxyPlayer.getMinecraft().renderGlobal);

            ((IMixinRenderGlobal) this.mc.renderGlobal)
                .loadRenderersForNewSubWorld(((IMixinWorld) newSubWorld).getSubWorldID());
            this.mc.renderGlobal.allRenderLists = new RenderList[4 * ((IMixinWorld) this).getWorldsCount()];
            for (int i = 0; i < 4 * ((IMixinWorld) this).getWorldsCount(); ++i) {
                this.mc.renderGlobal.allRenderLists[i] = new RenderList();
            }
        }
        return newSubWorld;
    }

    public Minecraft getMinecraft() {
        return this.mc;
    }

    public void setMinecraft(Minecraft newMinecraft) {
        this.mc = newMinecraft;
    }
    
    public void setSubworldFactory(SubWorldFactory subWorldFactory) {
    	this.subWorldFactory = subWorldFactory;
    }
    
    public SubWorldFactory getSubworldFactory() {
    	return subWorldFactory;
    }

    /**
     * Runs a single tick for the world
     */
    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        for (World curSubWorld : ((IMixinWorld) this).getSubWorlds()) {
            curSubWorld.tick();
        }
    }

    /**
     * Returns the Entity with the given ID, or null if it doesn't exist in this World.
     */
    @Overwrite
    public Entity getEntityByID(int par1) {
        if (par1 == this.mc.thePlayer.getEntityId()) return this.mc.thePlayer;
        Entity result = (Entity) this.entityHashSet.lookup(par1);
        if (result == null && !((IMixinWorld) this).isSubWorld())
            for (World curWorld : ((IMixinWorld) this).getSubWorlds()) {
                result = curWorld.getEntityByID(par1);
                if (result != null) return result;
            }
        return result;
    }

    @Overwrite
    public Entity removeEntityFromWorld(int p_73028_1_) {
        Entity entity = (Entity) this.entityHashSet.removeObject(p_73028_1_);

        if (entity != null) {
            this.entityList.remove(entity);
            this.removeEntity(entity);
        } else if (!((IMixinWorld) this).isSubWorld()) {
            for (World curSubWorld : ((IMixinWorld) this).getSubWorlds())
                ((WorldClient) curSubWorld).removeEntityFromWorld(p_73028_1_);
        }
        return entity;
    }

    @Inject(method = "doVoidFogParticles", at = @At("TAIL"))
    public void doVoidFogParticles(int p_73029_1_, int p_73029_2_, int p_73029_3_, CallbackInfo ci) {
        for (World curSubWorld : ((IMixinWorld) this).getSubWorlds()) {
            Vec3 transformedPos = ((IMixinWorld) curSubWorld).transformToLocal(p_73029_1_, p_73029_2_, p_73029_3_);
            ((WorldClient) curSubWorld).doVoidFogParticles(
                MathHelper.floor_double(transformedPos.xCoord),
                MathHelper.floor_double(transformedPos.yCoord),
                MathHelper.floor_double(transformedPos.zCoord));
        }
    }

    public NetHandlerPlayClient getSendQueue() {
        return this.sendQueue;
    }

    @Shadow(remap = true)
    abstract void removeEntity(Entity entity);

}
