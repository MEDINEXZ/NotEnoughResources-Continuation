package com.medinexz.notenoughresources.core;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class OreGenerationManager {

    private OreProfiler  profiler;
    private OreDiscovery discovery;

    private boolean profiled = false;

    // Set for the headless check (see NotEnoughResources.serverStarted): log every result in full
    private static final boolean HEADLESS_CHECK = System.getenv("NER_PROFILE_ON_START") != null;

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
        discovery.registerDiscoveredOres(discovered, world.provider.dimensionId);

        profileRegisteredOres();
    }

    /**
     * Profiles every registered ore, one chunk-based (JER-style) pass per dimension.
     * Needs a running server; nothing is read from or written to its save.
     */
    public void profileRegisteredOres() {
        profiled = true;

        Map<Integer, List<OreData>> oresByDimension = new TreeMap<Integer, List<OreData>>();
        for (OreData oreData : OreRegistry.getOres()) {
            List<OreData> list = oresByDimension.get(oreData.getDimension());
            if (list == null) {
                list = new ArrayList<OreData>();
                oresByDimension.put(oreData.getDimension(), list);
            }
            list.add(oreData);
        }

        // Seed and world settings are the same for every dimension of the save
        World overworld = MinecraftServer.getServer().worldServerForDimension(0);

        for (Map.Entry<Integer, List<OreData>> entry : oresByDimension.entrySet()) {
            int dimension = entry.getKey();
            List<OreData> oresToProfile = entry.getValue();

            System.out.println("=== NER PROFILING (chunk-based), dimension " + dimension + " ===");
            System.out.println("Ores: " + oresToProfile.size());

            long startMs = System.currentTimeMillis();

            Map<OreData, OreGenerationProfile> profiles =
                profiler.profileByChunks(overworld, dimension, oresToProfile);

            long elapsedMs = System.currentTimeMillis() - startMs;
            System.out.println("Profiling completed in " + elapsedMs + " ms");

            // ── Apply profiles ─────────────────────────────────────────────────
            for (OreData oreData : oresToProfile) {
                OreGenerationProfile profile = profiles.get(oreData);
                if (profile == null) continue;

                oreData.setGenerationProfile(profile);
                registerOre(oreData);

                System.out.println("Ore: " + oreData.getName()
                    + "  Y=" + oreData.getMinY() + "-" + oreData.getMaxY()
                    + "  Peak: Y=" + profile.getPeakY()
                    + " (" + String.format("%.4f%%", profile.getPeakProbability() * 100.0) + ")");
                if (HEADLESS_CHECK) logResource(oreData, profile);
            }
        }

        System.out.println("=== NER PROFILING COMPLETE ===");
    }

    /** Everything the World Generation entry of a resource shows, as one log line. */
    private static void logResource(OreData oreData, OreGenerationProfile profile) {
        double blocksPerChunk = 0.0;
        int lowestY = -1;
        int highestY = -1;
        for (int y = oreData.getMinY(); y <= oreData.getMaxY(); y++) {
            double probability = profile.getProbability(y);
            if (probability > 0.0) {
                if (lowestY < 0) lowestY = y;
                highestY = y;
            }
            blocksPerChunk += probability * 256.0;
        }

        StringBuilder drops = new StringBuilder();
        for (ItemStack drop : oreData.getDrops()) {
            DropStatistics stats = oreData.getDropStatistics(drop);
            drops.append(drop.getDisplayName());
            if (stats != null) {
                drops.append(String.format(Locale.ROOT, " avg[%.2f %.2f %.2f %.2f]%s",
                    stats.getAverage(0), stats.getAverage(1), stats.getAverage(2), stats.getAverage(3),
                    stats.isExact() ? "" : " sampled"));
            }
            drops.append("; ");
        }

        System.out.println("=== NER WORLDGEN RESOURCE === " + oreData.getName()
            + " | mod=" + oreData.getModName()
            + " | block=" + Block.blockRegistry.getNameForObject(oreData.getBlock()) + ":" + oreData.getMetadata()
            + " | dim=" + oreData.getDimension() + " (" + oreData.getDimensionName() + ")"
            + " | Y " + lowestY + ".." + highestY
            + String.format(Locale.ROOT, " | %.3f blocks/chunk", blocksPerChunk)
            + " | biomes=" + profile.getSpawnBiomes()
            + " | silk=" + oreData.isSilkTouchNeeded()
            + " | drops: " + drops);
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
