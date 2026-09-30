package com.medinexz.notenoughresources.core;

import net.minecraft.block.Block;

public interface IOreDetector {

    boolean isOre(Block block, int metadata);
}
