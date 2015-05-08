package com.ponnex.justdrive;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ponnex.justdrive.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

/**
 * Created by ramos on 4/15/2015.
 */

public class MainActivity extends ActionBarActivity {

    private View view1;
    private View view2;

    private boolean isFirstImage = true;

    private LocalBroadcastManager broadcastManager;

    public final static int LIGHT = 0;
    public final static int DARK = 1;

    static boolean active = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getIntent().hasExtra("bundle") && savedInstanceState == null) {
            savedInstanceState = getIntent().getExtras().getBundle("bundle");
        }

        SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final Integer theme = (mSharedPreference1.getInt("theme", 1));

        switch (theme) {
            case LIGHT:
                setTheme(R.style.JustDriveLightTheme);
                break;
            case DARK:
                setTheme(R.style.JustDriveDarkTheme);
                break;

            default:
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT>=21) {
            getFragmentManager().beginTransaction().replace(R.id.container, new SettingsFragmentLollipop()).commit();
        }else{
            getFragmentManager().beginTransaction().replace(R.id.container, new SettingsFragment()).commit();
        }

        SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean isSwitch = (mSharedPreference.getBoolean("isSwitch", true));

        if (isPlayServicesConfigured()) {
            //start only after verification that user has Google Play Services
            Log.e("TYPE", "User has Google Play Services");
            if (isSwitch) {
                startService(new Intent(getApplication(), LockScreen.class));
                startService(new Intent(getApplication(), ActivityRecognitionIntentService.class));
            }
        }

        view1 = (View) findViewById(R.id.fab_main1);
        view2 = (View) findViewById(R.id.fab_main2);
        view2.setVisibility(View.GONE);

        if (!isSwitch) {
            applyRotation(0, 90);
            isFirstImage = !isFirstImage;
        }

        broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

        view1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isFirstImage) {
                    applyRotation(0, 90);
                    isFirstImage = !isFirstImage;

                    SharedPreferences isSwitchup = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = isSwitchup.edit();
                    editor.putBoolean("isSwitch", false);
                    editor.apply();

                    SharedPreferences switchPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor1 = switchPref.edit();
                    editor1.putBoolean("switch", false);
                    editor1.apply();

                    sendSwitchInfo(false);
                    sendNotifInfo(false);

                    if (theme == 1) {
                        SnackbarManager.show(
                                Snackbar.with(MainActivity.this)
                                        .position(Snackbar.SnackbarPosition.TOP)
                                        .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                                        .textColor(Color.parseColor("#FFFFFF"))
                                        .color(Color.parseColor("#FF3D00"))
                                        .text("Just Drive is Disabled")
                                , (android.view.ViewGroup) findViewById(R.id.main_frame));
                    } else {
                        SnackbarManager.show(
                                Snackbar.with(MainActivity.this)
                                        .position(Snackbar.SnackbarPosition.TOP)
                                        .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                                        .textColor(Color.parseColor("#FFFFFF"))
                                        .text("Just Drive is Disabled")
                                , (android.view.ViewGroup) findViewById(R.id.main_frame));

                    }

                    SharedPreferences isCountup2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor2 = isCountup2.edit();
                    editor2.putInt("isCount", 0);
                    editor2.apply();

                }
            }
        });

        view2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                applyRotation(0, 90);
                isFirstImage = !isFirstImage;

                SharedPreferences isSwitchup = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = isSwitchup.edit();
                editor.putBoolean("isSwitch", true);
                editor.apply();

                SharedPreferences switchPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor1 = switchPref.edit();
                editor1.putBoolean("switch", true);
                editor1.apply();

                startService(new Intent(getApplication(), LockScreen.class));
                startService(new Intent(getApplication(), ActivityRecognitionIntentService.class));
                sendSwitchInfo(true);

                if (theme == 1) {
                    SnackbarManager.show(
                            Snackbar.with(MainActivity.this)
                                    .position(Snackbar.SnackbarPosition.TOP)
                                    .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                                    .textColor(Color.parseColor("#FFFFFF"))
                                    .color(Color.parseColor("#FF3D00"))
                                    .text("Just Drive is Enabled")
                            , (android.view.ViewGroup) findViewById(R.id.main_frame));
                } else {
                    SnackbarManager.show(
                            Snackbar.with(MainActivity.this)
                                    .position(Snackbar.SnackbarPosition.TOP)
                                    .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                                    .textColor(Color.parseColor("#FFFFFF"))
                                    .text("Just Drive is Enabled")
                            , (android.view.ViewGroup) findViewById(R.id.main_frame));
                }

            }

        });

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(switchReceiver1, new IntentFilter("com.ponnex.justdrive.SettingsFragment"));

        SharedPreferences mSharedPreference2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean isFirstRun = (mSharedPreference2.getBoolean("isFirstRun", true));

        if (isFirstRun) {

            SharedPreferences isFirstRun1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = isFirstRun1.edit();
            editor.putBoolean("isFirstRun", false);
            editor.apply();
        }

    }

    private BroadcastReceiver switchReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Boolean isSwitch = (mSharedPreference.getBoolean("isSwitch", true));

            Boolean SwitchVal = intent.getBooleanExtra("SwitchVal", isSwitch);
            if (SwitchVal && isSwitch) {
                applyRotation(0, 90);
                isFirstImage = !isFirstImage;
            }
            if (!SwitchVal && !isSwitch) {
                applyRotation(0, 90);
                isFirstImage = !isFirstImage;
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    private void applyRotation(float start, float end) {
        // Find the center of image
        final float centerX = view1.getWidth() / 2.0f;
        final float centerY = view1.getHeight() / 2.0f;

        // Create a new 3D rotation with the supplied parameter
        // The animation listener is used to trigger the next animation
        final Flip3dAnimation rotation =
                new Flip3dAnimation(start, end, centerX, centerY);
        rotation.setDuration(200);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        rotation.setAnimationListener(new DisplayNextView(isFirstImage, view1, view2));

        if (isFirstImage) {
            view1.startAnimation(rotation);
        } else {
            view2.startAnimation(rotation);
        }

    }

    public void sendSwitchInfo(Boolean switchval) {
        Intent intent = new Intent("com.ponnex.justdrive.MainActivity");
        intent.putExtra("SwitchVal", switchval);
        broadcastManager.sendBroadcast(intent);
    }

    public void sendNotifInfo(Boolean notifval) {
        Intent intent = new Intent("com.ponnex.justdrive.MainActivity1");
        intent.putExtra("NotifVal", notifval);
        broadcastManager.sendBroadcast(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.about) {
            new MaterialDialog.Builder(this)
                    .title(R.string.about)
                    .positiveText(R.string.dismiss)
                    .content(getString(R.string.about_body))
                    .contentLineSpacing(1.6f)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean isSwitch = (mSharedPreference.getBoolean("isSwitch", true));

        if (isSwitch) {
            startService(new Intent(getApplication(), LockScreen.class));
            startService(new Intent(getApplication(), ActivityRecognitionIntentService.class));
            sendSwitchInfo(true);
        }

        if (!isSwitch) {
            stopService(new Intent(getApplication(), LockScreen.class));
            stopService(new Intent(getApplication(), ActivityRecognitionIntentService.class));
            sendSwitchInfo(false);
        }
        super.onDestroy();
    }

    private boolean isPlayServicesConfigured() {
        final int googlePlayServicesCheck = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getApplicationContext());
        switch (googlePlayServicesCheck) {
            case ConnectionResult.SUCCESS:
                return true;
            case ConnectionResult.SERVICE_DISABLED:
            case ConnectionResult.SERVICE_INVALID:
            case ConnectionResult.SERVICE_MISSING:
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(googlePlayServicesCheck, this, 0);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        MainActivity.this.finish();
                    }
                });
                dialog.show();
        }
        return false;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        final SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean isPressOnce=(mSharedPreference.getBoolean("isPressOnce", true));

        SharedPreferences mSharedPreference1= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final Integer theme = (mSharedPreference1.getInt("theme", 1));

        int ONBACK_COUNT = 3500;
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //add runnable
            if (isPressOnce) {

                if (theme==1) {
                    SnackbarManager.show(
                            Snackbar.with(MainActivity.this)
                                    .position(Snackbar.SnackbarPosition.BOTTOM)
                                    .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                                    .textColor(Color.parseColor("#FFFFFF"))
                                    .color(Color.parseColor("#FF3D00"))
                                    .text("Press back again to leave")
                            , (android.view.ViewGroup) findViewById(R.id.layout_main));
                } else {
                    SnackbarManager.show(
                            Snackbar.with(MainActivity.this)
                                    .position(Snackbar.SnackbarPosition.BOTTOM)
                                    .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                                    .textColor(Color.parseColor("#FFFFFF"))
                                    .text("Press back again to leave")
                            , (android.view.ViewGroup) findViewById(R.id.layout_main));
                }
                SharedPreferences isPressOnceup = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = isPressOnceup.edit();
                editor.putBoolean("isPressOnce", false);
                editor.apply();

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        SharedPreferences isPressOnceup = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = isPressOnceup.edit();
                        editor.putBoolean("isPressOnce", true);
                        editor.apply();
                    }
                }, ONBACK_COUNT);
            }
            if (!isPressOnce){
                SharedPreferences isPressOnceup1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = isPressOnceup1.edit();
                editor.putBoolean("isPressOnce", true);
                editor.apply();
                this.finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {

    }
}