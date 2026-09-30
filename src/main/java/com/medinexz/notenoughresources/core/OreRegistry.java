package com.medinexz.notenoughresources.core;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import java.util.ArrayList;
import java.util.List;

public class OreRegistry {
    private static final List<OreData> ores = new ArrayList<>();

    public static void register(OreData oreData) {

        for (OreData registeredOre : ores) {

            if (registeredOre.getBlock() == oreData.getBlock()
                && registeredOre.getMetadata() == oreData.getMetadata()) {

                return;
            }
        }

        ores.add(oreData);
    }

    public static OreData getOre(Block block, int metadata) {

        for (OreData oreData : ores) {

            if (oreData.getBlock() == block
                && oreData.getMetadata() == metadata) {

                return oreData;
            }
        }

        return null;
    }

    public static List<OreData> getOres() {
        return ores;
    }

    public static void registerVanillaOres() {
        register(new OreData("Coal Ore", Blocks.coal_ore, 0, 0, 128));
        register(new OreData("Iron Ore", Blocks.iron_ore, 0, 0, 64));
        register(new OreData("Gold Ore", Blocks.gold_ore, 0, 0, 32));
        register(new OreData("Redstone Ore", Blocks.redstone_ore, 0, 0, 16));
        register(new OreData("Lapis Ore", Blocks.lapis_ore, 0, 0, 32));
        register(new OreData("Diamond Ore", Blocks.diamond_ore, 0, 0, 16));
        register(new OreData("Emerald Ore", Blocks.emerald_ore, 0, 0, 32));
    }
}
