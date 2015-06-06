package com.ponnex.justdrive;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationServices;


/**
 * Created by ramos on 4/14/2015.
 */

public class CoreService extends Service implements ConnectionCallbacks, OnConnectionFailedListener,SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int DETECTION_INT_MILLIS = 0;
    private GoogleApiClient mGoogleApiClient;
    private GPSManager gpsManager = null;

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

        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        if (switchstate()) {
            ServiceOn();
        }
        if (!switchstate()) {
            ServiceOff();
        }
        //keep the service running
        return Service.START_STICKY;
    }

    public void ServiceOn(){
        mGoogleApiClient.connect();
    }

    public void ServiceOff(){
        if (mGoogleApiClient.isConnected()) {
            Log.d(TAG, "Disconnected");
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, getActivityDetectionPendingIntent());
            mGoogleApiClient.disconnect();

            stopService(new Intent(CoreService.this, AppLockService.class));
            stopService(new Intent(CoreService.this, CallerService.class));

            if(gpsManager!=null){
                gpsManager.stopListening();
                gpsManager.setGPSCallback(null);
                gpsManager = null;
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, DETECTION_INT_MILLIS, getActivityDetectionPendingIntent());

        Log.d(TAG, "Connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Failed, Reconnecting...");
        mGoogleApiClient.connect();
    }

    private PendingIntent getActivityDetectionPendingIntent() {
        Log.d(TAG, "getActivityDetectionPendingIntent");
        Intent intent = new Intent(this, ActivityRecognitionIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "CS Destroyed");
        if(mGoogleApiClient.isConnected()) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, getActivityDetectionPendingIntent());
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(),
                this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(
                getApplicationContext(), 1, restartServiceIntent,
                PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext()
                .getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 500,
                restartServicePendingIntent);
        super.onTaskRemoved(rootIntent);
    }

    private boolean switchstate() {
        SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return (mSharedPreference.getBoolean("switch", true));
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("switch")) {
            if(switchstate()) {
                //connect
                ServiceOn();
            }
            if(!switchstate()) {
                //disconnect
                ServiceOff();
            }
        }
    }
}