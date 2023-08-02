package com.jgm.mybudgetapp.objects;

import java.util.List;

public class PendingListResponse {

    //id, description, SUM(amount) AS total, cardId, year, month, day

    private int id;
    private int type;
    private float total;
    private int cardId;
    private int month;
    private int year;
    private int day;
    private String description;
    boolean paid;

    public PendingListResponse(int id, int type, float total, int cardId, int month, int year, int day, String description, boolean paid) {
        this.cardId = cardId;
        this.month = month;
        this.year = year;
        this.id = id;
        this.type = type;
        this.total = total;
        this.day = day;
        this.description = description;
        this.paid = paid;
    }

    public int getDay() {
        return day;
    }

    public float getTotal() {
        return total;
    }

    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getCardId() {
        return cardId;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }
}
