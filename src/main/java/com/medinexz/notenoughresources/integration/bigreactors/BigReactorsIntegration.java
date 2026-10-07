package com.medinexz.notenoughresources.integration.bigreactors;

import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;

/**
 * Big Reactors (1.7.10) world generation resources.
 *
 * <p>Checked against Big Reactors 0.4.3A: its IWorldGenerator places yellorite ore
 * in the stone of the overworld (configurable).</p>
 */
public class BigReactorsIntegration implements INERIntegration {

    private static final String MOD_ID = "BigReactors";

    private static final int OVERWORLD = 0;

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        IntegrationHelper.register(MOD_ID, "YelloriteOre", 0, OVERWORLD);
    }
}
