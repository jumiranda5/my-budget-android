package com.jgm.mybudgetapp.objects;

public class HomeCategory {

    private float total;
    private String category;
    private int colorId;

    public HomeCategory(float total, String category, int colorId) {
        this.category = category;
        this.total = total;
        this.colorId = colorId;
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
}
