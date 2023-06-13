package com.jgm.mybudgetapp.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class Transaction {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;
    @ColumnInfo(name = "type")
    private int type;  // in(1)|out(-1)
    @ColumnInfo(name = "description")
    private String description;
    @ColumnInfo(name = "amount")
    private float amount;
    @ColumnInfo(name = "year")
    private int year;
    @ColumnInfo(name = "month")
    private int month;
    @ColumnInfo(name = "day")
    private int day;
    @ColumnInfo(name = "categoryId")
    private int categoryId;
    @ColumnInfo(name = "accountId")
    private Integer accountId;
    @ColumnInfo(name = "cardId")
    private Integer cardId;
    @ColumnInfo(name = "paid")
    private boolean isPaid;
    @ColumnInfo(name = "repeat")
    private int repeat;
    @ColumnInfo(name = "repeatCount")
    private Integer repeatCount;
    @ColumnInfo(name = "repeatId")
    private Long repeatId;

    public Transaction(int type, String description, float amount,
                       int year, int month, int day,
                       int categoryId, Integer accountId, Integer cardId,
                       boolean isPaid, int repeat, Integer repeatCount, Long repeatId) {

        this.type = type;
        this.description = description;
        this.amount = amount;
        this.year = year;
        this.month = month;
        this.day = day;
        this.categoryId = categoryId;
        this.accountId = accountId;
        this.cardId = cardId;
        this.isPaid = isPaid;
        this.repeat = repeat;
        this.repeatCount = repeatCount;
        this.repeatId = repeatId;

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

    public Integer getAccountId() {
        return accountId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public Integer getCardId() {
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

    public Integer getRepeatCount() {
        return repeatCount;
    }

    public Long getRepeatId() {
        return repeatId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setRepeatId(Long repeatId) {
        this.repeatId = repeatId;
    }

    public void setRepeatCount(Integer repeatCount) {
        this.repeatCount = repeatCount;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public void setCardId(Integer cardId) {
        this.cardId = cardId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    public void setYear(int year) {
        this.year = year;
    }
}