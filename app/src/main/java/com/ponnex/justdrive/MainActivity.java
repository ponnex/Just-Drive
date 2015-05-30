package com.ponnex.justdrive;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import android.support.design.widget.Snackbar;

/**
 * Created by ramos on 4/15/2015.
 */

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private FloatingActionButton fab;
    private boolean fab_state;
    static boolean active = false;
    AlertDialog alertDialog;
    private DrawerLayout mDrawerLayout;

    private String TAG = "com.ponnex.justdrive.MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        Fragment existingFragment = getFragmentManager().findFragmentById(R.id.container);
        if (existingFragment == null || !existingFragment.getClass().equals(SettingsFragment.class) || !existingFragment.getClass().equals(SettingsFragmentLollipop.class)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getFragmentManager().beginTransaction().replace(R.id.container, new SettingsFragmentLollipop()).commit();
            } else {
                getFragmentManager().beginTransaction().replace(R.id.container, new SettingsFragment()).commit();
            }
        }

        PackageManager pm = getPackageManager();
        boolean hasTelephony = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);

        if(!hasTelephony) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
            alertDialog = builder.create();
            alertDialog.setTitle("DEVICE DOESN'T HAVE TELEPHONY FEATURES");
            alertDialog.setMessage("Just Drive works on devices with telephony features only");
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "OK", new DialogInterface.OnClickListener() {
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
        fab = (FloatingActionButton) findViewById(R.id.fab);
        if (isSwitch) {
            fab.setImageResource(R.drawable.ic_on);
            fab_state = true;
        } else {
            fab.setImageResource(R.drawable.ic_off);
            fab_state = false;
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fab_state) {
                    SharedPreferences switchPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = switchPref.edit();
                    editor.putBoolean("switch", false);
                    editor.apply();
                }
                else {
                    SharedPreferences switchPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = switchPref.edit();
                    editor.putBoolean("switch", true);
                    editor.apply();
                }
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

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    public void ShowSnackbar(Integer text){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.layout_main), text, Snackbar.LENGTH_SHORT);
            View view = snackbar.getView();
            view.setBackgroundColor(getResources().getColor(R.color.accent));
            snackbar.show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.about:
                AboutDialog();
                return true;
            case R.id.debug_on:
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
            case R.id.debug_off:
                SharedPreferences debug1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor1 = debug1.edit();
                editor1.putBoolean("debug", false);
                editor1.apply();

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
            SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Boolean isSwitch = (mSharedPreference.getBoolean("switch", true));

            if(isSwitch) {
                fab.setImageResource(R.drawable.ic_on);
                ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.flipping);
                anim.setTarget(fab);
                anim.setDuration(500);
                anim.start();

                ShowSnackbar(R.string.justdriveenable);
                fab_state = true;
            } else {
                fab.setImageResource(R.drawable.ic_off);
                ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.flipping);
                anim.setTarget(fab);
                anim.setDuration(500);
                anim.start();

                startService(new Intent(MainActivity.this, CoreService.class));

                ShowSnackbar(R.string.justdrivedisable);
                fab_state = false;
            }
        }
    }
}