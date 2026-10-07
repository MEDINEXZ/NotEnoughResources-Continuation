package com.medinexz.notenoughresources.integration.ic2;

import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;

/**
 * IndustrialCraft 2 Experimental (1.7.10) world generation resources.
 *
 * <p>Checked against IC2 2.2.827: its IWorldGenerator places copper, tin, uranium
 * and lead ore in stone (each can be switched off in its config), and rubber
 * trees.</p>
 *
 * <p>Not registered: the rubber tree.</p>
 */
public class IC2Integration implements INERIntegration {

    private static final String MOD_ID = "IC2";

    private static final int OVERWORLD = 0;

    private static final String[] ORES = { "blockOreCopper", "blockOreTin", "blockOreUran", "blockOreLead" };

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        for (String ore : ORES) {
            IntegrationHelper.register(MOD_ID, ore, 0, OVERWORLD);
        }
    }
}
