package com.medinexz.notenoughresources.core;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * How many of one item a block drops when mined without Silk Touch, for every
 * Fortune level: a distribution "drop count → probability" per level.
 *
 * <p>The numbers come from the block's own drop methods, called the same way
 * {@code Block.getDrops} calls them.  Blocks that only use {@code Random.nextInt(n)}
 * (all vanilla ones) are evaluated exactly by walking every possible sequence of
 * random results; anything else falls back to sampling.</p>
 */
public class DropStatistics {

    public static final int MAX_FORTUNE = 3;

    // Exact evaluation gives up after this many random paths per Fortune level
    private static final int MAX_EXACT_PATHS = 20000;

    // Number of simulated block breaks per Fortune level when sampling
    private static final int SAMPLES = 100000;

    // A drop count above this is treated as a broken answer rather than simulated
    private static final int MAX_DROP_COUNT = 4096;

    // The world blocks are asked for their drops in; created on first use
    private static World dropWorld;
    private static boolean dropWorldFailed;

    private final ItemStack stack;
    private final boolean exact;

    // Index = Fortune level; drop count → probability
    private final List<Map<Integer, Double>> distributions;

    private DropStatistics(ItemStack stack, boolean exact, List<Map<Integer, Double>> distributions) {
        this.stack = stack;
        this.exact = exact;
        this.distributions = distributions;
    }

    public ItemStack getStack() {
        return stack;
    }

    /** False if the probabilities were sampled instead of computed exactly. */
    public boolean isExact() {
        return exact;
    }

    /** Drop count → probability at the given Fortune level (0 = no Fortune). */
    public Map<Integer, Double> getDistribution(int fortune) {
        return distributions.get(fortune);
    }

    public double getAverage(int fortune) {
        double average = 0.0;
        for (Map.Entry<Integer, Double> entry : distributions.get(fortune).entrySet()) {
            average += entry.getKey() * entry.getValue();
        }
        return average;
    }

    public boolean matches(ItemStack other) {
        return other != null
            && other.getItem() == stack.getItem()
            && other.getItemDamage() == stack.getItemDamage();
    }

    // ── Analysis ─────────────────────────────────────────────────────────────

