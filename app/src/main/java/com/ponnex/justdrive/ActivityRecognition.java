package com.ponnex.justdrive;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * Created by ramos on 4/13/2015.
 */

public class ActivityRecognition extends IntentService {

    private String TAG = "com.ponnex.justdrive.ActivityRecognition";

    public ActivityRecognition(String name) {
        super(name);
    }

    public ActivityRecognition() {
        super("Activity Recognition");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG + "_ARIS", "Created");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();
            int confidence = mostProbableActivity.getConfidence();
            int activityType = mostProbableActivity.getType();

            SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Boolean debug = (mSharedPreference.getBoolean("debug", false));

            if (switchstate() && !debug) {
                String activityName = getNameFromType(activityType);
                Log.d(TAG + "_HAS RESULT -->", activityName + ", " + confidence + "% ");

                if (activityName.equals("In Vehicle") || activityName.equals("On Bicycle")) {
                    if (confidence >= 75) {
                        //start applock service
                        startAppLock();
                    }
                }

                else if (activityName.equals("Still") || activityName.equals("On Foot") || activityName.equals("Running") || activityName.equals("Walking")) {
                    if (confidence >= 50) {
                        // /disable applock service
                        stopAppLock();
                    }
                }

                else {
                    Log.d(TAG, "UNKNOWN OR TILTING");
                }
            }
        }
    }

    private String getNameFromType(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "In Vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "On Bicycle";
            case DetectedActivity.ON_FOOT:
                return "On Foot";
            case DetectedActivity.STILL:
                return "Still";
            case DetectedActivity.UNKNOWN:
                return "Unknown";
            case DetectedActivity.RUNNING:
                return "Running";
            case DetectedActivity.WALKING:
                return "Walking";
            case DetectedActivity.TILTING:
                return "Tilting";
        }
        return "Unknown";
    }

    private void startAppLock() {
        if(!isServiceRunning(AppLockService.class)) {
            startService(new Intent(ActivityRecognition.this, AppLockService.class));
        }
        if(!isServiceRunning(CallerService.class)){
            startService(new Intent(ActivityRecognition.this, CallerService.class));
        }
    }

    private void stopAppLock() {
        if(isServiceRunning(AppLockService.class)) {
            stopService(new Intent(ActivityRecognition.this, AppLockService.class));
        }
        if(isServiceRunning(CallerService.class)){
            stopService(new Intent(ActivityRecognition.this, CallerService.class));
        }
    }

    private boolean switchstate(){
        Boolean isSwitch;
        SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        isSwitch = (mSharedPreference.getBoolean("switch", true));
        return isSwitch;
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
    public void onDestroy(){
        Log.d(TAG + "_ARIS", "Destroyed");
        super.onDestroy();
    }
}