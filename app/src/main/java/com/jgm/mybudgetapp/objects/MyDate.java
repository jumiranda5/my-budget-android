package com.jgm.mybudgetapp.objects;

public class MyDate {

    private final int year;
    private final int month;
    private final int day;
    private String monthName;
    private String weekday;

    public MyDate(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
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

    public String getMonthName() {
        return monthName;
    }

    public String getWeekday() {
        return weekday;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public void setWeekday(String weekday) {
        this.weekday = weekday;
    }
}
