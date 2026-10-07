package com.medinexz.notenoughresources.core;

import net.minecraft.block.Block;
import com.medinexz.notenoughresources.NotEnoughResources;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class OreProfiler {

    // ── Chunk-based profiling constants (JER-style) ───────────────────────────
    // Overworld chunks: enough for features of a single biome that only generate in
    // one chunk out of dozens (a mod's desert flowers) to be found in most worlds,
    // at about 5 ms per chunk
    static final int TOTAL_CHUNKS    = 2000;
    static final int CHUNKS_PER_BATCH =  25;

    // Side of the square groups the profiled chunks are taken in
    private static final int GROUP_SIDE = 4;

    // Chunks profiled in every other dimension
    static final int GENERATED_TERRAIN_CHUNKS = 250;

    // Chunk coordinate range for random sampling: ±1875 chunks (~30 000 blocks)
    private static final int CHUNK_RANGE = 1875;

    // An ore counts as biome-restricted only if finding it in none of the other
    // profiled chunks would be this unlikely for an ore that generates everywhere.
    private static final double BIOME_RESTRICTION_SIGNIFICANCE = 0.001;

    // ... and only if its biomes cover at most this share of the profiled chunks:
    // "everywhere except two rare biomes" is not worth a list of all the others.
    private static final double MAX_RESTRICTED_SHARE = 0.5;

    /**
     * JER-style profiling: generates the terrain of random chunks of the dimension,
     * populates them and counts the blocks of each ore per Y level.
     *
     * <p>Everything happens in a {@link ProfilerWorld} with a chunk generator of its
     * own; the server's generator and save are not touched.</p>
     *
     * @param realWorld    the live server-side overworld (seed and world settings)
     * @param dimension    the dimension the ores generate in
     * @param ores         ores to profile (from OreRegistry)
     * @return map from OreData to its computed OreGenerationProfile
     */
    public Map<OreData, OreGenerationProfile> profileByChunks(
        World realWorld,
        int dimension,
        List<OreData> ores
    ) {
        if (ores == null || ores.isEmpty()) return new LinkedHashMap<OreData, OreGenerationProfile>();

        ProfilerChunkProvider profilerProvider = new ProfilerChunkProvider();
        ProfilerWorld profilerWorld;
        IChunkProvider generator;
        int totalChunks;

        // A generator of the profiler world's own, which also supplies the terrain the
        // features are placed into.  The server's real generator is not touched.
        try {
            profilerWorld = new ProfilerWorld(realWorld, dimension);
            profilerWorld.beginProfiling(ores);
            generator = profilerWorld.provider.createChunkGenerator();
            // Terrain comes from a second instance: generating a chunk reseeds the
            // generator's random, which must not happen in the middle of populate()
            profilerWorld.useGeneratedTerrain(profilerWorld.provider.createChunkGenerator());
        } catch (Exception e) {
            NotEnoughResources.LOG.warn("Cannot profile dimension " + dimension, e);
            return new LinkedHashMap<OreData, OreGenerationProfile>();
        }
        totalChunks = dimension == 0 ? TOTAL_CHUNKS : GENERATED_TERRAIN_CHUNKS;

        // ── Profile totalChunks random chunks ─ ───────────────────────────────
        Random rand = new Random(realWorld.getSeed());
        int processed = 0;
        int failed = 0;
        int groupX = 0;
        int groupZ = 0;

        while (processed < totalChunks) {
            int batchEnd = Math.min(processed + CHUNKS_PER_BATCH, totalChunks);
            for (; processed < batchEnd; processed++) {
                // Chunks are taken in square groups at random places: populating a chunk
                // needs the terrain of its neighbours, which a group shares
                int inGroup = processed % (GROUP_SIDE * GROUP_SIDE);
                if (inGroup == 0) {
                    groupX = rand.nextInt(CHUNK_RANGE * 2 + 1) - CHUNK_RANGE;
                    groupZ = rand.nextInt(CHUNK_RANGE * 2 + 1) - CHUNK_RANGE;
                }
                int cx = groupX + inGroup % GROUP_SIDE;
                int cz = groupZ + inGroup / GROUP_SIDE;
                // populate() decorates with the biome at the centre of the populated area
                profilerWorld.beginChunk(profilerWorld.getBiomeGenForCoords(cx * 16 + 16, cz * 16 + 16));
                boolean complete = true;
                try {
                    // A generator only learns which structures (dungeons, hollow hills, ...)
                    // reach a chunk while generating the terrain around it
                    profilerWorld.setTerrainChunk(cx, cz, generator.provideChunk(cx, cz));
                    // Ores that are part of the terrain itself
                    profilerWorld.countTerrainOres(cx, cz);
                    // Fire the real ore/feature population with our intercepting world
                    generator.populate(profilerProvider, cx, cz);
                } catch (Exception ignored) {
                    // Some generators or forge event handlers may fail with a stub world.
                    // Individual failures are acceptable — we still aggregate valid samples.
                    complete = false;
                }
                try {
                    // Mods add their ores through IWorldGenerators, which Forge runs after populate()
                    GameRegistry.generateWorld(cx, cz, profilerWorld, generator, profilerProvider);
                } catch (Exception ignored) {
                    complete = false;
                }
                if (!complete) failed++;
            }
        }

        if (failed > 0) {
            NotEnoughResources.LOG.warn("World generation profiling: " + failed + " of " + totalChunks
                + " chunks in dimension " + dimension + " were only partly populated");
        }

        // ── Convert raw counts to OreGenerationProfiles ──────────────────────
        // Probability at Y = oresFoundAtY / (totalChunks × 16 × 16)
        long denominator = (long) totalChunks * 16L * 16L;
        Map<OreData, OreGenerationProfile> result = new LinkedHashMap<OreData, OreGenerationProfile>();

        for (OreData ore : ores) {
            OreGenerationProfile profile = new OreGenerationProfile(ore.getMinY(), ore.getMaxY());
            int[] counts = profilerWorld.getOreCounts(ore.getBlock(), ore.getMetadata());

            if (counts != null) {
                for (int y = ore.getMinY(); y <= ore.getMaxY(); y++) {
                    if (counts[y] > 0) {
                        profile.setProbability(y, (double) counts[y] / denominator);
                    }
                }
            }
            profile.setSpawnBiomes(findSpawnBiomes(
                profilerWorld.getChunksByBiome(),
                profilerWorld.getOreChunksByBiome(ore.getBlock(), ore.getMetadata())));
            result.put(ore, profile);
        }

        return result;
    }

    /**
     * Returns the biomes an ore is restricted to, or an empty list if it shows no
     * biome restriction.
     *
     * <p>Biomes where the ore was found are only reported when its absence from all
     * other profiled chunks cannot be explained by chance: for an ore that generates
     * everywhere, the chance that all {@code oreChunks} hits miss a share {@code u}
     * of the chunks is {@code (1 - u) ^ oreChunks}.</p>
     */
    private static List<String> findSpawnBiomes(
        Map<String, Integer> chunksByBiome,
        Map<String, Integer> oreChunksByBiome
    ) {
        List<String> biomes = new ArrayList<String>();
        if (oreChunksByBiome == null || oreChunksByBiome.isEmpty()) return biomes;

        int totalChunks   = 0;
        int chunksWithout = 0;
        int oreChunks     = 0;

        for (Map.Entry<String, Integer> entry : chunksByBiome.entrySet()) {
            totalChunks += entry.getValue();
            Integer found = oreChunksByBiome.get(entry.getKey());
            if (found == null) {
                chunksWithout += entry.getValue();
            } else {
                oreChunks += found;
            }
        }
        if (totalChunks == 0 || chunksWithout == 0) return biomes;

        double shareWithout = (double) chunksWithout / totalChunks;
        if (1.0 - shareWithout > MAX_RESTRICTED_SHARE) return biomes;
        if (Math.pow(1.0 - shareWithout, oreChunks) >= BIOME_RESTRICTION_SIGNIFICANCE) return biomes;

        biomes.addAll(oreChunksByBiome.keySet());
        Collections.sort(biomes);
        return biomes;
    }

    // ── Legacy API (kept for backward compatibility and fallback) ─────────────

    /**
     * Profile a single ore over the given XZ area (old scan-based approach).
     */
    public OreGenerationProfile profile(
        World world,
        OreData oreData,
        int minX, int maxX,
        int minZ, int maxZ
    ) {
        OreGenerationProfile profileResult =
            new OreGenerationProfile(oreData.getMinY(), oreData.getMaxY());

        Block targetBlock = oreData.getBlock();
        int   targetMeta  = oreData.getMetadata();

        for (int y = oreData.getMinY(); y <= oreData.getMaxY(); y++) {
            int oreCount   = 0;
            int blockCount = 0;

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (!world.blockExists(x, y, z)) continue;
                    Block block = world.getBlock(x, y, z);
                    int   meta  = world.getBlockMetadata(x, y, z);
                    blockCount++;
                    if (block == targetBlock && meta == targetMeta) oreCount++;
                }
            }
            if (blockCount > 0) profileResult.setProbability(y, (double) oreCount / blockCount);
        }
        return profileResult;
    }

    /**
     * Profile all ores in a single scan pass over the given XZ area.
     */
    public Map<OreData, OreGenerationProfile> profileAll(
        World world,
        List<OreData> ores,
        int minX, int maxX,
        int minZ, int maxZ
    ) {
        Map<OreData, OreGenerationProfile> result = new LinkedHashMap<OreData, OreGenerationProfile>();
        if (ores == null || ores.isEmpty()) return result;

        int overallMinY = Integer.MAX_VALUE;
        int overallMaxY = Integer.MIN_VALUE;
        for (OreData ore : ores) {
            if (ore.getMinY() < overallMinY) overallMinY = ore.getMinY();
            if (ore.getMaxY() > overallMaxY) overallMaxY = ore.getMaxY();
        }

        int yRange   = overallMaxY - overallMinY + 1;
        int oreCount = ores.size();

        int[]   totalBlocksPerY = new int[yRange];
        int[][] oreBlocksPerY   = new int[oreCount][yRange];

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (!world.blockExists(x, overallMinY, z)) continue;
                for (int y = overallMinY; y <= overallMaxY; y++) {
                    Block block = world.getBlock(x, y, z);
                    int   meta  = world.getBlockMetadata(x, y, z);
                    int   yIdx  = y - overallMinY;
                    totalBlocksPerY[yIdx]++;
                    for (int i = 0; i < oreCount; i++) {
                        OreData ore = ores.get(i);
                        if (y < ore.getMinY() || y > ore.getMaxY()) continue;
                        if (block == ore.getBlock() && meta == ore.getMetadata()) {
                            oreBlocksPerY[i][yIdx]++;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < oreCount; i++) {
            OreData            ore     = ores.get(i);
            OreGenerationProfile profile =
                new OreGenerationProfile(ore.getMinY(), ore.getMaxY());
            for (int y = ore.getMinY(); y <= ore.getMaxY(); y++) {
                int yIdx  = y - overallMinY;
                int total = totalBlocksPerY[yIdx];
                if (total > 0) profile.setProbability(y, (double) oreBlocksPerY[i][yIdx] / total);
            }
            result.put(ore, profile);
        }

        return result;
    }
}
