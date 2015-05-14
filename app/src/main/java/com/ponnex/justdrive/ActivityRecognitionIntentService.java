package com.ponnex.justdrive;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * Created by ramos on 4/13/2015.
 */

public class ActivityRecognitionIntentService extends IntentService {

    private LocalBroadcastManager broadcastManager;

    private String TAG = "com.ponnex.justdrive.ActivityRecognitionIntentService";

    public ActivityRecognitionIntentService(String name) {
        super(name);
    }

    public ActivityRecognitionIntentService() {
        super("Activity Recognition");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "ARIS Created");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        broadcastManager = LocalBroadcastManager.getInstance(this);
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result =
                    ActivityRecognitionResult.extractResult(intent);
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();
            int confidence = mostProbableActivity.getConfidence();
            int activityType = mostProbableActivity.getType();

            SharedPreferences mSharedPreference1= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Boolean isFirstRun=(mSharedPreference1.getBoolean("isFirstRun", true));

            if (!isFirstRun) {
                String activityName = getNameFromType(activityType);
                String resultstr = "Activity: " + activityName + ", Confidence: " + confidence + "% ";
                Log.d(TAG, "Activity: " + activityName + ", Confidence: " + confidence + "% ");

                sendResultInfo(resultstr);
                if (confidence >= 50) {
                    //if (activityName.equals("Still")) { //debugging code
                    if (activityName.equals("Still") || activityName.equals("On Foot") || activityName.equals("Running") || activityName.equals("Walking")) {
                        sendResult(activityName);
                    }
                }

                if (confidence >= 75) {
                    //if (activityName.equals("Tilting")) { //debugging code
                    if (activityName.equals("In Vehicle") || activityName.equals("On Bicycle")) {
                        sendResult(activityName);
                    }
                }
                // shows a notification with the current activity in it, to be checked on the debugging mode
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                if (prefs.getBoolean("notification", false)) {
                    sendNotification("Activity: " + activityName.toLowerCase() + ", Confidence: " + confidence + "% ");
                }
            }
        }
    }

    public void sendResultInfo(String message) {
        Intent intent = new Intent("com.ponnex.justdrive.ActivityRecognitionIntentService");
        if (message != null) {
            intent.putExtra("Activity", message);
        }
        broadcastManager.sendBroadcast(intent);
    }

    public void sendResult(String activity) {
        Intent intent = new Intent("com.ponnex.justdrive.ActivityRecognitionIntentService1");
        if (activity != null) {
            intent.putExtra("ActivityOnly", activity);
        }
        broadcastManager.sendBroadcast(intent);
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

    private void sendNotification(String activity) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext());

        // Set the title, text, and icon
        builder.setContentTitle(getString(R.string.app_name))
                .setContentText(activity)
                .setSmallIcon(R.drawable.ic_result);

        // Get an instance of the Notification Manager
        NotificationManager notifyManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the notification and post it
        notifyManager.notify(0, builder.build());
    }

    @Override
    public void onDestroy(){
        Log.d(TAG, "ARIS Destroyed");
        super.onDestroy();
    }
}