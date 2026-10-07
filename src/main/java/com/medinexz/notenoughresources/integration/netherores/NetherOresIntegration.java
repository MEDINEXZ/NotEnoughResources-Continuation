package com.medinexz.notenoughresources.integration.netherores;

import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;

/**
 * NetherOres (1.7.10) world generation resources.
 *
 * <p>Checked against NetherOres 2.3.2B1: its generator places Nether variants of
 * up to 32 ores in netherrack, 16 per block.  An ore only generates when its
 * config allows it and, unless forced, when some installed mod has the matching
 * overworld ore, so all variants are registered and the profiler shows the ones
 * this world really has.</p>
 */
public class NetherOresIntegration implements INERIntegration {

    private static final String MOD_ID = "NetherOres";

    private static final int NETHER = -1;

    private static final String[] ORE_BLOCKS = { "tile.netherores.ore.0", "tile.netherores.ore.1" };
    private static final int ORES_PER_BLOCK = 16;

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        for (String block : ORE_BLOCKS) {
            for (int ore = 0; ore < ORES_PER_BLOCK; ore++) {
                IntegrationHelper.register(MOD_ID, block, ore, NETHER);
            }
        }
    }
}
