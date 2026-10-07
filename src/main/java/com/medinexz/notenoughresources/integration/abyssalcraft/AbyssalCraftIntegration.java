package com.medinexz.notenoughresources.integration.abyssalcraft;

import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;

/**
 * AbyssalCraft (1.7.10) world generation resources.
 *
 * <p>Checked against AbyssalCraft 1.9.1.3.  In the overworld its IWorldGenerator
 * places coralium ore (swamps and oceans) and nitre ore, and the decorators of
 * its Darklands biomes place abyssalnite ore.  The ores of the Abyssal
 * Wasteland and of the Dreadlands come from the decorators of the biomes of
 * those dimensions.  Omothol and the Dark Realm have no ores.</p>
 *
 * <p>Not registered: darkstone and the other stone variants, and the blocks of
 * the mod's structures.</p>
 */
public class AbyssalCraftIntegration implements INERIntegration {

    private static final String MOD_ID = "abyssalcraft";

    private static final String PROVIDERS = "com.shinoow.abyssalcraft.common.world.";

    private static final int OVERWORLD = 0;

    // AbyssalCraft's ore blocks have no metadata variants
    private static final int METADATA = 0;

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        register(OVERWORLD, "coraliumore", "nitreore", "abyore");

        register(IntegrationHelper.findDimension(PROVIDERS + "WorldProviderAbyss"),
            "abycorore", "abylcorore", "abypcorore", "abynitore",
            "abyiroore", "abygolore", "abydiaore", "abytinore", "abycopore");

        register(IntegrationHelper.findDimension(PROVIDERS + "WorldProviderDreadlands"),
            "dreadore", "abydreadore");
    }

    private static void register(Integer dimension, String... blockNames) {
        for (String blockName : blockNames) {
            IntegrationHelper.register(MOD_ID, blockName, METADATA, dimension);
        }
    }
}
