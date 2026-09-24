package com.medinexz.notenoughresources.core;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public class OreProfiler {

    public OreGenerationProfile profile(
        World world,
        OreData oreData,
        int minX,
        int maxX,
        int minZ,
        int maxZ
    ) {
        OreGenerationProfile profile = oreData.getGenerationProfile();

        Block targetBlock = oreData.getBlock();
        int targetMetaData = oreData.getMetadata();
        for (int y = oreData.getMinY(); y <= oreData.getMaxY(); y++) {

            int oreCount = 0;
            int blockCount = 0;

            for (int x = minX;  x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {

                    Block block = world.getBlock(x, y, z);
                    int metadata = world.getBlockMetadata(x, y, z);

                    blockCount++;

                    if (block == targetBlock && metadata == targetMetaData) {
                        oreCount++;
                    }
                }
            }

            double probality = (double) oreCount / blockCount;

            profile.setProbability(y, probality);
        }

        return profile;
    }
}
