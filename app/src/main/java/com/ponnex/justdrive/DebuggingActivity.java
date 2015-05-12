package com.ponnex.justdrive;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

/**
 * Created by ramos on 4/15/2015.
 */

public class DebuggingActivity extends AppCompatActivity {
    private static TextView activityTV;

    public final static int ORANGELIGHT = 0;
    public final static int ORANGEDARK = 1;
    public final static int BLUEGREYLIGHT = 2;
    public final static int BLUEGREYDARK = 3;
    public final static int INDIGOLIGHT = 4;
    public final static int INDIGODARK = 5;

    private LocalBroadcastManager broadcastManager;

    Button testbutton;
    Button testbutton1;
    private static int audioMode;
    private Integer theme;
    private Integer color;

    private String TAG = "com.ponnex.justdrive.DebuggingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getIntent().hasExtra("bundle") && savedInstanceState == null){
            savedInstanceState = getIntent().getExtras().getBundle("bundle");
        }

        SharedPreferences mSharedPreference1= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        theme = (mSharedPreference1.getInt("theme", 1));

        switch(theme)
        {
            case ORANGELIGHT:
                setTheme(R.style.JustDriveOrangeLightTheme);
                break;
            case ORANGEDARK:
                setTheme(R.style.JustDriveOrangeDarkTheme);
                break;
            case BLUEGREYLIGHT:
                setTheme(R.style.JustDriveBlueGreyLightTheme);
                break;
            case BLUEGREYDARK:
                setTheme(R.style.JustDriveBlueGreyDarkTheme);
                break;
            case INDIGOLIGHT:
                setTheme(R.style.JustDriveIndigoLightTheme);
                break;
            case INDIGODARK:
                setTheme(R.style.JustDriveIndigoDarkTheme);
                break;

            default:
        }

        super.onCreate(savedInstanceState);
        Log.d(TAG, "DA Created");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_debugging);
        activityTV = (TextView) findViewById(R.id.debugText);

        broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

        testbutton = (Button) findViewById(R.id.testbutton);
        testbutton1 = (Button) findViewById(R.id.testbutton1);

        SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean isSwitch=(mSharedPreference.getBoolean("isSwitch", true));

        TypedValue typedValue = new TypedValue();
        Resources.Theme themecolor = getTheme();
        themecolor.resolveAttribute(R.attr.colorAccent, typedValue, true);
        color = typedValue.data;

        if(!isSwitch){
            activityTV.setText("Can't Read Activity :(");

            if((theme % 2) == 0){
                SnackbarManager.show(
                        Snackbar.with(DebuggingActivity.this)
                                .position(Snackbar.SnackbarPosition.BOTTOM)
                                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                                .textColor(Color.parseColor("#FFFFFF"))
                                .text("Please Enable Bloxt Services")
                                .swipeToDismiss(false)
                        , (android.view.ViewGroup) findViewById(R.id.list_layout));
            }

            else {
                SnackbarManager.show(
                        Snackbar.with(DebuggingActivity.this)
                                .position(Snackbar.SnackbarPosition.BOTTOM)
                                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                                .textColor(Color.parseColor("#FFFFFF"))
                                .color(color)
                                .text("Please Enable Bloxt Services")
                                .swipeToDismiss(false)
                        , (android.view.ViewGroup) findViewById(R.id.list_layout));
            }
        }

        testbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                Boolean isSwitch = (mSharedPreference.getBoolean("isSwitch", true));

                if (isSwitch) {
                    sendTestButton(true);

                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.cancel(1);
                }
            }
        });

        testbutton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                Boolean isSwitch = (mSharedPreference.getBoolean("isSwitch", true));

                AudioManager current = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
                audioMode = current.getRingerMode();

                normal();

                if (isSwitch) {
                    sendTestButton1(true);

                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            soundMode();
                        }
                    }, 9000);
                }
            }
        });

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(screenReceiver, new IntentFilter("com.ponnex.justdrive.ActivityRecognitionIntentService"));
    }

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
        final AudioManager mode = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // Silent Mode
        mode.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    void vibrate() {
        final AudioManager mode = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // vibrate mode
        mode.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
    }

    void normal() {
        final AudioManager mode = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // Normal Mode
        mode.setStreamVolume(AudioManager.STREAM_RING, mode.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
        mode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendTestButton(Boolean testbutton) {
        Intent intent = new Intent("com.ponnex.justdrive.DebuggingActivity");
        intent.putExtra("TestButton", testbutton);
        broadcastManager.sendBroadcast(intent);
    }

    public void sendTestButton1(Boolean testbutton) {
        Intent intent = new Intent("com.ponnex.justdrive.DebuggingActivity1");
        intent.putExtra("TestButton1", testbutton);
        broadcastManager.sendBroadcast(intent);
    }

    private BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String activity = intent.getStringExtra("Activity");
            updateUI(activity);
        }
    };

    public static void updateUI(String activity) {
        activityTV.setText(activity);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "DA Destroyed");
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(screenReceiver);
    }
}

