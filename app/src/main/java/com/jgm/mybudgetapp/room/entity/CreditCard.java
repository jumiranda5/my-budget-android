package com.jgm.mybudgetapp.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cards")
public class CreditCard {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "colorId")
    private int colorId;
    @ColumnInfo(name = "billingDay")
    private int billingDay;
    @ColumnInfo(name = "active")
    private boolean isActive;

    public CreditCard(String name, int colorId, int billingDay, boolean isActive) {
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
}
