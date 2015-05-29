package com.ponnex.justdrive;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.ScrollDirectionListener;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

/**
 * Created by ramos on 4/15/2015.
 */

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;
    private boolean isFirstImage;
    static boolean active = false;
    AlertDialog alertDialog;

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

        SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean isDebug = (mSharedPreference1.getBoolean("debug", false));

        if(isDebug) {
            startService(new Intent(MainActivity.this, AppLockService.class));
            startService(new Intent(MainActivity.this, CallerService.class));
        } else {
            stopService(new Intent(MainActivity.this, AppLockService.class));
            stopService(new Intent(MainActivity.this, CallerService.class));
        }

        if(Build.VERSION.SDK_INT >= 21) {
            showDialog();
        }

        fab1 = (FloatingActionButton) findViewById(R.id.fab_main1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab_main2);
        fab2.setVisibility(View.GONE);

        if (!isSwitch) {
            fab1.setVisibility(View.INVISIBLE);
            fab2.setVisibility(View.VISIBLE);
        }

        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFirstImage = true;
                SharedPreferences switchPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = switchPref.edit();
                editor.putBoolean("switch", false);
                editor.apply();

                ShowSnackbar(R.string.justdrivedisable);
            }
        });

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFirstImage = false;
                SharedPreferences switchPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = switchPref.edit();
                editor.putBoolean("switch", true);
                editor.apply();

                startService(new Intent(MainActivity.this, CoreService.class));

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
                        .swipeToDismiss(false)
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
        float centerX = fab1.getWidth() / 2.0f;
        float centerY = fab2.getHeight() / 2.0f;

        // Create a new 3D rotation with the supplied parameter
        // The animation listener is used to trigger the next animation
        final Flip3dAnimation rotation =
                new Flip3dAnimation(start, end, centerX, centerY);
        rotation.setDuration(200);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        rotation.setAnimationListener(new DisplayNextView(isFirstImage, fab1, fab2));

        if (isFirstImage) {
            fab1.startAnimation(rotation);
        } else {
            fab2.startAnimation(rotation);
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

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        alertDialog = builder.create();
        alertDialog.setTitle(getText(R.string.lollipop_title));
        alertDialog.setMessage(getText(R.string.lollipop_message));
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "CONFIGURE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction("android.settings.ACCESSIBILITY_SETTINGS");
                startActivity(intent);
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                MainActivity.this.finish();
            }
        });
        alertDialog.show();

        final int accentcolor = getApplicationContext().getResources().getColor(R.color.accent);

        Button positive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positive.setTextColor(accentcolor);

        Button negative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negative.setTextColor(accentcolor);
    }

    @Override
    protected void onDestroy() {
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
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(googlePlayServicesCheck, this, 0, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        MainActivity.this.finish();
                    }
                });
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