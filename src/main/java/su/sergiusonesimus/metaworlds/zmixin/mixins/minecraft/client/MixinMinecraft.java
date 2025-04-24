package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client;

import java.io.File;
import java.net.Proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Session;
import net.minecraft.world.World;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

import su.sergiusonesimus.metaworlds.client.MinecraftSubWorldProxy;
import su.sergiusonesimus.metaworlds.client.entity.EntityClientPlayerMPSubWorldProxy;
import su.sergiusonesimus.metaworlds.compat.CompatUtil;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.client.multiplayer.IMixinPlayerControllerMP;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinMovingObjectPosition;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(value = Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow(remap = true)
    public MovingObjectPosition objectMouseOver;

    @Shadow(remap = true)
    public EntityClientPlayerMP thePlayer;

    @Shadow(remap = true)
    public WorldClient theWorld;

    // To get an empty Minecraft variable

    private boolean generateEmpty = false;

    @Inject(
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;theMinecraft:Lnet/minecraft/client/Minecraft;"),
        method = "<init>")
    private void init(Session sessionIn, int displayWidth, int displayHeight, boolean fullscreen, boolean isDemo,
        File dataDir, File assetsDir, File resourcePackDir, Proxy proxy, String version, Multimap twitchDetails,
        String assetsJsonVersion, CallbackInfo info) {
        if (sessionIn == null && displayWidth == 0
            && displayHeight == 0
            && fullscreen == false
            && isDemo == false
            && dataDir == null
            && assetsDir == null
            && resourcePackDir == null
            && version == null
            && twitchDetails == null
            && assetsJsonVersion == null) {
            generateEmpty = true;
        }
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;theMinecraft:Lnet/minecraft/client/Minecraft;",
            opcode = Opcodes.PUTSTATIC))
    private void wrapTheMinecraft(Minecraft value, Operation<Void> original) {
        if (!generateEmpty) original.call(value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;mcDataDir:Ljava/io/File;",
            opcode = Opcodes.PUTFIELD))
    private void wrapMcDataDir(Minecraft instance, File value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;fileAssets:Ljava/io/File;",
            opcode = Opcodes.PUTFIELD))
    private void wrapFileAssets(Minecraft instance, File value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;fileResourcepacks:Ljava/io/File;",
            opcode = Opcodes.PUTFIELD))
    private void wrapFileResourcepacks(Minecraft instance, File value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;launchedVersion:Ljava/lang/String;",
            opcode = Opcodes.PUTFIELD))
    private void wrapLaunchedVersion(Minecraft instance, String value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;field_152356_J:Lcom/google/common/collect/Multimap;",
            opcode = Opcodes.PUTFIELD))
    private void wrapTwitchDetails(Minecraft instance, Multimap value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;mcDefaultResourcePack:Lnet/minecraft/client/resources/DefaultResourcePack;",
            opcode = Opcodes.PUTFIELD))
    private void wrapMcDefaultResourcePack(Minecraft instance, DefaultResourcePack value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;addDefaultResourcePack()V"))
    public void wrapAddDefaultResourcePack(Minecraft instance, Operation<Void> original) {
        if (!generateEmpty) original.call(instance);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;proxy:Ljava/net/Proxy;",
            opcode = Opcodes.PUTFIELD))
    private void wrapProxy(Minecraft instance, Proxy value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;field_152355_az:Lcom/mojang/authlib/minecraft/MinecraftSessionService;",
            opcode = Opcodes.PUTFIELD))
    private void wrapField_152355_az(Minecraft instance, MinecraftSessionService value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;startTimerHackThread()V"))
    public void wrapStartTimerHackThread(Minecraft instance, Operation<Void> original) {
        if (!generateEmpty) original.call(instance);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;session:Lnet/minecraft/util/Session;",
            opcode = Opcodes.PUTFIELD))
    private void wrapSession(Minecraft instance, Session value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;)V"))
    public void wrapLogger_Info(Logger instance, String value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;isDemo:Z", opcode = Opcodes.PUTFIELD))
    private void wrapIsDemo(Minecraft instance, boolean value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;fullscreen:Z", opcode = Opcodes.PUTFIELD))
    private void wrapFullscreen(Minecraft instance, boolean value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;jvm64bit:Z", opcode = Opcodes.PUTFIELD))
    private void wrapJVM64bit(Minecraft instance, boolean value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;displayWidth:I", opcode = Opcodes.PUTFIELD))
    private void wrapDisplayWidth(Minecraft instance, int value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;displayHeight:I",
            opcode = Opcodes.PUTFIELD))
    private void wrapDisplayHeight(Minecraft instance, int value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;tempDisplayWidth:I",
            opcode = Opcodes.PUTFIELD))
    private void wrapTempDisplayWidth(Minecraft instance, int value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;tempDisplayHeight:I",
            opcode = Opcodes.PUTFIELD))
    private void wrapTempDisplayHeight(Minecraft instance, int value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Ljavax/imageio/ImageIO;setUseCache(Z)V"))
    private void wrapSetUseCache(boolean value, Operation<Void> original) {
        if (!generateEmpty) original.call(value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/init/Bootstrap;func_151354_b()V"))
    private void wrapFunc_151354_b(Operation<Void> original) {
        if (!generateEmpty) original.call();
    }

    @WrapOperation(
        method = "<init>",
        at = @At(value = "NEW", target = "Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;"))
    private YggdrasilAuthenticationService yggdrasilAuthenticationServicePlaceholder(Proxy proxy, String clientToken,
        Operation<YggdrasilAuthenticationService> original) {
        if (!generateEmpty) return original.call(proxy, clientToken);
        else return new YggdrasilAuthenticationService(Minecraft.theMinecraft.proxy, clientToken);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;createMinecraftSessionService()Lcom/mojang/authlib/minecraft/MinecraftSessionService;"))
    public MinecraftSessionService disableCreateMinecraftSessionService(YggdrasilAuthenticationService instance,
        Operation<MinecraftSessionService> original) {
        if (!generateEmpty) return original.call(instance);
        else return null;
    }

    @WrapOperation(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Session;getUsername()Ljava/lang/String;"))
    public String disableGetUsername(Session instance, Operation<String> original) {
        if (!generateEmpty) return original.call(instance);
        else return "";
    }

    // Now we should probably have an empty Minecraft variable

    // func_147115_a

    @WrapOperation(
        method = "func_147115_a",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;theWorld:Lnet/minecraft/client/multiplayer/WorldClient;",
            opcode = Opcodes.GETFIELD))
    private WorldClient wrapTheWorld(Minecraft instance, Operation<WorldClient> original) {
        return (WorldClient) ((IMixinMovingObjectPosition) this.objectMouseOver).getWorld();
    }

    @WrapOperation(
        method = "func_147115_a",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;onPlayerDamageBlock(IIII)V"))
    private void wrapOnPlayerDamageBlock(PlayerControllerMP instance, int x, int y, int z, int side,
        Operation<Void> original) {
        ((IMixinPlayerControllerMP) instance)
            .onPlayerDamageBlock(x, y, z, side, ((IMixinMovingObjectPosition) this.objectMouseOver).getWorld());
    }

    @WrapOperation(
        method = "func_147115_a",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/entity/EntityClientPlayerMP;isCurrentToolAdventureModeExempt(III)Z"))
    private boolean wrapIsCurrentToolAdventureModeExempt(EntityClientPlayerMP instance, int p_82246_1_, int p_82246_2_,
        int p_82246_3_, Operation<Boolean> original) {
        return CompatUtil.isCurrentToolAdventureModeExempt(
            instance,
            p_82246_1_,
            p_82246_2_,
            p_82246_3_,
            ((IMixinMovingObjectPosition) this.objectMouseOver).getWorld());
    }

    // func_147116_af

    @Redirect(
        method = { "func_147116_af", "func_147121_ag" },
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/Minecraft;thePlayer:Lnet/minecraft/client/entity/EntityClientPlayerMP;",
            ordinal = 1))
    private EntityClientPlayerMP redirectThePlayer(Minecraft instance) {
        return (EntityClientPlayerMP) ((IMixinEntity) this.thePlayer)
            .getProxyPlayer(((IMixinMovingObjectPosition) this.objectMouseOver).getWorld());
    }

    @Redirect(
        method = { "func_147116_af", "func_147121_ag" },
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/Minecraft;theWorld:Lnet/minecraft/client/multiplayer/WorldClient;",
            ordinal = 0))
    private WorldClient redirectTheWorld1(Minecraft instance) {
        return (WorldClient) ((IMixinMovingObjectPosition) this.objectMouseOver).getWorld();
    }

    @Redirect(
        method = "func_147116_af",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;clickBlock(IIII)V"))
    private void redirectClickBlock(PlayerControllerMP instance, int x, int y, int z, int side) {
        ((IMixinPlayerControllerMP) instance)
            .clickBlock(x, y, z, side, ((IMixinMovingObjectPosition) this.objectMouseOver).getWorld());
    }

    // func_147121_ag

    @Redirect(
        method = "func_147121_ag",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/Minecraft;theWorld:Lnet/minecraft/client/multiplayer/WorldClient;",
            ordinal = 1))
    private WorldClient redirectTheWorld2(Minecraft instance) {
        return (WorldClient) ((IMixinMovingObjectPosition) this.objectMouseOver).getWorld();
    }

    @Redirect(
        method = "func_147121_ag",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/Minecraft;theWorld:Lnet/minecraft/client/multiplayer/WorldClient;",
            ordinal = 3))
    private WorldClient redirectTheWorld3(Minecraft instance) {
        return (WorldClient) ((IMixinMovingObjectPosition) this.objectMouseOver).getWorld();
    }

    // runTick

    @Redirect(
        method = "runTick()V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;updateEntities()V"))
    public void updateEntities(WorldClient world) {
        for (World curWorld : ((IMixinWorld) this.theWorld).getWorlds()) curWorld.updateEntities();
    }

    // func_147112_ai

    @Redirect(
        method = "func_147112_ai",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/Minecraft;theWorld:Lnet/minecraft/client/multiplayer/WorldClient;"))
    private WorldClient redirectTheWorld4(Minecraft instance) {
        return ((IMixinMovingObjectPosition) this.objectMouseOver).getWorld() != null
            ? (WorldClient) ((IMixinMovingObjectPosition) this.objectMouseOver).getWorld()
            : this.theWorld;
    }

    // toggleFullscreen

    /**
     * To fix various GUI bugs caused by a lack of screen size synchronization
     */
    @Inject(
        method = "toggleFullscreen()V",
        at = { @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;func_147120_f()V") })
    public void toggleFullscreen(CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld != null) {
            for (World subworld : ((IMixinWorld) mc.theWorld).getSubWorlds()) {
                EntityClientPlayerMPSubWorldProxy proxy = (EntityClientPlayerMPSubWorldProxy) ((IMixinEntity) mc.thePlayer)
                    .getProxyPlayer(subworld);
                MinecraftSubWorldProxy proxyMC = (MinecraftSubWorldProxy) proxy.getMinecraft();
                proxyMC.fullscreen = mc.fullscreen;
                proxyMC.displayWidth = mc.displayWidth;
                proxyMC.displayHeight = mc.displayHeight;
            }
        }
    }

}
