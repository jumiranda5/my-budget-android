package com.jgm.mybudgetapp.objects;

public class YearResponse {

    private final float balance;
    private final float income;
    private final float expenses;

    public YearResponse(float balance, float income, float expenses) {
        this.balance = balance;
        this.income = income;
        this.expenses = expenses;
    }

    public float getBalance() {
        return balance;
    }

    public float getIncome() {
        return income;
    }

    public float getExpenses() {
        return expenses;
    }

}
