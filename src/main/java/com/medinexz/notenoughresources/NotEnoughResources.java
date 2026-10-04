package com.medinexz.notenoughresources;

import com.medinexz.notenoughresources.core.OreGenerationManager;
import com.medinexz.notenoughresources.core.OreRegistry;
import com.medinexz.notenoughresources.integration.NERIntegrationManager;
import cpw.mods.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import net.minecraft.server.MinecraftServer;

@Mod(
    modid = NotEnoughResources.MODID,
    version = Tags.VERSION,
    name = NotEnoughResources.MODNAME,
    acceptedMinecraftVersions = "[1.7.10]")
public class NotEnoughResources {

    public static final String MODID = "notenoughresources";
    public static final String MODNAME = "NotEnoughResources Continuation";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(
        clientSide = "com.medinexz.notenoughresources.ClientProxy",
        serverSide = "com.medinexz.notenoughresources.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        proxy.init(event);

        OreRegistry.registerVanillaOres();
        OreRegistry.registerModdedOres();

        FMLCommonHandler.instance().bus().register(
            OreGenerationManager.getInstance()
        );
    }

    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    // Optional mod integrations: run last, once every mod has registered its blocks and dimensions
    public void loadComplete(FMLLoadCompleteEvent event) {
        NERIntegrationManager.loadIntegrations();
    }

    @Mod.EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }

    @Mod.EventHandler
    // Headless check of the world generation profiling: with NER_PROFILE_ON_START set, a
    // server profiles as soon as it has started (not at the first player tick) and stops
    public void serverStarted(FMLServerStartedEvent event) {
        if (System.getenv("NER_PROFILE_ON_START") == null) return;

        OreGenerationManager.getInstance().profileRegisteredOres();
        MinecraftServer.getServer().initiateShutdown();
    }
}
