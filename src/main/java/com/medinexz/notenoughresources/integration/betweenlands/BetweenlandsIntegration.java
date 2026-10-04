package com.medinexz.notenoughresources.integration.betweenlands;

import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;

/**
 * The Betweenlands (1.7.10) world generation resources.
 *
 * <p>Checked against The Betweenlands 1.0.6-alpha: the dimension has its own
 * chunk provider and its own decorator classes (not vanilla BiomeDecorators),
 * but both are driven through the ordinary provideChunk/populate calls, so the
 * generic profiler runs them unchanged.  The ores in betweenstone and pitstone
 * and the limestone pockets come from the decorator every biome shares; the
 * middle gem ores are placed in the mud under swamp water (most often in the
 * Sludge Plains) and life crystals grow in the water of the deep caves.</p>
 */
public class BetweenlandsIntegration implements INERIntegration {

    private static final String MOD_ID = "thebetweenlands";

    private static final String PROVIDER = "thebetweenlands.world.WorldProviderBetweenlands";

    // lifeCrystalOre: 0 is the bare stalk, 1 the stalk that holds a crystal
    private static final int LIFE_CRYSTAL = 1;

    private static final String[] RESOURCES = {
        "sulfurOre", "syrmoriteOre", "boneOre", "octineOre", "valoniteOre", "scabystOre",
        "aquaMiddleGemOre", "crimsonMiddleGemOre", "greenMiddleGemOre",
        "limestone",
    };

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        Integer dimension = IntegrationHelper.findDimension(PROVIDER);

        for (String blockName : RESOURCES) {
            IntegrationHelper.register(MOD_ID, blockName, 0, dimension);
        }
        IntegrationHelper.register(MOD_ID, "lifeCrystalOre", LIFE_CRYSTAL, dimension);
    }
}
