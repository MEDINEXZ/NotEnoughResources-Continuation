package com.medinexz.notenoughresources.core;

import net.minecraft.block.Block;

public class OreDiscoveryResult {

    private Block block;
    private int metadata;

    private int minY;
    private int maxY;

    public OreDiscoveryResult(
        Block block,
        int metadata,
        int y
    ) {
        this.block = block;
        this.metadata = metadata;

        this.minY = y;
        this.maxY = y;
    }

    public void updateY(int y) {

        if (y < minY) {
            minY = y;
        }

        if (y > maxY) {
            maxY = y;
        }
    }

    public Block getBlock() {
        return block;
    }

    public int getMetadata() {
        return metadata;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }
}
