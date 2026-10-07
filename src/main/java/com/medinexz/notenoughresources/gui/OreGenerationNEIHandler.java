package com.medinexz.notenoughresources.gui;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.medinexz.notenoughresources.core.DropStatistics;
import com.medinexz.notenoughresources.core.OreData;
import com.medinexz.notenoughresources.core.OreGenerationLayout;
import com.medinexz.notenoughresources.core.OreGenerationProfile;
import com.medinexz.notenoughresources.core.OreRegistry;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class OreGenerationNEIHandler extends TemplateRecipeHandler {

    public static final String HANDLER_ID = "notenoughresources_ore_generation";

    public static final String RECIPE_NAME = "World Generation";

    private static final OreGenerationLayout LAYOUT =
        new OreGenerationLayout();

    // Size of one entry; NEI fits as many entries on a page as the recipe GUI height allows
    public static final int GUI_WIDTH  = LAYOUT.getEntryWidth();
    public static final int GUI_HEIGHT = LAYOUT.getEntryHeight();
    public static final int ENTRY_STEP = LAYOUT.getEntryStep();

    private static final OreGenerationRenderer RENDERER =
        new OreGenerationRenderer(LAYOUT);

    private static final OreGenerationBackground BACKGROUND =
        new OreGenerationBackground(
            new ResourceLocation("notenoughresources", "textures/gui/world_gen.png"),
            LAYOUT
        );

    // ── CachedRecipe ─────────────────────────────────────────────────────────

    public class CachedOreGenerationRecipe extends CachedRecipe {

        private final OreData oreData;

        public CachedOreGenerationRecipe(OreData oreData) {
            this.oreData = oreData;
        }

        public OreData getOreData() { return oreData; }

        @Override
        public PositionedStack getResult() {
            return new PositionedStack(
                new ItemStack(oreData.getBlock(), 1, oreData.getMetadata()),
                LAYOUT.getOreItemX(),
                LAYOUT.getOreItemY()
            );
        }

        @Override
        public List<PositionedStack> getOtherStacks() {
            List<PositionedStack> stacks = new ArrayList<PositionedStack>();
            List<ItemStack> drops = oreData.getDrops();
            int dropX = LAYOUT.getDropsX();
            int dropY = LAYOUT.getDropsY();
            for (int i = 0; i < drops.size() && i < LAYOUT.getMaxDrops(); i++) {
                stacks.add(new PositionedStack(drops.get(i), dropX + i * LAYOUT.getDropSpacing(), dropY));
            }
            return stacks;
        }
    }

    // ── IUsageHandler (U key) ────────────────────────────────────────────────

    @Override
    public void loadUsageRecipes(ItemStack itemStack) {
        loadForItem(itemStack);
    }

    // ── IRecipeHandler (R key) ───────────────────────────────────────────────

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        loadForItem(result);
    }

    // ── Show All Recipes ─────────────────────────────────────────────────────

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if ("all".equals(outputId) || HANDLER_ID.equals(outputId)) {
            for (OreData oreData : OreRegistry.getOres()) {
                if (oreData.hasGenerationData()) arecipes.add(new CachedOreGenerationRecipe(oreData));
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    private void loadForItem(ItemStack itemStack) {
        if (itemStack == null) return;

        Block block = Block.getBlockFromItem(itemStack.getItem());
        int metadata = itemStack.getItemDamage();

        // The item of a block without variants stands for the block in any state
        // (a crop is generated at some growth stage, its item is always damage 0)
        boolean anyState = !itemStack.getItem().getHasSubtypes();

        // A filled bucket (or cell, can, ...) stands for the fluid it holds
        FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(itemStack);
        Block fluidBlock = fluid == null || fluid.getFluid() == null ? null : fluid.getFluid().getBlock();

        // One entry per dimension the block generates in
        for (OreData oreData : OreRegistry.getOres()) {
            boolean sameBlock = oreData.getBlock() == block && (anyState || oreData.getMetadata() == metadata);
            boolean sameFluid = fluidBlock != null && oreData.getBlock() == fluidBlock;
            if ((sameBlock || sameFluid) && oreData.hasGenerationData()) {
                arecipes.add(new CachedOreGenerationRecipe(oreData));
            }
        }
    }

    // ── Rendering ────────────────────────────────────────────────────────────

    @Override
    public void drawBackground(int recipe) {
        BACKGROUND.draw(0, 0);
    }

    @Override
    public void drawExtras(int recipe) {
        if (recipe >= arecipes.size()) return;

        CachedOreGenerationRecipe cached =
            (CachedOreGenerationRecipe) arecipes.get(recipe);
        if (cached == null) return;

        OreData oreData = cached.getOreData();
        OreGenerationProfile profile = oreData.getGenerationProfile();

        // Dimension name centred at the top of the entry
        String dimensionName = oreData.getDimensionName();
        int nameW = GuiDraw.getStringWidth(dimensionName);
        GL11.glColor4f(1, 1, 1, 1);
        GuiDraw.drawString(dimensionName, (GUI_WIDTH - nameW) / 2, LAYOUT.getTitleY(), 0xFF222222, false);

        // Draws label below drops row if any drops present
        if (!oreData.getDrops().isEmpty()) {
            GL11.glPushMatrix();
            GL11.glTranslatef(LAYOUT.getDropsLabelX(), LAYOUT.getDropsLabelY(), 0);
            GL11.glScalef(0.5f, 0.5f, 1.0f);
            GuiDraw.drawString("Drops:", 0, 0, 0xFF333333, false);
            GL11.glPopMatrix();
        }

        // Graph (axes + line + labels)
        RENDERER.drawGraph(profile, 0, 0);
    }

    // ── Tooltip ───────────────────────────────────────────────────────────────

    @Override
    public List<String> handleTooltip(GuiRecipe<?> gui, List<String> currenttip, int recipe) {
        List<String> result = super.handleTooltip(gui, currenttip, recipe);

        if (recipe >= arecipes.size() || !result.isEmpty()) return result;

        try {
            int guiLeft = (Integer) ObfuscationReflectionHelper.getPrivateValue(
                GuiContainer.class, (GuiContainer) gui,
                "guiLeft", "field_147003_i");
            int guiTop = (Integer) ObfuscationReflectionHelper.getPrivateValue(
                GuiContainer.class, (GuiContainer) gui,
                "guiTop", "field_147009_r");

            Point mousePos  = GuiDraw.getMousePosition();
            Point recipePos = gui.getRecipePosition(recipe);

            int localX = mousePos.x - guiLeft - recipePos.x;
            int localY = mousePos.y - guiTop  - recipePos.y;

            CachedOreGenerationRecipe cached =
                (CachedOreGenerationRecipe) arecipes.get(recipe);
            OreGenerationProfile profile = cached.getOreData().getGenerationProfile();

            List<String> graphTip = RENDERER.getTooltip(localX, localY, profile);
            if (graphTip != null) {
                result.addAll(graphTip);
            }
        } catch (Exception e) {
            // Silently skip tooltip on reflection failure
        }

        return result;
    }

    @Override
    public List<String> handleItemTooltip(GuiRecipe<?> gui, ItemStack stack, List<String> currenttip, int recipe) {
        List<String> result = super.handleItemTooltip(gui, stack, currenttip, recipe);

        if (stack == null || recipe >= arecipes.size()) return result;

        OreData oreData = ((CachedOreGenerationRecipe) arecipes.get(recipe)).getOreData();

        if (Block.getBlockFromItem(stack.getItem()) == oreData.getBlock()
            && stack.getItemDamage() == oreData.getMetadata()) {
            RENDERER.addOreTooltip(result, oreData);
        } else {
            DropStatistics drop = oreData.getDropStatistics(stack);
            if (drop != null) RENDERER.addDropTooltip(result, drop, NEIClientUtils.shiftKey());
        }

        return result;
    }

    // ── Handler metadata ─────────────────────────────────────────────────────

    @Override
    public String getRecipeName() { return RECIPE_NAME; }

    @Override
    public String getOverlayIdentifier() { return HANDLER_ID; }

    @Override
    public String getGuiTexture() {
        return "notenoughresources:textures/gui/world_gen.png";
    }

    // No recipiesPerPage() override: NEI's RecipePageManager fills each page with as
    // many entries of this height as fit into the recipe GUI (see NEIHandlerInfoRegistrar)
    @Override
    public int getRecipeHeight(int recipe) { return ENTRY_STEP; }
}
