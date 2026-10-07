package com.medinexz.notenoughresources.integration.thaumcraft.addons;

import com.medinexz.notenoughresources.integration.INERIntegration;

/**
 * Forbidden Magic (1.7.10).
 *
 * <p>Checked against Forbidden Magic 0.575: no world generation.  Its tainted
 * trees only grow from the sapling, and its flowers and stones are crafted or
 * grown; nothing is placed when a chunk generates.</p>
 */
public class ForbiddenMagicIntegration implements INERIntegration {

    private static final String MOD_ID = "ForbiddenMagic";

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        // No world generation in this addon
    }
}
