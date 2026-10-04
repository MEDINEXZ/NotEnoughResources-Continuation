package com.medinexz.notenoughresources.core;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import java.util.ArrayList;
import java.util.List;

/**
 * Central registry for all ores that NER should profile and display in NEI.
 *
 * <p><b>How to add a new ore</b></p>
 * <ol>
 *   <li><b>Find the Block and metadata.</b><br>
 *       Vanilla: {@code Blocks.iron_ore} (meta 0).<br>
 *       Modded:  {@code (Block) cpw.mods.fml.common.registry.GameRegistry.findBlock("modid", "block_name")}
 *               — returns {@code null} when the mod is absent, so always null-check.</li>
 *   <li><b>Decide the Y range (minY, maxY).</b><br>
 *       Use the known generation range or a wide range like {@code 0–64}.
 *       OreProfiler will compute the actual probability curve within that range.</li>
 *   <li><b>Register:</b>
 *       <pre>OreRegistry.register(new OreData("Display Name", block, metadata, minY, maxY));</pre></li>
 *   <li><b>Add the block to a detector</b> (optional but recommended).<br>
 *       If you want the ore to be auto-discovered via {@link OreDiscovery}, add it
 *       to the relevant {@link IOreDetector} implementation (e.g. {@link VanillaOreDetector}
 *       for vanilla, or a custom one for your mod).</li>
 *   <li><b>Done.</b>  On next world join the ore will be profiled and appear in NEI (R key).</li>
 * </ol>
 *
 * <p>Both {@code Block} instance <em>and</em> {@code metadata} form the identity
 * of an entry — {@code BlockOre@meta0} and {@code BlockOre@meta1} are distinct ores.</p>
 */
public class OreRegistry {

    private static final List<OreData> ores = new ArrayList<OreData>();

    public static void register(OreData oreData) {
        for (OreData existing : ores) {
            if (existing.getBlock()     == oreData.getBlock()
             && existing.getMetadata()  == oreData.getMetadata()
             && existing.getDimension() == oreData.getDimension()) {
                return; // already registered — skip duplicate
            }
        }
        ores.add(oreData);
    }

    public static OreData getOre(Block block, int metadata) {
        for (OreData oreData : ores) {
            if (oreData.getBlock()    == block
             && oreData.getMetadata() == metadata) {
                return oreData;
            }
        }
        return null;
    }

    /** All entries of a block, one per dimension it generates in. */
    public static List<OreData> getOres(Block block, int metadata) {
        List<OreData> result = new ArrayList<OreData>();
        for (OreData oreData : ores) {
            if (oreData.getBlock()    == block
             && oreData.getMetadata() == metadata) {
                result.add(oreData);
            }
        }
        return result;
    }

    public static List<OreData> getOres() {
        return ores;
    }

    // ── Built-in vanilla ores ─────────────────────────────────────────────────

    /**
     * Registers all standard Minecraft 1.7.10 overworld ores.
     * Call this once during mod {@code init()} before world join.
     */
    public static void registerVanillaOres() {
        register(new OreData("Coal Ore",      Blocks.coal_ore,      0, 0,  255));
        register(new OreData("Iron Ore",      Blocks.iron_ore,      0, 0,   128));
        register(new OreData("Gold Ore",      Blocks.gold_ore,      0, 0,   128));
        register(new OreData("Redstone Ore",  Blocks.redstone_ore,  0, 0,   128));
        register(new OreData("Lapis Ore",     Blocks.lapis_ore,     0, 0,   128));
        register(new OreData("Diamond Ore",   Blocks.diamond_ore,   0, 0,   128));
        register(new OreData("Emerald Ore",   Blocks.emerald_ore,   0, 0,   128));
        register(new OreData("Clay", Blocks.clay, 0, 0, 128));
        register(new OreData("Pumpkin", Blocks.pumpkin, 0, 0, 128));
        register(new OreData("Melon", Blocks.melon_block, 0, 0, 128));

        // Nether (dimension -1)
        register(new OreData("Nether Quartz Ore", Blocks.quartz_ore, 0, 0, 255, -1));
        register(new OreData("Glowstone",         Blocks.glowstone,  0, 0, 255, -1));
    }

    // ── Modded / additional ores ──────────────────────────────────────────────

    /**
     * Registration point for modded or extra ores.
     *
     * <p>Call this from your mod's {@code init()} or {@code postInit()} after
     * any target mods have had a chance to register their blocks.</p>
     *
     * <p>Pattern for a mod ore (null-safe — safe to call even if the mod is absent):</p>
     * <pre>{@code
     * // 1. Look up the block by mod ID + block name
     * Block copperOre = (Block) cpw.mods.fml.common.registry.GameRegistry
     *                       .findBlock("YourModID", "copper_ore");
     *
     * // 2. Guard against the mod not being installed
     * if (copperOre != null) {
     *     // 3. Register: name, block, metadata, minY, maxY
     *     register(new OreData("Copper Ore", copperOre, 0, 0, 64));
     * }
     * }</pre>
     *
     * <p>If the ore should also be <em>auto-discovered</em> by {@link OreDiscovery}
     * (i.e. tracked even if not pre-registered), add a corresponding
     * {@link IOreDetector} implementation and pass it to {@link OreDiscovery}.</p>
     */
    public static void registerModdedOres() {
        // Add modded ore entries here.  Example (adapt modid and block name):
        //
        // Block copperOre = (Block) cpw.mods.fml.common.registry.GameRegistry
        //                       .findBlock("YourModID", "copper_ore");
        // if (copperOre != null) {
        //     register(new OreData("Copper Ore", copperOre, 0, 0, 64));
        // }
    }
}
