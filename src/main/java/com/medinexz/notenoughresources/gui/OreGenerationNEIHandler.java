package com.medinexz.notenoughresources.gui;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.medinexz.notenoughresources.core.OreData;
import com.medinexz.notenoughresources.core.OreRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class OreGenerationNEIHandler
    extends TemplateRecipeHandler {


    public class CachedOreGenerationRecipe
        extends CachedRecipe {

        private final OreData oreData;


        public CachedOreGenerationRecipe(
            OreData oreData
        ) {

            this.oreData = oreData;
        }


        public OreData getOreData() {

            return oreData;
        }


        @Override
        public PositionedStack getResult() {

            return new PositionedStack(
                new ItemStack(
                    oreData.getBlock(),
                    1,
                    oreData.getMetadata()
                ),
                74,
                20
            );
        }
    }


    @Override
    public void loadUsageRecipes(
        ItemStack itemStack
    ) {

        System.out.println(
            "=== NER USAGE HANDLER CALLED ==="
        );

        if (itemStack == null) {

            System.out.println(
                "ItemStack is NULL"
            );

            return;
        }

        System.out.println(
            "ItemStack = " + itemStack
        );

        Block block =
            Block.getBlockFromItem(
                itemStack.getItem()
            );

        int metadata =
            itemStack.getItemDamage();

        System.out.println(
            "Block = " + block
        );

        System.out.println(
            "Metadata = " + metadata
        );

        OreData oreData =
            OreRegistry.getOre(
                block,
                metadata
            );

        if (oreData == null) {

            System.out.println(
                "OreData NOT FOUND"
            );

            return;
        }

        System.out.println(
            "OreData FOUND = " +
                oreData.getName()
        );

        arecipes.add(
            new CachedOreGenerationRecipe(
                oreData
            )
        );

        System.out.println(
            "Recipe ADDED"
        );
    }


    @Override
    public String getRecipeName() {

        return "Ore Generation";
    }


    @Override
    public String getGuiTexture() {

        return "notenoughresources:textures/gui/ore_generation.png";
    }


    @Override
    public int recipiesPerPage() {

        return 1;
    }
}
