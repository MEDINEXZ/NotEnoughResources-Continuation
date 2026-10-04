package com.medinexz.notenoughresources.integration.atum;

import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;

/**
 * Atum (1.7.10) world generation resources.
 *
 * <p>Checked against Atum 0.6.77: the dimension has a single desert biome whose
 * decorator is a vanilla BiomeDecorator with the ore generators replaced by
 * Atum's own limestone-hosted ores.  There is no emerald ore in Atum.</p>
 */
public class AtumIntegration implements INERIntegration {

    private static final String MOD_ID = "atum";

    private static final String PROVIDER = "com.teammetallurgy.atum.world.AtumWorldProvider";

    // The ore blocks have no metadata variants
    private static final int METADATA = 0;

    // Atum registers its blocks under their unlocalized names
    private static final String[] RESOURCES = {
        "tile.coalOre", "tile.ironOre", "tile.goldOre", "tile.redstoneOre", "tile.lapisOre", "tile.diamondOre",
    };

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        Integer dimension = IntegrationHelper.findDimension(PROVIDER);

        for (String blockName : RESOURCES) {
            IntegrationHelper.register(MOD_ID, blockName, METADATA, dimension);
        }
    }
}
