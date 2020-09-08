package com.example.smsblocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class PhoneStateListener extends BroadcastReceiver {
    private String blockingNumber = "+251911729045";

    @Override
    public void onReceive(Context context, Intent intent) {
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
            if (numberSms != null) {
                abortBroadcast();
                Toast.makeText(context, "SMS blocked", Toast.LENGTH_LONG).show();
            }
        }
    }
}
