package com.ponnex.justdrive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.TypedValue;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jenzz.materialpreference.SwitchPreference;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

/**
 * Created by ramos on 4/15/2015.
 */

public class SettingsFragment extends PreferenceFragment{
    SharedPreferences prefs;
    private LocalBroadcastManager broadcastManager;
    private SwitchPreference switchbloxt;
    static String mPhoneNumber;
    static boolean active = false;
    private Integer theme;
    private Integer color;

    private String TAG = "com.ponnex.justdrive.SettingsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Preference preferencebloxt;

        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        addPreferencesFromResource(R.xml.prefs);

        TelephonyManager mManager =(TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        mPhoneNumber =  mManager.getLine1Number();

        SharedPreferences isPhoneNumber = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = isPhoneNumber.edit();
        editor.putString("PhoneNumber", mPhoneNumber);
        editor.apply();

        SharedPreferences mSharedPreference6= PreferenceManager.getDefaultSharedPreferences(getActivity());
        theme = (mSharedPreference6.getInt("theme", 1));

        SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isSwitch = (mSharedPreference.getBoolean("switch", true));

        SharedPreferences mSharedPreference2= PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isphone = (mSharedPreference2.getBoolean("phone", true));

        SharedPreferences mSharedPreference3= PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isstartonboot = (mSharedPreference3.getBoolean("startonboot", true));

        SharedPreferences mSharedPreference4= PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isnotify = (mSharedPreference4.getBoolean("notification", true));

        if (isSwitch){
            getPreferenceScreen().findPreference("switch").setSummary("Enabled");
        }
        else {
            getPreferenceScreen().findPreference("switch").setSummary("Disabled");
        }

        if (isstartonboot){
            getPreferenceScreen().findPreference("startonboot").setSummary("Enable Just Drive on boot");
        }
        else {
            getPreferenceScreen().findPreference("startonboot").setSummary("Disable Just Drive on boot");
        }

        if (isphone){
            getPreferenceScreen().findPreference("phone").setSummary("(Headset or bluetooth mode only)\n" +
                    "Read caller ID of incoming phone calls");
        }
        else {
            getPreferenceScreen().findPreference("phone").setSummary("(Headset or bluetooth mode only)\n" +
                    "Disable reading caller ID of incoming phone calls");
        }

        if (isnotify){
            getPreferenceScreen().findPreference("notification").setSummary("Show notifications of activities");
        }
        else {
            getPreferenceScreen().findPreference("notification").setSummary("Hide notifications of activities");
        }

        TypedValue typedValue = new TypedValue();
        Resources.Theme themecolor = getActivity().getTheme();
        themecolor.resolveAttribute(R.attr.colorAccent, typedValue, true);
        color = typedValue.data;

        broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        switchbloxt = (SwitchPreference) getPreferenceManager().findPreference("switch");
        switchbloxt.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final boolean value = (Boolean) newValue;
                switchbloxt.setChecked(value);
                sendSwitchInfo(value);

                if (newValue.toString().equals("true")) {
                    SharedPreferences isSwitchup = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = isSwitchup.edit();
                    editor.putBoolean("isSwitch", true);
                    editor.apply();

                    getActivity().startService(new Intent(getActivity(), LockScreen.class));
                    getActivity().startService(new Intent(getActivity(), ActivityRecognitionIntentService.class));

                    getPreferenceScreen().findPreference("switch").setSummary("Enabled");

                    if ((theme % 2) == 0) {
                        SnackbarManager.show(
                                Snackbar.with(getActivity())
                                        .position(Snackbar.SnackbarPosition.BOTTOM)
                                        .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                                        .textColor(Color.parseColor("#FFFFFF"))
                                        .text("Just Drive is Enabled")
                                , (android.view.ViewGroup) getActivity().findViewById(R.id.main_frame));
                    } else {
                        SnackbarManager.show(
                                Snackbar.with(getActivity())
                                        .position(Snackbar.SnackbarPosition.BOTTOM)
                                        .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                                        .textColor(Color.parseColor("#FFFFFF"))
                                        .color(color)
                                        .text("Just Drive is Enabled")
                                , (android.view.ViewGroup) getActivity().findViewById(R.id.main_frame));
                    }
                }
                if (newValue.toString().equals("false")) {
                    SharedPreferences isSwitchup = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = isSwitchup.edit();
                    editor.putBoolean("isSwitch", false);
                    editor.apply();

                    getPreferenceScreen().findPreference("switch").setSummary("Disabled");

                    sendNotifInfo(false);

                    if ((theme % 2) == 0) {
                        SnackbarManager.show(
                                Snackbar.with(getActivity())
                                        .position(Snackbar.SnackbarPosition.BOTTOM)
                                        .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                                        .textColor(Color.parseColor("#FFFFFF"))
                                        .text("Just Drive is Disabled")
                                , (android.view.ViewGroup) getActivity().findViewById(R.id.main_frame));
                    } else {
                        SnackbarManager.show(
                                Snackbar.with(getActivity())
                                        .position(Snackbar.SnackbarPosition.BOTTOM)
                                        .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                                        .textColor(Color.parseColor("#FFFFFF"))
                                        .color(color)
                                        .text("Just Drive is Disabled")
                                , (android.view.ViewGroup) getActivity().findViewById(R.id.main_frame));
                    }

                    SharedPreferences isCountup2 = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor2 = isCountup2.edit();
                    editor2.putInt("isCount", 0);
                    editor2.apply();
                }
                return true;
            }
        });

        switchbloxt = (SwitchPreference) getPreferenceManager().findPreference("phone");
        switchbloxt.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue.toString().equals("true")) {
                    getPreferenceScreen().findPreference("phone").setSummary("(Headset or bluetooth mode only)\n" +
                            "Read caller ID of incoming phone calls");
                }
                if (newValue.toString().equals("false")) {
                    getPreferenceScreen().findPreference("phone").setSummary("(Headset or bluetooth mode only)\n" +
                            "Disable reading caller ID of incoming phone calls");
                }
                return true;
            }
        });

        preferencebloxt = (Preference)getPreferenceManager().findPreference("theme");
        preferencebloxt.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                showSingleChoice();
                return true;
            }
        });

        switchbloxt = (SwitchPreference) getPreferenceManager().findPreference("startonboot");
        switchbloxt.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue.toString().equals("true")) {
                    getPreferenceScreen().findPreference("startonboot").setSummary("Enable Just Drive on boot");
                }
                if (newValue.toString().equals("false")) {
                    showBasicNoTitle();
                }
                return true;
            }
        });

        switchbloxt = (SwitchPreference) getPreferenceManager().findPreference("notification");
        switchbloxt.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue.toString().equals("true")) {
                    getPreferenceScreen().findPreference("notification").setSummary("Show notifications of activities");
                    sendNotifInfo(true);
                }
                if (newValue.toString().equals("false")) {
                    getPreferenceScreen().findPreference("notification").setSummary("Hide notifications of activities");
                    sendNotifInfo(false);
                }
                return true;
            }
        });

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(switchReceiver, new IntentFilter("com.ponnex.justdrive.MainActivity"));
    }

    private void showBasicNoTitle() {
        new MaterialDialog.Builder(getActivity())
                .content(R.string.startonbootmsg)
                .positiveText(R.string.yes_accept)
                .negativeText(R.string.no_dont)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        getPreferenceScreen().findPreference("startonboot").setSummary("Disable Just Drive on boot");
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        SwitchPreference startcheck = (SwitchPreference) findPreference("startonboot");
                        startcheck.setChecked(true);
                    }

                })
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        getPreferenceScreen().findPreference("startonboot").setSummary("Enable Just Drive on boot");
                        SwitchPreference checkPrefs = (SwitchPreference) findPreference("startonboot");
                        checkPrefs.setChecked(true);
                    }
                })

                .show();
    }

    private void showSingleChoice() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.themetitle)
                .items(R.array.theme_values)
                .itemsCallbackSingleChoice(theme, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                        SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        theme = (mSharedPreference.getInt("theme", 1));

                        //if current theme not equal to the selected theme then apply
                        if (theme != which) {
                            SharedPreferences isTheme = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            SharedPreferences.Editor editor = isTheme.edit();
                            editor.putInt("theme", which);
                            editor.apply();

                            Bundle temp_bundle = new Bundle();
                            onSaveInstanceState(temp_bundle);
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.putExtra("bundle", temp_bundle);
                            startActivity(intent);
                            getActivity().finish();
                        }

                        return true; // allow selection
                    }
                })
                .positiveText(R.string.choose)
                .show();
    }

    public void sendSwitchInfo(Boolean switchval) {
        Intent intent = new Intent("com.ponnex.justdrive.SettingsFragment");
        intent.putExtra("SwitchVal", switchval);
        broadcastManager.sendBroadcast(intent);
    }

    public void sendNotifInfo(Boolean notifval) {
        Intent intent = new Intent("com.ponnex.justdrive.SettingsFragment1");
        intent.putExtra("NotifVal", notifval);
        broadcastManager.sendBroadcast(intent);
    }

    private BroadcastReceiver switchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Boolean SwitchVal = intent.getBooleanExtra("SwitchVal", true);

            if(SwitchVal) {
                getPreferenceScreen().findPreference("switch").setSummary("Enabled");
                SwitchPreference switchPrefs = (SwitchPreference) findPreference("switch");
                switchPrefs.setChecked(true);
            }

            if(!SwitchVal) {
                getPreferenceScreen().findPreference("switch").setSummary("Disabled");
                SwitchPreference switchPrefs = (SwitchPreference) findPreference("switch");
                switchPrefs.setChecked(false);
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
}
