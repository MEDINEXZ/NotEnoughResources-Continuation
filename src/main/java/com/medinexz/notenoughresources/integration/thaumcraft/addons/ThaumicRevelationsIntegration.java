package com.medinexz.notenoughresources.integration.thaumcraft.addons;

import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;

/**
 * Thaumic Revelations (1.7.10) world generation resources.
 *
 * <p>Checked against Thaumic Revelations 1.2.2: its IWorldGenerator plants the
 * Excubitura rose in the overworld, on grass and dirt in one chunk in ten.
 * The rose is a crop here; it is generated fully grown, which is the only
 * stage that drops petals and seeds.</p>
 */
public class ThaumicRevelationsIntegration implements INERIntegration {

    private static final String MOD_ID = "thaumrev";

    private static final int OVERWORLD = 0;

    // blockExcubitura growth stage
    private static final int FULLY_GROWN = 15;

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        IntegrationHelper.register(MOD_ID, "blockExcubitura", FULLY_GROWN, OVERWORLD);
    }
}
