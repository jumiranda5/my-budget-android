package com.jgm.mybudgetapp.objects;

public class HomeAccounts {

    private float cash;
    private float checking;
    private float savings;

    public HomeAccounts(float cash, float checking, float savings) {
        this.cash = cash;
        this.checking = checking;
        this.savings = savings;
    }

    public float getCash() {
        return cash;
    }

    public float getChecking() {
        return checking;
    }

    public float getSavings() {
        return savings;
    }
}
