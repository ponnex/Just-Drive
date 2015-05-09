package com.ponnex.justdrive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
/**
 * Created by ramos on 4/15/2015.
 */
public class RebootReceiving extends BroadcastReceiver {

    private String TAG = "com.ponnex.justdrive.RebootReceiving";

    public void onReceive(Context arg0, Intent arg1)
    {
        Log.d(TAG,"reboot");
        Boolean boot = PreferenceManager.getDefaultSharedPreferences(arg0).getBoolean("startonboot", true);
        if (boot) {
            Intent intent = new Intent(arg0, LockScreen.class);
            arg0.startService(intent);

            Toast toast;
            toast = Toast.makeText(arg0, "Starting Just Drive...", Toast.LENGTH_LONG);
            toast.show();

            Log.e("TYPE", "lockscreen rebooted");
        }
    }
}