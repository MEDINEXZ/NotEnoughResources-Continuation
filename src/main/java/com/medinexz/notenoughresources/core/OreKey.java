package com.medinexz.notenoughresources.core;

import net.minecraft.block.Block;

public class OreKey {
    private Block block;
    private int metadata;
    public OreKey(Block block, int metadata) {
        this.block = block;
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof OreKey)) {
            return false;
        }

        OreKey other = (OreKey) obj;

        return this.block == other.block && this.metadata == other.metadata;
    }

    @Override
    public int hashCode() {
        int result = block.hashCode();
        result = 31 * result + metadata;
        return result;
    }
}
