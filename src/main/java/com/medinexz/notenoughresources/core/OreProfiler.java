package com.medinexz.notenoughresources.core;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkProviderServer;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class OreProfiler {

    // ── Chunk-based profiling constants (JER-style) ───────────────────────────
    static final int TOTAL_CHUNKS    = 1000;
    static final int CHUNKS_PER_BATCH =  25;

    // Chunk coordinate range for random sampling: ±1875 chunks (~30 000 blocks)
    private static final int CHUNK_RANGE = 1875;

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
     * @param realWorld    the live server-side World
     * @param ores         ores to profile (from OreRegistry)
     * @return map from OreData to its computed OreGenerationProfile
     */
    public Map<OreData, OreGenerationProfile> profileByChunks(
        World realWorld,
        List<OreData> ores
    ) {
        if (ores == null || ores.isEmpty()) return new LinkedHashMap<OreData, OreGenerationProfile>();

        // ── Locate worldObj field in the real terrain generator ──────────────
        if (!(realWorld.getChunkProvider() instanceof ChunkProviderServer)) {
            return profileAll(realWorld, ores, -64, 64, -64, 64);
        }
        ChunkProviderServer cps = (ChunkProviderServer) realWorld.getChunkProvider();
        Object generator = cps.currentChunkProvider; // IChunkProvider (ChunkProviderGenerate in vanilla)

        Field worldObjField = findWorldField(generator);

        if (worldObjField == null) {
            // Not ChunkProviderGenerate — fall back to scanning loaded world area
            int px = (int) realWorld.getWorldInfo().getVanillaDimension(); // just a dummy; use player pos
            return profileAll(realWorld, ores, -64, 64, -64, 64);
        }

        // ── Build profiler world and set up ore tracking ─────────────────────
        ProfilerWorld profilerWorld = new ProfilerWorld(realWorld);
        profilerWorld.beginProfiling(ores);
        ProfilerChunkProvider profilerProvider = new ProfilerChunkProvider();

        // ── Swap worldObj ────────────────────────────────────────────────────
        World savedWorldObj;
        try {
            savedWorldObj = (World) worldObjField.get(generator);
            worldObjField.set(generator, profilerWorld);
        } catch (Exception e) {
            return profileAll(realWorld, ores, -64, 64, -64, 64);
        }

        // ── Profile TOTAL_CHUNKS random chunks ───────────────────────────────
        Random rand = new Random(realWorld.getSeed());
        int processed = 0;

        while (processed < TOTAL_CHUNKS) {
            int batchEnd = Math.min(processed + CHUNKS_PER_BATCH, TOTAL_CHUNKS);
            for (; processed < batchEnd; processed++) {
                int cx = rand.nextInt(CHUNK_RANGE * 2 + 1) - CHUNK_RANGE;
                int cz = rand.nextInt(CHUNK_RANGE * 2 + 1) - CHUNK_RANGE;
                try {
                    // Fire the real ore/feature population with our intercepting world
                    ((net.minecraft.world.chunk.IChunkProvider) generator)
                        .populate(profilerProvider, cx, cz);
                } catch (Exception ignored) {
                    // Some generators or forge event handlers may fail with a stub world.
                    // Individual failures are acceptable — we still aggregate valid samples.
                }
            }
        }

        // ── Restore worldObj ─────────────────────────────────────────────────
        try {
            worldObjField.set(generator, savedWorldObj);
        } catch (Exception ignored) {}

        // ── Convert raw counts to OreGenerationProfiles ──────────────────────
        // Probability at Y = oresFoundAtY / (totalChunks × 16 × 16)
        long denominator = (long) TOTAL_CHUNKS * 16L * 16L;
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
            result.put(ore, profile);
        }

        return result;
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
