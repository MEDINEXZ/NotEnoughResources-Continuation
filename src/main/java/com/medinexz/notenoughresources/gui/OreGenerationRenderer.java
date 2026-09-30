package com.medinexz.notenoughresources.gui;

import com.medinexz.notenoughresources.core.OreGenerationLayout;
import com.medinexz.notenoughresources.core.OreGenerationProfile;
import net.minecraft.client.renderer.Tessellator;

public class OreGenerationRenderer {

    private OreGenerationLayout layout;

    public OreGenerationRenderer(
        OreGenerationLayout layout
    ) {

        this.layout = layout;
    }

    public void drawGraph(
        OreGenerationProfile profile,
        int guiLeft,
        int guiTop
    ) {

        if (profile == null) {
            return;
        }

        int minY =
            profile.getMinY();

        int maxY =
            profile.getMaxY();

        int count =
            maxY - minY + 1;


        if (count <= 0) {
            return;
        }

        double maxProbability = 0.0;

        for (
            int y = minY;
            y <= maxY;
            y++
        ) {

            double probability =
                profile.getProbability(y);

            if (probability > maxProbability) {

                maxProbability =
                    probability;
            }
        }

        if (maxProbability <= 0.0) {
            return;
        }

        int graphX =
            guiLeft +
                layout.getGraphX();

        int graphY =
            guiTop +
                layout.getGraphY();

        int graphWidth =
            layout.getGraphWidth();

        int graphHeight =
            layout.getGraphHeight();

        if (count == 1) {

            double probability =
                profile.getProbability(minY);

            double normalized =
                probability /
                    maxProbability;

            double y =
                graphY -
                    normalized * graphHeight;

            drawPoint(
                graphX,
                y
            );

            return;
        }


        double space =
            (double) graphWidth /
                (count - 1);


        double previousX =
            graphX;

        double previousY =
            graphY;

        for (
            int i = 0;
            i < count;
            i++
        ) {

            int yLevel =
                minY + i;


            double probability =
                profile.getProbability(yLevel);


            double x =
                graphX +
                    i * space;

            double normalized =
                probability /
                    maxProbability;

            double y =
                graphY -
                    normalized * graphHeight;

            if (i > 0) {

                drawLine(
                    previousX,
                    previousY,
                    x,
                    y
                );
            }


            previousX =
                x;

            previousY =
                y;
        }
    }

    private void drawLine(
        double x1,
        double y1,
        double x2,
        double y2
    ) {

        Tessellator tessellator =
            Tessellator.instance;
        tessellator.startDrawing(1);
        tessellator.addVertex(
            x1,
            y1,
            0
        );
        tessellator.addVertex(
            x2,
            y2,
            0
        );
        tessellator.draw();
    }

    private void drawPoint(
        double x,
        double y
    ) {

        Tessellator tessellator =
            Tessellator.instance;


        tessellator.startDrawing(1);


        tessellator.addVertex(
            x - 1,
            y,
            0
        );


        tessellator.addVertex(
            x + 1,
            y,
            0
        );
        tessellator.draw();
    }
}
