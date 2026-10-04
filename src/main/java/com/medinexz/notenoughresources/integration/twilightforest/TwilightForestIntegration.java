package com.medinexz.notenoughresources.integration.twilightforest;

import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

/**
 * The Twilight Forest (1.7.10) world generation resources.
 *
 * <p>Checked against Twilight Forest 2.3.7: the mod has no ores of its own.
 * Its dimension generates the vanilla ores through the vanilla biome decorator
 * and again, together with glowstone, as the ore stalactites of the hollow
 * hills; its own resources are the roots and liveroots under the forest floor,
 * the cave plants, and the blocks of the fire swamp and of the troll caves.</p>
 */
public class TwilightForestIntegration implements INERIntegration {

    private static final String MOD_ID = "TwilightForest";

    private static final String PROVIDER = "twilightforest.world.WorldProviderTwilightForest";

    // tile.TFRoots variants
    private static final int ROOT     = 0;
    private static final int LIVEROOT = 1;

    // tile.TFPlant variants
    private static final int MUSHGLOOM  = 9;
    private static final int TORCHBERRY = 13;

    // tile.TFFireJet variants as the fire swamp generates them
    private static final int SMOKER   = 0;
    private static final int FIRE_JET = 8;

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
        register(Blocks.gold_ore);
        register(Blocks.redstone_ore);
        register(Blocks.lapis_ore);
        register(Blocks.diamond_ore);
        register(Blocks.emerald_ore);
        register(Blocks.glowstone);

        register("tile.TFRoots", ROOT);
        register("tile.TFRoots", LIVEROOT);
        register("tile.TFPlant", TORCHBERRY);
        register("tile.TFPlant", MUSHGLOOM);
        register("tile.TFFireJet", SMOKER);
        register("tile.TFFireJet", FIRE_JET);
        register("tile.TrollSteinn", 0);
        register("tile.TrollBer", 0);
    }

    private void register(Block vanillaBlock) {
        IntegrationHelper.register(vanillaBlock, 0, IntegrationHelper.MIN_Y, IntegrationHelper.MAX_Y, dimension);
    }

    private void register(String blockName, int metadata) {
        IntegrationHelper.register(MOD_ID, blockName, metadata, dimension);
    }
}
