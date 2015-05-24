package com.ponnex.justdrive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * Created by ramos on 4/15/2015.
 */

public class TextReceiver extends BroadcastReceiver {

    SmsMessage[] msgs;
    String msg_from;
    String msg = CallerService.msg + "\n--This is an automated SMS--";
    private String TAG = "com.ponnex.justdrive.Telephony.TextReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences mSharedPreference1= PreferenceManager.getDefaultSharedPreferences(context);
        String isMsgFrom = (mSharedPreference1.getString("isMsgfrom", null));

        Log.d(TAG, "message received");
        if (CallerService.autoreply) {
            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                Log.d(TAG, "SMS received");
                // gets the message
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    // ---retrieve the SMS message received---
                    Log.d(TAG, "Bundle != null");
                    try {
                        // gets the sender then sends a sms back
                        Object[] pdus = (Object[]) bundle.get("pdus");
                        msgs = new SmsMessage[pdus.length];

                        for (int i = 0; i < msgs.length; i++) {
                            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);

                            msg_from = msgs[i].getOriginatingAddress();

                            Log.d(TAG, "SMS Previous: " + isMsgFrom);
                            Log.d(TAG,"SMS Received: " + msg_from);

                            SmsManager smsManager = SmsManager.getDefault();
                            if (!msg_from.equals(isMsgFrom)) {
                                smsManager.sendTextMessage(msg_from, null, msg, null, null);
                            }
                        }

                        SharedPreferences isMsgfrom = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor editor = isMsgfrom.edit();
                        editor.putString("isMsgfrom", msg_from);
                        editor.apply();

                    } catch (Exception e) {
                        Log.d("Exception caught", e.getMessage());
                    }
                }
            }
        }
    }
}