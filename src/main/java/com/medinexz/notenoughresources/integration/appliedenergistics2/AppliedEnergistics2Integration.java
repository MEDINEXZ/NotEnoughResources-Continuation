package com.medinexz.notenoughresources.integration.appliedenergistics2;

import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;

/**
 * Applied Energistics 2 (1.7.10) world generation resources.
 *
 * <p>Checked against Applied Energistics 2 rv3-beta-6 and its GTNH fork
 * rv3-beta-1081, which share the mod id and the block names: an IWorldGenerator
 * places certus quartz ore in stone, some of it charged, in the dimensions the
 * config allows.</p>
 *
 * <p>Not registered: sky stone.  Its meteorites are not placed while a chunk is
 * populated but queued for a later tick of the world, which the profiler never
 * runs.</p>
 */
public class AppliedEnergistics2Integration implements INERIntegration {

    private static final String MOD_ID = "appliedenergistics2";

    private static final int OVERWORLD = 0;

    private static final String[] ORES = { "tile.OreQuartz", "tile.OreQuartzCharged" };

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
