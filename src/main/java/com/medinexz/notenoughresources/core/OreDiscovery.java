package com.medinexz.notenoughresources.core;

import net.minecraft.block.Block;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class OreDiscovery {

    private IOreDetector detector;

    public OreDiscovery(IOreDetector detector) {
        this.detector = detector;
    }

    public boolean isOre(Block block, int metadata) {
        return detector.isOre(block, metadata);
    }

    public List<OreDiscoveryResult> discover(
        World world,
        int minX,
        int maxX,
        int minY,
        int maxY,
        int minZ,
        int maxZ
    ) {

        List<OreDiscoveryResult> results =
            new ArrayList<OreDiscoveryResult>();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {

                    Block block = world.getBlock(x, y, z);
                    int metadata = world.getBlockMetadata(x, y, z);

                    if (isOre(block, metadata)) {

                        OreDiscoveryResult result =
                            findResult(results, block, metadata);

                        if (result == null) {

                            result = new OreDiscoveryResult(
                                block,
                                metadata,
                                y
                            );

                            results.add(result);

                        } else {

                            result.updateY(y);
                        }
                    }
                }
            }
        }

        return results;
    }

    public void registerDiscoveredOres(
        List<OreDiscoveryResult> results
    ) {
        for (OreDiscoveryResult result : results) {

            OreData oreData = new OreData(
                result.getBlock().getLocalizedName(),
                result.getBlock(),
                result.getMetadata(),
                result.getMinY(),
                result.getMaxY()
            );

            OreRegistry.register(oreData);
        }
    }

    private OreDiscoveryResult findResult(
        List<OreDiscoveryResult> results,
        Block block,
        int metadata
    ) {
        for (OreDiscoveryResult result : results) {

            if (result.getBlock() == block
                && result.getMetadata() == metadata) {

                return result;
            }
        }

        return null;
    }
}
