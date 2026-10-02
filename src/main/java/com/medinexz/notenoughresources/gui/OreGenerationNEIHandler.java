package com.medinexz.notenoughresources.gui;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.medinexz.notenoughresources.core.OreData;
import com.medinexz.notenoughresources.core.OreGenerationLayout;
import com.medinexz.notenoughresources.core.OreGenerationProfile;
import com.medinexz.notenoughresources.core.OreRegistry;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class OreGenerationNEIHandler extends TemplateRecipeHandler {

    private static final int GUI_WIDTH  = 166;
    private static final int GUI_HEIGHT = 90;

    private static final OreGenerationLayout LAYOUT =
        new OreGenerationLayout();

    private static final OreGenerationRenderer RENDERER =
        new OreGenerationRenderer(LAYOUT);

    private static final OreGenerationBackground BACKGROUND =
        new OreGenerationBackground(
            new ResourceLocation("notenoughresources", "textures/gui/world_gen.png"),
            GUI_WIDTH, GUI_HEIGHT
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
            for (int i = 0; i < drops.size() && i < 8; i++) {
                stacks.add(new PositionedStack(drops.get(i), dropX + i * 18, dropY));
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

    private void loadForItem(ItemStack itemStack) {
        if (itemStack == null) return;

        Block block = Block.getBlockFromItem(itemStack.getItem());
        int metadata = itemStack.getItemDamage();
        OreData oreData = OreRegistry.getOre(block, metadata);

        if (oreData == null) return;

        arecipes.add(new CachedOreGenerationRecipe(oreData));
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

        // Ore name centred at the top of the recipe area
        String oreName = oreData.getName();
        int nameW = GuiDraw.getStringWidth(oreName);
        GL11.glColor4f(1, 1, 1, 1);
        GuiDraw.drawString(oreName, (GUI_WIDTH - nameW) / 2, 1, 0xFF222222, false);

        // Draws label below drops row if any drops present
        if (!oreData.getDrops().isEmpty()) {
            GL11.glPushMatrix();
            GL11.glTranslatef(LAYOUT.getDropsX(), LAYOUT.getDropsY() - 8, 0);
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

    // ── Handler metadata ─────────────────────────────────────────────────────

    @Override
    public String getRecipeName() { return "Ore Generation"; }

    @Override
    public String getGuiTexture() {
        return "notenoughresources:textures/gui/world_gen.png";
    }

    @Override
    public int recipiesPerPage() { return 1; }

    @Override
    public int getRecipeHeight(int recipe) { return GUI_HEIGHT; }
}
