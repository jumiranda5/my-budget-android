package com.jgm.mybudgetapp.db;

import android.provider.BaseColumns;

public class DatabaseContract {

    private DatabaseContract() {}

    public static final class Accounts implements BaseColumns {

        public static final String TABLE_NAME = "accounts_table";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_COLOR = "color_id";
        public static final String COLUMN_ICON = "icon_id";
        public static final String COLUMN_TYPE = "type"; // 1 = checking | 2 = savings (0 is default cash => not included on db)
        public static final String COLUMN_ACTIVE = "active";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_NAME + " TEXT NOT NULL, " +
                        COLUMN_COLOR + " INTEGER NOT NULL, " +
                        COLUMN_ICON + " INTEGER NOT NULL, " +
                        COLUMN_TYPE  + " INTEGER NOT NULL, " +
                        COLUMN_ACTIVE + " INTEGER DEFAULT 1)";

        public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    }

    public static final class Categories implements BaseColumns {

        public static final String TABLE_NAME = "categories_table";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_COLOR = "color";
        public static final String COLUMN_ICON = "icon";
        public static final String COLUMN_ACTIVE = "active";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_NAME + " TEXT NOT NULL, " +
                        COLUMN_COLOR + " INTEGER NOT NULL, " +
                        COLUMN_ICON + " INTEGER NOT NULL, " +
                        COLUMN_ACTIVE + " INTEGER DEFAULT 1)";

        public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    }

    public static final class CreditCards implements BaseColumns {

        public static final String TABLE_NAME = "credit_cards_table";
        public static final String COLUMN_NICKNAME = "nickname";
        public static final String COLUMN_COLOR = "color";
        public static final String COLUMN_BILLING_DAY = "billing_day";
        public static final String COLUMN_ACTIVE = "is_active";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_NICKNAME + " TEXT NOT NULL, " +
                        COLUMN_COLOR + " INTEGER NOT NULL, " +
                        COLUMN_BILLING_DAY  + " INTEGER NOT NULL, " +
                        COLUMN_ACTIVE + " INTEGER DEFAULT 1)";

        public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    }

    public static final class Transactions implements BaseColumns {

        public static final String TABLE_NAME = "transactions";
        public static final String COLUMN_TYPE = "type"; // in(1)|out(-1)
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_AMOUNT = "amount";
        public static final String COLUMN_YEAR = "year";
        public static final String COLUMN_MONTH = "month";
        public static final String COLUMN_DAY = "day";
        public static final String COLUMN_CATEGORY_ID = "category_id";
        public static final String COLUMN_ACCOUNT_ID = "account_id";
        public static final String COLUMN_CARD_ID = "card_id";
        public static final String COLUMN_PAID = "paid";
        public static final String COLUMN_REPEAT = "repeat";
        public static final String COLUMN_REPEAT_COUNT = "repeat_count";
        public static final String COLUMN_REPEAT_ID = "repeat_id";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_TYPE + " INTEGER NOT NULL, " +
                        COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                        COLUMN_AMOUNT + " REAL NOT NULL, " +
                        COLUMN_YEAR + " INTEGER NOT NULL, " +
                        COLUMN_MONTH + " INTEGER NOT NULL, " +
                        COLUMN_DAY + " INTEGER NOT NULL, " +
                        COLUMN_CATEGORY_ID + " INTEGER NOT NULL, " +
                        COLUMN_ACCOUNT_ID + " INTEGER, " +
                        COLUMN_CARD_ID + " INTEGER, " +
                        COLUMN_PAID + " INTEGER, " +
                        COLUMN_REPEAT + " INTEGER DEFAULT 1, " +
                        COLUMN_REPEAT_COUNT + " INTEGER, " +
                        COLUMN_REPEAT_ID + " INTEGER)";

        public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    }

}
