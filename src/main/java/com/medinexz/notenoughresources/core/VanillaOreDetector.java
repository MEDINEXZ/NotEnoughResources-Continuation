package com.medinexz.notenoughresources.core;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

/**
 * Detects vanilla Minecraft 1.7.10 overworld ores during an {@link OreDiscovery} scan.
 *
 * <p>Discovery is a secondary mechanism: it scans the world area once on join and
 * auto-registers any ore it finds that is not already in {@link OreRegistry}.
 * Vanilla ores are pre-registered via {@link OreRegistry#registerVanillaOres()}, so
 * discovery primarily benefits modded ores that lack a pre-registration call.</p>
 *
 * <p><b>To support a modded ore in discovery:</b></p>
 * <ol>
 *   <li>Create a new {@link IOreDetector} implementation (e.g. {@code MyModOreDetector})
 *       that returns {@code true} for the modded blocks you care about.</li>
 *   <li>Compose it with this detector or pass it separately to {@link OreDiscovery}.</li>
 *   <li>Alternatively, add the block check directly to this class if it is always present.</li>
 * </ol>
 *
 * <p>Keep in mind: detection only controls which blocks are <em>auto-discovered</em>.
 * You can always register an ore explicitly via {@link OreRegistry#register} without
 * adding it to any detector.</p>
 */
public class VanillaOreDetector implements IOreDetector {

    @Override
    public boolean isOre(Block block, int metadata) {
        return block == Blocks.coal_ore
            || block == Blocks.iron_ore
            || block == Blocks.gold_ore
            || block == Blocks.redstone_ore
            || block == Blocks.lit_redstone_ore  // activated form; same ore, different block
            || block == Blocks.lapis_ore
            || block == Blocks.diamond_ore
            || block == Blocks.emerald_ore
            || block == Blocks.clay
            || block == Blocks.pumpkin
            || block == Blocks.melon_block;
    }
}