    /**
     * Computes the drop statistics of every item the block can drop.
     *
     * @return one entry per dropped item (empty if the block drops nothing), or
     *         {@code null} if the block could not answer without a real world
     */
    public static List<DropStatistics> analyze(Block block, int metadata) {
        try {
            Map<String, ItemStack> stacks = new LinkedHashMap<String, ItemStack>();
            List<Map<Map<String, Integer>, Double>> outcomesByFortune =
                new ArrayList<Map<Map<String, Integer>, Double>>();
            boolean exact = true;

            for (int fortune = 0; fortune <= MAX_FORTUNE; fortune++) {
                Map<Map<String, Integer>, Double> outcomes = enumerate(block, metadata, fortune, stacks);
                if (outcomes == null) {
                    exact = false;
                    outcomes = sample(block, metadata, fortune, stacks);
                }
                outcomesByFortune.add(outcomes);
            }

            List<DropStatistics> result = new ArrayList<DropStatistics>();
            for (Map.Entry<String, ItemStack> item : stacks.entrySet()) {
                List<Map<Integer, Double>> distributions = new ArrayList<Map<Integer, Double>>();
                for (Map<Map<String, Integer>, Double> outcomes : outcomesByFortune) {
                    Map<Integer, Double> distribution = new TreeMap<Integer, Double>();
                    for (Map.Entry<Map<String, Integer>, Double> outcome : outcomes.entrySet()) {
                        Integer count = outcome.getKey().get(item.getKey());
                        add(distribution, count == null ? 0 : count, outcome.getValue());
                    }
                    distributions.add(distribution);
                }
                result.add(new DropStatistics(item.getValue(), exact, distributions));
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /** Walks every possible sequence of random results; null if that is not possible. */
    private static Map<Map<String, Integer>, Double> enumerate(
        Block block, int metadata, int fortune, Map<String, ItemStack> stacks
    ) {
        Map<Map<String, Integer>, Double> outcomes = new LinkedHashMap<Map<String, Integer>, Double>();
        EnumeratingRandom random = new EnumeratingRandom();
        int paths = 0;

        do {
            random.begin();
            Map<String, Integer> outcome = breakOnce(block, metadata, fortune, random, stacks);
            if (!random.exact || ++paths > MAX_EXACT_PATHS) return null;
            add(outcomes, outcome, random.probability);
        } while (random.advance());

        return outcomes;
    }

    private static Map<Map<String, Integer>, Double> sample(
        Block block, int metadata, int fortune, Map<String, ItemStack> stacks
    ) {
        Map<Map<String, Integer>, Double> outcomes = new LinkedHashMap<Map<String, Integer>, Double>();
        Random random = new Random(0);

        for (int i = 0; i < SAMPLES; i++) {
            add(outcomes, breakOnce(block, metadata, fortune, random, stacks), 1.0 / SAMPLES);
        }
        return outcomes;
    }

    /** One block break, mirroring Block.getDrops: item key → number dropped. */
    private static Map<String, Integer> breakOnce(
        Block block, int metadata, int fortune, Random random, Map<String, ItemStack> stacks
    ) {
        Map<String, Integer> outcome = new TreeMap<String, Integer>();

        // Through the block's getDrops wherever possible: mods override it
        World world = getDropWorld();
        if (world != null) {
            breakInWorld(world, block, metadata, fortune, random, stacks, outcome);
            return outcome;
        }

        int count = block.quantityDropped(metadata, fortune, random);
        if (count > MAX_DROP_COUNT) throw new IllegalStateException("drop count " + count);

        for (int i = 0; i < count; i++) {
            Item item = block.getItemDropped(metadata, random, fortune);
            if (item == null) continue;

            int damage = block.damageDropped(metadata);
            String key = Item.getIdFromItem(item) + ":" + damage;
            if (!stacks.containsKey(key)) stacks.put(key, new ItemStack(item, 1, damage));
            add(outcome, key, 1);
        }
        return outcome;
    }

    /**
     * One block break through Block.getDrops, in a world whose random is the given
     * one (getDrops, overridden or not, takes its randomness from world.rand).
     */
    private static void breakInWorld(
        World dropWorld, Block block, int metadata, int fortune, Random random,
        Map<String, ItemStack> stacks, Map<String, Integer> outcome
    ) {
        Random worldRandom = dropWorld.rand;
        dropWorld.rand = random;
        try {
            for (ItemStack drop : block.getDrops(dropWorld, 0, 0, 0, metadata, fortune)) {
                if (drop == null || drop.getItem() == null) continue;
                if (drop.stackSize > MAX_DROP_COUNT) {
                    throw new IllegalStateException("drop count " + drop.stackSize);
                }

                String key = Item.getIdFromItem(drop.getItem()) + ":" + drop.getItemDamage();
                if (!stacks.containsKey(key)) {
                    stacks.put(key, new ItemStack(drop.getItem(), 1, drop.getItemDamage()));
                }
                add(outcome, key, drop.stackSize);
            }
        } finally {
            dropWorld.rand = worldRandom;
        }
    }

    /** The world blocks are asked for their drops in, or null if it cannot be created. */
    private static World getDropWorld() {
        if (dropWorld == null && !dropWorldFailed) {
            try {
                dropWorld = new ProfilerWorld();
            } catch (Exception e) {
                dropWorldFailed = true;
            }
        }
        return dropWorld;
    }

    private static <K> void add(Map<K, Double> map, K key, double value) {
        Double old = map.get(key);
        map.put(key, old == null ? value : old + value);
    }

    private static void add(Map<String, Integer> map, String key, int value) {
        Integer old = map.get(key);
        map.put(key, old == null ? value : old + value);
    }

    /**
     * A Random whose nextInt(n) results are scripted, so that every possible
     * sequence can be replayed in turn together with its probability.
     */
    private static final class EnumeratingRandom extends Random {

        private static final int MAX_DEPTH = 64;

        private final int[] path   = new int[MAX_DEPTH];
        private final int[] bounds = new int[MAX_DEPTH];
        private int length;   // scripted prefix
        private int depth;    // nextInt calls made in the current run

        double probability;
        boolean exact = true;

        void begin() {
            depth = 0;
            probability = 1.0;
        }

        /** Moves on to the next unexplored sequence; false when all are done. */
        boolean advance() {
            length = depth;
            while (length > 0) {
                if (path[length - 1] + 1 < bounds[length - 1]) {
                    path[length - 1]++;
                    return true;
                }
                length--;
            }
            return false;
        }

        @Override
        public int nextInt(int n) {
            if (n <= 0) throw new IllegalArgumentException("n must be positive");
            if (depth >= MAX_DEPTH) {
                exact = false;
                return 0;
            }
            if (depth >= length) {
                path[depth] = 0;
                length = depth + 1;
            }
            bounds[depth] = n;
            probability /= n;
            return path[depth++];
        }

        @Override
        public boolean nextBoolean() {
            return nextInt(2) == 1;
        }

        // Every other source of randomness (nextFloat, nextDouble, ...) ends up here
        @Override
        protected int next(int bits) {
            exact = false;
            return super.next(bits);
        }
    }
}
