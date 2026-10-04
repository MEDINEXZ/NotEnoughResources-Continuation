package com.medinexz.notenoughresources.core;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A fake World that intercepts setBlock() calls to count ore placements per Y level.
 *
 * <p>Used by {@link OreProfiler#profileByChunks} as the target world for
 * {@code ChunkProviderGenerate.populate()} calls.  Block reads always return stone
 * so that WorldGenMinable vein placement always succeeds.  Nothing is saved to disk.</p>
 *
 * <p>getBiomeGenForCoords() delegates to the real running world so the correct biome
 * decorators fire for each profiled chunk position.</p>
 */
public class ProfilerWorld extends World {

    // ── Dummy save handler: loadWorldInfo() returns null so the World constructor
    //    creates a fresh WorldInfo from the WorldSettings we pass in.          ──────
    private static final ISaveHandler DUMMY_SAVE_HANDLER = new ISaveHandler() {
        @Override public WorldInfo          loadWorldInfo()                                { return null; }
        @Override public void               checkSessionLock()                             {}
        @Override public IChunkLoader       getChunkLoader(WorldProvider p)               { return null; }
        @Override public void               saveWorldInfoWithPlayer(WorldInfo i, NBTTagCompound t) {}
        @Override public void               saveWorldInfo(WorldInfo i)                    {}
        @Override public IPlayerFileData    getSaveHandler()                              { return null; }
        @Override public void               flush()                                       {}
        @Override public File               getWorldDirectory()                           { return null; }
        @Override public File               getMapFileFromName(String s)                  { return null; }
        @Override public String             getWorldDirectoryName()                       { return "ner_profiler"; }
    };

    private final World realWorld;

    // True if realWorld is the world of the profiled dimension itself
    private final boolean sameDimension;

    // Blocks of the tracked ores, to tell quickly that a block is not one of them
    private Set<Block> trackedBlocks = new HashSet<Block>();

    // Per-ore-key count array indexed by absolute Y (0-255).
    private Map<OreKey, int[]> oreCountsMap = new HashMap<OreKey, int[]>();
    private List<OreData> currentOres;

    // Biome statistics: profiled chunks per biome, and per ore the number of
    // those chunks in which at least one block of the ore was placed.
    private Map<String, Integer> chunksByBiome = new HashMap<String, Integer>();
    private Map<OreKey, Map<String, Integer>> oreChunksByBiome = new HashMap<OreKey, Map<String, Integer>>();
    private Set<OreKey> oresInCurrentChunk = new HashSet<OreKey>();
    private String currentBiome;

    // Reused stub chunk returned to any code that calls getChunkFromChunkCoords.
    // This prevents NPE in structure generators and light-calculation code while
    // still letting our overridden getBlock/setBlock handle all ore logic.
    private Chunk stubChunk;

    // Generated-terrain mode: block reads come from terrain produced by this generator
    // (plus the blocks placed while populating the current chunk) instead of the fixed
    // overworld strata.  Needed where features depend on the real terrain shape, such
    // as glowstone hanging from Nether cave ceilings.
    private IChunkProvider terrainGenerator;
    private Map<Long, Chunk> terrainChunks = new HashMap<Long, Chunk>();
    private Map<Long, Block> placedBlocks = new HashMap<Long, Block>();
    private Map<Long, Integer> placedMetadata = new HashMap<Long, Integer>();
    private Map<Long, TileEntity> placedTileEntities = new HashMap<Long, TileEntity>();
    private static final int MAX_CACHED_TERRAIN_CHUNKS = 64;

    /**
     * @param realWorld a live server world, used for the seed and world settings
     * @param dimension the dimension to profile (may differ from realWorld's)
     */
    public ProfilerWorld(World realWorld, int dimension) {
        this(
            realWorld,
            new WorldSettings(
                realWorld.getWorldInfo().getSeed(),
                WorldSettings.GameType.SURVIVAL,
                realWorld.getWorldInfo().isMapFeaturesEnabled(),
                false,
                realWorld.getWorldInfo().getTerrainType()
            ),
            dimension
        );
    }

    /**
     * A profiler world that belongs to no save: for code that only needs some
     * World to be asked a question in (such as a block's drops), at any time.
     */
    public ProfilerWorld() {
        this(null, new WorldSettings(0L, WorldSettings.GameType.SURVIVAL, false, false, WorldType.DEFAULT), 0);
    }

    private ProfilerWorld(World realWorld, WorldSettings settings, int dimension) {
        super(
            DUMMY_SAVE_HANDLER,
            "ner_profiler",
            settings,
            WorldProvider.getProviderForDimension(dimension),
            new Profiler()
        );
        this.realWorld = realWorld;
        this.sameDimension = realWorld != null && realWorld.provider.dimensionId == dimension;
        this.isRemote   = false;   // server-side world — prevents client checks
    }

    // ── ProfilerWorld API ─────────────────────────────────────────────────────

    public void beginProfiling(List<OreData> ores) {
        currentOres = ores;
        oreCountsMap.clear();
        chunksByBiome.clear();
        oreChunksByBiome.clear();
        currentBiome = null;
        trackedBlocks.clear();
        for (OreData ore : ores) {
            OreKey key = new OreKey(ore.getBlock(), ore.getMetadata());
            trackedBlocks.add(ore.getBlock());
            oreCountsMap.put(key, new int[256]);
            oreChunksByBiome.put(key, new HashMap<String, Integer>());
        }
    }

    /** Switches block reads to terrain generated by the given generator. */
    public void useGeneratedTerrain(IChunkProvider generator) {
        terrainGenerator = generator;
    }

    private Block getGeneratedBlock(int x, int y, int z) {
        if (y < 0 || y >= 256) return Blocks.air;

        if (!placedBlocks.isEmpty()) {
            Block placed = placedBlocks.get(positionKey(x, y, z));
            if (placed != null) return placed;
        }

        return getTerrainChunk(x >> 4, z >> 4).getBlock(x & 15, y, z & 15);
    }

    private Chunk getTerrainChunk(int cx, int cz) {
        Long chunkKey = chunkKey(cx, cz);
        Chunk chunk = terrainChunks.get(chunkKey);
        if (chunk == null) {
            chunk = terrainGenerator.provideChunk(cx, cz);
            terrainChunks.put(chunkKey, chunk);
        }
        return chunk;
    }

    /** Supplies the terrain of a chunk that was generated outside this world. */
    public void setTerrainChunk(int cx, int cz, Chunk chunk) {
        terrainChunks.put(chunkKey(cx, cz), chunk);
    }

    private static Long chunkKey(int cx, int cz) {
        return ((long) cx << 32) | (cz & 0xFFFFFFFFL);
    }

    /**
     * Counts the tracked ores that are part of the generated terrain of a chunk
     * itself (placed by the generator's terrain step rather than by populate).
     */
    public void countTerrainOres(int cx, int cz) {
        if (terrainGenerator == null) return;

        Chunk chunk = getTerrainChunk(cx, cz);
        int maxY = Math.min(256, chunk.getTopFilledSegment() + 16);

        for (int y = 0; y < maxY; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    Block block = chunk.getBlock(x, y, z);
                    if (trackedBlocks.contains(block)) {
                        countOre(block, chunk.getBlockMetadata(x, y, z), y);
                    }
                }
            }
        }
    }

    private void countOre(Block block, int meta, int y) {
        OreKey key = new OreKey(block, meta);
        int[] counts = oreCountsMap.get(key);
        if (counts != null) {
            counts[y]++;
            if (currentBiome != null && oresInCurrentChunk.add(key)) {
                increment(oreChunksByBiome.get(key), currentBiome);
            }
        }
    }

    private static Long positionKey(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | y;
    }

    /** Call before populating a chunk, with the biome whose decorator will run for it. */
    public void beginChunk(BiomeGenBase biome) {
        currentBiome = biome == null ? null : biome.biomeName;
        oresInCurrentChunk.clear();
        placedBlocks.clear();
        placedMetadata.clear();
        placedTileEntities.clear();
        if (terrainChunks.size() > MAX_CACHED_TERRAIN_CHUNKS) terrainChunks.clear();
        if (currentBiome != null) increment(chunksByBiome, currentBiome);
    }

    /** Number of profiled chunks per biome name. */
    public Map<String, Integer> getChunksByBiome() {
        return chunksByBiome;
    }

    /** Per biome name, the number of profiled chunks that contained the given ore. */
    public Map<String, Integer> getOreChunksByBiome(Block block, int metadata) {
        return oreChunksByBiome.get(new OreKey(block, metadata));
    }

    private static void increment(Map<String, Integer> map, String key) {
        Integer old = map.get(key);
        map.put(key, old == null ? 1 : old + 1);
    }

    /** Returns the raw count-per-Y array for the given ore, or null if not tracked. */
    public int[] getOreCounts(Block block, int metadata) {
        return oreCountsMap.get(new OreKey(block, metadata));
    }

    // ── Chunk access (stub prevents NPE in structure generators) ─────────────

    /**
     * Returns a reused empty stub Chunk instead of delegating to a real chunk
     * provider.  Structure generators call setBlock/getBlock via World methods
     * that are overridden anyway, but some code paths read the Chunk object
     * directly (e.g. light values, height maps).  Returning a consistent empty
     * Chunk prevents NPE without affecting ore counting.
     */
    @Override
    public Chunk getChunkFromChunkCoords(int cx, int cz) {
        if (stubChunk == null) stubChunk = new Chunk(this, 0, 0);
        return stubChunk;
    }

    // ── Abstract method implementations ──────────────────────────────────────

    @Override
    protected IChunkProvider createChunkProvider() {
        return new ProfilerChunkProvider();
    }

    @Override
    protected int func_152379_p() {
        return 0; // view distance — irrelevant for profiling
    }

    @Override
    public Entity getEntityByID(int id) {
        return null;
    }

    /**
     * Generators also spawn their initial animals; the profiler world has no
     * chunks to hold entities, so they are simply not spawned.
     */
    @Override
    public boolean spawnEntityInWorld(Entity entity) {
        return false;
    }

    // ── Block access overrides ────────────────────────────────────────────────

    /**
     * Returns a terrain-stratified block to satisfy multiple world-gen generator types:
     * <ul>
     *   <li>Y &lt; 0   → air    — same as the real World below the build height</li>
     *   <li>Y 0     → bedrock — the overworld floor; ore veins cannot replace it</li>
     *   <li>Y 1-61  → stone  — WorldGenMinable (ores) checks getBlock()==stone</li>
     *   <li>Y 62    → dirt   — WorldGenClay replaces dirt/clay blocks near water</li>
     *   <li>Y 63    → water  — WorldGenClay center check: getMaterial()==water</li>
     *   <li>Y 64    → grass  — WorldGenPumpkin/WorldGenMelon check getBlock(Y-1)==grass</li>
     *   <li>Y ≥ 65  → air   — WorldGenPumpkin/WorldGenMelon check isAirBlock(Y)</li>
     * </ul>
     * No chunk lookup — no chunk data exists in the profiler world.
     */
    @Override
    public Block getBlock(int x, int y, int z) {
        if (terrainGenerator != null) return getGeneratedBlock(x, y, z);

        if (y >= 65) return Blocks.air;
        if (y == 64) return Blocks.grass;
        if (y == 63) return Blocks.water;
        if (y == 62) return Blocks.dirt;
        if (y < 0)   return Blocks.air;
        if (y == 0)  return Blocks.bedrock;
        return Blocks.stone; // y 1-61
    }

    @Override
    public int getBlockMetadata(int x, int y, int z) {
        if (terrainGenerator == null || y < 0 || y >= 256) return 0;

        Integer placed = placedMetadata.get(positionKey(x, y, z));
        if (placed != null) return placed;

        return getTerrainChunk(x >> 4, z >> 4).getBlockMetadata(x & 15, y, z & 15);
    }

    /**
     * Generators fill the chests and spawners they place; in generated-terrain mode
     * they get the tile entity of the block they placed, which is kept until the
     * next chunk only.
     */
    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        if (terrainGenerator == null || y < 0 || y >= 256) return null;

        Long key = positionKey(x, y, z);
        TileEntity tileEntity = placedTileEntities.get(key);
        if (tileEntity == null) {
            Block block = getBlock(x, y, z);
            int meta = getBlockMetadata(x, y, z);
            if (!block.hasTileEntity(meta)) return null;

            tileEntity = block.createTileEntity(this, meta);
            if (tileEntity == null) return null;

            tileEntity.setWorldObj(this);
            tileEntity.xCoord = x;
            tileEntity.yCoord = y;
            tileEntity.zCoord = z;
            placedTileEntities.put(key, tileEntity);
        }
        return tileEntity;
    }

    /**
     * The key intercept: count ore placements instead of modifying a real chunk.
     */
    @Override
    public boolean setBlock(int x, int y, int z, Block block, int meta, int flags) {
        if (terrainGenerator != null && y >= 0 && y < 256) {
            // Remembered so that features growing from their own blocks see them
            Long key = positionKey(x, y, z);
            placedBlocks.put(key, block);
            placedMetadata.put(key, meta);
            placedTileEntities.remove(key);
        }
        if (y >= 0 && y < 256 && currentOres != null && trackedBlocks.contains(block)) {
            countOre(block, meta, y);
        }
        return true;
    }

    @Override
    public boolean setBlock(int x, int y, int z, Block block) {
        return setBlock(x, y, z, block, 0, 3);
    }

    @Override
    public boolean setBlockMetadataWithNotify(int x, int y, int z, int meta, int flags) {
        if (terrainGenerator != null && y >= 0 && y < 256 && getBlockMetadata(x, y, z) != meta) {
            placedMetadata.put(positionKey(x, y, z), meta);

            // A variant chosen after its block was placed
            Block block = getBlock(x, y, z);
            if (currentOres != null && trackedBlocks.contains(block)) countOre(block, meta, y);
        }
        return true;
    }

    @Override
    public boolean setBlockToAir(int x, int y, int z) {
        return setBlock(x, y, z, Blocks.air, 0, 3);
    }

    // ── Height / precipitation helpers ───────────────────────────────────────

    /**
     * BiomeDecorator calls clayGen.generate(world, rand, x, getTopSolidOrLiquidBlock(x,z), z).
     * The default implementation accesses the stub chunk (empty → returns -1), which
     * causes WorldGenClay to receive Y=-1 → no water → clay never placed.
     * Return 63 (water Y level) so WorldGenClay finds water at the generation point.
     */
    @Override
    public int getTopSolidOrLiquidBlock(int x, int z) {
        if (terrainGenerator != null) return getHeightValue(x, z);
        return 63;
    }

    /**
     * The ice/snow placement loop in ChunkProviderGenerate.populate() calls this
     * unconditionally.  Return 64 (grass Y level) so the subsequent
     * isBlockFreezable(y-1)/canSnowAt(y) checks see grass/water and don't crash
     * via chunk lookup.
     */
    @Override
    public int getPrecipitationHeight(int x, int z) {
        if (terrainGenerator != null) return getHeightValue(x, z);
        return 64;
    }

    @Override
    public int getHeightValue(int x, int z) {
        if (terrainGenerator != null) {
            return getTerrainChunk(x >> 4, z >> 4).getHeightValue(x & 15, z & 15);
        }
        return 64;
    }

    @Override
    public boolean canBlockSeeTheSky(int x, int y, int z) {
        if (terrainGenerator != null) return y >= getHeightValue(x, z);
        return y >= 65;
    }

    // ── Biome delegation ─────────────────────────────────────────────────────

    /**
     * Biomes of the profiled dimension: taken from the real world if it is that
     * dimension, otherwise from this world's own chunk manager (same seed), so
     * that biome-specific decorators fire correctly for each profiled chunk.
     */
    @Override
    public BiomeGenBase getBiomeGenForCoords(int x, int z) {
        if (!sameDimension) return provider.worldChunkMgr.getBiomeGenAt(x, z);
        return realWorld.getBiomeGenForCoords(x, z);
    }
}
