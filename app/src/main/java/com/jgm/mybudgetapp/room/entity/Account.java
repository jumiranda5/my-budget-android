package com.jgm.mybudgetapp.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "accounts")
public class Account {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "colorId")
    private int colorId;
    @ColumnInfo(name = "iconId")
    private int iconId;
    @ColumnInfo(name = "type")
    private int type; // 0 = cash | 1 = checking | 2 = savings
    @ColumnInfo(name = "active")
    private boolean isActive;

    public Account(String name, int colorId, int iconId, int type, boolean isActive) {
        this.name = name;
        this.colorId = colorId;
        this.iconId = iconId;
        this.type = type;
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

    public int getType() {
        return type;
    }

    public boolean isActive() {
        return isActive;
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

    public void setType(int type) {
        this.type = type;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
