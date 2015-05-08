package com.ponnex.justdrive;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ponnex.justdrive.R;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

/**
 * Created by ramos on 4/15/2015.
 */

public class DebuggingActivity extends ActionBarActivity {
    private static TextView activityTV;

    public final static int LIGHT  = 0;
    public final static int DARK  = 1;

    private LocalBroadcastManager broadcastManager;

    Button testbutton;
    Button testbutton1;
    private static int audioMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getIntent().hasExtra("bundle") && savedInstanceState==null){
            savedInstanceState = getIntent().getExtras().getBundle("bundle");
        }

        SharedPreferences mSharedPreference1= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Integer theme = (mSharedPreference1.getInt("theme", 1));

        switch(theme)
        {
            case LIGHT:
                setTheme(R.style.JustDriveLightTheme);
                break;
            case DARK:
                setTheme(R.style.JustDriveDarkTheme);
                break;

            default:
        }

        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_debugging);
        activityTV = (TextView) findViewById(R.id.debugText);

        broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

        testbutton = (Button) findViewById(R.id.testbutton);
        testbutton1 = (Button) findViewById(R.id.testbutton1);

        SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean isSwitch=(mSharedPreference.getBoolean("isSwitch", true));

        if(!isSwitch){
            activityTV.setText("Can't Read Activity :(");

            if (theme==1) {
                SnackbarManager.show(
                        Snackbar.with(DebuggingActivity.this)
                                .position(Snackbar.SnackbarPosition.BOTTOM)
                                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                                .textColor(Color.parseColor("#FFFFFF"))
                                .color(Color.parseColor("#FF3D00"))
                                .text("Please Enable Bloxt Services")
                                .swipeToDismiss(false)
                        , (android.view.ViewGroup) findViewById(R.id.list_layout));
            }else{
                SnackbarManager.show(
                        Snackbar.with(DebuggingActivity.this)
                                .position(Snackbar.SnackbarPosition.BOTTOM)
                                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                                .textColor(Color.parseColor("#FF3D00"))
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

    private BroadcastReceiver screenReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String activity = intent.getStringExtra("Activity");
            updateUI(activity);
        }
    };

    public static void updateUI(String activity) {
        activityTV.setText(activity);
    }
}

