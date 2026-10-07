package com.medinexz.notenoughresources.integration.thaumcraft;

import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;

/**
 * Thaumcraft 4 (1.7.10) world generation resources.
 *
 * <p>Checked against Thaumcraft 4.2.3.5.  Its IWorldGenerator places, outside the
 * End and the dimensions and biomes on its blacklists: cinnabar, amber and the
 * six infused stones (more often the one matching the biome's aspect), aura
 * nodes, greatwood and silverwood trees with shimmerleaf under the latter, and
 * cinderpearl in hot sandy biomes.  The Magical Forest biome adds vishrooms
 * next to its trees through its decorator.</p>
 *
 * <p>Registered are the ores, shimmerleaf and cinderpearl.  Left out on purpose:
 * aura nodes, the trees' wood, vishrooms and the taint of the Tainted Land.  Also not registered: the
 * Outer Lands (a maze built around the portal a player opens, not chunk
 * generation), mana pods (generated in a random growth stage per pod) and the
 * blocks of the mounds, obelisks and totems.</p>
 */
public class ThaumcraftIntegration implements INERIntegration {

    private static final String MOD_ID = ThaumcraftEcosystemManager.THAUMCRAFT;

    private static final int OVERWORLD = 0;

    // blockCustomOre variants
    private static final int CINNABAR            = 0;
    private static final int FIRST_INFUSED_STONE = 1;
    private static final int LAST_INFUSED_STONE  = 6;
    private static final int AMBER               = 7;

    // blockCustomPlant variants
    private static final int SHIMMERLEAF = 2;
    private static final int CINDERPEARL = 3;

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        register("blockCustomOre", CINNABAR, OVERWORLD);
        register("blockCustomOre", AMBER, OVERWORLD);
        for (int stone = FIRST_INFUSED_STONE; stone <= LAST_INFUSED_STONE; stone++) {
            register("blockCustomOre", stone, OVERWORLD);
        }

        register("blockCustomPlant", SHIMMERLEAF, OVERWORLD);
        register("blockCustomPlant", CINDERPEARL, OVERWORLD);
    }

    private static void register(String blockName, int metadata, int dimension) {
        IntegrationHelper.register(MOD_ID, blockName, metadata, dimension);
    }
}
