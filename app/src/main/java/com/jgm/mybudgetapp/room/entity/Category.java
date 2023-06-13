package com.jgm.mybudgetapp.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "colorId")
    private int colorId;
    @ColumnInfo(name = "iconId")
    private int iconId;
    @ColumnInfo(name = "active")
    private boolean isActive;

    public Category(String name, int colorId, int iconId, boolean isActive) {
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

    public void setId(int id) {
        this.id = id;
    }

    public void setActive(boolean active) {
        isActive = active;
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
}
