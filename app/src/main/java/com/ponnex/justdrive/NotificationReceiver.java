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

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(context);
        Boolean isTEST = (mSharedPreference.getBoolean("isTEST", false));

        broadcastManager = LocalBroadcastManager.getInstance(context);

        cancellable(true);

        SharedPreferences isHeadsup = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor2 = isHeadsup.edit();
        editor2.putBoolean("headsup", false);
        editor2.apply();

        if (!isTEST) {
            context.stopService(new Intent(context, CarMode.class));

            SharedPreferences isSwitchup = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = isSwitchup.edit();
            editor.putBoolean("isSwitch", false);
            editor.apply();

            SharedPreferences switchPref = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor1 = switchPref.edit();
            editor1.putBoolean("switch", false);
            editor1.apply();

            sendSwitchInfo(false);
            sendSwitchInfo1(false);
        }
    }

    public void sendSwitchInfo(Boolean switchval) {
        Intent intent = new Intent("com.ponnex.justdrive.MainActivity");
        intent.putExtra("SwitchVal", switchval);
        broadcastManager.sendBroadcast(intent);
    }

    public void sendSwitchInfo1(Boolean switchval) {
        Intent intent = new Intent("com.ponnex.justdrive.SettingsFragment");
        intent.putExtra("SwitchVal", switchval);
        broadcastManager.sendBroadcast(intent);
    }

    public void cancellable(Boolean message) {
        Intent intent = new Intent("com.ponnex.justdrive.NotificationReceiver");
        if (message != null) {
            intent.putExtra("isCancel", message);
            Log.e("TYPE", "Cancelled");
        }
        broadcastManager.sendBroadcast(intent);
    }
}
