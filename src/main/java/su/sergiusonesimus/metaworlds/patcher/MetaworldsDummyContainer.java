package su.sergiusonesimus.metaworlds.patcher;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.Level;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.versioning.DefaultArtifactVersion;
import su.sergiusonesimus.metaworlds.core.MetaworldsMod;

public class MetaworldsDummyContainer extends DummyModContainer {

    public MetaworldsDummyContainer() {
        super(new ModMetadata());
        ModMetadata meta = this.getMetadata();
        meta.modId = "metaworldsmod";
        meta.name = "Metaworlds_patcher";
        meta.version = "0.995";
        meta.credits = "";
        meta.authorList = Arrays.asList(new String[] { "TCLProject", "MMM_MasterM" });
        meta.description = "description missing";
        meta.url = "url missing";
        meta.updateUrl = "";
        meta.screenshots = new String[0];
        meta.logoFile = "";
        meta.dependencies = new ArrayList();
        meta.dependencies.add(new DefaultArtifactVersion("Forge", true));
    }

    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }

    @Subscribe
    public void preInit(FMLPreInitializationEvent event) {
        FMLLog.log("Metaworlds_patcher", Level.INFO, "Container PreInit", new Object[0]);
        MetaworldsMod.instance = new MetaworldsMod();
        MetaworldsMod.instance.preInit(event);
    }

    @Subscribe
    public void load(FMLInitializationEvent event) {
        MetaworldsMod.instance.load(event);
    }

    @Subscribe
    public void postInit(FMLPostInitializationEvent event) {
        MetaworldsMod.instance.postInit(event);
    }

    @Subscribe
    public void serverStart(FMLServerStartingEvent event) {
        MetaworldsMod.instance.serverStart(event);
    }
}
