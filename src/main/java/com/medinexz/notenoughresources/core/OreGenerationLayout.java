package com.medinexz.notenoughresources.core;

public class OreGenerationLayout {

    /*
     * Graph
     */

    private int graphX;
    private int graphY;

    private int graphWidth;
    private int graphHeight;


    /*
     * Ore item
     */

    private int oreItemX;
    private int oreItemY;


    /*
     * Drops
     */

    private int dropsX;
    private int dropsY;


    public OreGenerationLayout() {

        /*
         * Graph
         */

        graphX = 29;
        graphY = 52;

        graphWidth = 128;
        graphHeight = 40;


        /*
         * Ore item
         */

        oreItemX = 5;
        oreItemY = 21;


        /*
         * Drops
         */

        dropsX = 5;
        dropsY = 66;
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
}
