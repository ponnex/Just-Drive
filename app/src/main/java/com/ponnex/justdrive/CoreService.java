package com.ponnex.justdrive;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.mingle.headsUp.HeadsUp;
import com.mingle.headsUp.HeadsUpManager;

import java.util.List;

/**
 * Created by ramos on 4/14/2015.
 */

public class CoreService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int MILLIS_PER_SEC = 0;
    private static final int DETECTION_INT_SEC = 0;
    private static final int DETECTION_INT_MILLIS = MILLIS_PER_SEC * DETECTION_INT_SEC; //change to variable type next update -- let user pick detection time from a drop down menu, will be added to Debugging Mode
    private GoogleApiClient mGoogleApiClient;
    private PendingIntent pendingIntent;
    private Boolean second = false;
    SharedPreferences sharedPrefs;
    private static int audioMode;
    AudioManager current;
    LockerThread lockerThread;
    boolean isInterrupted;
    private Handler lockscreenHandler = new Handler();
    private Runnable lockrun;
    int TIMER_COUNT = 15000;
    private Boolean activated = false;

    private String TAG = "com.ponnex.justdrive.CoreService";

    //required by the service, keep service running in the background
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        isInterrupted = true;
        lockerThread = new LockerThread();
        lockerThread.start();

        Log.d(TAG, "LS Created");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        prefs.registerOnSharedPreferenceChangeListener(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
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

        Intent intent1 = new Intent(this, ActivityRecognitionIntentService.class);
        pendingIntent = PendingIntent.getService(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(activityReceiver, new IntentFilter("com.ponnex.justdrive.ActivityRecognitionIntentService1"));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(cancelReceiver, new IntentFilter("com.ponnex.justdrive.NotificationReceiver"));
        current = (AudioManager) this.getSystemService(Service.AUDIO_SERVICE);
    }

    private BroadcastReceiver cancelReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Boolean isCancel = intent.getBooleanExtra("isCancel", false);

            SharedPreferences isCancelup = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = isCancelup.edit();
            editor.putBoolean("isCancelup", isCancel);
            editor.apply();

            if (isCancel){

                ServiceOff();
                sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor2 = sharedPrefs.edit();
                editor2.putBoolean("switch", false);
                editor2.apply();

                Intent intent5 = new Intent();
                try {
                    pendingIntent.send(getApplicationContext(), 0, intent5);
                } catch (PendingIntent.CanceledException e) {
                    Log.d(TAG,"Error on PendingIntent5");
                }

                try {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.cancel(1);
                }catch (RuntimeException e){
                    e.printStackTrace();
                }

            }
        }
    };

    void soundMode() {
        if (audioMode == 1) {
            vibrate();
        } else if (audioMode == 2) {
            normal();
        } else {
            silent();
        }
    }

    void silent() {
        final AudioManager mode = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        // Silent Mode
        mode.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    void vibrate() {
        final AudioManager mode = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        // vibrate mode
        mode.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
    }

    void normal() {
        final AudioManager mode = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        // Normal Mode
        mode.setStreamVolume(AudioManager.STREAM_RING, mode.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
        mode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }

    public void ServiceOn(){
        mGoogleApiClient.connect();

        //to make sure
        second = false;

        SharedPreferences isHeadsup = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor2 = isHeadsup.edit();
        editor2.putBoolean("headsup", false);
        editor2.apply();

        SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean isDebugtest = (mSharedPreference1.getBoolean("debugmode", false));
        if(isDebugtest) {
            isInterrupted = false;
            lockerThread = new LockerThread();
            lockerThread.start();

            if (!isServiceRunning()) {
                startService(new Intent(CoreService.this, TelephonyService.class));
            }
        }
    }

    public void ServiceOff(){
        if (mGoogleApiClient.isConnected()) {
            Log.d(TAG, "Disconnected");
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, pendingIntent);
            mGoogleApiClient.disconnect();
        }

        //cancel runnable for the lockscreen
        lockscreenHandler.removeCallbacks(lockrun);

        //to make sure that it is reset back to zero
        SharedPreferences isCountup = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor1 = isCountup.edit();
        editor1.putInt("isCount", 0);
        editor1.apply();

        SharedPreferences isHeadsup = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor2 = isHeadsup.edit();
        editor2.putBoolean("headsup", false);
        editor2.apply();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);

        NotificationManager notificationManager1 = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager1.cancel(1);

        isInterrupted = true;
    }

    private BroadcastReceiver activityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Boolean isSwitch=(mSharedPreference.getBoolean("switch", true));

            if(isSwitch)
            {
                String activity = intent.getStringExtra("ActivityOnly");

                SharedPreferences isActivity = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = isActivity.edit();
                editor.putString("isActivity", activity);
                editor.apply();


                //if (activity.equals("Tilting") && !second) { //debugging code
                if ((activity.equals("In Vehicle") && !second) || (activity.equals("On Bicycle") && !second)) {

                    //counter for the in vehicle, if it is detected 3 times
                    int count = 0;

                    SharedPreferences mSharedPreference1= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    Integer defaultValue=(mSharedPreference1.getInt("isCount", count));
                    ++defaultValue;
                    SharedPreferences isCountup = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor1 = isCountup.edit();
                    editor1.putInt("isCount", defaultValue);
                    editor1.apply();
                    count = (mSharedPreference1.getInt("isCount",count));
                    Log.d(TAG, "VALUE = " + count);

                    SharedPreferences mSharedPreference2= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    Boolean isHeadsup=(mSharedPreference2.getBoolean("headsup", false));

                    if (count == 3) {
                        if (!activated && !isHeadsup) {
                            headsup();
                        }

                        audioMode = current.getRingerMode();

                        normal();

                        launchLockwithtimer();

                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                soundMode();
                            }
                        }, 9000);

                        //counter for the in vehicle, if it is detected 3 times, if it detects still and etc if the value is < 3 then reset it back to 0
                        SharedPreferences isCountup2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor2 = isCountup2.edit();
                        editor2.putInt("isCount", 0);
                        editor2.apply();

                    }
                }

                //if (activity.equals("Tilting") && second) { //debugging code
                if ((activity.equals("In Vehicle") && second) || (activity.equals("On Bicycle") && second)) {
                    try {
                        activated = true;

                        isInterrupted = false;
                        lockerThread = new LockerThread();
                        lockerThread.start();

                        if (!isServiceRunning()) {
                            startService(new Intent(CoreService.this, TelephonyService.class));
                        }

                        try {
                            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            notificationManager.cancel(1);
                        }catch (RuntimeException e){
                            e.printStackTrace();
                        }

                        //to make sure that the count will reset back to 0
                        SharedPreferences isCountup1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor2 = isCountup1.edit();
                        editor2.putInt("isCount", 0);
                        editor2.apply();
                    }
                    catch (RuntimeException e){
                        e.printStackTrace();
                        Log.d(TAG, "layout has already been added");
                    }
                }

                //if (activity.equals("Still")) { //debugging code
                if (activity.equals("Still") || activity.equals("On Foot") || activity.equals("Running") || activity.equals("Walking")) {

                    SharedPreferences mSharedPreference5= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    Boolean isdebug = (mSharedPreference5.getBoolean("debugmode", false));
                    if(!isdebug) {
                        isInterrupted = true;
                        activated = false;

                        if (isServiceRunning()) {
                            stopService(new Intent(context, TelephonyService.class));
                        }
                    }

                    //cancel runnable for the lockscreen
                    lockscreenHandler.removeCallbacks(lockrun);

                    //counter for the in vehicle, if it is detected 3 times, if it detects still and etc if the value is < 3 then reset it back to 0
                    SharedPreferences isCountup2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor2 = isCountup2.edit();
                    editor2.putInt("isCount", 0);
                    editor2.apply();

                    if (second) {

                        //counter for the still and etc, if it is detected 10 times then reset second back to false to show the headsup again and notify the user
                        int count1 = 0;

                        SharedPreferences mSharedPreference2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        Integer defaultValue = (mSharedPreference2.getInt("isCount1", count1));
                        ++defaultValue;
                        SharedPreferences isCountup = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor3 = isCountup.edit();
                        editor3.putInt("isCount1", defaultValue);
                        editor3.apply();
                        count1 = (mSharedPreference2.getInt("isCount1", count1));
                        Log.d(TAG, "VALUE1 = " + count1);

                        if (count1 >= 7) {
                            second = false;
                            //counter for the still, if it is detected 10 times, reset back to 0 when it return second to false
                            SharedPreferences isCountup3 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor4 = isCountup3.edit();
                            editor4.putInt("isCount1", 0);
                            editor4.apply();

                            SharedPreferences isHeadsup = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor5 = isHeadsup.edit();
                            editor5.putBoolean("headsup", false);
                            editor5.apply();
                        }
                    }

                    try {
                        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        notificationManager.cancel(1);
                    }catch (RuntimeException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, DETECTION_INT_MILLIS, pendingIntent);
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
    public int onStartCommand(Intent intent,int flags,int startId){
        //keep the service running
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy(){
        Log.d(TAG, "LS Destroyed");
        isInterrupted = true;
        if(mGoogleApiClient.isConnected()) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, pendingIntent);
            mGoogleApiClient.disconnect();
        }
        try {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(activityReceiver);
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(cancelReceiver);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        if(isServiceRunning()){
            stopService(new Intent(getApplicationContext(), TelephonyService.class));
        }

        SharedPreferences isCountup2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor2 = isCountup2.edit();
        editor2.putInt("isCount", 0);
        editor2.apply();

        super.onDestroy();
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (TelephonyService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void headsup(){

        audioMode = current.getRingerMode();

        SharedPreferences isHeadsup = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor1 = isHeadsup.edit();
        editor1.putBoolean("headsup", true);
        editor1.apply();

        HeadsUpManager manage = HeadsUpManager.getInstant(getApplication());
        HeadsUp.Builder builder = new HeadsUp.Builder(CoreService.this);

        builder.setContentTitle("Are you driving?")
                .setTicker("Just Drive needs your attention")
                .setDefaults(Notification.DEFAULT_SOUND)
                        //To display the notification bar notification, this must be set
                .setSmallIcon(R.drawable.ic_driving)
                .setContentText("Ignore or Dismiss if you're Driving")
                        //2.3 ?To set this parameter set, will be responsible for the error
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, false)
                .setPriority(1)
                        //Set whether to display the action buttons
                .setUsesChronometer(true)
                .addAction(R.drawable.ic_cancel, "I'm a passenger", notDriving());

        HeadsUp headsUp = builder.buildHeadUp();
        manage.notify(1, headsUp);
    }

    PendingIntent notDriving() {
        Intent i = new Intent(getBaseContext(), NotificationReceiver.class);
        return PendingIntent.getBroadcast(getApplicationContext(), 0, i, 0);
    }

    private void launchLockwithtimer(){
        Log.d(TAG, "launchLockwithtimer");
        lockrun = new Runnable() {
            public void run() {
                try {
                    SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    Boolean isCancelup=(mSharedPreference.getBoolean("isCancelup", false));

                    SharedPreferences mSharedPreference1= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    Boolean isSwitch=(mSharedPreference1.getBoolean("switch", false));

                    SharedPreferences mSharedPreference2= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String isActivity=(mSharedPreference2.getString("isActivity", "Unknown"));

                    second = true;

                    //if(isSwitch && isActivity.equals("Tilting")) { //debugging code
                    if(isSwitch && (isActivity.equals("In Vehicle")||isActivity.equals("On Bicycle"))) {
                        if (!isCancelup) {
                            activated = true;
                            isInterrupted = false;
                            lockerThread = new LockerThread();
                            lockerThread.start();

                            if (!isServiceRunning()) {
                                startService(new Intent(CoreService.this, TelephonyService.class));
                            }

                            try {
                                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                notificationManager.cancel(1);
                            }catch (RuntimeException e){
                                e.printStackTrace();
                            }

                        } else {
                            SharedPreferences isCancelup1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = isCancelup1.edit();
                            editor.putBoolean("isCancelup", false);
                            editor.apply();

                            //counter for the in vehicle, if it is detected 3 times, if it detects still and etc if the value is < 3 then reset it back to 0
                            SharedPreferences isCountup2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor2 = isCountup2.edit();
                            editor2.putInt("isCount", 0);
                            editor2.apply();
                        }
                    }
                }catch (RuntimeException e){
                    e.printStackTrace();
                }
            }
        };
        lockscreenHandler.postDelayed(lockrun, TIMER_COUNT);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("switch")) {
            SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Boolean isSwitch = (mSharedPreference.getBoolean("switch", true));
            if(isSwitch) {
                ServiceOn();
            }
            if(!isSwitch) {
                ServiceOff();
            }
        }

        if (key.equals("debugmode")){
            SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Boolean debugmode = (mSharedPreference.getBoolean("debugmode", false));
            if (debugmode){
                isInterrupted = false;
                lockerThread = new LockerThread();
                lockerThread.start();

                if (!isServiceRunning()) {
                    startService(new Intent(CoreService.this, TelephonyService.class));
                }
            }
            if(!debugmode){
                isInterrupted = true;

                if (isServiceRunning()) {
                    stopService(new Intent(CoreService.this, TelephonyService.class));
                }
            }
        }
    }

    class LockerThread extends Thread
    {
        private String getTopActivityPkgName()
        {
            ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> runTask = activityManager.getRunningTasks(1);
            return runTask.get(0).topActivity.getPackageName();
        }

        @Override
        public  void  run ()
        {
            while (!isInterrupted)
            {
                SharedPreferences mSharedPreference1= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                Boolean isSwitch=(mSharedPreference1.getBoolean("switch", false));

                if(isSwitch) {
                    String runPkgName = getTopActivityPkgName();

                    Log.d(TAG, "TopActivity: " + runPkgName);

                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    String currentHomePackage = resolveInfo.activityInfo.packageName;

                    String PACKAGE_NAME = BuildConfig.APPLICATION_ID;

                    if (!isSystemApp(runPkgName) && !currentHomePackage.equals(runPkgName) && !PACKAGE_NAME.equals(runPkgName)) {

                        Log.d(TAG, runPkgName + " process locked!");

                        Intent dialogintent = new Intent(CoreService.this, AppLockActivity.class);
                        dialogintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(dialogintent);

                    } else {
                        Log.d(TAG, runPkgName + " process is exclude!");
                    }

                    Log.d(TAG, "================== CYCLE ====================");

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public boolean isSystemApp(String packageName) {
        PackageManager mPackageManager = getPackageManager();
        try {
            // Get packageinfo for target application
            PackageInfo targetPkgInfo = mPackageManager.getPackageInfo(
                    packageName, PackageManager.GET_SIGNATURES);
            // Get packageinfo for system package
            PackageInfo sys = mPackageManager.getPackageInfo(
                    "android", PackageManager.GET_SIGNATURES);
            // Match both packageinfo for there signatures
            return (targetPkgInfo != null && targetPkgInfo.signatures != null && sys.signatures[0]
                    .equals(targetPkgInfo.signatures[0]));
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}