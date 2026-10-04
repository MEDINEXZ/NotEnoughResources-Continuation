package com.medinexz.notenoughresources.integration.divinerpg;

import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;

/**
 * Divine RPG (1.7.10) world generation resources.
 *
 * <p>Blocks are looked up by their registry names and dimensions by their
 * WorldProvider classes, so neither block ids nor the configurable dimension
 * ids are fixed here, and nothing is compiled against Divine RPG.</p>
 *
 * <p>Where each ore generates (checked against Divine RPG 1.4.1.5):
 * overworld and Nether ores come from its IWorldGenerator, the five twilight
 * ores from the populate step of their dimension's chunk provider, and
 * arcanium ore is built into the Arcana dungeon rooms.</p>
 */
public class DivineRPGIntegration implements INERIntegration {

    private static final String MOD_ID = "divinerpg";

    private static final String PROVIDERS = "net.divinerpg.dimensions.";

    // The ore blocks have no metadata variants
    private static final int METADATA = 0;

    private static final int MIN_Y = 0;
    private static final int MAX_Y = 128;

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        register("realmiteOre",  0);
        register("arlemiteOre",  0);
        register("rupeeOre",     0);

        register("netheriteOre", -1);
        register("bloodgemOre",  -1);

        register("edenOre",      findDimension(PROVIDERS + "twilight.eden.WorldProviderEden"));
        register("wildwoodOre",  findDimension(PROVIDERS + "twilight.wildwood.WorldProviderWildwood"));
        register("apalachiaOre", findDimension(PROVIDERS + "twilight.apalachia.WorldProviderApalachia"));
        register("skythernOre",  findDimension(PROVIDERS + "twilight.skythern.WorldProviderSkythern"));
        register("mortumOre",    findDimension(PROVIDERS + "twilight.mortum.WorldProviderMortum"));

        register("arcaniumOre",  findDimension(PROVIDERS + "arcana.WorldProviderArcana"));
    }

    private static void register(String blockName, Integer dimension) {
        IntegrationHelper.register(MOD_ID, blockName, METADATA, MIN_Y, MAX_Y, dimension);
    }

    private static Integer findDimension(String providerClass) {
        return IntegrationHelper.findDimension(providerClass);
    }
}
