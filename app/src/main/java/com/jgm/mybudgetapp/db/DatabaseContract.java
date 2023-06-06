package com.jgm.mybudgetapp.db;

import android.provider.BaseColumns;

public class DatabaseContract {

    private DatabaseContract() {}

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
}
