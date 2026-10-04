package com.medinexz.notenoughresources.integration.aoa;

import com.medinexz.notenoughresources.core.OreData;
import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;

/**
 * Advent of Ascension / Nevermine (1.7.10) world generation resources.
 *
 * <p>Where each ore generates (checked against AoA Tslat-1.1.3): the overworld,
 * Nether, Mysterium, Precasia, Crystevia and Creeponia ores come from the mod's
 * IWorldGenerator, which dispatches on the dimension id; the Barathos, Greckon,
 * Iromine, Abyss, Haven and Deeplands ores come from the populate step of
 * their dimension's chunk provider.</p>
 */
public class AoAIntegration implements INERIntegration {

    private static final String MOD_ID = "nevermine";

    private static final String PROVIDERS = "net.nevermine.dimension.";

    // AoA's ore blocks have no metadata variants
    private static final int METADATA = 0;

    // AoA's WorldProviders name their dimensions "DimensionAbyss", "DimensionHaven", ...
    private static final String DIMENSION_NAME_PREFIX = "Dimension";

    private static final int MIN_Y = 0;
    private static final int MAX_Y = 255;

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        register(0, "oreAmethyst", "oreJade", "oreLimonite", "oreRosite", "oreRunium", "oreSapphire");

        register(-1, "oreEmberstone");

        register(dimension("mysterium.WorldProviderMy"), "oreMystite");

        register(dimension("precasia.WorldProviderPc"),
            "oreChestFragments", "oreFootFragments", "oreLegFragments", "oreSkullFragments");

        register(dimension("crystevia.WorldProviderCr"),
            "oreCrystalBlue", "oreCrystalGreen", "oreCrystalPurple",
            "oreCrystalRed", "oreCrystalWhite", "oreCrystalYellow");

        register(dimension("creeponia.WorldProviderCp"), "oreGemenyte", "oreJewelyte", "oreOrnamyte");

        register(dimension("barathos.WorldProviderBt"),
            "oreBaronyte", "oreBlazium", "oreElecanium", "oreVarsium");

        register(dimension("greckon.WorldProviderGk"), "oreGhastly", "oreGhoulish");

        register(dimension("iromine.WorldProviderIr"), "oreLyon");

        register(dimension("abyss.WorldProviderAB"), "oreBloodstone");

        register(dimension("haven.WorldProviderHv"), "oreCrystallite");

        register(dimension("deeplands.WorldProviderDp"), "oreChargedRunium");
    }

    private static Integer dimension(String providerClass) {
        return IntegrationHelper.findDimension(PROVIDERS + providerClass);
    }

    private static void register(Integer dimension, String... blockNames) {
        for (String blockName : blockNames) {
            OreData oreData =
                IntegrationHelper.register(MOD_ID, blockName, METADATA, MIN_Y, MAX_Y, dimension);
            if (oreData == null) continue;

            // Show "Abyss" rather than "DimensionAbyss" as the entry header
            String name = oreData.getDimensionName();
            if (name.startsWith(DIMENSION_NAME_PREFIX) && name.length() > DIMENSION_NAME_PREFIX.length()) {
                oreData.setDimensionName(name.substring(DIMENSION_NAME_PREFIX.length()));
            }
        }
    }
}
