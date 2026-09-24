package com.medinexz.notenoughresources.core;

public class OreGenerationProfile {
    private int minY;
    private int maxY;

    private double[] probability;

    public OreGenerationProfile(int minY, int maxY) {
        this.minY = minY;
        this.maxY = maxY;
        this.probability = new double[maxY - minY + 1];
    }

    public double getProbability(int y) {
        return probability[y - minY];
    }

    public void setProbability(int y, double value) {
        probability[y - minY] = value;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }
}
