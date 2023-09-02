package com.jgm.mybudgetapp.objects;

public class CategoryPercent {

    private int id;
    private String name;
    private int colorId;
    private int iconId;
    private float total;
    private float percent;

    public CategoryPercent(int id, String name, int colorId, int iconId, float total) {
        this.id = id;
        this.name = name;
        this.colorId = colorId;
        this.iconId = iconId;
        this.total = total;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getColorId() {
        return colorId;
    }

    public int getIconId() {
        return iconId;
    }

    public float getTotal() {
        return total;
    }

    public float getPercent() {
        return percent;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColorId(int colorId) {
        this.colorId = colorId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }

}
