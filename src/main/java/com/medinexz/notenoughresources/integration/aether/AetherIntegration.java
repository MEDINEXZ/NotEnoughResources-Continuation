package com.medinexz.notenoughresources.integration.aether;

import com.medinexz.notenoughresources.core.OreData;
import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;

/**
 * The Aether (Aether Legacy, 1.7.10) world generation resources.  This is not
 * Aether II, which has another mod id and other blocks.
 *
 * <p>Checked against The Aether v1.1.2.5: the ores, icestone and the aerclouds
 * come from the decorator of the dimension's only biome, quicksoil from the
 * terrain generator (under the edges of the islands), and the cold aercloud
 * also from a structure generator.</p>
 */
public class AetherIntegration implements INERIntegration {

    private static final String MOD_ID = "aether_legacy";

    private static final String PROVIDER = "com.gildedgames.the_aether.world.AetherWorldProvider";

    // aercloud variants
    private static final int COLD_AERCLOUD   = 0;
    private static final int BLUE_AERCLOUD   = 1;
    private static final int GOLDEN_AERCLOUD = 2;

    private Integer dimension;

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        dimension = IntegrationHelper.findDimension(PROVIDER);

        register("ambrosium_ore", 0);
        register("zanite_ore", 0);
        register("gravitite_ore", 0);
        register("icestone", 0);
        // Drops itself as the "placed by a player" variant (metadata 1)
        OreData quicksoil = register("quicksoil", 0);
        if (quicksoil != null) quicksoil.treatAsSelfDropping();
        register("aercloud", COLD_AERCLOUD);
        register("aercloud", BLUE_AERCLOUD);
        register("aercloud", GOLDEN_AERCLOUD);
    }

    private OreData register(String blockName, int metadata) {
        OreData oreData = IntegrationHelper.register(MOD_ID, blockName, metadata, dimension);

        // The WorldProvider calls its dimension "the_aether"; the mod is installed as "The Aether"
        if (oreData != null) oreData.setDimensionName(IntegrationHelper.findModName(MOD_ID));
        return oreData;
    }
}
