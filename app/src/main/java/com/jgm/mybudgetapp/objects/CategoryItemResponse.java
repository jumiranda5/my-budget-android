package com.jgm.mybudgetapp.objects;

public class CategoryItemResponse {

    private final String name;
    private final float total;
    private final int count;

    public CategoryItemResponse(String name, float total, int count) {
        this.name = name;
        this.total = total;
        this.count = count;
    }

    public float getTotal() {
        return total;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }
}
