package com.ponnex.justdrive;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by ramos on 4/15/2015.
 */

public class MainActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private FloatingActionButton fab;
    private boolean fab_state;
    private AlertDialog alertDialog;

    private String TAG = "com.ponnex.justdrive.MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            setContentView(R.layout.activity_main_lollipop);
        } else {
            setContentView(R.layout.activity_main);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

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

            Button positive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positive.setTextColor(getResources().getColor(R.color.accent));

            Button negative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            negative.setTextColor(getResources().getColor(R.color.accent));
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

                    SharedPreferences debug = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor1 = debug.edit();
                    editor1.putBoolean("debug", false);
                    editor1.apply();
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

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_HOME;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        SharedPreferences NavItem = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = NavItem.edit();
        editor.putInt("NavItem", NAVDRAWER_ITEM_HOME);
        editor.apply();

        super.onResume();
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

                fab_state = true;
            } else {
                fab.setImageResource(R.drawable.ic_off);
                ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.flipping);
                anim.setTarget(fab);
                anim.setDuration(500);
                anim.start();

                startService(new Intent(MainActivity.this, CoreService.class));

                fab_state = false;
            }
        }
    }
}