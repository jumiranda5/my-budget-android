package com.jgm.mybudgetapp.objects;

import java.util.Comparator;

public class Category {

    private int id;
    private String name;
    private int colorId;
    private int iconId;
    private boolean isActive;
    private float total;
    private float percent;

    public Category(int id, String name, int colorId, int iconId, boolean isActive) {
        this.id = id;
        this.name = name;
        this.colorId = colorId;
        this.iconId = iconId;
        this.isActive = isActive;
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

    public boolean isActive() {
        return isActive;
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

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }


    // Comparator (Name)
    public static Comparator<Category> CategoryNameComparator = (c1, c2) -> {

        String CategoryName1 = c1.getName().toUpperCase();
        String CategoryName2 = c2.getName().toUpperCase();

        // Returning in ascending order
        return CategoryName1.compareTo(CategoryName2);

        // descending order
        // return CategoryName2.compareTo(CategoryName1);
    };

    // Comparator (Total)
    public static Comparator<Category> CategoryTotalComparator = new Comparator<Category>() {

        // Method
        public int compare(Category c1, Category c2) {

            float total1 = c1.getTotal();
            float total2 = c2.getTotal();

            // For descending order
            return (int) (total2 - total1);
        }
    };
}
