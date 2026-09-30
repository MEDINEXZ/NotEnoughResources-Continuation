package com.medinexz.notenoughresources.core;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public class VanillaOreDetector implements IOreDetector {

    @Override
    public boolean isOre(Block block, int metadata) {
        return block == Blocks.coal_ore
            || block == Blocks.iron_ore
            || block == Blocks.gold_ore
            || block == Blocks.redstone_ore
            || block == Blocks.lapis_ore
            || block == Blocks.diamond_ore
            || block == Blocks.emerald_ore;
    }
}
