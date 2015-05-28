package com.ponnex.justdrive;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.TypedValue;

import com.jenzz.materialpreference.SwitchPreference;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

/**
 * Created by ramos on 4/15/2015.
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    SharedPreferences prefs;
    private SwitchPreference switchbloxt;
    static String mPhoneNumber;
    static boolean active = false;
    private Integer color;

    private String TAG = "com.ponnex.justdrive.SettingsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        addPreferencesFromResource(R.xml.prefs);

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        TelephonyManager mManager =(TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        mPhoneNumber =  mManager.getLine1Number();

        SharedPreferences isPhoneNumber = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = isPhoneNumber.edit();
        editor.putString("PhoneNumber", mPhoneNumber);
        editor.apply();

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

        TypedValue typedValue = new TypedValue();
        Resources.Theme themecolor = getActivity().getTheme();
        themecolor.resolveAttribute(R.attr.colorAccent, typedValue, true);
        color = typedValue.data;

        switchbloxt = (SwitchPreference) getPreferenceManager().findPreference("switch");
        switchbloxt.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue.toString().equals("true")) {

                    SharedPreferences isSwitchup = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = isSwitchup.edit();
                    editor.putBoolean("switch", true);
                    editor.apply();

                    getActivity().startService(new Intent(getActivity(), CoreService.class));
                    getActivity().startService(new Intent(getActivity(), ActivityRecognition.class));

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
        SnackbarManager.show(
                Snackbar.with(getActivity())
                        .position(Snackbar.SnackbarPosition.TOP)
                        .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                        .textColor(Color.parseColor("#FFFFFF"))
                        .color(color)
                        .text(text)
                , (android.view.ViewGroup) getActivity().findViewById(R.id.main_frame));
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

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("switch")) {
            SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getActivity());
            Boolean isSwitch = (mSharedPreference.getBoolean("switch", true));

            switchbloxt = (SwitchPreference) getPreferenceManager().findPreference("switch");
            switchbloxt.setChecked(isSwitch);

            if(isSwitch){
                getPreferenceScreen().findPreference("switch").setSummary("Enabled");
            }
            else{
                getPreferenceScreen().findPreference("switch").setSummary("Disabled");
            }
        }
    }
}
