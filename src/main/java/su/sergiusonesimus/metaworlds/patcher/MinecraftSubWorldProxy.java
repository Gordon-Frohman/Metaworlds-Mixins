package su.sergiusonesimus.metaworlds.patcher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.resources.ResourcePackRepository;

public class MinecraftSubWorldProxy extends Minecraft {

    protected Minecraft realMinecraft;

    public MinecraftSubWorldProxy(Minecraft original) {
    	super(null, 0, 0, false, false, null, null, null, null, null, null, null);
    	
    	this.mcDataDir = original.mcDataDir;
    	this.proxy = original.getProxy();
        // this.launchedVersion = original.getLaunchedVersion(original);
    	this.launchedVersion = null;
    	this.fileAssets = null;
    	this.fileResourcepacks = null;
    	this.field_152355_az = original.field_152355_az;
    	this.field_152356_J = original.field_152356_J;
    	this.isDemo = original.isDemo();
    	this.session = original.getSession();
    	this.displayWidth = original.displayWidth;
    	this.displayHeight = original.displayHeight;
    	this.jvm64bit = Minecraft.isJvm64bit();
    	this.gameSettings = original.gameSettings;
    	this.fontRenderer = original.fontRenderer;
    	this.standardGalacticFontRenderer = original.standardGalacticFontRenderer;
    	this.mcSoundHandler = original.getSoundHandler();
    	this.ingameGUI = original.ingameGUI;
    	this.entityRenderer = original.entityRenderer;

        this.realMinecraft = original;
    }

    @Override
    public void displayGuiScreen(GuiScreen par1GuiScreen) {
        this.realMinecraft.displayGuiScreen(par1GuiScreen);

        if (par1GuiScreen != null) {
            par1GuiScreen.setWorldAndResolution(this, par1GuiScreen.width, par1GuiScreen.height);
        }
    }

    @Override
    public TextureManager getTextureManager() {
        return this.realMinecraft.getTextureManager();
    }

    @Override
    public IResourceManager getResourceManager() {
        return this.realMinecraft.getResourceManager();
    }

    @Override
    public ResourcePackRepository getResourcePackRepository() {
        return this.realMinecraft.getResourcePackRepository();
    }

    @Override
    public LanguageManager getLanguageManager() {
        return this.realMinecraft.getLanguageManager();
    }
}