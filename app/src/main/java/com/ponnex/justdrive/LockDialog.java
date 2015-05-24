package com.ponnex.justdrive;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by EmmanuelFrancis on 5/22/2015.
 */

public class LockDialog extends Service {
    AlertDialog alertDialog;
    private String TAG = "com.ponnex.justdrive.LockDialog";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "LockDialog Created");
        super.onCreate();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(dismissReceiver, new IntentFilter("com.bloxt.ponnex.guard.dissmisslockdialog"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "LockDialog onStartCommand");
        super.onStartCommand(intent, flags, startId);
        LockDialog();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "LockDialog Destroy");
        super.onDestroy();
    }

    private void LockDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getApplicationContext(), R.style.AppCompatAlertDialogStyle));
        alertDialog = builder.create();
        alertDialog.setTitle(getText(R.string.dialog_title));
        alertDialog.setMessage(getText(R.string.dialog_message));
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "QUIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Go to the Home screen
                Intent homeIntent = new Intent();
                homeIntent.setAction(Intent.ACTION_MAIN);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                getApplicationContext().startActivity(homeIntent);

                dialog.cancel();
                //stop this service
                stopSelf();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "MORE INFO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dismiss dialog
                dialog.cancel();
                //stop this service
                stopSelf();
                startService(new Intent(LockDialog.this, AboutDialog.class));
            }
        });

        alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    // Go to the Home screen
                    Intent homeIntent = new Intent();
                    homeIntent.setAction(Intent.ACTION_MAIN);
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    homeIntent.addCategory(Intent.CATEGORY_HOME);
                    getApplicationContext().startActivity(homeIntent);

                    dialog.dismiss();
                }
                return false;
            }
        });
        alertDialog.getWindow().setType(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();

        final int accentcolor = getApplicationContext().getResources().getColor(R.color.accent);

        TextView messageText = (TextView) alertDialog.findViewById(android.R.id.message);
        messageText.setGravity(Gravity.CENTER);

        Button positive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positive.setTextColor(accentcolor);

        Button negative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negative.setTextColor(accentcolor);
    }

    private BroadcastReceiver dismissReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Boolean isDismiss = intent.getBooleanExtra("isDismiss", false);
            if(isDismiss){
                alertDialog.dismiss();
                //stop this service
                stopSelf();
            }
        }
    };
}