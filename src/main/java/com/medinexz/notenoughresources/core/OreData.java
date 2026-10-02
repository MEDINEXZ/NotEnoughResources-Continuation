package com.medinexz.notenoughresources.core;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class OreData {
    private String name;
    private Block block;
    private int metadata;

    private int minY;
    private int maxY;

    private OreGenerationProfile generationProfile;

    private List<ItemStack> drops = new ArrayList<ItemStack>();
    private int dimension = 0;

    public OreData(String name, Block block, int metadata, int minY, int maxY) {
        this.name = name;
        this.block = block;
        this.metadata = metadata;
        this.minY = minY;
        this.maxY = maxY;
        this.generationProfile = new OreGenerationProfile(minY, maxY);
    }

    public String getName() { return name; }
    public Block getBlock() { return block; }
    public int getMetadata() { return metadata; }
    public int getMinY() { return minY; }
    public int getMaxY() { return maxY; }

    public OreGenerationProfile getGenerationProfile() { return generationProfile; }
    public void setGenerationProfile(OreGenerationProfile generationProfile) {
        this.generationProfile = generationProfile;
    }

    public List<ItemStack> getDrops() { return drops; }
    public void addDrop(ItemStack drop) { drops.add(drop); }

    public int getDimension() { return dimension; }
    public void setDimension(int dimension) { this.dimension = dimension; }
}
