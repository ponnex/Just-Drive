package com.ponnex.justdrive;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by EmmanuelFrancis on 5/23/2015.
 */

public class AboutDialog extends Service {
    AlertDialog alertDialog;
    private LocalBroadcastManager broadcastManager;
    private String TAG = "com.ponnex.justdrive.AboutDialog";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "AboutDialog Created");
        super.onCreate();

        broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

        if(isServiceRunning(LockDialog.class)){
            isDismiss(true);
        }

        // Go to the Home screen
        Intent homeIntent = new Intent();
        homeIntent.setAction(Intent.ACTION_MAIN);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        getApplicationContext().startActivity(homeIntent);

        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getApplicationContext(), R.style.AppCompatAlertDialogStyle));
        alertDialog = builder.create();
        alertDialog.setTitle(getText(R.string.about_title));
        alertDialog.setMessage(getText(R.string.about_message));
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "DISMISS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dismiss dialog
                dialog.cancel();
                //stop this service
                stopSelf();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NOT DRIVING?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //test speed
                //test screen
                startService(new Intent(AboutDialog.this, SpeedService.class));

                //dismiss dialog
                dialog.cancel();
                //stop this service
                stopSelf();
            }
        });
        alertDialog.getWindow().setType(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();

        final int accentcolor = getApplicationContext().getResources().getColor(R.color.accent);

        TextView messageText = (TextView) alertDialog.findViewById(android.R.id.message);
        messageText.setGravity(Gravity.CENTER);

        Button negative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negative.setTextColor(accentcolor);

        Button positive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positive.setTextColor(accentcolor);
    }

    public void isDismiss(Boolean testbutton) {
        Intent intent = new Intent("com.bloxt.ponnex.guard.dissmisslockdialog");
        intent.putExtra("isDismiss", testbutton);
        broadcastManager.sendBroadcast(intent);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "AboutDialog onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "AboutDialog Destroy");
        super.onDestroy();
    }
}