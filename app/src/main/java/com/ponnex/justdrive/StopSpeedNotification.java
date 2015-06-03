package com.ponnex.justdrive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by EmmanuelFrancis on 6/4/2015.
 */
public class StopSpeedNotification extends BroadcastReceiver {
    // stops service if user isn't driving
    private LocalBroadcastManager broadcastManager;

    private String TAG = "com.ponnex.justdrive.StopSpeedNotification";

    @Override
    public void onReceive(Context context, Intent intent) {
        broadcastManager = LocalBroadcastManager.getInstance(context);
        sendStopSpeed(true);
    }

    public void sendStopSpeed(Boolean isStop) {
        Intent intent = new Intent("com.ponnex.justdrive.StopSpeedNotification");
        intent.putExtra("isStop", isStop);
        broadcastManager.sendBroadcast(intent);
    }
}

