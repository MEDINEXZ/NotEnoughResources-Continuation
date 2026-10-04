package com.medinexz.notenoughresources.integration;

import com.medinexz.notenoughresources.NotEnoughResources;
import com.medinexz.notenoughresources.core.OreRegistry;
import cpw.mods.fml.common.Loader;

/**
 * Loads the integrations of the optional mods that are actually installed.
 *
 * <p>Integrations are referred to by class name and only instantiated after
 * {@code Loader.isModLoaded} confirmed their mod, so an integration class (and
 * any class of the other mod it refers to) is never loaded when the mod is absent.</p>
 */
public class NERIntegrationManager {

    private static final String PACKAGE = "com.medinexz.notenoughresources.integration.";

    // { mod id, integration class }
    private static final String[][] INTEGRATIONS = {
        { "divinerpg",       PACKAGE + "divinerpg.DivineRPGIntegration" },
        { "nevermine",       PACKAGE + "aoa.AoAIntegration" },
        { "TwilightForest",  PACKAGE + "twilightforest.TwilightForestIntegration" },
        { "aether_legacy",   PACKAGE + "aether.AetherIntegration" },
        { "erebus",          PACKAGE + "erebus.ErebusIntegration" },
        { "thebetweenlands", PACKAGE + "betweenlands.BetweenlandsIntegration" },
        { "atum",            PACKAGE + "atum.AtumIntegration" },
        { "tropicraft",      PACKAGE + "tropicraft.TropicraftIntegration" },
    };

    /** Call once all mods have finished their own registration (load complete). */
    public static void loadIntegrations() {
        for (String[] entry : INTEGRATIONS) {
            String modId = entry[0];

            if (!Loader.isModLoaded(modId)) {
                NotEnoughResources.LOG.info("Mod " + modId + " not loaded, integration skipped");
                continue;
            }

            try {
                INERIntegration integration = (INERIntegration) Class.forName(entry[1]).newInstance();
                int registeredBefore = OreRegistry.getOres().size();
                integration.registerResources();
                NotEnoughResources.LOG.info("Integration loaded for mod " + integration.getModId()
                    + " (" + IntegrationHelper.findModName(modId) + "): "
                    + (OreRegistry.getOres().size() - registeredBefore) + " world generation resources");
            } catch (Throwable t) {
                NotEnoughResources.LOG.warn("Integration for mod " + modId + " failed to load", t);
            }
        }
    }
}
