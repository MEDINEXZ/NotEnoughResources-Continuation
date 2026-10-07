package com.medinexz.notenoughresources.integration.arsmagica2;

import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;

/**
 * Ars Magica 2 (1.7.10) world generation resources.
 *
 * <p>Checked against Ars Magica 2 1.4.0.009.  Its IWorldGenerator places, in every
 * dimension that is not the End or blacklisted in its config: vinteum,
 * chimerite and blue topaz ore in stone, sunstone ore in lava (the only thing
 * it generates in the Nether), and the flowers cerublossom, desert nova,
 * tarma root, wakebloom and aum.  Moonstone ore is not generated; it falls
 * with meteors.</p>
 *
 * <p>Not registered: witchwood trees and the liquid essence pools and lakes.</p>
 */
public class ArsMagica2Integration implements INERIntegration {

    private static final String MOD_ID = "arsmagica2";

    private static final int OVERWORLD = 0;
    private static final int NETHER    = -1;

    // The ore block (registered as "vinteumOre") variants
    private static final String ORES = "vinteumOre";
    private static final int VINTEUM    = 0;
    private static final int CHIMERITE  = 1;
    private static final int BLUE_TOPAZ = 2;
    private static final int SUNSTONE   = 4;

    private static final String[] FLOWERS = { "blueOrchid", "desertNova", "TarmaRoot", "wakebloom", "Aum" };

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        IntegrationHelper.register(MOD_ID, ORES, VINTEUM, OVERWORLD);
        IntegrationHelper.register(MOD_ID, ORES, CHIMERITE, OVERWORLD);
        IntegrationHelper.register(MOD_ID, ORES, BLUE_TOPAZ, OVERWORLD);
        IntegrationHelper.register(MOD_ID, ORES, SUNSTONE, OVERWORLD);
        IntegrationHelper.register(MOD_ID, ORES, SUNSTONE, NETHER);

        for (String flower : FLOWERS) {
            IntegrationHelper.register(MOD_ID, flower, 0, OVERWORLD);
        }
    }
}
