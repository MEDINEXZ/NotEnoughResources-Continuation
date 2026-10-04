package com.medinexz.notenoughresources.integration;

import com.medinexz.notenoughresources.NotEnoughResources;
import com.medinexz.notenoughresources.core.OreData;
import com.medinexz.notenoughresources.core.OreRegistry;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;

/**
 * Lookups shared by the integrations: blocks by registry name and dimensions by
 * WorldProvider class, so that neither block ids nor configurable dimension ids
 * are fixed in NER and nothing has to be compiled against the other mod.
 */
public class IntegrationHelper {

    // The whole build height: the profiler finds where a resource really generates
    public static final int MIN_Y = 0;
    public static final int MAX_Y = 255;

    /** The id the dimension with the given WorldProvider class is registered under, or null. */
    public static Integer findDimension(String providerClass) {
        for (Integer id : DimensionManager.getStaticDimensionIDs()) {
            try {
                WorldProvider provider = DimensionManager.createProviderFor(id);
                if (provider.getClass().getName().equals(providerClass)) return id;
            } catch (Exception e) {
                // A dimension whose provider cannot be created is not the one we look for
            }
        }
        return null;
    }

    /** The name the given mod is installed under ("The Aether"), or null if it is absent. */
    public static String findModName(String modId) {
        ModContainer mod = Loader.instance().getIndexedModList().get(modId);
        return mod == null ? null : mod.getName();
    }

    /**
     * Registers a world generation resource of a mod.
     *
     * @return the registered resource, or null if the block is not present in the
     *         installed version of the mod or its dimension is not registered
     */
    public static OreData register(
        String modId, String blockName, int metadata, int minY, int maxY, Integer dimension
    ) {
        Block block = GameRegistry.findBlock(modId, blockName);
        if (block == null) {
            NotEnoughResources.LOG.warn("Block " + modId + ":" + blockName + " not found, resource skipped");
        }
        return register(block, metadata, minY, maxY, dimension);
    }

    /** Registers a world generation resource of a mod over the whole build height. */
    public static OreData register(String modId, String blockName, int metadata, Integer dimension) {
        return register(modId, blockName, metadata, MIN_Y, MAX_Y, dimension);
    }

    /**
     * Registers a block (of any mod, or a vanilla one that a mod's dimension
     * generates) as a world generation resource of the given dimension.
     *
     * @return the registered resource, or null if the block or the dimension is unknown
     */
    public static OreData register(Block block, int metadata, int minY, int maxY, Integer dimension) {
        if (block == null || dimension == null) return null;

        OreData oreData = new OreData(displayName(block, metadata), block, metadata, minY, maxY, dimension);
        OreRegistry.register(oreData);
        return oreData;
    }

    /** The name of the block variant as its item shows it. */
    private static String displayName(Block block, int metadata) {
        try {
            Item item = Item.getItemFromBlock(block);
            if (item != null) return new ItemStack(item, 1, metadata).getDisplayName();
        } catch (Exception e) {
            // An item that cannot name this variant: the block's own name is used
        }
        return block.getLocalizedName();
    }
}
