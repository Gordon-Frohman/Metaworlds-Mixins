package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.multiplayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.RenderList;
import net.minecraft.entity.Entity;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import su.sergiusonesimus.metaworlds.MetaworldsMod;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.client.entity.EntityClientPlayerMPSubWorldProxy;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.client.renderer.IMixinRenderGlobal;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorldIntermediate;
import su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.world.MixinWorld;

@Mixin(value = WorldClient.class, priority = 101)
public class MixinWorldClient extends MixinWorld {

    @Shadow(remap = true)
    private Minecraft mc;

    @Shadow(remap = true)
    private IntHashMap entityHashSet;

    @Shadow(remap = true)
    public NetHandlerPlayClient sendQueue;

    public World createSubWorld() {
        return this.createSubWorld(this.getUnoccupiedSubworldID());
    }

    @Override
    public World createSubWorld(double centerX, double centerY, double centerZ, double translationX,
        double translationY, double translationZ, double rotationPitch, double rotationYaw, double rotationRoll,
        double scaling) {
        return this.createSubWorld(
            this.getUnoccupiedSubworldID(),
            centerX,
            centerY,
            centerZ,
            translationX,
            translationY,
            translationZ,
            rotationPitch,
            rotationYaw,
            rotationRoll,
            scaling);
    }

    public World createSubWorld(int newSubWorldID) {
        return this.generateSubWorld(newSubWorldID);
    }

    @Override
    public World createSubWorld(int newSubWorldID, double centerX, double centerY, double centerZ, double translationX,
        double translationY, double translationZ, double rotationPitch, double rotationYaw, double rotationRoll,
        double scaling) {
        World newSubWorld = this.generateSubWorld(newSubWorldID);
        SubWorld subworld = (SubWorld) newSubWorld;

        subworld.setCenter(centerX, centerY, centerZ);
        subworld.setTranslation(translationX, translationY, translationZ);
        subworld.setRotationYaw(rotationYaw);
        subworld.setRotationPitch(rotationPitch);
        subworld.setRotationRoll(rotationRoll);
        subworld.setScaling(scaling);

        return newSubWorld;
    }

    protected World generateSubWorld(int newSubWorldID) {
        World newSubWorld = MetaworldsMod.proxy.createSubWorld(((World) (Object) this), newSubWorldID);
        if (((IMixinWorld) this).getSubWorldsMap()
            .get(((IMixinWorld) newSubWorld).getSubWorldID()) == null) {
            ((IMixinWorld) this).getSubWorldsMap()
                .put(((IMixinWorld) newSubWorld).getSubWorldID(), newSubWorld);

            // The constructor assigns the proxy to the real player
            EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
            EntityClientPlayerMPSubWorldProxy proxyPlayer = new EntityClientPlayerMPSubWorldProxy(player, newSubWorld);
            proxyPlayer.getMinecraft().renderViewEntity = proxyPlayer;

            newSubWorld.addWorldAccess(proxyPlayer.getMinecraft().renderGlobal);
            MinecraftForge.EVENT_BUS.post(new EntityJoinWorldEvent(proxyPlayer, newSubWorld));

            ((IMixinRenderGlobal) this.mc.renderGlobal)
                .loadRenderersForNewSubWorld(((IMixinWorld) newSubWorld).getSubWorldID());
            this.mc.renderGlobal.allRenderLists = new RenderList[4 * ((IMixinWorld) this).getWorldsCount()];
            for (int i = 0; i < 4 * ((IMixinWorld) this).getWorldsCount(); ++i) {
                this.mc.renderGlobal.allRenderLists[i] = new RenderList();
            }
        }
        return newSubWorld;
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

    @WrapOperation(
        method = "getEntityByID",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/IntHashMap;lookup(I)Ljava/lang/Object;"))
    private Object wrapEntityHashSetLookup(IntHashMap instance, int value, Operation<Object> original) {
        Entity result = (Entity) original.call(instance, value);
        if (result == null && !((IMixinWorld) this).isSubWorld())
            for (World curWorld : ((IMixinWorld) this).getSubWorlds()) {
                result = curWorld.getEntityByID(value);
                if (result != null) return result;
            }
        return result;
    }

    @Inject(method = "removeEntityFromWorld", at = { @At(value = "RETURN") })
    public void injectRemoveEntityFromWorld(int entityId, CallbackInfoReturnable<Entity> ci, @Local Entity entity) {
        if (entity == null && !((IMixinWorld) this).isSubWorld()) {
            for (World curSubWorld : ((IMixinWorld) this).getSubWorlds())
                ((WorldClient) curSubWorld).removeEntityFromWorld(entityId);
        }
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

    @WrapOperation(
        method = "spawnEntityInWorld",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;spawnEntityInWorld(Lnet/minecraft/entity/Entity;)Z"))
    public boolean spawnEntityInWorld(WorldClient instance, Entity entity, Operation<Boolean> original) {
        return ((IMixinWorldIntermediate) this).spawnEntityInWorldIntermediate(entity);
    }

    @WrapOperation(
        method = "removeEntity",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;removeEntity(Lnet/minecraft/entity/Entity;)V"))
    public void removeEntity(WorldClient instance, Entity entity, Operation<Void> original) {
        ((IMixinWorldIntermediate) this).removeEntityIntermediate(entity);
    }

    @ModifyArgs(
        method = "playSound",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/audio/PositionedSoundRecord;<init>(Lnet/minecraft/util/ResourceLocation;FFFFF)V"))
    private void modifyPositionedSoundRecord(Args args) {
        Vec3 globalPosition = this.transformToGlobal((float) args.get(3), (float) args.get(4), (float) args.get(5));
        args.set(3, (float) globalPosition.xCoord);
        args.set(4, (float) globalPosition.yCoord);
        args.set(5, (float) globalPosition.zCoord);
    }

}
