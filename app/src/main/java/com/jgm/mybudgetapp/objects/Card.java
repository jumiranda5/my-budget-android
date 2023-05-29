package com.jgm.mybudgetapp.objects;

public class Card {

    private int id;
    private String name;
    private int colorId;
    private int billingDay;
    private boolean isActive;
    private float total;

    public Card(int id, String name, int colorId, int billingDay, boolean isActive) {
        this.id = id;
        this.name = name;
        this.colorId = colorId;
        this.billingDay = billingDay;
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

    public int getBillingDay() {
        return billingDay;
    }

    public boolean isActive() {
        return isActive;
    }

    public float getTotal() {
        return total;
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

    public void setBillingDay(int billingDay) {
        this.billingDay = billingDay;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setTotal(float total) {
        this.total = total;
    }
}
