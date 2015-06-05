package com.ponnex.justdrive;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by ramos on 4/14/2015.
 */

public class CoreService extends Service implements ConnectionCallbacks, OnConnectionFailedListener,SharedPreferences.OnSharedPreferenceChangeListener, LocationListener {

    private static final int DETECTION_INT_MILLIS = 0;
    private GoogleApiClient mGoogleApiClient;
    private GPSManager gpsManager = null;
    private PendingIntent mActivityDetectionPendingIntent;
    private LocalBroadcastManager broadcastManager;

    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    private LocationRequest locationRequest;
    private Location location;

    private float speed;

    private String TAG = "com.ponnex.justdrive.CoreService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();

        Log.d(TAG, "CS Created");

       broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(16);

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

            SharedPreferences isFirstRun_write = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = isFirstRun_write.edit();
            editor.putInt("count", 0);
            editor.apply();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, DETECTION_INT_MILLIS, getActivityDetectionPendingIntent());

        Location currentLocation = fusedLocationProviderApi.getLastLocation(mGoogleApiClient);
        if (currentLocation != null) {
            location = currentLocation;
            getSpeed(location);

        } else {
            fusedLocationProviderApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            // Schedule a Thread to unregister location listeners
            Executors.newScheduledThreadPool(1).schedule(new Runnable() {
                @Override
                public void run() {
                    fusedLocationProviderApi.removeLocationUpdates(mGoogleApiClient, CoreService.this);
                }
            }, 60000, TimeUnit.MILLISECONDS);
        }

        Log.d(TAG, "Connected");
    }

    @Override
    public void onLocationChanged(Location location) {
        getSpeed(location);
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
        // Reuse the PendingIntent if we already have it.
        if (mActivityDetectionPendingIntent != null) {
            return mActivityDetectionPendingIntent;
        }
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

        if (switchstate()) {
            Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
            restartServiceIntent.setPackage(getPackageName());
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

    private void getSpeed(Location location) {
        speed = location.getSpeed();
        Log.d(TAG, "Speed: " + speed);
        sendSpeed(speed);
    }

    public void sendSpeed (Float speed) {
        Intent intent = new Intent("com.bloxt.ponnex.guard.CoreService");
        intent.putExtra("setSpeed", speed);
        broadcastManager.sendBroadcast(intent);
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