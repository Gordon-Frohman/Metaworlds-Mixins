package su.sergiusonesimus.metaworlds.mixin.mixins.minecraft.client;

import java.io.File;
import java.net.Proxy;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Multimap;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSleepMP;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.ResourceIndex;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Session;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import su.sergiusonesimus.metaworlds.api.IMixinEntity;
import su.sergiusonesimus.metaworlds.api.IMixinWorld;
import su.sergiusonesimus.metaworlds.compat.CompatUtil;
import su.sergiusonesimus.metaworlds.mixin.interfaces.client.multiplayer.IMixinPlayerControllerMP;
import su.sergiusonesimus.metaworlds.mixin.interfaces.util.IMixinMovingObjectPosition;
import su.sergiusonesimus.metaworlds.patcher.EntityClientPlayerMPSubWorldProxy;
import su.sergiusonesimus.metaworlds.patcher.MinecraftSubWorldProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.client.gui.GuiChat;

@Mixin(value = Minecraft.class, priority = 800)
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
	
	//TODO

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

	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;theMinecraft:Lnet/minecraft/client/Minecraft;", opcode = Opcodes.PUTSTATIC))
	private void disableTheMinecraft(Minecraft value) {
	    // Do nothing
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;mcDataDir:Ljava/io/File;", opcode = Opcodes.PUTFIELD))
	private void disableMcDataDir(Minecraft minecraft, File value) {
	    // Do nothing
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;fileAssets:Ljava/io/File;", opcode = Opcodes.PUTFIELD))
	private void disableFileAssets(Minecraft minecraft, File value) {
	    // Do nothing
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;fileResourcepacks:Ljava/io/File;", opcode = Opcodes.PUTFIELD))
	private void disableFileResourcepacks(Minecraft minecraft, File value) {
	    // Do nothing
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;launchedVersion:Ljava/lang/String;", opcode = Opcodes.PUTFIELD))
	private void disableLaunchedVersion(Minecraft minecraft, String value) {
	    // Do nothing
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;field_152356_J:Lcom/google/common/collect/Multimap;", opcode = Opcodes.PUTFIELD))
	private void disableField_152356_J(Minecraft minecraft, Multimap value) {
	    // Do nothing
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;mcDefaultResourcePack:Lnet/minecraft/client/resources/DefaultResourcePack;", opcode = Opcodes.PUTFIELD))
	private void disableMcDefaultResourcePack(Minecraft minecraft, DefaultResourcePack value) {
	    // Do nothing
	}
	
	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;addDefaultResourcePack()V"))
	public void disableAddDefaultResourcePack(Minecraft minecraft) {
		// Do nothing
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;proxy:Ljava/net/Proxy;", opcode = Opcodes.PUTFIELD))
	private void disableProxy(Minecraft minecraft, Proxy value) {
	    // Do nothing
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;field_152355_az:Lcom/mojang/authlib/minecraft/MinecraftSessionService;", opcode = Opcodes.PUTFIELD))
	private void disableField_152355_az(Minecraft minecraft, MinecraftSessionService value) {
	    // Do nothing
	}
	
	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;startTimerHackThread()V"))
	public void disableStartTimerHackThread(Minecraft minecraft) {
		// Do nothing
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;session:Lnet/minecraft/util/Session;", opcode = Opcodes.PUTFIELD))
	private void disableSession(Minecraft minecraft, Session value) {
	    // Do nothing
	}
	
	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;)V"))
	public void disableLogger_Info(Logger logger, String s) {
		// Do nothing
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;isDemo:Z", opcode = Opcodes.PUTFIELD))
	private void disableIsDemo(Minecraft minecraft, boolean value) {
	    // Do nothing
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;fullscreen:Z", opcode = Opcodes.PUTFIELD))
	private void disableFullscreen(Minecraft minecraft, boolean value) {
	    // Do nothing
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;jvm64bit:Z", opcode = Opcodes.PUTFIELD))
	private void disableJVM64bit(Minecraft minecraft, boolean value) {
	    // Do nothing
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;displayWidth:I", opcode = Opcodes.PUTFIELD))
	private void disableDisplayWidth(Minecraft minecraft, int value) {
	    // Do nothing
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;displayHeight:I", opcode = Opcodes.PUTFIELD))
	private void disableDisplayHeight(Minecraft minecraft, int value) {
	    // Do nothing
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;tempDisplayWidth:I", opcode = Opcodes.PUTFIELD))
	private void disableTempDisplayWidth(Minecraft minecraft, int value) {
	    // Do nothing
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;tempDisplayHeight:I", opcode = Opcodes.PUTFIELD))
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
	
	@Redirect(method = "<init>", at = @At(value = "NEW", target = "Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;"))
	private YggdrasilAuthenticationService yggdrasilAuthenticationServicePlaceholder(Proxy proxy, String clientToken) {
	  return new YggdrasilAuthenticationService(Minecraft.theMinecraft.proxy, clientToken);
	}
	
	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;createMinecraftSessionService()Lcom/mojang/authlib/minecraft/MinecraftSessionService;"))
	public MinecraftSessionService disableCreateMinecraftSessionService(YggdrasilAuthenticationService yggdrasilAuthenticationService) {
		// Do nothing, yet
		return null;
	}
	
	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Session;getUsername()Ljava/lang/String;"))
	public String disableGetUsername(Session session) {
		// Do nothing, yet
		return "";
	}
	
	// And secondly - set up a new constructor
	
	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;theMinecraft:Lnet/minecraft/client/Minecraft;"), method = "<init>")
	private void init(Session sessionIn, int displayWidth, int displayHeight, boolean fullscreen, boolean isDemo, File dataDir, File assetsDir, File resourcePackDir, Proxy proxy, String version, Multimap twitchDetails, String assetsJsonVersion, CallbackInfo info) {
		if(sessionIn == null && displayWidth == 0 && displayHeight == 0 && fullscreen ==  false && isDemo == false && dataDir == null && assetsDir == null && resourcePackDir == null && version == null && twitchDetails == null && assetsJsonVersion == null) {
			//logger.info("Empty Minecraft object created");
		}
		else {
			//logger.info("Normal Minecraft object created");
	        Minecraft.theMinecraft = (Minecraft)(Object)this;
	        this.mcDataDir = dataDir;
	        this.fileAssets = assetsDir;
	        this.fileResourcepacks = resourcePackDir;
	        this.launchedVersion = version;
	        this.field_152356_J = twitchDetails;
	        this.mcDefaultResourcePack = new DefaultResourcePack((new ResourceIndex(assetsDir, assetsJsonVersion)).func_152782_a());
	        this.addDefaultResourcePack();
	        this.proxy = proxy == null ? Proxy.NO_PROXY : proxy;
	        this.field_152355_az = (new YggdrasilAuthenticationService(proxy, UUID.randomUUID().toString())).createMinecraftSessionService();
	        this.startTimerHackThread();
	        this.session = sessionIn;
	        logger.info("Setting user: " + sessionIn.getUsername());
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

                if (((IMixinMovingObjectPosition)this.objectMouseOver).getWorld().getBlock(i, j, k)
                    .getMaterial() != Material.air) {
                    ((IMixinPlayerControllerMP)this.playerController)
                        .onPlayerDamageBlock(i, j, k, this.objectMouseOver.sideHit, ((IMixinMovingObjectPosition)this.objectMouseOver).getWorld());

                    if (CompatUtil
                        .isCurrentToolAdventureModeExempt(this.thePlayer, i, j, k, ((IMixinMovingObjectPosition)this.objectMouseOver).getWorld())) {
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
    private void func_147116_af() {
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
                            ((IMixinEntity)this.thePlayer).getProxyPlayer(((IMixinMovingObjectPosition)this.objectMouseOver).getWorld()),
                            this.objectMouseOver.entityHit);
                        break;
                    case 2:
                        int i = this.objectMouseOver.blockX;
                        int j = this.objectMouseOver.blockY;
                        int k = this.objectMouseOver.blockZ;

                        if (((IMixinMovingObjectPosition)this.objectMouseOver).getWorld().getBlock(i, j, k)
                            .getMaterial() == Material.air) {
                            if (this.playerController.isNotCreative()) {
                                this.leftClickCounter = 10;
                            }
                        } else {
                            ((IMixinPlayerControllerMP)this.playerController)
                                .clickBlock(i, j, k, this.objectMouseOver.sideHit, ((IMixinMovingObjectPosition)this.objectMouseOver).getWorld());
                        }
                }
            }
        }
    }

	@Overwrite
    private void func_147121_ag() {
        this.rightClickDelayTimer = 4;
        boolean flag = true;
        ItemStack itemstack = this.thePlayer.inventory.getCurrentItem();

        if (this.objectMouseOver == null) {
            logger.warn("Null returned as \'hitResult\', this shouldn\'t happen!");
        } else {
            switch (Minecraft.SwitchMovingObjectType.field_152390_a[this.objectMouseOver.typeOfHit.ordinal()]) {
                case 1:
                    if (this.playerController.interactWithEntitySendPacket(
                        ((IMixinEntity)this.thePlayer).getProxyPlayer(((IMixinMovingObjectPosition)this.objectMouseOver).getWorld()),
                        this.objectMouseOver.entityHit)) {
                        flag = false;
                    }

                    break;
                case 2:
                    int i = this.objectMouseOver.blockX;
                    int j = this.objectMouseOver.blockY;
                    int k = this.objectMouseOver.blockZ;

                    if (!((IMixinMovingObjectPosition)this.objectMouseOver).getWorld().getBlock(i, j, k)
                        .isAir(((IMixinMovingObjectPosition)this.objectMouseOver).getWorld(), i, j, k)) {
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
                            ((IMixinMovingObjectPosition)this.objectMouseOver).getWorld(),
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

    /**
     * Runs the current tick.
     */
	@Overwrite
    public void runTick() {
        this.mcProfiler.startSection("scheduledExecutables");
        Queue queue = this.field_152351_aB;

        synchronized (this.field_152351_aB) {
            while (!this.field_152351_aB.isEmpty()) {
                ((FutureTask) this.field_152351_aB.poll()).run();
            }
        }

        this.mcProfiler.endSection();

        if (this.rightClickDelayTimer > 0) {
            --this.rightClickDelayTimer;
        }

        FMLCommonHandler.instance()
            .onPreClientTick();

        this.mcProfiler.startSection("gui");

        if (!this.isGamePaused) {
            this.ingameGUI.updateTick();
        }

        this.mcProfiler.endStartSection("pick");
        this.entityRenderer.getMouseOver(1.0F);
        this.mcProfiler.endStartSection("gameMode");

        if (!this.isGamePaused && this.theWorld != null) {
            this.playerController.updateController();
        }

        this.mcProfiler.endStartSection("textures");

        if (!this.isGamePaused) {
            this.renderEngine.tick();
        }

        if (this.currentScreen == null && this.thePlayer != null) {
            if (this.thePlayer.getHealth() <= 0.0F) {
                this.displayGuiScreen((GuiScreen) null);
            } else if (this.thePlayer.isPlayerSleeping() && this.theWorld != null) {
                this.displayGuiScreen(new GuiSleepMP());
            }
        } else if (this.currentScreen != null && this.currentScreen instanceof GuiSleepMP
            && !this.thePlayer.isPlayerSleeping()) {
                this.displayGuiScreen((GuiScreen) null);
            }

        if (this.currentScreen != null) {
            this.leftClickCounter = 10000;
        }

        CrashReport crashreport;
        CrashReportCategory crashreportcategory;

        if (this.currentScreen != null) {
            try {
                this.currentScreen.handleInput();
            } catch (Throwable throwable1) {
                crashreport = CrashReport.makeCrashReport(throwable1, "Updating screen events");
                crashreportcategory = crashreport.makeCategory("Affected screen");
                crashreportcategory.addCrashSectionCallable("Screen name", new Callable() {

                    private static final String __OBFID = "CL_00000640";

                    public String call() {
                        return Minecraft.getMinecraft().currentScreen.getClass()
                            .getCanonicalName();
                    }
                });
                throw new ReportedException(crashreport);
            }

            if (this.currentScreen != null) {
                try {
                    this.currentScreen.updateScreen();
                } catch (Throwable throwable) {
                    crashreport = CrashReport.makeCrashReport(throwable, "Ticking screen");
                    crashreportcategory = crashreport.makeCategory("Affected screen");
                    crashreportcategory.addCrashSectionCallable("Screen name", new Callable() {

                        private static final String __OBFID = "CL_00000642";

                        public String call() {
                            return Minecraft.getMinecraft().currentScreen.getClass()
                                .getCanonicalName();
                        }
                    });
                    throw new ReportedException(crashreport);
                }
            }
        }

        if (this.currentScreen == null || this.currentScreen.allowUserInput) {
            this.mcProfiler.endStartSection("mouse");
            int j;

            while (Mouse.next()) {
                if (net.minecraftforge.client.ForgeHooksClient.postMouseEvent()) continue;

                j = Mouse.getEventButton();
                KeyBinding.setKeyBindState(j - 100, Mouse.getEventButtonState());

                if (Mouse.getEventButtonState()) {
                    KeyBinding.onTick(j - 100);
                }

                long k = Minecraft.getSystemTime() - this.systemTime;

                if (k <= 200L) {
                    int i = Mouse.getEventDWheel();

                    if (i != 0) {
                        this.thePlayer.inventory.changeCurrentItem(i);

                        if (this.gameSettings.noclip) {
                            if (i > 0) {
                                i = 1;
                            }

                            if (i < 0) {
                                i = -1;
                            }

                            this.gameSettings.noclipRate += (float) i * 0.25F;
                        }
                    }

                    if (this.currentScreen == null) {
                        if (!this.inGameHasFocus && Mouse.getEventButtonState()) {
                            this.setIngameFocus();
                        }
                    } else if (this.currentScreen != null) {
                        this.currentScreen.handleMouseInput();
                    }
                }
                FMLCommonHandler.instance()
                    .fireMouseInput();
            }

            if (this.leftClickCounter > 0) {
                --this.leftClickCounter;
            }

            this.mcProfiler.endStartSection("keyboard");
            boolean flag;

            while (Keyboard.next()) {
                KeyBinding.setKeyBindState(Keyboard.getEventKey(), Keyboard.getEventKeyState());

                if (Keyboard.getEventKeyState()) {
                    KeyBinding.onTick(Keyboard.getEventKey());
                }

                if (this.field_83002_am > 0L) {
                    if (Minecraft.getSystemTime() - this.field_83002_am >= 6000L) {
                        throw new ReportedException(new CrashReport("Manually triggered debug crash", new Throwable()));
                    }

                    if (!Keyboard.isKeyDown(46) || !Keyboard.isKeyDown(61)) {
                        this.field_83002_am = -1L;
                    }
                } else if (Keyboard.isKeyDown(46) && Keyboard.isKeyDown(61)) {
                    this.field_83002_am = Minecraft.getSystemTime();
                }

                this.func_152348_aa();

                if (Keyboard.getEventKeyState()) {
                    if (Keyboard.getEventKey() == 62 && this.entityRenderer != null) {
                        this.entityRenderer.deactivateShader();
                    }

                    if (this.currentScreen != null) {
                        this.currentScreen.handleKeyboardInput();
                    } else {
                        if (Keyboard.getEventKey() == 1) {
                            this.displayInGameMenu();
                        }

                        if (Keyboard.getEventKey() == 31 && Keyboard.isKeyDown(61)) {
                            this.refreshResources();
                        }

                        if (Keyboard.getEventKey() == 20 && Keyboard.isKeyDown(61)) {
                            this.refreshResources();
                        }

                        if (Keyboard.getEventKey() == 33 && Keyboard.isKeyDown(61)) {
                            flag = Keyboard.isKeyDown(42) | Keyboard.isKeyDown(54);
                            this.gameSettings.setOptionValue(GameSettings.Options.RENDER_DISTANCE, flag ? -1 : 1);
                        }

                        if (Keyboard.getEventKey() == 30 && Keyboard.isKeyDown(61)) {
                            this.renderGlobal.loadRenderers();
                        }

                        if (Keyboard.getEventKey() == 35 && Keyboard.isKeyDown(61)) {
                            this.gameSettings.advancedItemTooltips = !this.gameSettings.advancedItemTooltips;
                            this.gameSettings.saveOptions();
                        }

                        if (Keyboard.getEventKey() == 48 && Keyboard.isKeyDown(61)) {
                            RenderManager.debugBoundingBox = !RenderManager.debugBoundingBox;
                        }

                        if (Keyboard.getEventKey() == 25 && Keyboard.isKeyDown(61)) {
                            this.gameSettings.pauseOnLostFocus = !this.gameSettings.pauseOnLostFocus;
                            this.gameSettings.saveOptions();
                        }

                        if (Keyboard.getEventKey() == 59) {
                            this.gameSettings.hideGUI = !this.gameSettings.hideGUI;
                        }

                        if (Keyboard.getEventKey() == 61) {
                            this.gameSettings.showDebugInfo = !this.gameSettings.showDebugInfo;
                            this.gameSettings.showDebugProfilerChart = GuiScreen.isShiftKeyDown();
                        }

                        if (this.gameSettings.keyBindTogglePerspective.isPressed()) {
                            ++this.gameSettings.thirdPersonView;

                            if (this.gameSettings.thirdPersonView > 2) {
                                this.gameSettings.thirdPersonView = 0;
                            }
                        }

                        if (this.gameSettings.keyBindSmoothCamera.isPressed()) {
                            this.gameSettings.smoothCamera = !this.gameSettings.smoothCamera;
                        }
                    }

                    if (this.gameSettings.showDebugInfo && this.gameSettings.showDebugProfilerChart) {
                        if (Keyboard.getEventKey() == 11) {
                            this.updateDebugProfilerName(0);
                        }

                        for (j = 0; j < 9; ++j) {
                            if (Keyboard.getEventKey() == 2 + j) {
                                this.updateDebugProfilerName(j + 1);
                            }
                        }
                    }
                }
                FMLCommonHandler.instance()
                    .fireKeyInput();
            }

            for (j = 0; j < 9; ++j) {
                if (this.gameSettings.keyBindsHotbar[j].isPressed()) {
                    this.thePlayer.inventory.currentItem = j;
                }
            }

            flag = this.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN;

            while (this.gameSettings.keyBindInventory.isPressed()) {
                if (this.playerController.func_110738_j()) {
                    this.thePlayer.func_110322_i();
                } else {
                    this.getNetHandler()
                        .addToSendQueue(
                            new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                    this.displayGuiScreen(new GuiInventory(this.thePlayer));
                }
            }

            while (this.gameSettings.keyBindDrop.isPressed()) {
                this.thePlayer.dropOneItem(GuiScreen.isCtrlKeyDown());
            }

            while (this.gameSettings.keyBindChat.isPressed() && flag) {
                this.displayGuiScreen(new GuiChat());
            }

            if (this.currentScreen == null && this.gameSettings.keyBindCommand.isPressed() && flag) {
                this.displayGuiScreen(new GuiChat("/"));
            }

            if (this.thePlayer.isUsingItem()) {
                if (!this.gameSettings.keyBindUseItem.getIsKeyPressed()) {
                    this.playerController.onStoppedUsingItem(this.thePlayer);
                }

                label391:

                while (true) {
                    if (!this.gameSettings.keyBindAttack.isPressed()) {
                        while (this.gameSettings.keyBindUseItem.isPressed()) {
                            ;
                        }

                        while (true) {
                            if (this.gameSettings.keyBindPickBlock.isPressed()) {
                                continue;
                            }

                            break label391;
                        }
                    }
                }
            } else {
                while (this.gameSettings.keyBindAttack.isPressed()) {
                    this.func_147116_af();
                }

                while (this.gameSettings.keyBindUseItem.isPressed()) {
                    this.func_147121_ag();
                }

                while (this.gameSettings.keyBindPickBlock.isPressed()) {
                    this.func_147112_ai();
                }
            }

            if (this.gameSettings.keyBindUseItem.getIsKeyPressed() && this.rightClickDelayTimer == 0
                && !this.thePlayer.isUsingItem()) {
                this.func_147121_ag();
            }

            this.func_147115_a(
                this.currentScreen == null && this.gameSettings.keyBindAttack.getIsKeyPressed() && this.inGameHasFocus);
        }

        if (this.theWorld != null) {
            if (this.thePlayer != null) {
                ++this.joinPlayerCounter;

                if (this.joinPlayerCounter == 30) {
                    this.joinPlayerCounter = 0;
                    this.theWorld.joinEntityInSurroundings(this.thePlayer);
                }
            }

            this.mcProfiler.endStartSection("gameRenderer");

            if (!this.isGamePaused) {
                this.entityRenderer.updateRenderer();
            }

            this.mcProfiler.endStartSection("levelRenderer");

            if (!this.isGamePaused) {
                this.renderGlobal.updateClouds();
            }

            this.mcProfiler.endStartSection("level");

            if (!this.isGamePaused) {
                if (this.theWorld.lastLightningBolt > 0) {
                    --this.theWorld.lastLightningBolt;
                }

                for (World curWorld : ((IMixinWorld)this.theWorld).getWorlds()) curWorld.updateEntities();
            }
        }

        if (!this.isGamePaused) {
            this.mcMusicTicker.update();
            this.mcSoundHandler.update();
        }

        if (this.theWorld != null) {
            if (!this.isGamePaused) {
                this.theWorld.setAllowedSpawnTypes(this.theWorld.difficultySetting != EnumDifficulty.PEACEFUL, true);

                try {
                    this.theWorld.tick();
                } catch (Throwable throwable2) {
                    crashreport = CrashReport.makeCrashReport(throwable2, "Exception in world tick");

                    if (this.theWorld == null) {
                        crashreportcategory = crashreport.makeCategory("Affected level");
                        crashreportcategory.addCrashSection("Problem", "Level is null!");
                    } else {
                        this.theWorld.addWorldInfoToCrashReport(crashreport);
                    }

                    throw new ReportedException(crashreport);
                }
            }

            this.mcProfiler.endStartSection("animateTick");

            if (!this.isGamePaused && this.theWorld != null) {
                this.theWorld.doVoidFogParticles(
                    MathHelper.floor_double(this.thePlayer.posX),
                    MathHelper.floor_double(this.thePlayer.posY),
                    MathHelper.floor_double(this.thePlayer.posZ));
            }

            this.mcProfiler.endStartSection("particles");

            if (!this.isGamePaused) {
                this.effectRenderer.updateEffects();
            }
        } else if (this.myNetworkManager != null) {
            this.mcProfiler.endStartSection("pendingConnection");
            this.myNetworkManager.processReceivedPackets();
        }

        FMLCommonHandler.instance()
            .onPostClientTick();

        this.mcProfiler.endSection();
        this.systemTime = Minecraft.getSystemTime();
    }

	@Overwrite
    private void func_147112_ai() {
        if (this.objectMouseOver != null) {
            boolean flag = this.thePlayer.capabilities.isCreativeMode;
            int j;

            if (!ForgeHooks.onPickBlock(
                this.objectMouseOver,
                this.thePlayer,
                ((IMixinMovingObjectPosition)this.objectMouseOver).getWorld() != null ? ((IMixinMovingObjectPosition)this.objectMouseOver).getWorld() : this.theWorld)) return;
            // We delete this code wholly instead of commenting it out, to make sure we detect changes in it between MC
            // versions
            if (flag) {
                j = this.thePlayer.inventoryContainer.inventorySlots.size() - 9 + this.thePlayer.inventory.currentItem;
                this.playerController
                    .sendSlotPacket(this.thePlayer.inventory.getStackInSlot(this.thePlayer.inventory.currentItem), j);
            }
        }
    }
	
	//To fix various GUI bugs caused by a lack of screen size synchronization
	@Inject(method = "toggleFullscreen()V", at = { @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;func_147120_f()V") })
    public void toggleFullscreen(CallbackInfo ci)
    {
		Minecraft mc = Minecraft.getMinecraft();
		for(World subworld : ((IMixinWorld)mc.theWorld).getSubWorlds()) {
			EntityClientPlayerMPSubWorldProxy proxy = (EntityClientPlayerMPSubWorldProxy)((IMixinEntity)mc.thePlayer).getProxyPlayer(subworld);
			MinecraftSubWorldProxy proxyMC = (MinecraftSubWorldProxy)proxy.getMinecraft();
			proxyMC.fullscreen = mc.fullscreen;
			proxyMC.displayWidth = mc.displayWidth;
			proxyMC.displayHeight = mc.displayHeight;
		}
    }

}
