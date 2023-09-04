package com.jgm.mybudgetapp.utils;

import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.objects.Color;

import java.util.ArrayList;

public class ColorUtils {

    private static final Color red = new Color(0, R.color.red, "Red");
    private static final Color pink = new Color(1, R.color.pink, "Pink");
    private static final Color purple = new Color(2, R.color.purple, "Purple");
    private static final Color deep_purple = new Color(3, R.color.deep_purple, "Deep purple");
    private static final Color indigo = new Color(4, R.color.indigo, "Indigo");
    private static final Color blue = new Color(5, R.color.blue, "Blue");
    private static final Color cyan = new Color(6, R.color.cyan, "Cyan");
    private static final Color teal = new Color(7, R.color.teal, "Teal");
    private static final Color green = new Color(8, R.color.green, "Green");
    private static final Color light_green = new Color(9, R.color.light_green, "Light Green");
    private static final Color lime = new Color(10, R.color.lime, "Lime");
    private static final Color yellow = new Color(11, R.color.yellow, "Yellow");
    private static final Color amber = new Color(12, R.color.amber, "Amber");
    private static final Color orange = new Color(13, R.color.orange, "Orange");
    private static final Color deep_orange = new Color(14, R.color.deep_orange, "Deep orange");
    private static final Color brown = new Color(15, R.color.brown, "Brown");
    private static final Color grey = new Color(16, R.color.grey, "Grey");
    private static final Color black_white = new Color(17, R.color.black, "Black/White");

    // App colors
    private static final Color expenses = new Color(18, R.color.expense, "Red");
    private static final Color income = new Color(19, R.color.income, "Green");
    private static final Color savings = new Color(20, R.color.savings, "Purple");
    private static final Color cash = new Color(21, R.color.cash, "Green");
    private static final Color light_grey = new Color(22, R.color.bg_progress_track, "Light Grey");
    private static final Color accumulated = new Color(23, R.color.accumulated, "Gold");


    public static ArrayList<Color> getColorList() {
        ArrayList<Color> colors = new ArrayList<>();

        colors.add(red);
        colors.add(pink);
        colors.add(purple);
        colors.add(deep_purple);
        colors.add(indigo);
        colors.add(blue);

        colors.add(yellow);
        colors.add(lime);
        colors.add(light_green);
        colors.add(green);
        colors.add(teal);
        colors.add(cyan);

        colors.add(amber);
        colors.add(orange);
        colors.add(deep_orange);
        colors.add(brown);
        colors.add(grey);
        colors.add(black_white);

        return colors;
    }

    public static Color getColor(int id) {
        Color color;

        switch (id) {
            case 0: color = red; break;
            case 1: color = pink; break;
            case 2: color = purple; break;
            case 3: color = deep_purple; break;
            case 4: color = indigo; break;
            case 5: color = blue; break;
            case 6: color = cyan; break;
            case 7: color = teal; break;
            case 8: color = green; break;
            case 9: color = light_green; break;
            case 10: color = lime; break;
            case 11: color = yellow; break;
            case 12: color = amber; break;
            case 13: color = orange; break;
            case 14: color = deep_orange; break;
            case 15: color = brown; break;
            case 16: color = grey; break;
            case 18: color = expenses; break;
            case 19: color = income; break;
            case 20: color = savings; break;
            case 21: color = cash; break;
            case 22: color = light_grey; break;
            case 23: color = accumulated; break;
            default: color = black_white;
        }

        return color;
    }

    public static int getColorNameResource(int id) {
        int color;

        switch (id) {
            case 0: color = R.string.desc_color_red; break;
            case 1: color = R.string.desc_color_pink; break;
            case 2: color = R.string.desc_color_purple; break;
            case 3: color = R.string.desc_color_deep_purple; break;
            case 4: color = R.string.desc_color_indigo; break;
            case 5: color = R.string.desc_color_blue; break;
            case 6: color = R.string.desc_color_cyan; break;
            case 7: color = R.string.desc_color_teal; break;
            case 8: color = R.string.desc_color_green; break;
            case 9: color = R.string.desc_color_light_green; break;
            case 10: color = R.string.desc_color_lime; break;
            case 11: color = R.string.desc_color_yellow; break;
            case 12: color = R.string.desc_color_amber; break;
            case 13: color = R.string.desc_color_orange; break;
            case 14: color = R.string.desc_color_deep_orange; break;
            case 15: color = R.string.desc_color_brown; break;
            case 16: color = R.string.desc_color_grey; break;
            case 18: color = R.string.desc_color_red; break;
            case 19: color = R.string.desc_color_green; break;
            case 20: color = R.string.desc_color_purple; break;
            case 21: color = R.string.desc_color_green; break;
            case 22: color = R.string.desc_color_grey; break;
            case 23: color = R.string.desc_color_grey; break;
            default: color = R.string.desc_color_black_white;
        }

        return color;
    }
}
