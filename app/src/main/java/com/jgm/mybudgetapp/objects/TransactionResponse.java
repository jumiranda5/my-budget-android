package com.jgm.mybudgetapp.objects;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class TransactionResponse implements Parcelable {

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

    protected TransactionResponse(Parcel in) {
        id = in.readInt();
        type = in.readInt();
        description = in.readString();
        amount = in.readFloat();
        year = in.readInt();
        month = in.readInt();
        day = in.readInt();
        categoryId = in.readInt();
        if (in.readByte() == 0) {
            accountId = null;
        } else {
            accountId = in.readInt();
        }
        if (in.readByte() == 0) {
            cardId = null;
        } else {
            cardId = in.readInt();
        }
        paid = in.readByte() != 0;
        repeat = in.readInt();
        if (in.readByte() == 0) {
            repeatCount = null;
        } else {
            repeatCount = in.readInt();
        }
        if (in.readByte() == 0) {
            repeatId = null;
        } else {
            repeatId = in.readLong();
        }
        categoryName = in.readString();
        colorId = in.readInt();
        iconId = in.readInt();
    }

    public static final Creator<TransactionResponse> CREATOR = new Creator<TransactionResponse>() {
        @Override
        public TransactionResponse createFromParcel(Parcel in) {
            return new TransactionResponse(in);
        }

        @Override
        public TransactionResponse[] newArray(int size) {
            return new TransactionResponse[size];
        }
    };

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

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setCardId(Integer cardId) {
        this.cardId = cardId;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(type);
        dest.writeString(description);
        dest.writeFloat(amount);
        dest.writeInt(year);
        dest.writeInt(month);
        dest.writeInt(day);
        dest.writeInt(categoryId);
        if (accountId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(accountId);
        }
        if (cardId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(cardId);
        }
        dest.writeByte((byte) (paid ? 1 : 0));
        dest.writeInt(repeat);
        if (repeatCount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(repeatCount);
        }
        if (repeatId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(repeatId);
        }
        dest.writeString(categoryName);
        dest.writeInt(colorId);
        dest.writeInt(iconId);
    }
}
