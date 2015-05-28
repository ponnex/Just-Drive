package com.ponnex.justdrive;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

/**
 * Created by ramos on 4/15/2015.
 */

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private View view1;
    private View view2;
    private boolean isFirstImage;
    static boolean active = false;

    private String TAG = "com.ponnex.justdrive.MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        Fragment existingFragment = getFragmentManager().findFragmentById(R.id.container);
        if (existingFragment == null || !existingFragment.getClass().equals(SettingsFragment.class) || !existingFragment.getClass().equals(SettingsFragmentLollipop.class)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getFragmentManager().beginTransaction().replace(R.id.container, new SettingsFragmentLollipop()).commit();
            } else {
                getFragmentManager().beginTransaction().replace(R.id.container, new SettingsFragment()).commit();
            }
        }
        SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean isSwitch = (mSharedPreference.getBoolean("switch", true));

        if (isPlayServicesConfigured()) {
            //start only after verification that user has Google Play Services
            Log.d(TAG, "User has Google Play Services");
            if (isSwitch) {
                startService(new Intent(MainActivity.this, CoreService.class));
            }
        }

        view1 = (View) findViewById(R.id.fab_main1);
        view2 = (View) findViewById(R.id.fab_main2);
        view2.setVisibility(View.GONE);

        if (!isSwitch) {
            view1.setVisibility(View.INVISIBLE);
            view2.setVisibility(View.VISIBLE);
        }

        view1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFirstImage = true;
                SharedPreferences switchPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = switchPref.edit();
                editor.putBoolean("switch", false);
                editor.apply();

                ShowSnackbar(R.string.justdrivedisable);

                SharedPreferences isCountup2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor2 = isCountup2.edit();
                editor2.putInt("isCount", 0);
                editor2.apply();

            }
        });

        view2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFirstImage = false;
                SharedPreferences switchPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = switchPref.edit();
                editor.putBoolean("switch", true);
                editor.apply();

                startService(new Intent(MainActivity.this, CoreService.class));
                startService(new Intent(MainActivity.this, ActivityRecognition.class));

                ShowSnackbar(R.string.justdriveenable);

            }

        });
        SharedPreferences mSharedPreference2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean isFirstRun = (mSharedPreference2.getBoolean("isFirstRun", true));

        if (isFirstRun) {
            SharedPreferences isFirstRun_write = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = isFirstRun_write.edit();
            editor.putBoolean("isFirstRun", false);
            editor.apply();
        }
    }

    public void ShowSnackbar(Integer text){
        final int accentcolor = getApplicationContext().getResources().getColor(R.color.accent);
        SnackbarManager.show(
                Snackbar.with(MainActivity.this)
                        .position(Snackbar.SnackbarPosition.TOP)
                        .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                        .textColor(Color.parseColor("#FFFFFF"))
                        .color(accentcolor)
                        .text(text)
                , (android.view.ViewGroup) findViewById(R.id.main_frame));
    }

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
        float centerX = view1.getWidth() / 2.0f;
        float centerY = view1.getHeight() / 2.0f;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.about) {
            AboutDialog();
            return true;
        }
        if (item.getItemId() == R.id.debug_on) {
            SharedPreferences debug = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = debug.edit();
            editor.putBoolean("debug", true);
            editor.apply();

            if(!isServiceRunning(AppLockService.class)) {
                startService(new Intent(MainActivity.this, AppLockService.class));
            }
            if(!isServiceRunning(CallerService.class)){
                startService(new Intent(MainActivity.this, CallerService.class));
            }

            return true;
        }
        if (item.getItemId() == R.id.debug_off) {
            SharedPreferences debug = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = debug.edit();
            editor.putBoolean("debug", false);
            editor.apply();

            if (isServiceRunning(AppLockService.class)) {
                stopService(new Intent(MainActivity.this, AppLockService.class));
            }
            if (isServiceRunning(CallerService.class)) {
                stopService(new Intent(MainActivity.this, CallerService.class));
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
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
    protected void onDestroy() {
        SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean isSwitch = (mSharedPreference.getBoolean("switch", true));

        if (!isSwitch) {
            stopService(new Intent(this, CoreService.class));
        }
        super.onDestroy();
    }

    private void AboutDialog(){
        new MaterialDialog.Builder(this)
                .title(R.string.about)
                .positiveText(R.string.dismiss)
                .content(getString(R.string.about_body))
                .contentLineSpacing(1.6f)
                .show();
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
                dialog.show();
        }
        return false;
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("switch")) {
            applyRotation(0, 90);
            isFirstImage = !isFirstImage;
        }
    }
}