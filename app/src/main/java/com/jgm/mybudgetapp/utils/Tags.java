package com.jgm.mybudgetapp.utils;

public class Tags {
    
    // Logs
    public static final String LOG_NAV = "debug-nav";
    public static final String LOG_LIFECYCLE = "debug-lifecycle";
    public static final String LOG_DB = "debug-database";

    // Form
    public static final int TYPE_IN = 1;
    public static final int TYPE_OUT = -1;
    public static final int METHOD_CASH = 0;
    public static final int METHOD_CHECKING = 1;
    public static final int METHOD_SAVINGS = 2;
    public static final int METHOD_CARD = 3;
    public static final int CARD_ICON_ID = 70;

    public static final String expense = "EXPENSE";
    public static final String income = "INCOME";
    public static final String transfer = "TRANSFER";

    // Fragment Tags
    public static final String accountsTag = "ACCOUNTS";
    public static final String accountFormTag = "ACCOUNT_FORM";
    public static final String accountDetailsTag = "ACCOUNT_DETAILS";
    public static final String categoriesFormTag = "CATEGORIES_FORM";
    public static final String categoriesListTag = "CATEGORIES_LIST";
    public static final String cardsTag = "CARDS";
    public static final String cardFormTag = "CARD_FORM";
    public static final String homeTag = "HOME";
    public static final String settingsTag = "SETTINGS";
    public static final String transactionFormTag = "TRANSACTION_FORM";
    public static final String transactionsOutTag = "TRANSACTIONS_OUT";
    public static final String transactionsInTag = "TRANSACTIONS_IN";
    public static final String pendingTag = "PENDING";
    public static final String adLockTag = "AD_LOCK";

    // Activity tags
    public static final String categoriesTag = "CATEGORIES";
    public static final String yearTag = "YEAR";

    // Main icons
    public static final Integer cardIconId = 70;

    // Shared Prefs
    public static final String keyInitialAccounts = "hasInitialAccounts";
    public static final String keyInitialCategories = "hasInitialCategories";
    public static final String keyIsPremium = "isPremium";
    public static final String keyIapOrder = "iapOrder";
    public static final String keyDarkMode = "isDark";
    public static final String keyRefresh = "shouldRefresh";
    public static final String keyStartCount = "appStartCount";
    
}
