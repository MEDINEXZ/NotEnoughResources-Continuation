package com.medinexz.notenoughresources.core;

public class OreGenerationLayout {

    /*
     * Entry (one world-generation recipe; NEI stacks as many as fit on a page)
     */

    private int entryWidth;
    private int entryHeight;
    private int entrySpacing;


    /*
     * Graph
     */

    private int graphX;
    private int graphY;

    private int graphWidth;
    private int graphHeight;


    /*
     * Ore slot (the 18x18 slot sprite baked into world_gen.png)
     */

    private static final int ITEM_SIZE = 16;

    private int oreSlotX;
    private int oreSlotY;

    private int oreSlotWidth;
    private int oreSlotHeight;


    /*
     * Ore item (centred inside the ore slot)
     */

    private int oreItemX;
    private int oreItemY;


    /*
     * Drops (first slot of the 156x18 bar sprite baked into world_gen.png)
     */

    private int dropsBarX;
    private int dropsBarY;
    private int dropsBarHeight;

    private int dropsX;
    private int dropsY;

    private int dropSpacing;
    private int maxDrops;


    /*
     * Best Y label (two lines: "Best:" / "Y=N")
     */

    private int bestLabelX;
    private int bestLabelY;

    private int bestLineHeight;


    /*
     * Title (dimension name) and labels
     */

    private int titleY;
    private int titleHeight;

    private int axisLabelHeight;

    private int dropsBarWidth;

    private int dropsLabelX;
    private int dropsLabelY;


    public OreGenerationLayout() {

        /*
         * Vertical structure of an entry, top to bottom:
         *   title line, graph (ore slot and Best label beside it),
         *   Y-level labels, drops bar
         */

        titleY = 1;
        titleHeight = 10;

        graphHeight = 40;

        axisLabelHeight = 7;

        dropsBarHeight = 18;

        int graphTop = titleY + titleHeight;


        /*
         * Graph (graphY is the baseline)
         */

        graphX = 29;
        graphY = graphTop + graphHeight;

        graphWidth = 128;


        /*
         * Ore slot (18x18 slot sprite of world_gen.png), level with the graph top
         */

        oreSlotX = 0;
        oreSlotY = graphTop;

        oreSlotWidth = 18;
        oreSlotHeight = 18;


        /*
         * Ore item (centred inside the ore slot)
         */

        oreItemX = oreSlotX + (oreSlotWidth - ITEM_SIZE) / 2;
        oreItemY = oreSlotY + (oreSlotHeight - ITEM_SIZE) / 2;


        /*
         * Best Y label: directly under the ore slot, left of the graph
         */

        bestLabelX = oreItemX;
        bestLabelY = oreSlotY + oreSlotHeight + 2;

        bestLineHeight = 5;


        /*
         * Drops (156x18 bar sprite of world_gen.png), under the Y-level labels
         */

        dropsBarX = 0;
        dropsBarY = graphY + axisLabelHeight;
        dropsBarWidth = 156;

        dropsX = dropsBarX + (dropsBarHeight - ITEM_SIZE) / 2;
        dropsY = dropsBarY + (dropsBarHeight - ITEM_SIZE) / 2;

        dropSpacing = 18;
        maxDrops = 8;

        dropsLabelX = dropsX;
        dropsLabelY = dropsBarY - bestLineHeight;


        /*
         * Entry: ends with the drops bar
         */

        entryWidth = 166;
        entryHeight = dropsBarY + dropsBarHeight;
        entrySpacing = 4;
    }


    /*
     * Entry
     */

    public int getEntryWidth() {
        return entryWidth;
    }

    public int getEntryHeight() {
        return entryHeight;
    }

    public int getEntrySpacing() {
        return entrySpacing;
    }

    // Vertical distance between the tops of two consecutive entries
    public int getEntryStep() {
        return entryHeight + entrySpacing;
    }

    // How many whole entries fit into the given height
    public int getEntriesPerPage(int availableHeight) {
        return Math.max(1, availableHeight / getEntryStep());
    }


    /*
     * Graph
     */

    public int getGraphX() {
        return graphX;
    }

    public int getGraphY() {
        return graphY;
    }

    public int getGraphWidth() {
        return graphWidth;
    }

    public int getGraphHeight() {
        return graphHeight;
    }


    /*
     * Ore slot
     */

    public int getOreSlotX() {
        return oreSlotX;
    }

    public int getOreSlotY() {
        return oreSlotY;
    }

    public int getOreSlotWidth() {
        return oreSlotWidth;
    }

    public int getOreSlotHeight() {
        return oreSlotHeight;
    }


    /*
     * Ore item
     */

    public int getOreItemX() {
        return oreItemX;
    }

    public int getOreItemY() {
        return oreItemY;
    }


    /*
     * Drops
     */

    public int getDropsX() {
        return dropsX;
    }

    public int getDropsY() {
        return dropsY;
    }

    public int getDropsBarX() {
        return dropsBarX;
    }

    public int getDropsBarY() {
        return dropsBarY;
    }

    public int getDropsBarWidth() {
        return dropsBarWidth;
    }

    public int getDropsBarHeight() {
        return dropsBarHeight;
    }

    public int getDropsLabelX() {
        return dropsLabelX;
    }

    public int getDropsLabelY() {
        return dropsLabelY;
    }

    public int getTitleY() {
        return titleY;
    }

    public int getDropSpacing() {
        return dropSpacing;
    }

    public int getMaxDrops() {
        return maxDrops;
    }


    /*
     * Best Y label
     */

    public int getBestLabelX() {
        return bestLabelX;
    }

    public int getBestLabelY() {
        return bestLabelY;
    }

    public int getBestLineHeight() {
        return bestLineHeight;
    }
}
