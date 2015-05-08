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
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ponnex.justdrive.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

/**
 * Created by ramos on 4/14/2015.
 */

public class LockScreen extends Service implements View.OnClickListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int MILLIS_PER_SEC = 0;
    private static final int DETECTION_INT_SEC = 0;
    private static final int DETECTION_INT_MILLIS = MILLIS_PER_SEC * DETECTION_INT_SEC; //change to variable type next update -- let user pick detection time from a drop down menu, will be added to Debugging Mode
    private GoogleApiClient mGoogleApiClient;
    private PendingIntent pendingIntent;
    WindowManager windowManager;
    WindowManager.LayoutParams layoutparams;
    RelativeLayout relativeLayout;
    ImageButton ok;
    private Boolean showing = false;
    private Boolean showingtest = false;
    private Boolean second = false;
    SharedPreferences sharedPrefs;
    private Handler lockscreenHandler = new Handler();
    private Runnable lockrun;
    int TIMER_COUNT = 15000;
    private static int audioMode;
    AudioManager current;

    //required by the service, keep service running in the background
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate(){
        super.onCreate();
        Log.e("TYPE", "LS Created");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean isSwitch=(mSharedPreference.getBoolean("isSwitch", true));

        if (isSwitch) {
            ServiceOn();
        }
        if (!isSwitch) {
            ServiceOff();
        }
        Intent intent1 = new Intent(this, ActivityRecognitionIntentService.class);
        pendingIntent = PendingIntent.getService(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(screenReceiver, new IntentFilter("com.ponnex.justdrive.ActivityRecognitionIntentService1"));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(switchReceiver, new IntentFilter("com.ponnex.justdrive.MainActivity"));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(switchReceiver1, new IntentFilter("com.ponnex.justdrive.SettingsFragment"));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(cancelReceiver, new IntentFilter("com.ponnex.justdrive.NotificationReceiver"));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(testReceiver, new IntentFilter("com.ponnex.justdrive.DebuggingActivity"));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(testReceiver1, new IntentFilter("com.ponnex.justdrive.DebuggingActivity1"));
        setUpLayout();
        gpsNotification();
        current = (AudioManager) this.getSystemService(Service.AUDIO_SERVICE);
    }


    private BroadcastReceiver switchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Boolean isSwitch=(mSharedPreference.getBoolean("isSwitch", true));

            Boolean SwitchVal = intent.getBooleanExtra("SwitchVal", isSwitch);

            if(SwitchVal) {
                ServiceOn();
            }
            if(!SwitchVal) {
                ServiceOff();
            }
        }
    };

    private BroadcastReceiver switchReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Boolean isSwitch=(mSharedPreference.getBoolean("isSwitch", true));

            Boolean SwitchVal = intent.getBooleanExtra("SwitchVal", isSwitch);

            if(SwitchVal) {
                ServiceOn();
            }
            if(!SwitchVal) {
                ServiceOff();
            }

        }
    };

    private BroadcastReceiver cancelReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Boolean isCancel = intent.getBooleanExtra("isCancel", false);

            SharedPreferences isCancelup = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = isCancelup.edit();
            editor.putBoolean("isCancelup", isCancel);
            editor.apply();

            SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(context);
            Boolean isTEST = (mSharedPreference.getBoolean("isTEST", false));

            if (isCancel){

                if (!isTEST) {
                    ServiceOff();
                    sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor2 = sharedPrefs.edit();
                    editor2.putBoolean("isSwitch", false);
                    editor2.apply();
                }

                Intent intent5 = new Intent();
                try {
                    pendingIntent.send(getApplicationContext(), 0, intent5);
                } catch (PendingIntent.CanceledException e) {
                    Log.e("TYPE","Error on PendingIntent5");
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

    private BroadcastReceiver testReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Boolean isTest= intent.getBooleanExtra("TestButton", false);
            if (isTest && !showing){
                launchLockwithtimerTEST();
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

    private BroadcastReceiver testReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Boolean isTest= intent.getBooleanExtra("TestButton1", false);

            SharedPreferences mSharedPreference2= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Boolean isHeadsup=(mSharedPreference2.getBoolean("headsup", false));

            if (isTest && !showing && !isHeadsup){
                headsupTEST();
            }
        }
    };

    private void ServiceOn(){
        mGoogleApiClient.connect();

        //to make sure
        second = false;

        SharedPreferences isHeadsup = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor2 = isHeadsup.edit();
        editor2.putBoolean("headsup", false);
        editor2.apply();
    }

    private void ServiceOff(){
        if (mGoogleApiClient.isConnected()) {
            Log.e("TYPE", "Disconnected");
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
    }

    private BroadcastReceiver screenReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Boolean isSwitch=(mSharedPreference.getBoolean("isSwitch", true));

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
                    Log.e("TYPE", "VALUE = " + count);

                    SharedPreferences mSharedPreference2= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    Boolean isHeadsup=(mSharedPreference2.getBoolean("headsup", false));

                    if (count == 3) {
                        if (!showing && !isHeadsup) {
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
                        ///launchLock
                        windowManager.addView(relativeLayout, layoutparams);
                        showing = true;
                        if (!isServiceRunning()) {
                            startService(new Intent(LockScreen.this, CarMode.class));
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
                        Log.e("TYPE window manager", "layout has already been added");
                    }
                }

                //if (activity.equals("Still")) { //debugging code
                if (activity.equals("Still") || activity.equals("On Foot") || activity.equals("Running") || activity.equals("Walking")) {

                    if (!showingtest) {
                        removeLock();
                    }

                    if (isServiceRunning()) {
                        stopService(new Intent(context, CarMode.class));
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
                        Log.e("TYPE", "VALUE1 = " + count1);

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
        Log.e("TYPE", "Connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("TYPE", "Suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("TYPE", "Failed, Reconnecting...");
        mGoogleApiClient.connect();
    }

    private void gpsNotification() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Create a notification builder that's compatible with platforms >= version 4
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(getApplicationContext());
            Intent gpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            PendingIntent gpsPI = PendingIntent.getActivity(getApplicationContext(), 0, gpsIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            // Set the title, text, and icon
            builder.setContentTitle(getString(R.string.app_name))
                    .setContentText("Turn on GPS for better accuracy")
                    .setSmallIcon(R.drawable.ic_notification)

                            // Get the Intent that starts the Location settings panel
                    .setContentIntent(gpsPI);

            // Get an instance of the Notification Manager
            NotificationManager notifyManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);

            // Build the notification and post it
            notifyManager.notify(0, builder.build());
        }
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        //keep the service running
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy(){
        Log.e("TYPE", "LS Destroyed");
        if(mGoogleApiClient.isConnected()) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, pendingIntent);
            mGoogleApiClient.disconnect();
        }
        try {
        unregisterReceiver(screenReceiver);
        unregisterReceiver(switchReceiver);
        unregisterReceiver(cancelReceiver);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        if(isServiceRunning()){
            stopService(new Intent(getApplicationContext(), CarMode.class));
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
            if (CarMode.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private void setUpLayout(){

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutparams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.RGBA_8888
        );

        relativeLayout = new RelativeLayout(this);
        relativeLayout.setBackgroundResource(R.drawable.back);
        int paddingPix=dpToPx(20);
        RelativeLayout.LayoutParams ok_param = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        ok_param.addRule(RelativeLayout.CENTER_HORIZONTAL);
        ok_param.addRule(RelativeLayout.CENTER_IN_PARENT);
        ok_param.setMargins(dpToPx(12), dpToPx(6), 0, 0);
        ok=new ImageButton(this);
        ok.setId(R.id.ok);
        ok.setImageResource(R.drawable.ic_lockscreen_ok);
        ok.setBackgroundColor(Color.parseColor("#00000000"));
        ok.setPadding(paddingPix, paddingPix, paddingPix, paddingPix);
        ok.setLayoutParams(ok_param);
        ok.setOnClickListener(this);
        relativeLayout.addView(ok);
    }

    private void headsup(){

        audioMode = current.getRingerMode();

        SharedPreferences isHeadsup = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor1 = isHeadsup.edit();
        editor1.putBoolean("headsup", true);
        editor1.apply();

        HeadsUpManager manage = HeadsUpManager.getInstant(getApplication());
        HeadsUp.Builder builder = new HeadsUp.Builder(LockScreen.this);

        long pattern[]={0, 3000};
        builder.setContentTitle("Are you driving?")
                .setTicker("Just Drive needs your attention")
                .setDefaults(Notification.DEFAULT_SOUND)
                        //To display the notification bar notification, this must be set
                .setSmallIcon(R.drawable.driver)
                .setContentText("Ignore or Dismiss if you're Driving")
                        //2.3 ?To set this parameter set, will be responsible for the error
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true)
                .setVibrate(pattern)
                .setAutoCancel(false)
                .setPriority(1)
                        //Set whether to display the action buttons
                .setUsesChronometer(true)
                .addAction(R.drawable.cancel, "I'm a passenger", notDriving());

        HeadsUp headsUp = builder.buildHeadUp();
        manage.notify(1, headsUp);
    }

    private void headsupTEST(){

        audioMode = current.getRingerMode();

        HeadsUpManager manage = HeadsUpManager.getInstant(getApplication());
        HeadsUp.Builder builder = new HeadsUp.Builder(LockScreen.this);

        SharedPreferences isTEST = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = isTEST.edit();
        editor.putBoolean("isTEST", true);
        editor.apply();

        long pattern[]={0, 3000};
        builder.setContentTitle("Are you driving?")
                .setTicker("Just Drive needs your attention")
                .setDefaults(Notification.DEFAULT_SOUND)
                //To display the notification bar notification, this must be set
                .setSmallIcon(R.drawable.driver)
                .setContentText("Ignore or Dismiss if you're Driving")
                        //2.3 ?To set this parameter set, will be responsible for the error
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true)
                .setVibrate(pattern)
                .setAutoCancel(false)
                .setPriority(1)
                        //Set whether to display the action buttons
                .setUsesChronometer(true)
                .addAction(R.drawable.cancel, "I'm a passenger", notDriving());

        HeadsUp headsUp = builder.buildHeadUp();
        manage.notify(1, headsUp);
    }

    private void JustdriveNotification() {

        // Create a notification builder that's compatible with platforms >= version 4
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext());
        // Set the title, text, and icon
        builder.setContentTitle("Just Drive")
                .setContentText("No text, tweet, Facebook update, or email is worth your life. Put the phone down and Just Drive")
                .setSmallIcon(R.drawable.driver)
                .setPriority(1);
                        // Get the Intent that starts the Location settings panel
        // Get an instance of the Notification Manager
        NotificationManager notifyManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the notification and post it
        notifyManager.notify(0, builder.build());
    }

    PendingIntent notDriving() {
        Intent i = new Intent(getBaseContext(), NotificationReceiver.class);
        return PendingIntent.getBroadcast(getApplicationContext(), 0, i, 0);
    }

    private void launchLockwithtimer(){
        Log.e("TYPE","launchLockwithtimer");
        lockrun = new Runnable() {
            public void run() {
                try {
                    SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    Boolean isCancelup=(mSharedPreference.getBoolean("isCancelup", false));

                    SharedPreferences mSharedPreference1= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    Boolean isSwitch=(mSharedPreference1.getBoolean("isSwitch", false));

                    SharedPreferences mSharedPreference2= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String isActivity=(mSharedPreference2.getString("isActivity", "Unknown"));

                    second = true;

                    //if(isSwitch && isActivity.equals("Tilting")) { //debugging code
                    if(isSwitch && (isActivity.equals("In Vehicle")||isActivity.equals("On Bicycle"))) {
                        if (!isCancelup) {
                            showing = true;
                            windowManager.addView(relativeLayout, layoutparams);
                            if (!isServiceRunning()) {
                                startService(new Intent(LockScreen.this, CarMode.class));
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

    private void launchLockwithtimerTEST(){
        Log.e("TYPE", "launchLockwithtimerTEST");

        try {
            showingtest = true;
            windowManager.addView(relativeLayout, layoutparams);

            Toast toast;
            toast = Toast.makeText(getBaseContext(), "Click Image to Dismiss", Toast.LENGTH_LONG);
            toast.show();

            if (!isServiceRunning()) {
                startService(new Intent(LockScreen.this, CarMode.class));
            }
        }

        catch(RuntimeException e) {
            e.printStackTrace();
        }
    }

    private void removeLock(){

        if(showing || showingtest){
            windowManager.removeView(relativeLayout);
            showing = false;
            showingtest = false;

            SharedPreferences isMsgfrom = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = isMsgfrom.edit();
            editor.putString("isMsgfrom", null);
            editor.apply();
        }
    }

    @Override
    public void onClick(View view) {
        int id=view.getId();
        if (id==R.id.ok)
        {
            removeLock();

            if (isServiceRunning()) {
                stopService(new Intent(getApplicationContext(), CarMode.class));
            }

            //to make sure
            second = false;

            //cancel runnable for the lockscreen
            lockscreenHandler.removeCallbacks(lockrun);

            //counter for the in vehicle, if it is detected 3 times, if it detects still and etc if the value is < 3 then reset it back to 0
            SharedPreferences isCountup2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor2 = isCountup2.edit();
            editor2.putInt("isCount", 0);
            editor2.apply();

            try {
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancel(1);
            }catch (RuntimeException e){
                e.printStackTrace();
            }
        }
    }
}