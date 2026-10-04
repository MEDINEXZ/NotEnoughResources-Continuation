package com.medinexz.notenoughresources.core;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.Collections;
import java.util.List;

/**
 * Stub IChunkProvider used as the "caller" argument to ChunkProviderGenerate.populate().
 * We never actually generate or load chunks; we just satisfy the interface contract.
 */
public class ProfilerChunkProvider implements IChunkProvider {

    @Override
    public boolean chunkExists(int cx, int cz) {
        return true;
    }

    @Override
    public Chunk provideChunk(int cx, int cz) {
        return null;
    }

    @Override
    public Chunk loadChunk(int cx, int cz) {
        return null;
    }

    @Override
    public void populate(IChunkProvider provider, int cx, int cz) {
        // no-op: we call populate on the real generator directly
    }

    @Override
    public boolean saveChunks(boolean all, IProgressUpdate progress) {
        return true;
    }

    @Override
    public boolean unloadQueuedChunks() {
        return false;
    }

    @Override
    public boolean canSave() {
        return false;
    }

    @Override
    public String makeString() {
        return "NER_ProfilerChunkProvider";
    }

    @Override
    public List<BiomeGenBase.SpawnListEntry> getPossibleCreatures(
        EnumCreatureType type, int x, int y, int z
    ) {
        return Collections.emptyList();
    }

    @Override
    public ChunkPosition func_147416_a(World world, String s, int x, int y, int z) {
        return null;
    }

    @Override
    public int getLoadedChunkCount() {
        return 0;
    }

    @Override
    public void recreateStructures(int cx, int cz) {
        // no-op
    }

    @Override
    public void saveExtraData() {
        // no-op
    }
}
