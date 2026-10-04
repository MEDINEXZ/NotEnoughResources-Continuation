package com.medinexz.notenoughresources.core;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldProvider;

import java.util.ArrayList;
import java.util.List;

public class OreData {
    private String name;
    private Block block;
    private int metadata;

    private int minY;
    private int maxY;

    private OreGenerationProfile generationProfile;
    private boolean profiled;

    private List<ItemStack> drops = new ArrayList<ItemStack>();
    private List<DropStatistics> dropStatistics = new ArrayList<DropStatistics>();
    private int dimension = 0;
    private String dimensionName;

    private String modName;
    private boolean silkTouchNeeded;

    public OreData(String name, Block block, int metadata, int minY, int maxY) {
        this.name = name;
        this.block = block;
        this.metadata = metadata;
        this.minY = minY;
        this.maxY = maxY;
        this.generationProfile = new OreGenerationProfile(minY, maxY);

        this.modName = findModName(block);
        analyzeDrops();
    }

    public OreData(String name, Block block, int metadata, int minY, int maxY, int dimension) {
        this(name, block, metadata, minY, maxY);
        this.dimension = dimension;
    }

    /**
     * Asks the block what it drops when mined without Silk Touch.  A block that
     * only ever drops itself has no drops worth listing; any other block is
     * obtainable as a block only with Silk Touch (if it allows silk harvesting).
     */
    private void analyzeDrops() {
        List<DropStatistics> normalDrops = DropStatistics.analyze(block, metadata);

        // A modded block that needs a real world to answer: assume it drops itself
        boolean dropsItself = normalDrops == null;

        if (normalDrops != null) {
            for (DropStatistics drop : normalDrops) {
                if (isSelf(drop.getStack())) {
                    dropsItself = normalDrops.size() == 1;
                } else {
                    dropStatistics.add(drop);
                    drops.add(drop.getStack());
                }
            }
        }

        silkTouchNeeded = !dropsItself && canSilkHarvest();
    }

    /**
     * For a block whose drop only differs from the generated variant by a
     * bookkeeping metadata (some mods mark blocks a player has placed that way):
     * it is shown as dropping itself.
     */
    public void treatAsSelfDropping() {
        drops.clear();
        dropStatistics.clear();
        silkTouchNeeded = false;
    }

    private boolean isSelf(ItemStack stack) {
        Item self = Item.getItemFromBlock(block);
        return self != null
            && stack.getItem() == self
            && (!self.getHasSubtypes() || stack.getItemDamage() == metadata);
    }

    private boolean canSilkHarvest() {
        try {
            return block.canSilkHarvest(null, null, 0, 0, 0, metadata);
        } catch (Exception e) {
            return true;
        }
    }

    private static String findModName(Block block) {
        GameRegistry.UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(block);
        if (id != null) {
            ModContainer mod = Loader.instance().getIndexedModList().get(id.modId);
            if (mod != null) return mod.getName();
        }
        return "Minecraft";
    }

    public String getName() { return name; }
    public Block getBlock() { return block; }
    public int getMetadata() { return metadata; }
    public int getMinY() { return minY; }
    public int getMaxY() { return maxY; }

    public OreGenerationProfile getGenerationProfile() { return generationProfile; }
    public void setGenerationProfile(OreGenerationProfile generationProfile) {
        this.generationProfile = generationProfile;
        this.profiled = true;
    }

    /**
     * False once profiling has run without finding the resource anywhere (it does
     * not generate with this world's settings, or is too rare for the sample):
     * there is nothing to show for it then.
     */
    public boolean hasGenerationData() {
        return !profiled || generationProfile.getPeakProbability() > 0.0;
    }

    public List<ItemStack> getDrops() { return drops; }
    public void addDrop(ItemStack drop) { drops.add(drop); }

    public int getDimension() { return dimension; }
    public void setDimension(int dimension) {
        this.dimension = dimension;
        this.dimensionName = null;
    }

    public String getDimensionName() {
        if (dimensionName == null) {
            try {
                dimensionName = WorldProvider.getProviderForDimension(dimension).getDimensionName();
            } catch (Exception e) {
                dimensionName = "Dimension " + dimension;
            }
        }
        return dimensionName;
    }

    /** Statistics of the given drop of this ore, or null if it is not one of its drops. */
    public DropStatistics getDropStatistics(ItemStack stack) {
        for (DropStatistics drop : dropStatistics) {
            if (drop.matches(stack)) return drop;
        }
        return null;
    }

    /** Overrides the name reported by the dimension's WorldProvider. */
    public void setDimensionName(String dimensionName) { this.dimensionName = dimensionName; }

    public String getModName() { return modName; }
    public boolean isSilkTouchNeeded() { return silkTouchNeeded; }

    /** Biomes this ore is restricted to; empty when it generates everywhere (or is unknown). */
    public List<String> getSpawnBiomes() { return generationProfile.getSpawnBiomes(); }
}
