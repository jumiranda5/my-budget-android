package com.jgm.mybudgetapp.objects;

public class MonthResponse {

    private final int month;
    private final float income;
    private final float expenses;

    public MonthResponse(int month, float income, float expenses) {
        this.month = month;
        this.income = income;
        this.expenses = expenses;
    }

    public float getIncome() {
        return income;
    }

    public float getExpenses() {
        return expenses;
    }

    public int getMonth() {
        return month;
    }
}
