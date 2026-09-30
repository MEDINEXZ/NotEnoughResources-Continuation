package com.medinexz.notenoughresources.core;

public class OreGenerationProfile {

    private int minY;
    private int maxY;

    private double[] probability;

    public OreGenerationProfile(int minY, int maxY) {
        this.minY = minY;
        this.maxY = maxY;

        this.probability =
            new double[maxY - minY + 1];
    }

    public double getProbability(int y) {
        return probability[y - minY];
    }

    public void setProbability(int y, double value) {
        probability[y - minY] = value;
    }

    public double getPeakProbability() {

        double peak = 0.0;

        for (double value : probability) {

            if (value > peak) {
                peak = value;
            }
        }

        return peak;
    }

    public int getPeakY() {

        int peakY = minY;
        double peak = probability[0];

        for (int i = 1; i < probability.length; i++) {

            if (probability[i] > peak) {

                peak = probability[i];
                peakY = minY + i;
            }
        }

        return peakY;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }
}
