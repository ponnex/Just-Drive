package com.ponnex.justdrive;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by EmmanuelFrancis on 5/23/2015.
 */

public class AppLockService extends Service {
    LockerThread lockerThread;
    boolean isInterrupted;
    private String TAG = "com.ponnex.justdrive.AppLockService";

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG + "_ALS", "Created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG + "_ALS", "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        isInterrupted = false;
        lockerThread = new LockerThread();
        lockerThread.start();
        return START_STICKY;
    }

    @SuppressWarnings("deprecation")
    class LockerThread extends Thread {
        private String getTopActivityPkgName() {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String topPackageName = "" ;

                UsageStatsManager mUsageStatsManager = (UsageStatsManager)getSystemService("usagestats");
                long time = System.currentTimeMillis();
                // We get usage stats for the last 10 seconds
                List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000*10, time);
                // Sort the stats by the last time used
                if(stats != null) {
                    SortedMap<Long,UsageStats> mySortedMap = new TreeMap<Long,UsageStats>();
                    for (UsageStats usageStats : stats) {
                        mySortedMap.put(usageStats.getLastTimeUsed(),usageStats);
                    }
                    if(mySortedMap != null && !mySortedMap.isEmpty()) {
                        topPackageName =  mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                    }
                }
                return topPackageName;
            }
            else {
                ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                //List<ActivityManager.RunningTaskInfo> runTask = activityManager.getRunningTasks(1);
                //return runTask.get(0).topActivity.getPackageName();
                return activityManager.getRunningTasks(1).get(0).topActivity.getPackageName();
            }
        }

        @Override
        public  void  run () {
            while (!isInterrupted) {
                SharedPreferences mSharedPreference1= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                Boolean isSwitch=(mSharedPreference1.getBoolean("switch", false));

                if(isSwitch) {
                    LockNotification();

                    String runPkgName = getTopActivityPkgName();

                    Log.d(TAG, "TopActivity: " + runPkgName);

                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    String currentHomePackage = resolveInfo.activityInfo.packageName;

                    String PACKAGE_NAME = BuildConfig.APPLICATION_ID;

                    if (!isSystemApp(runPkgName) && !currentHomePackage.equals(runPkgName) && !PACKAGE_NAME.equals(runPkgName)) {

                        Log.d(TAG, runPkgName + " process locked!");
                        if(!isServiceRunning(LockDialog.class)) {
                            startService(new Intent(AppLockService.this, LockDialog.class));
                        }

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

    private void LockNotification() {
        Log.d(TAG + "_ALS", "LockNotification");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());

        Intent intent = new Intent(getApplicationContext(), AboutDialog.class);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Set the title, text, and icon
        builder.setContentTitle(getString(R.string.app_name))
                .setContentText("App Lock Enabled")
                .setSmallIcon(R.drawable.ic_applock)
                .setOngoing(true)
                .setContentIntent(pendingIntent);

        // Get an instance of the Notification Manager
        NotificationManager notifyManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the notification and post it
        notifyManager.notify(0, builder.build());
    }

    @Override
    public void onDestroy() {
        isInterrupted = true;
        Log.i(TAG + "_ALS", "Destroy");
        super.onDestroy();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
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

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /*
    @SuppressWarnings("deprecation")
    public boolean isScreenOn() {
        if (Build.VERSION.SDK_INT>=20) {
            DisplayManager dm = (DisplayManager) getApplicationContext().getSystemService(Context.DISPLAY_SERVICE);
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    return true;
                }
            }
            return false;
        }
        else {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            return powerManager.isScreenOn();
        }
    }
    */
}
