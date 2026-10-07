package com.medinexz.notenoughresources.integration.immersiveengineering;

import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;

/**
 * Immersive Engineering (1.7.10) world generation resources.
 *
 * <p>Checked against Immersive Engineering 0.7.7: its IWorldGenerator places
 * copper, bauxite, lead, silver and nickel ore in stone, in every dimension
 * that is not on its blacklist; vein size, height and frequency come from its
 * config.</p>
 */
public class ImmersiveEngineeringIntegration implements INERIntegration {

    private static final String MOD_ID = "ImmersiveEngineering";

    private static final int OVERWORLD = 0;

    // "ore" block variants: copper, bauxite, lead, silver, nickel
    private static final int ORE_VARIANTS = 5;

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        for (int ore = 0; ore < ORE_VARIANTS; ore++) {
            IntegrationHelper.register(MOD_ID, "ore", ore, OVERWORLD);
        }
    }
}
