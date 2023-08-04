package com.jgm.mybudgetapp.objects;

public class CategoryResponse {

    private int id;
    private float total;
    private String category;
    private int colorId;
    private int iconId;

    public CategoryResponse(int id, float total, String category, int colorId, int iconId) {
        this.id = id;
        this.category = category;
        this.total = total;
        this.colorId = colorId;
        this.iconId = iconId;
    }

    public int getId() {
        return id;
    }

    public float getTotal() {
        return total;
    }

    public int getColorId() {
        return colorId;
    }

    public String getCategory() {
        return category;
    }

    public int getIconId() {
        return iconId;
    }
}
