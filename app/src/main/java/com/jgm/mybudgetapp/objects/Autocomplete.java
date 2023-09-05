package com.jgm.mybudgetapp.objects;

public class Autocomplete {

    private String description;
    private String categoryName;
    private int iconId;
    private int colorId;
    private int categoryId;

    public Autocomplete(String description, int categoryId, String categoryName, int iconId, int colorId) {
        this.description = description;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.iconId = iconId;
        this.colorId = colorId;
    }

    public String getDescription() {
        return description;
    }

    public int getIconId() {
        return iconId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public int getColorId() {
        return colorId;
    }

    public int getCategoryId() {
        return categoryId;
    }
}
