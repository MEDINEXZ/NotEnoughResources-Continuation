package com.medinexz.notenoughresources.integration;

/**
 * Mod-specific knowledge for an optional mod: which of its blocks are world
 * generation resources and where they generate.  Everything else (profiling,
 * drops, graph, tooltip, NEI) stays in the generic NER classes.
 */
public interface INERIntegration {

    /** The Forge mod id this integration belongs to. */
    String getModId();

    /** Registers the mod's world generation resources in OreRegistry. */
    void registerResources();
}
