package com.ponnex.justdrive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import com.afollestad.materialdialogs.prefs.MaterialEditTextPreference;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

/**
 * Created by ramos on 4/15/2015.
 */

public class SettingsFragmentLollipop extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    SharedPreferences prefs;
    private LocalBroadcastManager broadcastManager;
    private LocalBroadcastManager broadcastManager1;
    private SwitchPreference switchbloxt;
    static String mPhoneNumber;
    static boolean active = false;
    private View positiveAction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        CheckBoxPreference checkbloxtautoReply;
        CheckBoxPreference checkbloxtstartonboot;
        CheckBoxPreference checkbloxtnotify;
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
        final Integer theme = (mSharedPreference6.getInt("theme", 1));

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isSwitch = (mSharedPreference.getBoolean("switch", true));

        SharedPreferences mSharedPreference2= PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isautoReply = (mSharedPreference2.getBoolean("autoReply", true));

        SharedPreferences mSharedPreference3= PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isstartonboot = (mSharedPreference3.getBoolean("startonboot", true));

        SharedPreferences mSharedPreference4= PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isnotify = (mSharedPreference4.getBoolean("notification", true));

        SharedPreferences mSharedPreference5= PreferenceManager.getDefaultSharedPreferences(getActivity());
        String isMsg=(mSharedPreference5.getString("msg", "I am driving right now, I will contact you later."));

        if (isSwitch){
            getPreferenceScreen().findPreference("switch").setSummary("Enabled");
        }
        else {
            getPreferenceScreen().findPreference("switch").setSummary("Disabled");
        }

        if (isautoReply){
            getPreferenceScreen().findPreference("autoReply").setSummary("Enabled.");
        }
        else {
            getPreferenceScreen().findPreference("autoReply").setSummary("Disabled");
        }

        if (isstartonboot){
            getPreferenceScreen().findPreference("startonboot").setSummary("Start Just Drive on boot");
        }
        else {
            getPreferenceScreen().findPreference("startonboot").setSummary("Do not start Just Drive on boot");
        }

        if (isnotify){
            getPreferenceScreen().findPreference("notification").setSummary("Show notifications of activities.");
        }
        else {
            getPreferenceScreen().findPreference("notification").setSummary("Hide notifications of activities.");
        }

        getPreferenceScreen().findPreference("msg").setSummary("''" + isMsg + " --This is an automated SMS--''");

        broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        broadcastManager1 = LocalBroadcastManager.getInstance(getActivity());
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

                    if (theme == 1) {
                        SnackbarManager.show(
                                Snackbar.with(getActivity())
                                        .position(Snackbar.SnackbarPosition.BOTTOM)
                                        .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                                        .textColor(Color.parseColor("#FFFFFF"))
                                        .color(Color.parseColor("#FF3D00"))
                                        .text("Just Drive is Enabled")
                                , (android.view.ViewGroup) getActivity().findViewById(R.id.main_frame));
                    } else {
                        SnackbarManager.show(
                                Snackbar.with(getActivity())
                                        .position(Snackbar.SnackbarPosition.BOTTOM)
                                        .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                                        .textColor(Color.parseColor("#FFFFFF"))
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

                    if (theme == 1) {
                        SnackbarManager.show(
                                Snackbar.with(getActivity())
                                        .position(Snackbar.SnackbarPosition.BOTTOM)
                                        .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                                        .textColor(Color.parseColor("#FFFFFF"))
                                        .color(Color.parseColor("#FF3D00"))
                                        .text("Just Drive is Disabled")
                                , (android.view.ViewGroup) getActivity().findViewById(R.id.main_frame));
                    } else {
                        SnackbarManager.show(
                                Snackbar.with(getActivity())
                                        .position(Snackbar.SnackbarPosition.BOTTOM)
                                        .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                                        .textColor(Color.parseColor("#FFFFFF"))
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

        preferencebloxt = (Preference)getPreferenceManager().findPreference("theme");
        preferencebloxt.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                showSingleChoice();
                return true;
            }
        });

        checkbloxtautoReply = (CheckBoxPreference) getPreferenceManager().findPreference("autoReply");
        checkbloxtautoReply.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue.toString().equals("true")) {

                    SharedPreferences mSharedPreference2 = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    String isPhoneNumber = (mSharedPreference2.getString("PhoneNumber", null));

                    if (isPhoneNumber == null){
                        showBasicNoTitleNumberCheck();
                    }
                    else {
                        getPreferenceScreen().findPreference("autoReply").setSummary("Auto reply Enabled.");
                        Log.e("TYPE", isPhoneNumber);
                    }

                }
                if (newValue.toString().equals("false")) {
                    getPreferenceScreen().findPreference("autoReply").setSummary("Auto reply Disabled.");
                }
                return true;
            }
        });

        checkbloxtstartonboot = (CheckBoxPreference) getPreferenceManager().findPreference("startonboot");
        checkbloxtstartonboot.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue.toString().equals("true")) {
                    getPreferenceScreen().findPreference("startonboot").setSummary("Start Just Drive on boot");
                }
                if (newValue.toString().equals("false")) {
                    showBasicNoTitle();
                }
                return true;
            }
        });

        checkbloxtnotify = (CheckBoxPreference) getPreferenceManager().findPreference("notification");
        checkbloxtnotify.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue.toString().equals("true")) {
                    getPreferenceScreen().findPreference("notification").setSummary("Show notifications of activities.");
                    sendNotifInfo(true);
                }
                if (newValue.toString().equals("false")) {
                    getPreferenceScreen().findPreference("notification").setSummary("Hide notifications of activities.");
                    sendNotifInfo(false);
                }
                return true;
            }
        });

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(switchReceiver, new IntentFilter("com.ponnex.justdrive.MainActivity"));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(refreshReceiver, new IntentFilter("com.ponnex.justdrive.NotificationReceiver2"));

    }

    private void showBasicNoTitleNumberCheck() {
        EditText phonenumber;

        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.phonenumber)
                .customView(R.layout.dialog_numberview, true)
                .positiveText(R.string.yes_update)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        SharedPreferences mSharedPreference2 = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        String isPhoneNumber = (mSharedPreference2.getString("PhoneNumber", null));

                        getPreferenceScreen().findPreference("autoReply").setSummary("Auto reply Enabled.");

                        Log.e("TYPE", isPhoneNumber);
                    }
                })
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {

                    }
                }).build();

        positiveAction = dialog.getActionButton(DialogAction.POSITIVE);
        phonenumber = (EditText) dialog.getCustomView().findViewById(R.id.phonenumber);
        phonenumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                positiveAction.setEnabled(s.toString().trim().length() > 6);
                SharedPreferences isPhoneNumber = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = isPhoneNumber.edit();
                editor.putString("PhoneNumber", s.toString());
                editor.apply();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        dialog.show();
        positiveAction.setEnabled(false);
    }

    private void showBasicNoTitle() {
        new MaterialDialog.Builder(getActivity())
                .content(R.string.startonbootmsg)
                .positiveText(R.string.yes_accept)
                .negativeText(R.string.no_dont)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        getPreferenceScreen().findPreference("startonboot").setSummary("Do not start Just Drive on boot");
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        CheckBoxPreference startcheck = (CheckBoxPreference) findPreference("startonboot");
                        startcheck.setChecked(true);
                    }

                })
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        getPreferenceScreen().findPreference("startonboot").setSummary("Start Just Drive on boot");
                        CheckBoxPreference checkPrefs = (CheckBoxPreference) findPreference("startonboot");
                        checkPrefs.setChecked(true);
                    }
                })

                .show();
    }

    private void showSingleChoice() {

        new MaterialDialog.Builder(getActivity())
                .title(R.string.themetitle)
                .items(R.array.theme_values)
                .itemsCallbackSingleChoice(2, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                        SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        final Integer theme = (mSharedPreference.getInt("theme", 1));

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
        broadcastManager1.sendBroadcast(intent);
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

    private BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Boolean refreshVal = intent.getBooleanExtra("isRefresh1", false);
            if (!refreshVal && active){
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

    @Override
    public void onResume() {
        super.onResume();

        try {
            for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i) {
                Preference preference = getPreferenceScreen().getPreference(i);
                if (preference instanceof PreferenceGroup) {
                    PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
                    for (int j = 0; j < preferenceGroup.getPreferenceCount(); ++j) {
                        updatePreference(preferenceGroup.getPreference(j));
                    }
                } else {
                    updatePreference(preference);
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreference(findPreference(key));
    }

    private void updatePreference(Preference preference) {
        if (preference instanceof MaterialEditTextPreference) {
            MaterialEditTextPreference editbloxtmsg = (MaterialEditTextPreference) preference;
            preference.setSummary("''" + editbloxtmsg.getText()+ " --This is an automated SMS--''");

            if (editbloxtmsg.getText() == null || editbloxtmsg.getText().equals("")) {
                preference.setSummary("''I am driving right now, I will contact you later. --This is an automated SMS--''");

            }
        }
    }
}
