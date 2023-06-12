package com.jgm.mybudgetapp.objects;

public class Transaction {

    private int id;
    private int type;  // in(1)|out(-1)
    private String description;
    private float amount;
    private int year;
    private int month;
    private int day;
    private int categoryId;
    private int accountId;
    private int cardId;
    private boolean isPaid;
    private int repeat;
    private int repeatCount;
    private long repeatId;

    public Transaction(int type, String description, float amount,
                       int categoryId, int year, int month, int day, boolean isPaid) {

        this.type = type;
        this.description = description;
        this.amount = amount;
        this.categoryId = categoryId;
        this.year = year;
        this.month = month;
        this.day = day;
        this.isPaid = isPaid;
    }

    // Getters

    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }


    public int getAccountId() {return accountId;}

    public int getCategoryId() {
        return categoryId;
    }

    public int getCardId() {
        return cardId;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getRepeat() {
        return repeat;
    }

    public float getAmount() {
        return amount;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public long getRepeatId() {
        return repeatId;
    }

    // Setters


    public void setId(int id) {
        this.id = id;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public void setRepeatId(long repeatId) {
        this.repeatId = repeatId;
    }
}
