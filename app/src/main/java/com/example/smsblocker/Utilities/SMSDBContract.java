package com.example.smsblocker.Utilities;

import android.provider.BaseColumns;

public final class SMSDBContract implements BaseColumns {

    public static final String TABLE_NAME = "blocked_sms";

    public static final String COLUMN_ID = "sms_id";
    public static final String COLUMN_ADDRESS = "sms_address";
    public static final String COLUMN_MESSAGE = "sms_message";
    public static final String COLUMN_DATE = "sms_date";

    //    create sql table
    public static final String CREATE_TABLE = "CREATE TABLE " +
            TABLE_NAME + " ( " + _ID + " INTEGER PRIMARY KEY," + COLUMN_ID + " TEXT UNIQUE NOT NULL," +
            COLUMN_ADDRESS + " TEXT NOT NULL, " + COLUMN_MESSAGE + " TEXT, " +
            COLUMN_DATE + " TEXT )";
}
