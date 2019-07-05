package com.karelmikie3.craftcord.util;

import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.*;

public final class ColorHelper {

    private static final Map<TextFormatting, Integer> cToI = new HashMap<TextFormatting, Integer>() {{
        put(TextFormatting.BLACK, 0);
        put(TextFormatting.DARK_BLUE, 170);
        put(TextFormatting.DARK_GREEN, 43520);
        put(TextFormatting.DARK_AQUA, 43690);
        put(TextFormatting.DARK_RED, 11141120);
        put(TextFormatting.DARK_PURPLE, 11141290);
        put(TextFormatting.GOLD, 16755200);
        put(TextFormatting.GRAY, 11184810);
        put(TextFormatting.DARK_GRAY, 5592405);
        put(TextFormatting.BLUE, 5592575);
        put(TextFormatting.GREEN, 5635925);
        put(TextFormatting.AQUA, 5636095);
        put(TextFormatting.RED, 16733525);
        put(TextFormatting.LIGHT_PURPLE, 16733695);
        put(TextFormatting.YELLOW, 16777045);
        put(TextFormatting.WHITE, 16777215);
    }};


    public static TextFormatting getNearestColor(Color color) {
        TextFormatting best = TextFormatting.WHITE;
        double bestDelta = Double.MAX_VALUE;

        for (TextFormatting format : TextFormatting.values()) {
            if (!format.isColor())
                continue;

            Color color2 = new Color(cToI.get(format));

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
