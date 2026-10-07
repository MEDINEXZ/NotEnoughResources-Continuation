package com.medinexz.notenoughresources.integration.thaumcraft.addons;

import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;

/**
 * Thaumic Warden (1.7.10) world generation resources.
 *
 * <p>Checked against Thaumic Warden 1.1.1: its IWorldGenerator plants the
 * Excubitura rose, the source of the mod's petals, on grass and dirt in about
 * one chunk in a hundred of every dimension.  Only the overworld is
 * registered: that is where the surface it needs is.</p>
 */
public class ThaumicWardenIntegration implements INERIntegration {

    private static final String MOD_ID = "TWarden";

    private static final int OVERWORLD = 0;

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        IntegrationHelper.register(MOD_ID, "blockExubitura", 0, OVERWORLD);
    }
}
