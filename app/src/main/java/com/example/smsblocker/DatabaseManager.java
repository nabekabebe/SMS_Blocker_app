package com.example.smsblocker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.smsblocker.Utilities.BlockedSMSDBHelper;

public class DatabaseManager {
    private static final String TAG = "DatabaseManager";

    public static BlockedSMSDBHelper sms_db;
    public static DatabaseManager db_manager = null;

    private DatabaseManager() {
    }

    public static DatabaseManager getInstance(Context context) {
        if (db_manager == null) {
            db_manager = new DatabaseManager();
            sms_db = new BlockedSMSDBHelper(context);
        }
        return db_manager;
    }

    public SQLiteDatabase getBlockedSmsDb() {
        return sms_db.getReadableDatabase();
    }

    public SQLiteDatabase storeBlockedSms() {
        return sms_db.getWritableDatabase();
    }

    public void closeDatabase() {
        if (sms_db != null) {
            sms_db.close();
        }
    }
}
