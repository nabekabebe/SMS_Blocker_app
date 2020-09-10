package com.example.smsblocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.example.smsblocker.Utilities.SMSDBContract;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class PhoneStateListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        assert intent.getAction() != null;
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle sms_bundle = intent.getExtras();
            Object[] messages = (Object[]) sms_bundle.get("pdus");
            SmsMessage[] smsMessage = new SmsMessage[messages.length];

            for (int i = 0; i < messages.length; i++) {
                smsMessage[i] = SmsMessage.createFromPdu((byte[]) messages[i]);
            }

            final String numberSms = smsMessage[0].getOriginatingAddress();
            final String messageSms = smsMessage[0].getDisplayMessageBody();
            long dateTimeSms = smsMessage[0].getTimestampMillis();
            final String dateFormat = new SimpleDateFormat("MM/dd/yyyy")
                    .format(new Date(dateTimeSms));

            //block sms if number is matched to our blocking number
            Pattern p = Pattern.compile("[0-9]{4,6}");

            SQLiteDatabase sms_db = DatabaseManager.getInstance(context).storeBlockedSms();
            String[] queryColumn = new String[]{SMSDBContract._ID, SMSDBContract.COLUMN_ADDRESS,
                    SMSDBContract.COLUMN_MESSAGE, SMSDBContract.COLUMN_DATE};

            Cursor cursor = sms_db.query(SMSDBContract.TABLE_NAME, queryColumn, null,
                    null, null, null, null);

            ArrayList<SMS_Model> blocked_sms = new ArrayList<>();
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


            if (p.matcher(String.valueOf(numberSms)).matches() || checkBlocked(blocked_sms, numberSms)) {
                abortBroadcast();
                Toast.makeText(context, "short SMS blocked", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "SMS allowed", Toast.LENGTH_LONG).show();
                SMS_Model sms = new SMS_Model();
                sms.setId(SMSDBContract._ID);
                sms.setAddress(numberSms);
                sms.setMsg(messageSms);
                sms.setDate(dateFormat);

                SQLiteDatabase db = DatabaseManager.getInstance(context).storeBlockedSms();
                try {
                    String insertSMS = "INSERT INTO " + SMSDBContract.TABLE_NAME + " ( " + SMSDBContract.COLUMN_ID + ", " +
                            SMSDBContract.COLUMN_ADDRESS + ", " + SMSDBContract.COLUMN_MESSAGE + ", " +
                            SMSDBContract.COLUMN_DATE + ") VALUES ( ?, ?, ?, ? )";

                    String[] col = new String[]{sms.getId(), sms.getAddress(), sms.getMsg()
                            , sms.getDate()};

                    db.execSQL(insertSMS, col);
                } catch (SQLiteConstraintException e) {
                    Log.d("Constraint exception", e.getMessage());
                }
            }
        }
    }

    public Boolean checkBlocked(ArrayList<SMS_Model> sms, String num) {
        for (SMS_Model s :
                sms) {
            if (s.getAddress().equals(num)) {
                return true;
            }
        }
        return false;
    }

}
