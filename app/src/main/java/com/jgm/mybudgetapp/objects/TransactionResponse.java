package com.jgm.mybudgetapp.objects;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

public class TransactionResponse {

    private int id;
    private int type;  // in(1)|out(-1)
    private String description;
    private float amount;
    private int year;
    private int month;
    private int day;
    private int categoryId;
    private Integer accountId;
    private Integer cardId;
    private boolean paid;
    private int repeat;
    private Integer repeatCount;
    private Long repeatId;
    private String categoryName;
    private int colorId;
    private int iconId;


    public TransactionResponse(
            int id,
            int type,
            String description,
            float amount,
            int year, int month, int day,
            int categoryId,
            Integer accountId,
            Integer cardId,
            boolean paid,
            int repeat,
            Integer repeatCount,
            Long repeatId,
            String categoryName,
            int colorId,
            int iconId) {

        this.id = id;
        this.type = type;
        this.description = description;
        this.amount = amount;
        this.year = year;
        this.month = month;
        this.day = day;
        this.categoryId = categoryId;
        this.accountId = accountId;
        this.cardId = cardId;
        this.paid = paid;
        this.repeat = repeat;
        this.repeatCount = repeatCount;
        this.repeatId = repeatId;
        this.categoryName = categoryName;
        this.colorId = colorId;
        this.iconId = iconId;

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
        return paid;
    }

    public Integer getRepeatCount() {
        return repeatCount;
    }

    public Long getRepeatId() {
        return repeatId;
    }

    public int getColorId() {
        return colorId;
    }

    public int getIconId() {
        return iconId;
    }

    public String getCategoryName() {
        return categoryName;
    }
}
