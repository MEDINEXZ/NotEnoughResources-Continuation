package com.medinexz.notenoughresources.integration.thaumcraft.addons;

import com.medinexz.notenoughresources.NotEnoughResources;
import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;

/**
 * Thaumic Tinkerer (1.7.10) world generation resources.
 *
 * <p>Checked against Thaumic Tinkerer 2.5-538: its only world generation is the
 * Bedrock dimension of KAMI, solid bedrock with ore clusters.  Which ores those
 * are is decided at runtime: the mod has a list of ore dictionary names and
 * generates the first block registered under each name that is present, so
 * the list is read from the mod and resolved the same way here.  Nothing is
 * registered when KAMI or the dimension is disabled in its config.</p>
 */
public class ThaumicTinkererIntegration implements INERIntegration {

    private static final String MOD_ID = "ThaumicTinkerer";

    private static final String PROVIDER = "thaumic.tinkerer.common.dim.WorldProviderBedrock";

    // The enum of the ores of the Bedrock dimension and its ore dictionary name field
    private static final String ORES       = "thaumic.tinkerer.common.dim.EnumOreFrequency";
    private static final String VALID_ORES = "getValidOres";
    private static final String ORE_NAME   = "name";

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        Integer dimension = IntegrationHelper.findDimension(PROVIDER);
        if (dimension == null) return;

        try {
            Class<?> ores = Class.forName(ORES);
            for (Object ore : (List<?>) ores.getMethod(VALID_ORES).invoke(null)) {
                register((String) ores.getField(ORE_NAME).get(ore), dimension);
            }
        } catch (Exception e) {
            NotEnoughResources.LOG.warn("Cannot read the Bedrock dimension ores of Thaumic Tinkerer", e);
        }
    }

    private static void register(String oreName, Integer dimension) {
        List<ItemStack> stacks = OreDictionary.getOres(oreName);
        if (stacks.isEmpty()) return;

        ItemStack stack = stacks.get(0);
        int metadata = stack.getItemDamage() == OreDictionary.WILDCARD_VALUE ? 0 : stack.getItemDamage();
        IntegrationHelper.register(
            Block.getBlockFromItem(stack.getItem()), metadata,
            IntegrationHelper.MIN_Y, IntegrationHelper.MAX_Y, dimension);
    }
}
