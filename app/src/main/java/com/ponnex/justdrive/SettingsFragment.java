package com.ponnex.justdrive;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;

import com.afollestad.materialdialogs.prefs.MaterialEditTextPreference;
import com.jenzz.materialpreference.CheckBoxPreference;
import com.jenzz.materialpreference.SwitchPreference;

/**
 * Created by ramos on 4/15/2015.
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    SharedPreferences prefs;
    private SwitchPreference switchbloxt;
    static boolean active = false;

    private String TAG = "com.ponnex.justdrive.SettingsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        CheckBoxPreference checkbloxtautoReplyCalls;
        CheckBoxPreference checkbloxtautoReply;

        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        addPreferencesFromResource(R.xml.prefs);

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isSwitch = (mSharedPreference.getBoolean("switch", true));

        SharedPreferences mSharedPreference2= PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isphone = (mSharedPreference2.getBoolean("phone", true));

        SharedPreferences mSharedPreference3= PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isstartonboot = (mSharedPreference3.getBoolean("startonboot", true));

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

        SharedPreferences mSharedPreference4 = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isautoReplyCalls = (mSharedPreference4.getBoolean("autoReplyCalls", true));

        SharedPreferences mSharedPreference5 = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isautoReply = (mSharedPreference5.getBoolean("autoReply", true));

        SharedPreferences mSharedPreference6= PreferenceManager.getDefaultSharedPreferences(getActivity());
        String isMsg=(mSharedPreference6.getString("msg", "I am driving right now, I will contact you later."));

        if (isautoReplyCalls){
            getPreferenceScreen().findPreference("autoReplyCalls").setSummary("Reply incoming calls with SMS");
        }
        else {
            getPreferenceScreen().findPreference("autoReplyCalls").setSummary("Disabled");
        }

        if (isautoReply){
            getPreferenceScreen().findPreference("autoReply").setSummary("Reply text messages with SMS");
        }
        else {
            getPreferenceScreen().findPreference("autoReply").setSummary("Disabled");
        }

        getPreferenceScreen().findPreference("msg").setSummary("''" + isMsg + " --This is an automated SMS--''");

        checkbloxtautoReplyCalls = (CheckBoxPreference) getPreferenceManager().findPreference("autoReplyCalls");
        checkbloxtautoReplyCalls.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue.toString().equals("true")) {
                    getPreferenceScreen().findPreference("autoReplyCalls").setSummary("Reply incoming calls with SMS");
                }
                if (newValue.toString().equals("false")) {
                    getPreferenceScreen().findPreference("autoReplyCalls").setSummary("Disabled");
                }
                return true;
            }
        });

        checkbloxtautoReply = (CheckBoxPreference) getPreferenceManager().findPreference("autoReply");
        checkbloxtautoReply.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue.toString().equals("true")) {
                    getPreferenceScreen().findPreference("autoReply").setSummary("Reply text messages with SMS");
                }
                if (newValue.toString().equals("false")) {
                    getPreferenceScreen().findPreference("autoReply").setSummary("Disabled");
                }
                return true;
            }
        });

        switchbloxt = (SwitchPreference) getPreferenceManager().findPreference("switch");
        switchbloxt.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue.toString().equals("true")) {

                    SharedPreferences isSwitchup = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = isSwitchup.edit();
                    editor.putBoolean("switch", true);
                    editor.apply();

                    getActivity().startService(new Intent(getActivity(), CoreService.class));
                    getActivity().startService(new Intent(getActivity(), ActivityRecognitionIntentService.class));

                    getPreferenceScreen().findPreference("switch").setSummary("Enabled");

                    ShowSnackbar(R.string.justdriveenable);
                }
                if (newValue.toString().equals("false")) {
                    SharedPreferences isSwitchup = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = isSwitchup.edit();
                    editor.putBoolean("switch", false);
                    editor.apply();

                    getPreferenceScreen().findPreference("switch").setSummary("Disabled");

                    ShowSnackbar(R.string.justdrivedisable);
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

        switchbloxt = (SwitchPreference) getPreferenceManager().findPreference("startonboot");
        switchbloxt.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue.toString().equals("true")) {
                    getPreferenceScreen().findPreference("startonboot").setSummary("Enable Just Drive on boot");
                }
                if (newValue.toString().equals("false")) {
                    getPreferenceScreen().findPreference("startonboot").setSummary("Disable Just Drive on boot");
                }
                return true;
            }
        });
    }

    public void ShowSnackbar(Integer text){
        android.support.design.widget.Snackbar
                .make(getActivity().findViewById(R.id.layout_main), text, android.support.design.widget.Snackbar.LENGTH_SHORT)
                .show();
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
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

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

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    private boolean switchstate(){
        SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getActivity());
        return (mSharedPreference.getBoolean("switch", true));
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("switch")) {
            switchbloxt = (SwitchPreference) getPreferenceManager().findPreference("switch");
            switchbloxt.setChecked(switchstate());

            if(switchstate()){
                getPreferenceScreen().findPreference("switch").setSummary("Enabled");
            }
            else{
                getPreferenceScreen().findPreference("switch").setSummary("Disabled");
            }
        }
        updatePreference(findPreference(key));
    }

    private void updatePreference(Preference preference) {
        if (preference instanceof MaterialEditTextPreference) {
            MaterialEditTextPreference editbloxtmsg = (MaterialEditTextPreference) preference;
            preference.setSummary("''" + editbloxtmsg.getText() + " --This is an automated SMS--''");

            if (editbloxtmsg.getText() == null || editbloxtmsg.getText().equals("")) {
                preference.setSummary("''I am driving right now, I will contact you later. --This is an automated SMS--''");
            }
        }
    }
}
