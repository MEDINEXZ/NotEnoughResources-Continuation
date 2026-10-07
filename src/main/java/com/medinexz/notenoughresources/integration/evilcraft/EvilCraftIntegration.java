package com.medinexz.notenoughresources.integration.evilcraft;

import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;

/**
 * EvilCraft (1.7.10) world generation resources.
 *
 * <p>Checked against EvilCraft 0.9.13: its ore generator places dark ore in the
 * stone of the overworld (vein size, count and height come from its config).
 * The same generator hides netherfish in Nether blocks and can add extra
 * silverfish stone; neither is a resource.  Undead trees only grow from the
 * sapling.</p>
 *
 * <p>Not registered: the blocks of the dark temple and of the evil dungeons.</p>
 */
public class EvilCraftIntegration implements INERIntegration {

    private static final String MOD_ID = "evilcraft";

    private static final int OVERWORLD = 0;

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        IntegrationHelper.register(MOD_ID, "darkOre", 0, OVERWORLD);
    }
}
