package com.medinexz.notenoughresources.core;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OreGenerationManager {

    private OreProfiler  profiler;
    private OreDiscovery discovery;

    private boolean profiled = false;

    private Map<OreKey, OreData> ores;

    private static final OreGenerationManager INSTANCE = new OreGenerationManager();

    private OreGenerationManager() {
        ores      = new HashMap<OreKey, OreData>();
        profiler  = new OreProfiler();
        discovery = new OreDiscovery(new VanillaOreDetector());
    }

    public static OreGenerationManager getInstance() {
        return INSTANCE;
    }

    public void registerOre(OreData data) {
        ores.put(new OreKey(data.getBlock(), data.getMetadata()), data);
    }

    public OreData getOreData(Block block, int metadata) {
        return ores.get(new OreKey(block, metadata));
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {

        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.worldObj.isRemote)     return;
        if (profiled)                           return;

        profiled = true;

        EntityPlayer player = event.player;
        World        world  = player.worldObj;

        int playerX = (int) player.posX;
        int playerZ = (int) player.posZ;

        // ── Discovery (unchanged) ──────────────────────────────────────────────
        int minX = playerX - 64;
        int maxX = playerX + 64;
        int minZ = playerZ - 64;
        int maxZ = playerZ + 64;

        List<OreDiscoveryResult> discovered =
            discovery.discover(world, minX, maxX, 0, 128, minZ, maxZ);
        discovery.registerDiscoveredOres(discovered);

        // ── Chunk-based profiling (JER-style) ─────────────────────────────────
        List<OreData> oresToProfile = OreRegistry.getOres();

        System.out.println("=== NER PROFILING (chunk-based) ===");
        System.out.println("Ores:         " + oresToProfile.size());
        System.out.println("Total chunks: " + OreProfiler.TOTAL_CHUNKS
            + "  batch: " + OreProfiler.CHUNKS_PER_BATCH);

        long startMs = System.currentTimeMillis();

        Map<OreData, OreGenerationProfile> profiles =
            profiler.profileByChunks(world, oresToProfile);

        long elapsedMs = System.currentTimeMillis() - startMs;
        System.out.println("Profiling completed in " + elapsedMs + " ms");

        // ── Apply profiles ─────────────────────────────────────────────────────
        long samplesPerY = (long) OreProfiler.TOTAL_CHUNKS * 16L * 16L;

        for (OreData oreData : oresToProfile) {
            OreGenerationProfile profile = profiles.get(oreData);
            if (profile == null) continue;

            oreData.setGenerationProfile(profile);
            registerOre(oreData);

            System.out.println("Ore: " + oreData.getName()
                + "  Y=" + oreData.getMinY() + "-" + oreData.getMaxY()
                + "  Samples/Y=" + samplesPerY
                + "  Peak: Y=" + profile.getPeakY()
                + " (" + String.format("%.4f%%", profile.getPeakProbability() * 100.0) + ")");
        }

        System.out.println("=== NER PROFILING COMPLETE ===");
    }

    // ── Inner helper kept for backward compat with OreProfilerTest ────────────

    public OreGenerationProfile profileOre(
        World world,
        OreData oreData,
        int minX, int maxX,
        int minZ, int maxZ
    ) {
        OreGenerationProfile profile =
            profiler.profile(world, oreData, minX, maxX, minZ, maxZ);
        oreData.setGenerationProfile(profile);
        registerOre(oreData);
        return profile;
    }
}
