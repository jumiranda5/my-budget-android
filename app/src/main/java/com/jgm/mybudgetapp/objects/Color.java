package com.jgm.mybudgetapp.objects;

public class Color {

    private final int id;
    private final int color;
    private final String colorName;

    public Color(int id, int color, String colorName) {
        this.id = id;
        this.color = color;
        this.colorName = colorName;
    }

    public int getId() {
        return id;
    }

    public int getColor() {
        return color;
    }

    public String getColorName() {
        return colorName;
    }
}
