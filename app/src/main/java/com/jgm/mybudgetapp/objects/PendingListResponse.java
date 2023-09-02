package com.jgm.mybudgetapp.objects;

public class PendingListResponse {

    //id, description, SUM(amount) AS total, cardId, year, month, day

    private final int id;
    private final int type;
    private final float total;
    private final int cardId;
    private final int month;
    private final int year;
    private final int day;
    private final String description;
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
