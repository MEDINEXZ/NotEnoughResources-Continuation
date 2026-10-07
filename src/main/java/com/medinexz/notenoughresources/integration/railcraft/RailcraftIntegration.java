package com.medinexz.notenoughresources.integration.railcraft;

import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;

/**
 * Railcraft (1.7.10) world generation resources.
 *
 * <p>Checked against Railcraft 9.12.2.1.  Its populators place, in the overworld:
 * sulfur ore near lava, saltpeter ore under desert sand, poor iron, gold,
 * copper, tin and lead ore in a few large deposits, and abyssal geodes under
 * the oceans with dark diamond, emerald and lapis ore.  Firestone ore
 * generates in the lava seas of the Nether.</p>
 *
 * <p>Not registered: quarried and abyssal stone, which are building stone.</p>
 */
public class RailcraftIntegration implements INERIntegration {

    private static final String MOD_ID = "Railcraft";

    private static final int OVERWORLD = 0;
    private static final int NETHER    = -1;

    private static final String ORES = "ore";

    // Ore variants
    private static final int FIRESTONE = 5;
    private static final int[] OVERWORLD_ORES = {
        0,  // sulfur
        1,  // saltpeter
        2,  // dark diamond
        3,  // dark emerald
        4,  // dark lapis
        7,  // poor iron
        8,  // poor gold
        9,  // poor copper
        10, // poor tin
        11, // poor lead
    };

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        for (int ore : OVERWORLD_ORES) {
            IntegrationHelper.register(MOD_ID, ORES, ore, OVERWORLD);
        }
        IntegrationHelper.register(MOD_ID, ORES, FIRESTONE, NETHER);
    }
}
