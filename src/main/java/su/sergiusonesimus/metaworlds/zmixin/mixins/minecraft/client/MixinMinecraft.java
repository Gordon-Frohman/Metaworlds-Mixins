package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client;

import java.io.File;
import java.net.Proxy;
import java.util.Queue;
import java.util.UUID;

import javax.imageio.ImageIO;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.ResourceIndex;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Session;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Multimap;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

import su.sergiusonesimus.metaworlds.client.MinecraftSubWorldProxy;
import su.sergiusonesimus.metaworlds.client.entity.EntityClientPlayerMPSubWorldProxy;
import su.sergiusonesimus.metaworlds.compat.CompatUtil;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.client.multiplayer.IMixinPlayerControllerMP;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.util.IMixinMovingObjectPosition;

@Mixin(value = Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow(remap = true)
    private int leftClickCounter;

    @Shadow(remap = true)
    public MovingObjectPosition objectMouseOver;

    @Shadow(remap = true)
    public PlayerControllerMP playerController;

    @Shadow(remap = true)
    public EntityClientPlayerMP thePlayer;

    @Shadow(remap = true)
    public EffectRenderer effectRenderer;

    @Shadow(remap = true)
    private static Logger logger;

    @Shadow(remap = true)
    private int rightClickDelayTimer;

    @Shadow(remap = true)
    public WorldClient theWorld;

    @Shadow(remap = true)
    public EntityRenderer entityRenderer;

    @Shadow(remap = true)
    public Profiler mcProfiler;

    @Shadow(remap = true)
    private Queue field_152351_aB;

    @Shadow(remap = true)
    private boolean isGamePaused;

    @Shadow(remap = true)
    public GuiIngame ingameGUI;

    @Shadow(remap = true)
    public TextureManager renderEngine;

    @Shadow(remap = true)
    public GuiScreen currentScreen;

    @Shadow(remap = true)
    long systemTime;

    @Shadow(remap = true)
    public GameSettings gameSettings;

    @Shadow(remap = true)
    public boolean inGameHasFocus;

    @Shadow(remap = true)
    private long field_83002_am;

    @Shadow(remap = true)
    public RenderGlobal renderGlobal;

    @Shadow(remap = true)
    private int joinPlayerCounter;

    @Shadow(remap = true)
    private SoundHandler mcSoundHandler;

    @Shadow(remap = true)
    private MusicTicker mcMusicTicker;

    @Shadow(remap = true)
    private NetworkManager myNetworkManager;

    @Shadow(remap = true)
    public File mcDataDir;

    @Shadow(remap = true)
    public File fileAssets;

    @Shadow(remap = true)
    public String launchedVersion;

    @Shadow(remap = true)
    public Proxy proxy;

    @Shadow(remap = true)
    public boolean jvm64bit;

    @Shadow(remap = true)
    public boolean isDemo;

    @Shadow(remap = true)
    public File fileResourcepacks;

    @Shadow(remap = true)
    public Multimap field_152356_J;

    @Shadow(remap = true)
    public DefaultResourcePack mcDefaultResourcePack;

    @Shadow(remap = true)
    public MinecraftSessionService field_152355_az;

    @Shadow(remap = true)
    public Session session;

    @Shadow(remap = true)
    public int displayWidth;

    @Shadow(remap = true)
    public int displayHeight;

    @Shadow(remap = true)
    public boolean fullscreen;

    @Shadow(remap = true)
    public int tempDisplayWidth;

    @Shadow(remap = true)
    public int tempDisplayHeight;

    // TODO

    @Shadow(remap = true)
    private void startTimerHackThread() {}

    @Shadow(remap = true)
    private void addDefaultResourcePack() {}

    @Shadow(remap = true)
    public abstract void displayGuiScreen(GuiScreen p_147108_1_);

    @Shadow(remap = true)
    public abstract void setIngameFocus();

    @Shadow(remap = true)
    public abstract void func_152348_aa();

    @Shadow(remap = true)
    public abstract void displayInGameMenu();

    @Shadow(remap = true)
    public abstract void refreshResources();

    @Shadow(remap = true)
    protected abstract void updateDebugProfilerName(int p_71383_1_);

    @Shadow(remap = true)
    public abstract NetHandlerPlayClient getNetHandler();

    // To get an empty Minecraft variable
    // Firstly - disable the original constructor

    @Redirect(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;theMinecraft:Lnet/minecraft/client/Minecraft;",
            opcode = Opcodes.PUTSTATIC))
    private void disableTheMinecraft(Minecraft value) {
        // Do nothing
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;mcDataDir:Ljava/io/File;",
            opcode = Opcodes.PUTFIELD))
    private void disableMcDataDir(Minecraft minecraft, File value) {
        // Do nothing
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;fileAssets:Ljava/io/File;",
            opcode = Opcodes.PUTFIELD))
    private void disableFileAssets(Minecraft minecraft, File value) {
        // Do nothing
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;fileResourcepacks:Ljava/io/File;",
            opcode = Opcodes.PUTFIELD))
    private void disableFileResourcepacks(Minecraft minecraft, File value) {
        // Do nothing
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;launchedVersion:Ljava/lang/String;",
            opcode = Opcodes.PUTFIELD))
    private void disableLaunchedVersion(Minecraft minecraft, String value) {
        // Do nothing
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;field_152356_J:Lcom/google/common/collect/Multimap;",
            opcode = Opcodes.PUTFIELD))
    private void disableField_152356_J(Minecraft minecraft, Multimap value) {
        // Do nothing
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;mcDefaultResourcePack:Lnet/minecraft/client/resources/DefaultResourcePack;",
            opcode = Opcodes.PUTFIELD))
    private void disableMcDefaultResourcePack(Minecraft minecraft, DefaultResourcePack value) {
        // Do nothing
    }

    @Redirect(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;addDefaultResourcePack()V"))
    public void disableAddDefaultResourcePack(Minecraft minecraft) {
        // Do nothing
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;proxy:Ljava/net/Proxy;",
            opcode = Opcodes.PUTFIELD))
    private void disableProxy(Minecraft minecraft, Proxy value) {
        // Do nothing
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;field_152355_az:Lcom/mojang/authlib/minecraft/MinecraftSessionService;",
            opcode = Opcodes.PUTFIELD))
    private void disableField_152355_az(Minecraft minecraft, MinecraftSessionService value) {
        // Do nothing
    }

    @Redirect(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;startTimerHackThread()V"))
    public void disableStartTimerHackThread(Minecraft minecraft) {
        // Do nothing
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;session:Lnet/minecraft/util/Session;",
            opcode = Opcodes.PUTFIELD))
    private void disableSession(Minecraft minecraft, Session value) {
        // Do nothing
    }

    @Redirect(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;)V"))
    public void disableLogger_Info(Logger logger, String s) {
        // Do nothing
    }

    @Redirect(
        method = "<init>",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;isDemo:Z", opcode = Opcodes.PUTFIELD))
    private void disableIsDemo(Minecraft minecraft, boolean value) {
        // Do nothing
    }

    @Redirect(
        method = "<init>",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;fullscreen:Z", opcode = Opcodes.PUTFIELD))
    private void disableFullscreen(Minecraft minecraft, boolean value) {
        // Do nothing
    }

    @Redirect(
        method = "<init>",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;jvm64bit:Z", opcode = Opcodes.PUTFIELD))
    private void disableJVM64bit(Minecraft minecraft, boolean value) {
        // Do nothing
    }

    @Redirect(
        method = "<init>",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;displayWidth:I", opcode = Opcodes.PUTFIELD))
    private void disableDisplayWidth(Minecraft minecraft, int value) {
        // Do nothing
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;displayHeight:I",
            opcode = Opcodes.PUTFIELD))
    private void disableDisplayHeight(Minecraft minecraft, int value) {
        // Do nothing
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;tempDisplayWidth:I",
            opcode = Opcodes.PUTFIELD))
    private void disableTempDisplayWidth(Minecraft minecraft, int value) {
        // Do nothing
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;tempDisplayHeight:I",
            opcode = Opcodes.PUTFIELD))
    private void disableTempDisplayHeight(Minecraft minecraft, int value) {
        // Do nothing
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljavax/imageio/ImageIO;setUseCache(Z)V"))
    private void disableSetUseCache(boolean b) {
        // Do nothing
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/init/Bootstrap;func_151354_b()V"))
    private void disableFunc_151354_b() {
        // Do nothing
    }

    @Redirect(
        method = "<init>",
        at = @At(value = "NEW", target = "Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;"))
    private YggdrasilAuthenticationService yggdrasilAuthenticationServicePlaceholder(Proxy proxy, String clientToken) {
        return new YggdrasilAuthenticationService(Minecraft.theMinecraft.proxy, clientToken);
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;createMinecraftSessionService()Lcom/mojang/authlib/minecraft/MinecraftSessionService;"))
    public MinecraftSessionService disableCreateMinecraftSessionService(
        YggdrasilAuthenticationService yggdrasilAuthenticationService) {
        // Do nothing, yet
        return null;
    }

    @Redirect(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Session;getUsername()Ljava/lang/String;"))
    public String disableGetUsername(Session session) {
        // Do nothing, yet
        return "";
    }

    // And secondly - set up a new constructor

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
            && assetsJsonVersion == null) {} else {
            Minecraft.theMinecraft = (Minecraft) (Object) this;
            this.mcDataDir = dataDir;
            this.fileAssets = assetsDir;
            this.fileResourcepacks = resourcePackDir;
            this.launchedVersion = version;
            this.field_152356_J = twitchDetails;
            this.mcDefaultResourcePack = new DefaultResourcePack(
                (new ResourceIndex(assetsDir, assetsJsonVersion)).func_152782_a());
            this.addDefaultResourcePack();
            this.proxy = proxy == null ? Proxy.NO_PROXY : proxy;
            this.field_152355_az = (new YggdrasilAuthenticationService(
                proxy,
                UUID.randomUUID()
                    .toString())).createMinecraftSessionService();
            this.startTimerHackThread();
            this.session = sessionIn;
            this.isDemo = isDemo;
            this.displayWidth = displayWidth;
            this.displayHeight = displayHeight;
            this.tempDisplayWidth = displayWidth;
            this.tempDisplayHeight = displayHeight;
            this.fullscreen = fullscreen;
            this.jvm64bit = Minecraft.isJvm64bit();
            ImageIO.setUseCache(false);
            Bootstrap.func_151354_b();
        }
    }

    @Overwrite
    private void func_147115_a(boolean p_147115_1_) {
        if (!p_147115_1_) {
            this.leftClickCounter = 0;
        }

        if (this.leftClickCounter <= 0) {
            if (p_147115_1_ && this.objectMouseOver != null
                && this.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                int i = this.objectMouseOver.blockX;
                int j = this.objectMouseOver.blockY;
                int k = this.objectMouseOver.blockZ;

                if (((IMixinMovingObjectPosition) this.objectMouseOver).getWorld()
                    .getBlock(i, j, k)
                    .getMaterial() != Material.air) {
                    ((IMixinPlayerControllerMP) this.playerController).onPlayerDamageBlock(
                        i,
                        j,
                        k,
                        this.objectMouseOver.sideHit,
                        ((IMixinMovingObjectPosition) this.objectMouseOver).getWorld());

                    if (CompatUtil.isCurrentToolAdventureModeExempt(
                        this.thePlayer,
                        i,
                        j,
                        k,
                        ((IMixinMovingObjectPosition) this.objectMouseOver).getWorld())) {
                        this.effectRenderer.addBlockHitEffects(i, j, k, this.objectMouseOver);
                        this.thePlayer.swingItem();
                    }
                }
            } else {
                this.playerController.resetBlockRemoving();
            }
        }
    }

    @Overwrite
    public void func_147116_af() {
        if (this.leftClickCounter <= 0) {
            this.thePlayer.swingItem();

            if (this.objectMouseOver == null) {
                logger.error("Null returned as \'hitResult\', this shouldn\'t happen!");

                if (this.playerController.isNotCreative()) {
                    this.leftClickCounter = 10;
                }
            } else {
                switch (Minecraft.SwitchMovingObjectType.field_152390_a[this.objectMouseOver.typeOfHit.ordinal()]) {
                    case 1:
                        this.playerController.attackEntity(
                            ((IMixinEntity) this.thePlayer)
                                .getProxyPlayer(((IMixinMovingObjectPosition) this.objectMouseOver).getWorld()),
                            this.objectMouseOver.entityHit);
                        break;
                    case 2:
                        int i = this.objectMouseOver.blockX;
                        int j = this.objectMouseOver.blockY;
                        int k = this.objectMouseOver.blockZ;

                        if (((IMixinMovingObjectPosition) this.objectMouseOver).getWorld()
                            .getBlock(i, j, k)
                            .getMaterial() == Material.air) {
                            if (this.playerController.isNotCreative()) {
                                this.leftClickCounter = 10;
                            }
                        } else {
                            ((IMixinPlayerControllerMP) this.playerController).clickBlock(
                                i,
                                j,
                                k,
                                this.objectMouseOver.sideHit,
                                ((IMixinMovingObjectPosition) this.objectMouseOver).getWorld());
                        }
                }
            }
        }
    }

    @Overwrite
    public void func_147121_ag() {
        this.rightClickDelayTimer = 4;
        boolean flag = true;
        ItemStack itemstack = this.thePlayer.inventory.getCurrentItem();

        if (this.objectMouseOver == null) {
            logger.warn("Null returned as \'hitResult\', this shouldn\'t happen!");
        } else {
            switch (Minecraft.SwitchMovingObjectType.field_152390_a[this.objectMouseOver.typeOfHit.ordinal()]) {
                case 1:
                    if (this.playerController.interactWithEntitySendPacket(
                        ((IMixinEntity) this.thePlayer)
                            .getProxyPlayer(((IMixinMovingObjectPosition) this.objectMouseOver).getWorld()),
                        this.objectMouseOver.entityHit)) {
                        flag = false;
                    }

                    break;
                case 2:
                    int i = this.objectMouseOver.blockX;
                    int j = this.objectMouseOver.blockY;
                    int k = this.objectMouseOver.blockZ;

                    if (!((IMixinMovingObjectPosition) this.objectMouseOver).getWorld()
                        .getBlock(i, j, k)
                        .isAir(((IMixinMovingObjectPosition) this.objectMouseOver).getWorld(), i, j, k)) {
                        int l = itemstack != null ? itemstack.stackSize : 0;

                        boolean result = !net.minecraftforge.event.ForgeEventFactory
                            .onPlayerInteract(
                                thePlayer,
                                net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK,
                                i,
                                j,
                                k,
                                this.objectMouseOver.sideHit,
                                this.theWorld)
                            .isCanceled();
                        if (result && this.playerController.onPlayerRightClick(
                            this.thePlayer,
                            ((IMixinMovingObjectPosition) this.objectMouseOver).getWorld(),
                            itemstack,
                            i,
                            j,
                            k,
                            this.objectMouseOver.sideHit,
                            this.objectMouseOver.hitVec)) {
                            flag = false;
                            this.thePlayer.swingItem();
                        }

                        if (itemstack == null) {
                            return;
                        }

                        if (itemstack.stackSize == 0) {
                            this.thePlayer.inventory.mainInventory[this.thePlayer.inventory.currentItem] = null;
                        } else if (itemstack.stackSize != l || this.playerController.isInCreativeMode()) {
                            this.entityRenderer.itemRenderer.resetEquippedProgress();
                        }
                    }
            }
        }

        if (flag) {
            ItemStack itemstack1 = this.thePlayer.inventory.getCurrentItem();

            boolean result = !net.minecraftforge.event.ForgeEventFactory
                .onPlayerInteract(
                    thePlayer,
                    net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_AIR,
                    0,
                    0,
                    0,
                    -1,
                    this.theWorld)
                .isCanceled();
            if (result && itemstack1 != null
                && this.playerController.sendUseItem(this.thePlayer, this.theWorld, itemstack1)) {
                this.entityRenderer.itemRenderer.resetEquippedProgress2();
            }
        }
    }

    // TODO

    @Redirect(
        method = "runTick()V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;updateEntities()V"))
    public void updateEntities(WorldClient world) {
        for (World curWorld : ((IMixinWorld) this.theWorld).getWorlds()) curWorld.updateEntities();
    }

    @Overwrite
    public void func_147112_ai() {
        if (this.objectMouseOver != null) {
            boolean flag = this.thePlayer.capabilities.isCreativeMode;
            int j;

            if (!ForgeHooks.onPickBlock(
                this.objectMouseOver,
                this.thePlayer,
                ((IMixinMovingObjectPosition) this.objectMouseOver).getWorld() != null
                    ? ((IMixinMovingObjectPosition) this.objectMouseOver).getWorld()
                    : this.theWorld))
                return;
            // We delete this code wholly instead of commenting it out, to make sure we detect changes in it between MC
            // versions
            if (flag) {
                j = this.thePlayer.inventoryContainer.inventorySlots.size() - 9 + this.thePlayer.inventory.currentItem;
                this.playerController
                    .sendSlotPacket(this.thePlayer.inventory.getStackInSlot(this.thePlayer.inventory.currentItem), j);
            }
        }
    }

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
