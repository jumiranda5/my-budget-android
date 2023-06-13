package com.jgm.mybudgetapp.objects;

public class Balance {

    private float balance;
    private float income;
    private float expenses;

    public Balance(float balance, float income, float expenses) {
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
