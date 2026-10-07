package com.medinexz.notenoughresources.integration.minefactoryreloaded;

import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;

/**
 * MineFactory Reloaded (1.7.10) world generation resources.
 *
 * <p>Checked against MineFactory Reloaded 2.8.2B1: its CoFH Core feature places
 * lakes of sludge and sewage, lakes of mushroom soup in the mushroom biomes,
 * and rubber trees (all configurable).</p>
 *
 * <p>Not registered: the rubber tree.</p>
 */
public class MineFactoryReloadedIntegration implements INERIntegration {

    private static final String MOD_ID = "MineFactoryReloaded";

    private static final int OVERWORLD = 0;

    private static final String[] LAKES = { "sludge.still", "sewage.still", "mushroomsoup.still" };

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        for (String lake : LAKES) {
            IntegrationHelper.register(MOD_ID, lake, 0, OVERWORLD);
        }
    }
}
