package su.sergiusonesimus.metaworlds.controls.captain;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import su.sergiusonesimus.metaworlds.api.RecipeConfig;
import su.sergiusonesimus.metaworlds.api.RecipeConfig.RecipePlaceHolderDef;

@Mod(
    modid = "mwcaptainmod",
    name = "MetaworldsControlsCaptainMod",
    version = "0.995",
    dependencies = "required-after:mwcore")
public class MetaworldsControlsCaptainMod {

    public static final String MODID = "mwcaptainmod";
    public static final String VERSION = "0.995";
    public static RecipeConfig subWorldControllerConfig;
    public static Block subWorldController;
    Configuration config;
    private static int count = 0;
    public static final String CHANNEL = "mwcaptain";
    @Instance("mwcaptainmod")
    public static MetaworldsControlsCaptainMod instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        this.config = new Configuration(event.getSuggestedConfigurationFile());
        subWorldController = (new BlockSubWorldController()).setHardness(0.5F)
            .setStepSound(Block.soundTypeGravel)
            .setBlockName("subWorldController");
        subWorldController.setBlockTextureName("metaworlds:" + subWorldController.getUnlocalizedName());
        this.config.load();
        subWorldControllerConfig = new RecipeConfig(
            this.config,
            "subWorldController",
            new ItemStack(subWorldController, 1, 0),
            false,
            new String[] { "PPP", "PPP", "PPP" },
            new RecipePlaceHolderDef[] { new RecipePlaceHolderDef('P', Blocks.planks.getUnlocalizedName()) });
        this.config.save();
        GameRegistry.registerBlock(subWorldController, "subWorldController");
        subWorldControllerConfig.addRecipeToGameRegistry();
        EntityRegistry.registerModEntity(
            EntitySubWorldController.class,
            "EntitySubWorldController2",
            EntityRegistry.findGlobalUniqueEntityId(),
            this,
            80,
            3,
            true);
    }

    @EventHandler
    public void load(FMLInitializationEvent event) {
        if (event.getSide()
            .isClient()) {
            FMLCommonHandler.instance()
                .bus()
                .register(new SubWorldControllerKeyHandler());
            MinecraftForge.EVENT_BUS.register(new SubWorldControllerKeyHandler());
        }
        FMLCommonHandler.instance()
            .bus()
            .register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {}

    // awful, what I'd call a walking stick for the code, What it essentially does it unsets the shift needed to
    // completely dismount the entity, idk why or how
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTick(TickEvent.PlayerTickEvent event) {
        // if (event.player.worldBelowFeet instanceof SubWorld) {
        // SubWorld world = (SubWorld)event.player.worldBelowFeet;
        // }
        if (BlockSubWorldController.toMakeFalse && count > 10) {
            Minecraft.getMinecraft().gameSettings.keyBindSneak
                .setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode(), false);
            BlockSubWorldController.toMakeFalse = false;
            count = 0;
        } else if (BlockSubWorldController.toMakeFalse) {
            count++;
        }
    }
}
