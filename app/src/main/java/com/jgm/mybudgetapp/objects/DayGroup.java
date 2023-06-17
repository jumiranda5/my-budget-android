package com.jgm.mybudgetapp.objects;

import java.util.List;

public class DayGroup {

    private int day;
    private int month;
    private int year;
    private List<TransactionResponse> transactions;

    public DayGroup(int day, int month, int year, List<TransactionResponse> transactions) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.transactions =  transactions;
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public List<TransactionResponse> getTransactions() {
        return transactions;
    }

}