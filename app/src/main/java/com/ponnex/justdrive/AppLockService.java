package com.ponnex.justdrive;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.display.DisplayManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by EmmanuelFrancis on 5/23/2015.
 */

public class AppLockService extends Service implements GPSCallback {
    AlertDialog LockalertDialog;
    AlertDialog AboutalertDialog;
    AlertDialog SpeedalertDialog;
    boolean background = false;
    boolean LOCKshowing = false;
    boolean ABOUTshowing = false;
    boolean SPEEDshowing = false;
    boolean showdialog = false;
    private BroadcastReceiver mScreenReceiver;

    private GPSManager gpsManager = null;
    private double speed = 0.0;
    String speedString;

    private String TAG = "com.ponnex.justdrive.AppLockService";

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
        super.onCreate();
        LockNotification();

        mScreenReceiver = new ScreenReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenReceiver, filter);

        Log.i(TAG + "_ALS", "Created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG + "_ALS", "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        mHandler.postDelayed(looperTask, 100);
        return START_STICKY;
    }

    private final class ScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                Log.i(TAG, "Screen ON");
                mHandler.postDelayed(looperTask, 100);
            }
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Log.i(TAG, "Screen OFF");
                mHandler.removeCallbacks(looperTask);
            }
        }
    }

    @SuppressWarnings("deprecation")
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
            return activityManager.getRunningTasks(1).get(0).topActivity.getPackageName();
        }
    }

    private Handler mHandler = new Handler();
    private Runnable looperTask = new Runnable() {
        public void run() {
            Log.v(TAG,"LOOP");
            new AppLockTask().execute();
            mHandler.postDelayed(looperTask, 1500);
        }
    };

    private class AppLockTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
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
                    if(!LOCKshowing) {
                        showdialog = true;
                    }
                }
                else {
                    Log.d(TAG, runPkgName + " process is exclude!");
                    showdialog = false;
                    if(LOCKshowing) {
                        LockalertDialog.dismiss();
                    }
                }
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            if(showdialog && !LOCKshowing && !ABOUTshowing && !SPEEDshowing) {
                LockDialog();
            }
        }
    }

    private void LockDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getApplicationContext(), R.style.AppCompatAlertDialogStyle));
        LockalertDialog = builder.create();
        LockalertDialog.setTitle(getText(R.string.dialog_title));
        LockalertDialog.setMessage(getText(R.string.dialog_message));
        LockalertDialog.setCanceledOnTouchOutside(false);
        LockalertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "QUIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Go to the Home screen
                Intent homeIntent = new Intent();
                homeIntent.setAction(Intent.ACTION_MAIN);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                getApplicationContext().startActivity(homeIntent);

                dialog.dismiss();
            }
        });
        LockalertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "MORE INFO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if(!ABOUTshowing) {
                    AboutDialog();
                }
            }
        });

        LockalertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    // Go to the Home screen
                    Intent homeIntent = new Intent();
                    homeIntent.setAction(Intent.ACTION_MAIN);
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    homeIntent.addCategory(Intent.CATEGORY_HOME);
                    getApplicationContext().startActivity(homeIntent);

                    dialog.dismiss();
                }
                return false;
            }
        });
        LockalertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Log.d(TAG, "LOCK SHOWING");
                LOCKshowing = true;
            }
        });
        LockalertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                LOCKshowing = false;
            }
        });
        LockalertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        LockalertDialog.show();

        final int accentcolor = getApplicationContext().getResources().getColor(R.color.accent);

        Button positive = LockalertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positive.setTextColor(accentcolor);

        Button negative = LockalertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negative.setTextColor(accentcolor);
    }

    private void AboutDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getApplicationContext(), R.style.AppCompatAlertDialogStyle));
        AboutalertDialog = builder.create();
        AboutalertDialog.setTitle(getText(R.string.about_title));
        AboutalertDialog.setMessage(getText(R.string.about_message));
        AboutalertDialog.setCanceledOnTouchOutside(false);
        AboutalertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "DISMISS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AboutalertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NOT DRIVING?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (!SPEEDshowing) {
                    SpeedDialog();
                }
                gpsManager = new GPSManager();
                gpsManager.startListening(getApplicationContext());
                gpsManager.setGPSCallback(AppLockService.this);
            }
        });
        AboutalertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ABOUTshowing = true;
            }
        });
        AboutalertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                ABOUTshowing = false;
            }
        });
        AboutalertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        AboutalertDialog.show();

        final int accentcolor = getApplicationContext().getResources().getColor(R.color.accent);

        Button negative = AboutalertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negative.setTextColor(accentcolor);

        Button positive = AboutalertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positive.setTextColor(accentcolor);
    }

    private void SpeedDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getApplicationContext(), R.style.AppCompatAlertDialogStyle));
        SpeedalertDialog = builder.create();
        SpeedalertDialog.setTitle("Speed Test");
        SpeedalertDialog.setMessage("Reading Current Speed...");
        SpeedalertDialog.setCanceledOnTouchOutside(false);
        SpeedalertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "BACKGROUND", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                background = true;
                dialog.dismiss();
            }
        });
        SpeedalertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                gpsManager.stopListening();
                gpsManager.setGPSCallback(null);
                gpsManager = null;
                dialog.dismiss();
            }
        });
        SpeedalertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    gpsManager.stopListening();
                    gpsManager.setGPSCallback(null);
                    gpsManager = null;
                    dialog.cancel();
                }
                return false;
            }
        });
        SpeedalertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                SPEEDshowing = true;
            }
        });
        SpeedalertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                SPEEDshowing = false;
            }
        });
        SpeedalertDialog.getWindow().setType(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        SpeedalertDialog.show();

        final int accentcolor = getApplicationContext().getResources().getColor(R.color.accent);

        Button Speedpositive = SpeedalertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Speedpositive.setTextColor(accentcolor);

        Button Speednegative = SpeedalertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        Speednegative.setTextColor(accentcolor);
    }

    @Override
    public void onGPSUpdate(Location location) {
        location.getLatitude();
        location.getLongitude();
        if(location.hasSpeed()) {
            speed = roundDecimal(convertSpeed(location.getSpeed()), 2);

            if(speed >= 30){
                speedString = "Current Speed: " + speed + " KM" + "\n\nJust Drive detected that you are running in a NON-SAFE speed.";

                TextView Speedmessage = (TextView) SpeedalertDialog.findViewById(android.R.id.message);
                Speedmessage.setText(speedString);
                Speedmessage.setGravity(Gravity.CENTER);

                stopService(new Intent(AppLockService.this, AppLockService.class));
                stopService(new Intent(AppLockService.this, CallerService.class));

            } else {
                speedString = "Current Speed: " + speed + " KM" + "\n\nJust Drive detected that you are running in a SAFE speed.  \n" + "Sorry for the inconvenient";

                TextView Speedmessage = (TextView) SpeedalertDialog.findViewById(android.R.id.message);
                Speedmessage.setText(speedString);
                Speedmessage.setGravity(Gravity.CENTER);
            }
        }

    }

    private void LockNotification() {
        Log.d(TAG + "_ALS", "LockNotification");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());

        // Set the title, text, and icon
        builder.setContentTitle(getString(R.string.app_name))
                .setContentText("App Lock Enabled")
                .setSmallIcon(R.drawable.ic_applock)
                .setOngoing(true);

        // Get an instance of the Notification Manager
        NotificationManager notifyManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the notification and post it
        notifyManager.notify(0, builder.build());
    }

    @Override
    public void onDestroy() {
        Log.i(TAG + "_ALS", "Destroy");
        super.onDestroy();

        mHandler.removeCallbacks(looperTask);

        if (mScreenReceiver != null)
            unregisterReceiver(mScreenReceiver);

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
