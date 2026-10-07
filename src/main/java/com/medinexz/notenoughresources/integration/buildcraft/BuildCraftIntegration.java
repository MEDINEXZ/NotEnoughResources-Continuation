package com.medinexz.notenoughresources.integration.buildcraft;

import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;

/**
 * BuildCraft (1.7.10) world generation resources.
 *
 * <p>Checked against BuildCraft 7.1.23: its energy module places oil before a
 * chunk is populated, as small lakes and, rarely, as wells with a spout above
 * the surface, mostly in deserts, oceans and its own oil biomes (configurable).</p>
 *
 * <p>Not registered: the water spring in the bedrock, which cannot be mined.</p>
 */
public class BuildCraftIntegration implements INERIntegration {

    // Oil belongs to the energy module, which can be left out of an installation
    private static final String MOD_ID = "BuildCraft|Energy";

    private static final int OVERWORLD = 0;

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        IntegrationHelper.register(MOD_ID, "blockOil", 0, OVERWORLD);
    }
}
