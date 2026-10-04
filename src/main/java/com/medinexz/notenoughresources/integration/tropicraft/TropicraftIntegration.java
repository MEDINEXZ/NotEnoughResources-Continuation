package com.medinexz.notenoughresources.integration.tropicraft;

import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

/**
 * Tropicraft (1.7.10) world generation resources.
 *
 * <p>Checked against Tropicraft 6.0.5: the ores of the Tropics come from the
 * populate step of the dimension's chunk provider, which generates its three
 * own ores next to vanilla coal, iron and lapis; pineapples are placed by the
 * biome decoration.  What Tropicraft adds to the overworld (palms, bamboo,
 * flowers) is surface decoration and is not listed.</p>
 */
public class TropicraftIntegration implements INERIntegration {

    private static final String MOD_ID = "tropicraft";

    private static final String PROVIDER = "net.tropicraft.world.WorldProviderTropicraft";

    // tile.pineapple: the upper half of the plant, the one that drops the fruit
    private static final int PINEAPPLE_TOP = 8;

    private Integer dimension;

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        dimension = IntegrationHelper.findDimension(PROVIDER);

        register(Blocks.coal_ore);
        register(Blocks.iron_ore);
        register(Blocks.lapis_ore);

        IntegrationHelper.register(MOD_ID, "tile.oreEudialyte", 0, dimension);
        IntegrationHelper.register(MOD_ID, "tile.oreZircon", 0, dimension);
        IntegrationHelper.register(MOD_ID, "tile.oreAzurite", 0, dimension);
        IntegrationHelper.register(MOD_ID, "tile.pineapple", PINEAPPLE_TOP, dimension);
    }

    private void register(Block vanillaBlock) {
        IntegrationHelper.register(vanillaBlock, 0, IntegrationHelper.MIN_Y, IntegrationHelper.MAX_Y, dimension);
    }
}
