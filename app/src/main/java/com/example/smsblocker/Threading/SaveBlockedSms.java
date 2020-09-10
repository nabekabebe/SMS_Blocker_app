package com.example.smsblocker.Threading;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.example.smsblocker.DatabaseManager;
import com.example.smsblocker.SMS_Model;
import com.example.smsblocker.Utilities.SMSDBContract;

import java.util.Objects;

public class SaveBlockedSms extends AsyncTaskLoader<Void> {

    SQLiteDatabase sms_db;
    SMS_Model save_sms;

    public SaveBlockedSms(@NonNull Context context, SMS_Model sms) {
        super(context);
        sms_db = DatabaseManager.getInstance(context).storeBlockedSms();
        save_sms = sms;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Nullable
    @Override
    public Void loadInBackground() {
        try {
            String insertSMS = "INSERT INTO " + SMSDBContract.TABLE_NAME + " ( " + SMSDBContract.COLUMN_ID + ", " +
                    SMSDBContract.COLUMN_ADDRESS + ", " + SMSDBContract.COLUMN_MESSAGE + ", " +
                    SMSDBContract.COLUMN_DATE + ") VALUES ( ?, ?, ?, ? )";

            String[] queryColumn = new String[]{save_sms.getId(), save_sms.getAddress(), save_sms.getMsg()
                    , save_sms.getDate()};

            sms_db.execSQL(insertSMS, queryColumn);
        } catch (SQLiteConstraintException e) {
            Log.d("Constraint exception", Objects.requireNonNull(e.getMessage()));
        }

        return null;
    }
}
