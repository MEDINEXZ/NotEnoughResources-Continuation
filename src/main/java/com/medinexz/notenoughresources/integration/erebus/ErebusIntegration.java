package com.medinexz.notenoughresources.integration.erebus;

import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;

/**
 * The Erebus (1.7.10) world generation resources.
 *
 * <p>Checked against The Erebus 0.4.7: the umberstone ores come from the biome
 * decorators, which only place a vein next to a cave (they look for air around
 * the chosen spot) and vary the amounts per biome; red gems hang from the cave
 * ceilings and amber is generated in the ground of some biomes.  The metal ores
 * (aluminium, copper, lead, silver, tin) only generate when the Erebus config
 * enables them; a resource the profiler does not find is not shown.</p>
 */
public class ErebusIntegration implements INERIntegration {

    private static final String MOD_ID = "erebus";

    private static final String PROVIDER = "erebus.world.WorldProviderErebus";

    // The generated variants of all these blocks are their metadata 0
    private static final int METADATA = 0;

    private static final String[] RESOURCES = {
        "oreCoal", "oreIron", "oreGold", "oreLapis", "oreDiamond", "oreEmerald",
        "oreJade", "oreEncrustedDiamond", "orePetrifiedWood", "oreFossil", "oreGneiss", "oreQuartz",
        "oreAluminium", "oreCopper", "oreLead", "oreSilver", "oreTin",
        "redGem", "amber",
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
