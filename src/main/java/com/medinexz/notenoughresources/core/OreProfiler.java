package com.medinexz.notenoughresources.core;

import net.minecraft.block.Block;
import com.medinexz.notenoughresources.NotEnoughResources;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class OreProfiler {

    // ── Chunk-based profiling constants (JER-style) ───────────────────────────
    static final int TOTAL_CHUNKS    = 1000;
    static final int CHUNKS_PER_BATCH =  25;

    // Chunks profiled in dimensions that need real terrain generated around each
    // of them (much slower per chunk than the overworld's fixed strata)
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
     * JER-style profiling: generate synthetic ore population across
     * {@code TOTAL_CHUNKS} random chunks and accumulate block counts per Y level.
     *
     * <p>Uses the real {@code ChunkProviderGenerate} from the running server, but
     * temporarily redirects its {@code worldObj} to a {@link ProfilerWorld} that
     * intercepts {@code setBlock()} calls.  Nothing is written to the actual save.</p>
     *
     * <p>Falls back to {@link #profileAll} if the terrain generator is not
     * {@code ChunkProviderGenerate} (e.g. a modded dimension generator).</p>
     *
     * @param realWorld    the live server-side overworld
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
        Field worldObjField = null;
        World savedWorldObj = null;
        int totalChunks;

        if (dimension == 0) {
            // ── Overworld: the server's own generator, redirected to the profiler world ──
            if (!(realWorld.getChunkProvider() instanceof ChunkProviderServer)) {
                return profileAll(realWorld, ores, -64, 64, -64, 64);
            }
            ChunkProviderServer cps = (ChunkProviderServer) realWorld.getChunkProvider();
            generator = cps.currentChunkProvider; // ChunkProviderGenerate in vanilla

            worldObjField = findWorldField(generator);

            if (worldObjField == null) {
                // Not ChunkProviderGenerate — fall back to scanning loaded world area
                return profileAll(realWorld, ores, -64, 64, -64, 64);
            }

            profilerWorld = new ProfilerWorld(realWorld, dimension);
            profilerWorld.beginProfiling(ores);

            try {
                savedWorldObj = (World) worldObjField.get(generator);
                worldObjField.set(generator, profilerWorld);
            } catch (Exception e) {
                return profileAll(realWorld, ores, -64, 64, -64, 64);
            }
            totalChunks = TOTAL_CHUNKS;
        } else {
            // ── Other dimensions: a generator of the profiler world's own, which also
            //    supplies the terrain the features are placed into.  The server's real
            //    generator is not touched. ──
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
            totalChunks = GENERATED_TERRAIN_CHUNKS;
        }

        // ── Profile totalChunks random chunks ─ ───────────────────────────────
        Random rand = new Random(realWorld.getSeed());
        int processed = 0;
        int failed = 0;

        while (processed < totalChunks) {
            int batchEnd = Math.min(processed + CHUNKS_PER_BATCH, totalChunks);
            for (; processed < batchEnd; processed++) {
                int cx = rand.nextInt(CHUNK_RANGE * 2 + 1) - CHUNK_RANGE;
                int cz = rand.nextInt(CHUNK_RANGE * 2 + 1) - CHUNK_RANGE;
                // populate() decorates with the biome at the centre of the populated area
                profilerWorld.beginChunk(profilerWorld.getBiomeGenForCoords(cx * 16 + 16, cz * 16 + 16));
                boolean complete = true;
                try {
                    if (dimension != 0) {
                        // A generator only learns which structures (dungeons, hollow hills, ...)
                        // reach a chunk while generating the terrain around it
                        profilerWorld.setTerrainChunk(cx, cz, generator.provideChunk(cx, cz));
                    }
                    // Ores that are part of the terrain itself (generated-terrain mode only)
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

        // ── Restore worldObj ─────────────────────────────────────────────────
        if (worldObjField != null) {
            try {
                worldObjField.set(generator, savedWorldObj);
            } catch (Exception ignored) {}
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

    /**
     * Searches the generator's declared fields for the first one typed as {@link World}.
     * Works in both dev (field named "worldObj") and production (obfuscated name).
     * Returns null if not found or not accessible.
     */
    private static Field findWorldField(Object generator) {
        if (generator == null) return null;
        for (Field f : generator.getClass().getDeclaredFields()) {
            if (World.class.isAssignableFrom(f.getType())) {
                f.setAccessible(true);
                return f;
            }
        }
        return null;
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
