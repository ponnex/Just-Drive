package com.ponnex.justdrive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by ramos on 4/15/2015.
 */

public class NotificationReceiver extends BroadcastReceiver {
    // stops service if user isn't driving
    private LocalBroadcastManager broadcastManager;

    private String TAG = "com.ponnex.justdrive.NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        broadcastManager = LocalBroadcastManager.getInstance(context);

        cancellable(true);

        SharedPreferences isHeadsup = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor2 = isHeadsup.edit();
        editor2.putBoolean("headsup", false);
        editor2.apply();

        context.stopService(new Intent(context, TelephonyService.class));

        SharedPreferences isSwitch = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor1 = isSwitch.edit();
        editor1.putBoolean("switch", false);
        editor1.apply();

    }

    public void cancellable(Boolean message) {
        Intent intent = new Intent("com.ponnex.justdrive.NotificationReceiver");
        if (message != null) {
            intent.putExtra("isCancel", message);
            Log.d(TAG, "Cancelled");
        }
        broadcastManager.sendBroadcast(intent);
    }
}
