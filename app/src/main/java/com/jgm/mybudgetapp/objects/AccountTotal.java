package com.jgm.mybudgetapp.objects;

public class AccountTotal {

    private int id;
    private String name;
    private int colorId;
    private int iconId;
    private int type; // 0 = cash | 1 = checking | 2 = savings
    private boolean isActive;
    private float total;

    public AccountTotal(int id, String name, int colorId, int iconId, int type, boolean isActive, float total) {
        this.id = id;
        this.name = name;
        this.colorId = colorId;
        this.iconId = iconId;
        this.type = type;
        this.isActive = isActive;
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

    public int getType() {
        return type;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getTotal() {
        return total;
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

    public void setType(int type) {
        this.type = type;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setTotal(float total) {
        this.total = total;
    }
}
