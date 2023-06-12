package com.jgm.mybudgetapp.objects;

public class PaymentMethod {

    private int id;
    private int type; // 0 = cash | 1 = checking | 2 = savings | 3 = credit card
    private String name;
    private int colorId;
    private int iconId;
    private int billingDay;

    public PaymentMethod(int id,
                         int type,
                         String name,
                         int colorId,
                         int iconId,
                         int billingDay) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.colorId = colorId;
        this.iconId = iconId;
        this.billingDay = billingDay;
    }

    public int getId() {
        return id;
    }

    public int getType() {
        return type;
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

    public int getBillingDay() {
        return billingDay;
    }
}
