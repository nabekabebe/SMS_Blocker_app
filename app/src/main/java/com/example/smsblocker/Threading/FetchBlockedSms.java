package com.example.smsblocker.Threading;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.example.smsblocker.DatabaseManager;
import com.example.smsblocker.MainActivity;
import com.example.smsblocker.SMS_Model;
import com.example.smsblocker.Utilities.SMSDBContract;

import java.util.ArrayList;

public class FetchBlockedSms extends AsyncTaskLoader<ArrayList<SMS_Model>> {

    SQLiteDatabase sms_db;
    ArrayList<SMS_Model> blocked_sms;

    public FetchBlockedSms(@NonNull Context context) {
        super(context);
        sms_db = DatabaseManager.getInstance(context).getBlockedSmsDb();
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Nullable
    @Override
    public ArrayList<SMS_Model> loadInBackground() {
        String[] queryColumn = new String[]{SMSDBContract._ID, SMSDBContract.COLUMN_ADDRESS,
                SMSDBContract.COLUMN_MESSAGE, SMSDBContract.COLUMN_DATE};

        Cursor cursor = sms_db.query(SMSDBContract.TABLE_NAME, queryColumn, null,
                null, null, null, null);

        blocked_sms = new ArrayList<>();
        while (cursor.moveToNext()) {
            SMS_Model sms = new SMS_Model();
            int id_index = cursor.getColumnIndex(SMSDBContract._ID);
            int address_index = cursor.getColumnIndex(SMSDBContract.COLUMN_ADDRESS);
            int msg_index = cursor.getColumnIndex(SMSDBContract.COLUMN_MESSAGE);
            int date_index = cursor.getColumnIndex(SMSDBContract.COLUMN_DATE);

            sms.setId(cursor.getString(id_index));
            sms.setAddress(cursor.getString(address_index));
            sms.setMsg(cursor.getString(msg_index));
            sms.setDate(cursor.getString(date_index));

            blocked_sms.add(sms);
        }
        cursor.close();
        return blocked_sms;
    }

}
