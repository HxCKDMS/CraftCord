package com.karelmikie3.craftcord.util;

import net.minecraft.util.text.TextFormatting;

import java.awt.*;

import static java.lang.Math.*;

public final class ColorHelper {

    public static TextFormatting getNearestColor(Color color) {
        TextFormatting best = TextFormatting.WHITE;
        double bestDelta = Double.MAX_VALUE;

        for (TextFormatting format : TextFormatting.values()) {
            if (!format.isColor())
                continue;

            Color color2 = new Color(format.getColor());

            int rmean = ( color.getRed() + color2.getRed() ) / 2;
            int deltaRed = color.getRed() - color2.getRed();
            int deltaGreen = color.getGreen() - color2.getGreen();
            int deltaBlue = color.getBlue() - color2.getBlue();


            double delta = sqrt((((512+rmean)*deltaRed*deltaRed)>>8) + 4*deltaGreen*deltaGreen + (((767-rmean)*deltaBlue*deltaBlue)>>8));
            if (Math.min(delta, bestDelta) == delta) {
                best = format;
                bestDelta = delta;
            }
        }

        return best;
    }
}
