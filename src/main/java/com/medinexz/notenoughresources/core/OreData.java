package com.medinexz.notenoughresources.core;

import net.minecraft.block.Block;

public class OreData {
    private String name;
    private Block block;
    private int metadata;

    private int minY;
    private int maxY;

    private OreGenerationProfile generationProfile;

    public OreData(String name,Block block, int metadata, int minY, int maxY) {
        this.name = name;
        this.block = block;
        this.metadata = metadata;
        this.minY = minY;
        this.maxY = maxY;

        this.generationProfile = new OreGenerationProfile(minY, maxY);
    }

    public String getName() {
        return name;
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

    public OreGenerationProfile getGenerationProfile() {
        return generationProfile;
    }

    public void setGenerationProfile(OreGenerationProfile generationProfile) {
        this.generationProfile = generationProfile;
    }
}
