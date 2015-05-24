package com.ponnex.justdrive;

import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by EmmanuelFrancis on 5/24/2015.
 */
public class SpeedService extends Service implements GPSCallback {
    private GPSManager gpsManager = null;
    //the speed of the phone
    private double speed = 0.0;
    AlertDialog alertDialog;
    String speedString;
    private boolean showing = false;
    private String TAG = "com.ponnex.justdrive.SpeedService";

    //required by service (service runs in background)
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        gpsManager = new GPSManager();
        gpsManager.startListening(getApplicationContext());
        gpsManager.setGPSCallback(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!showing){
            showdialog("Reading Current Speed...");
        }
        //keep service running
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        gpsManager.stopListening();
        gpsManager.setGPSCallback(null);
        gpsManager = null;
        super.onDestroy();
    }

    @Override
    public void onGPSUpdate(Location location)
    {
        location.getLatitude();
        location.getLongitude();
        speed = roundDecimal(convertSpeed(location.getSpeed()),2);

        if(speed > 30){
            speedString = "Current Speed: " + speed + " KM" + "\n\nJust Drive detected that you are running NON-SAFE speed.";
            //show/tell user that he is in a vehicle
            //dismiss and stop this service
            showing = false;
            alertDialog.dismiss();
            //stop this service
            stopSelf();
        } else {
            speedString = "Current Speed: " + speed + " KM" + "\n\nJust Drive detected that you are running SAFE speed.";
            //show/tell user that we are sorry for the inconvenient:'(
            //dismiss and stop this service, and stop AppLockService

            showing = false;
            alertDialog.dismiss();
            //stop this service
            stopSelf();

            stopService(new Intent(SpeedService.this, AppLockService.class));
            stopService(new Intent(SpeedService.this, LockDialog.class));
            stopService(new Intent(SpeedService.this, CallerService.class));
        }

        Toast toast;
        toast = Toast.makeText(getApplicationContext(), speedString, Toast.LENGTH_LONG);
        toast.show();

        showdialog(speedString);
    }

    private void showdialog(String message){
        showing = true;
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getApplicationContext(), R.style.AppCompatAlertDialogStyle));
        alertDialog = builder.create();
        alertDialog.setTitle("Speed Test");
        alertDialog.setMessage(message);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "HIDE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Go to the Home screen
                Intent homeIntent = new Intent();
                homeIntent.setAction(Intent.ACTION_MAIN);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                getApplicationContext().startActivity(homeIntent);

                showing = false;
                dialog.dismiss();

                /*
                *
                *
                * display notification then
                *
                *
                 */
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dismiss dialog
                showing = false;
                dialog.dismiss();

                stopSelf();
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

                    showing = false;
                    dialog.cancel();
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

    private double convertSpeed(double speed){
        return ((speed * 3600) * 0.001); // to kilometers
    }

    private double roundDecimal(double value, final int decimalPlace) {
        BigDecimal bd = new BigDecimal(value);

        bd = bd.setScale(decimalPlace, RoundingMode.HALF_UP);
        value = bd.doubleValue();

        return value;
    }
}
