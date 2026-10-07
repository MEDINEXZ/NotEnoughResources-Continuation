package com.medinexz.notenoughresources.integration.thaumcraft.addons;

import com.medinexz.notenoughresources.integration.INERIntegration;

/**
 * Nodal Mechanics (1.7.10).
 *
 * <p>Checked against Nodal Mechanics 1.0-7: no world generation.  The mod adds
 * recipes that craft and attune aura nodes; it generates none.</p>
 */
public class NodalMechanicsIntegration implements INERIntegration {

    private static final String MOD_ID = "NodalMechanics";

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        // No world generation in this addon
    }
}
