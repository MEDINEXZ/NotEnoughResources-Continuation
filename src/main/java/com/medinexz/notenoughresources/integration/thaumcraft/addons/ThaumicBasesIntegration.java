package com.medinexz.notenoughresources.integration.thaumcraft.addons;

import com.medinexz.notenoughresources.integration.INERIntegration;

/**
 * Thaumic Bases (1.7.10).
 *
 * <p>Checked against Thaumic Bases 1.3.1710.4 (requires Baubles and DummyCore): no
 * world generation.  Its trees only grow from saplings and its plants are
 * crops; nothing is placed when a chunk generates.</p>
 */
public class ThaumicBasesIntegration implements INERIntegration {

    private static final String MOD_ID = "thaumicbases";

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        // No world generation in this addon
    }
}
