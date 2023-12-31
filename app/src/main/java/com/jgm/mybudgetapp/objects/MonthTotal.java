package com.jgm.mybudgetapp.objects;

public class MonthTotal {

    private final int year;
    private final int monthId;
    private final String monthName;
    private final String monthNameSmall;
    private final float expenses;
    private final float income;

    public MonthTotal(
            int year,
            int monthId,
            String monthName,
            String monthNameSmall,
            float expenses,
            float income) {
        this.year = year;
        this.monthId = monthId;
        this.monthName = monthName;
        this.monthNameSmall = monthNameSmall;
        this.expenses = expenses;
        this.income = income;
    }

    public int getYear() {
        return year;
    }

    public int getMonthId() {
        return monthId;
    }

    public String getMonthName() {
        return monthName;
    }

    public String getMonthNameSmall() {
        return monthNameSmall;
    }

    public float getExpenses() {
        return expenses;
    }

    public float getIncome() {
        return income;
    }
}
