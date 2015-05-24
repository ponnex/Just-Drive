package com.ponnex.justdrive;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by ramos on 4/14/2015.
 */

public class CoreService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int MILLIS_PER_SEC = 0;
    private static final int DETECTION_INT_SEC = 0;
    private static final int DETECTION_INT_MILLIS = MILLIS_PER_SEC * DETECTION_INT_SEC; //change to variable type next update -- let user pick detection time from a drop down menu, will be added to Debugging Mode
    private GoogleApiClient mGoogleApiClient;
    private PendingIntent pendingIntent;
    private String TAG = "com.ponnex.justdrive.CoreService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();

        Log.d(TAG, "CS Created");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(com.google.android.gms.location.ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean isSwitch = (mSharedPreference.getBoolean("switch", true));

        if (isSwitch) {
            ServiceOn();
        }
        if (!isSwitch) {
            ServiceOff();
        }

        SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean isDebug =(mSharedPreference1.getBoolean("isDebug", false));

        if(isDebug){
            DebugNotification();
        }
        else {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(1);
        }

        Intent intent = new Intent(this, ActivityRecognition.class);
        pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        //keep the service running
        return Service.START_STICKY;
    }

    public void ServiceOn(){
        mGoogleApiClient.connect();

        SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean isDebug =(mSharedPreference1.getBoolean("isDebug", false));

        if(isDebug){
            DebugNotification();
        }
        else {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(1);
        }
    }

    public void ServiceOff(){
        if (mGoogleApiClient.isConnected()) {
            Log.d(TAG, "Disconnected");
            com.google.android.gms.location.ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, pendingIntent);
            mGoogleApiClient.disconnect();

            stopService(new Intent(CoreService.this, AppLockService.class));
            stopService(new Intent(CoreService.this, LockDialog.class));
            stopService(new Intent(CoreService.this, CallerService.class));
            stopService(new Intent(CoreService.this, SpeedService.class));

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(1);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        com.google.android.gms.location.ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, DETECTION_INT_MILLIS, pendingIntent);
        Log.d(TAG, "Connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Failed, Reconnecting...");
        mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "CS Destroyed");
        if(mGoogleApiClient.isConnected()) {
            com.google.android.gms.location.ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, pendingIntent);
            mGoogleApiClient.disconnect();
        }
    }

    private void DebugNotification() {
        Log.d(TAG + "_DebugNotification", "Triggered");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Set the title, text, and icon
        builder.setContentTitle(getString(R.string.app_name))
                .setContentText("Debugging Mode")
                .setSmallIcon(R.drawable.ic_debug)
                .setOngoing(true)
                .setContentIntent(pendingIntent);

        // Get an instance of the Notification Manager
        NotificationManager notifyManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the notification and post it
        notifyManager.notify(1, builder.build());
    }

    @Override
    public void onTaskRemoved(Intent rootIntent){
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("switch")) {
            SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Boolean isSwitch = (mSharedPreference.getBoolean("switch", true));
            if(isSwitch) {
                //connect
                ServiceOn();
            }
            if(!isSwitch) {
                //disconnect
                ServiceOff();
            }
        }

        if(key.equals("isDebug")){
            SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Boolean isDebug =(mSharedPreference1.getBoolean("isDebug", false));

            if(isDebug){
                DebugNotification();
            }
            else {

                stopService(new Intent(CoreService.this, AppLockService.class));
                stopService(new Intent(CoreService.this, LockDialog.class));
                stopService(new Intent(CoreService.this, CallerService.class));
                stopService(new Intent(CoreService.this, SpeedService.class));

                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancel(1);
            }
        }
    }
}